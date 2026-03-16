package com.wynvers.wdialogues.addon.luxdialogues.manager;

import com.wynvers.wdialogues.addon.luxdialogues.LuxDialoguesAddon;
import com.wynvers.wdialogues.addon.luxdialogues.model.DialogueEntry;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.logging.Level;

/**
 * Manages dialogue loading, validation, and storage
 */
public class DialogueManager {

    private final LuxDialoguesAddon plugin;
    private final Map<String, DialogueEntry> dialogues;
    private final Map<String, List<DialogueEntry>> targetToDialogues;

    public DialogueManager(LuxDialoguesAddon plugin) {
        this.plugin = plugin;
        this.dialogues = new HashMap<>();
        this.targetToDialogues = new HashMap<>();
    }

    /**
     * Load all dialogues from configuration
     */
    public void loadDialogues() {
        dialogues.clear();
        targetToDialogues.clear();

        ConfigurationSection dialoguesSection = plugin.getDialoguesConfig().getConfigurationSection("dialogues");
        if (dialoguesSection == null) {
            plugin.getLogger().warning("No dialogues section found in dialogues.yml");
            return;
        }

        int loadedCount = 0;
        for (String dialogueId : dialoguesSection.getKeys(false)) {
            ConfigurationSection dialogueSection = dialoguesSection.getConfigurationSection(dialogueId);
            if (dialogueSection == null) continue;

            String target = dialogueSection.getString("target", "");
            String luxDialoguesId = dialogueSection.getString("luxdialogues-id", "");
            String description = dialogueSection.getString("description", "");
            boolean enabled = dialogueSection.getBoolean("enabled", true);

            if (target.isEmpty()) {
                plugin.getLogger().warning("Dialogue '" + dialogueId + "' has no target specified!");
                continue;
            }

            DialogueEntry entry = new DialogueEntry(dialogueId, target, luxDialoguesId, description, enabled);
            dialogues.put(dialogueId, entry);

            // Index by target for quick lookup
            targetToDialogues.computeIfAbsent(target, k -> new ArrayList<>()).add(entry);

            loadedCount++;
            plugin.debug("Loaded dialogue: " + entry);
        }

        plugin.getLogger().info(plugin.getMessage("dialogue-loaded", "count", String.valueOf(loadedCount)));
    }

    /**
     * Validate all loaded dialogues
     * @return number of invalid dialogues found
     */
    public int validateAllDialogues() {
        plugin.log(Level.INFO, plugin.getMessage("validation-started"));

        int validCount = 0;
        int invalidCount = 0;

        for (DialogueEntry dialogue : dialogues.values()) {
            if (!dialogue.isEnabled()) {
                plugin.debug("Skipping disabled dialogue: " + dialogue.getId());
                continue;
            }

            ValidationResult result = validateDialogue(dialogue);
            if (result.isValid()) {
                validCount++;
                plugin.debug("Dialogue '" + dialogue.getId() + "' is valid");
            } else {
                invalidCount++;
                String message = plugin.getMessage("invalid-target",
                        "dialogue", dialogue.getId(),
                        "target", dialogue.getTarget());
                plugin.getLogger().warning(message + " - Reason: " + result.getReason());
            }
        }

        plugin.log(Level.INFO, plugin.getMessage("validation-complete",
                "valid", String.valueOf(validCount),
                "invalid", String.valueOf(invalidCount)));

        return invalidCount;
    }

    /**
     * Validate a single dialogue entry
     * @param dialogue The dialogue to validate
     * @return validation result
     */
    public ValidationResult validateDialogue(DialogueEntry dialogue) {
        IntegrationManager integrationMgr = plugin.getIntegrationManager();

        switch (dialogue.getTargetType()) {
            case MYTHICMOBS:
                if (!integrationMgr.isMythicMobsAvailable()) {
                    return ValidationResult.invalid("MythicMobs plugin not available");
                }
                boolean mobExists = integrationMgr.getMythicMobsIntegration().mobExists(dialogue.getTargetId());
                return mobExists ?
                        ValidationResult.valid() :
                        ValidationResult.invalid("MythicMobs model '" + dialogue.getTargetId() + "' not found");

            case NEXO_BLOCK:
                if (!integrationMgr.isNexoAvailable()) {
                    return ValidationResult.invalid("Nexo plugin not available");
                }
                boolean blockExists = integrationMgr.getNexoIntegration().blockExists(dialogue.getTargetId());
                return blockExists ?
                        ValidationResult.valid() :
                        ValidationResult.invalid("Nexo block '" + dialogue.getTargetId() + "' not found");

            case NEXO_FURNITURE:
                if (!integrationMgr.isNexoAvailable()) {
                    return ValidationResult.invalid("Nexo plugin not available");
                }
                boolean furnitureExists = integrationMgr.getNexoIntegration().furnitureExists(dialogue.getTargetId());
                return furnitureExists ?
                        ValidationResult.valid() :
                        ValidationResult.invalid("Nexo furniture '" + dialogue.getTargetId() + "' not found");

            case NEXO_SIMPLE:
                if (!integrationMgr.isNexoAvailable()) {
                    return ValidationResult.invalid("Nexo plugin not available");
                }
                boolean itemExists = integrationMgr.getNexoIntegration().itemExists(dialogue.getTargetId());
                return itemExists ?
                        ValidationResult.valid() :
                        ValidationResult.invalid("Nexo item '" + dialogue.getTargetId() + "' not found as block or furniture");

            case FANCYNPCS:
                if (!integrationMgr.isFancyNpcsAvailable()) {
                    return ValidationResult.invalid("FancyNpcs plugin not available");
                }
                boolean npcExists = integrationMgr.getFancyNpcsIntegration().npcExists(dialogue.getTargetId());
                return npcExists ?
                        ValidationResult.valid() :
                        ValidationResult.invalid("FancyNpcs entity '" + dialogue.getTargetId() + "' not found");

            case UNKNOWN:
            default:
                return ValidationResult.invalid("Unknown target type for: " + dialogue.getTarget());
        }
    }

    /**
     * Get a dialogue by ID
     */
    public DialogueEntry getDialogue(String id) {
        return dialogues.get(id);
    }

    /**
     * Get all dialogues for a specific target
     */
    public List<DialogueEntry> getDialoguesForTarget(String target) {
        return targetToDialogues.getOrDefault(target, Collections.emptyList());
    }

    /**
     * Get all loaded dialogues
     */
    public Collection<DialogueEntry> getAllDialogues() {
        return dialogues.values();
    }

    /**
     * Get dialogue count
     */
    public int getDialogueCount() {
        return dialogues.size();
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        dialogues.clear();
        targetToDialogues.clear();
    }

    /**
     * Validation result helper class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String reason;

        private ValidationResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, "Valid");
        }

        public static ValidationResult invalid(String reason) {
            return new ValidationResult(false, reason);
        }

        public boolean isValid() {
            return valid;
        }

        public String getReason() {
            return reason;
        }
    }
}
