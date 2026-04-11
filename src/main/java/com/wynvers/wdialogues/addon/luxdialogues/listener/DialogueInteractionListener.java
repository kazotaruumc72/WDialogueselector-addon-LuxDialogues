package com.wynvers.wdialogues.addon.luxdialogues.listener;

import com.wynvers.wdialogues.addon.luxdialogues.LuxDialoguesAddon;
import com.wynvers.wdialogues.addon.luxdialogues.manager.DialogueManager;
import com.wynvers.wdialogues.addon.luxdialogues.manager.DialogueSelector;
import com.wynvers.wdialogues.addon.luxdialogues.manager.LuxDialoguesIntegration;
import com.wynvers.wdialogues.addon.luxdialogues.model.DialogueEntry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.List;

/**
 * Listener for dialogue interactions
 * Detects player interactions with registered targets and triggers dialogue selection
 */
public class DialogueInteractionListener implements Listener {

    private final LuxDialoguesAddon plugin;
    private final DialogueSelector dialogueSelector;

    public DialogueInteractionListener(LuxDialoguesAddon plugin) {
        this.plugin = plugin;
        this.dialogueSelector = new DialogueSelector(plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();

        // Try to identify the block as a Nexo custom block
        String targetId = identifyBlockTarget(event);
        if (targetId == null) return;

        handleTargetInteraction(player, targetId);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        // Try to identify the entity as a FancyNPC or MythicMob
        String targetId = identifyEntityTarget(event);
        if (targetId == null) return;

        handleTargetInteraction(player, targetId);
    }

    /**
     * Handle interaction with a recognized target
     * Finds matching dialogues and triggers dialogue selection
     */
    private void handleTargetInteraction(Player player, String targetId) {
        if (!player.hasPermission("luxdialogue.use")) {
            return;
        }

        DialogueManager manager = plugin.getDialogueManager();
        List<DialogueEntry> matchingDialogues = manager.getDialoguesForTarget(targetId);

        if (matchingDialogues.isEmpty()) {
            plugin.debug("No dialogues found for target: " + targetId);
            return;
        }

        for (DialogueEntry dialogue : matchingDialogues) {
            if (!dialogue.isEnabled()) {
                plugin.debug("Skipping disabled dialogue: " + dialogue.getId());
                continue;
            }

            // Validate the dialogue before triggering
            if (!shouldAllowDialogue(dialogue)) {
                continue;
            }

            // Select a LuxDialogues ID from the list
            String selectedId = dialogueSelector.selectDialogueId(player, dialogue);
            if (selectedId == null) {
                plugin.debug("No LuxDialogues ID available for dialogue: " + dialogue.getId());
                continue;
            }

            plugin.debug("Selected LuxDialogues ID '" + selectedId + "' from dialogue '" + dialogue.getId() +
                    "' (available: " + dialogue.getLuxDialoguesId() + ")");

            // Trigger the dialogue via LuxDialogues API
            triggerDialogue(player, selectedId);
            break; // Only trigger the first matching dialogue
        }
    }

    /**
     * Trigger a LuxDialogues dialogue for a player
     */
    private void triggerDialogue(Player player, String luxDialogueId) {
        if (plugin.getIntegrationManager().isLuxDialoguesAvailable()) {
            LuxDialoguesIntegration luxApi = plugin.getIntegrationManager().getLuxDialoguesIntegration();
            boolean success = luxApi.startDialogue(player, luxDialogueId);
            if (success) {
                plugin.debug("Triggered dialogue '" + luxDialogueId + "' for " + player.getName() + " via API");
            } else {
                plugin.debug("Failed to trigger dialogue via API, falling back to command");
                triggerDialogueViaCommand(player, luxDialogueId);
            }
        } else {
            triggerDialogueViaCommand(player, luxDialogueId);
        }
    }

    /**
     * Fallback: trigger dialogue via server command
     */
    private void triggerDialogueViaCommand(Player player, String luxDialogueId) {
        String command = plugin.getConfig().getString("settings.dialogue-command",
                "luxdialogues open {player} {id}");
        command = command.replace("{player}", player.getName())
                .replace("{id}", luxDialogueId);

        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        plugin.debug("Triggered dialogue '" + luxDialogueId + "' for " + player.getName() + " via command");
    }

    /**
     * Validate a dialogue before allowing it to trigger
     */
    private boolean shouldAllowDialogue(DialogueEntry dialogue) {
        boolean requireValid = plugin.getConfig().getBoolean("settings.validation.require-valid-targets", true);
        if (!requireValid) {
            return true;
        }

        DialogueManager manager = plugin.getDialogueManager();
        DialogueManager.ValidationResult result = manager.validateDialogue(dialogue);
        if (!result.isValid()) {
            plugin.debug("Dialogue validation failed: " + dialogue.getId() + " - " + result.getReason());
            boolean blockInvalid = plugin.getConfig().getBoolean("settings.validation.block-invalid-dialogues", true);
            return !blockInvalid;
        }

        return true;
    }

    /**
     * Try to identify a block interaction target as a Nexo block
     * @return the target string (e.g., "nexo:block_id") or null
     */
    private String identifyBlockTarget(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return null;

        // Check if it's a Nexo block via reflection
        if (plugin.getIntegrationManager().isNexoAvailable()) {
            try {
                Class<?> nexoBlocksClass = Class.forName("com.nexomc.nexo.api.NexoBlocks");
                Object blockId = nexoBlocksClass.getMethod("customBlockId", org.bukkit.block.Block.class)
                        .invoke(null, event.getClickedBlock());
                if (blockId != null) {
                    return "nexo:" + blockId;
                }
            } catch (Exception e) {
                plugin.debug("Error identifying Nexo block: " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * Try to identify an entity interaction target as a FancyNPC or MythicMob
     * @return the target string (e.g., "fancynpcs:npc_name") or null
     */
    private String identifyEntityTarget(PlayerInteractEntityEvent event) {
        org.bukkit.entity.Entity entity = event.getRightClicked();

        // Check if it's a FancyNPC
        if (plugin.getIntegrationManager().isFancyNpcsAvailable()) {
            try {
                Class<?> fancyNpcsClass = Class.forName("de.oliver.fancynpcs.api.FancyNpcsPlugin");
                Object pluginInstance = fancyNpcsClass.getMethod("get").invoke(null);
                Object npcAdapter = pluginInstance.getClass().getMethod("getNpcAdapter").invoke(pluginInstance);
                Object npcsCollection = npcAdapter.getClass().getMethod("getAllNpcs").invoke(npcAdapter);

                if (npcsCollection instanceof Iterable) {
                    for (Object npc : (Iterable<?>) npcsCollection) {
                        try {
                            Object npcEntity = npc.getClass().getMethod("getEntity").invoke(npc);
                            if (npcEntity != null && npcEntity.equals(entity)) {
                                Object data = npc.getClass().getMethod("getData").invoke(npc);
                                String name = (String) data.getClass().getMethod("getName").invoke(data);
                                return "fancynpcs:" + name;
                            }
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception e) {
                plugin.debug("Error identifying FancyNPC: " + e.getMessage());
            }
        }

        // Check if it's a MythicMob
        if (plugin.getIntegrationManager().isMythicMobsAvailable()) {
            try {
                Class<?> mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
                Object instance = mythicBukkitClass.getMethod("inst").invoke(null);
                Object mobManager = instance.getClass().getMethod("getMobManager").invoke(instance);

                // Check if entity is an ActiveMob
                Object activeMob = mobManager.getClass()
                        .getMethod("getActiveMob", java.util.UUID.class)
                        .invoke(mobManager, entity.getUniqueId());

                if (activeMob != null) {
                    // Get internal name from the Optional
                    if (activeMob instanceof java.util.Optional) {
                        java.util.Optional<?> opt = (java.util.Optional<?>) activeMob;
                        if (opt.isPresent()) {
                            Object mob = opt.get();
                            Object mobType = mob.getClass().getMethod("getMobType").invoke(mob);
                            String internalName = (String) mobType.getClass().getMethod("getInternalName").invoke(mobType);
                            return "mythicmobs:" + internalName;
                        }
                    }
                }
            } catch (Exception e) {
                plugin.debug("Error identifying MythicMob: " + e.getMessage());
            }
        }

        // Check if it's a Nexo furniture
        if (plugin.getIntegrationManager().isNexoAvailable()) {
            try {
                Class<?> nexoFurnitureClass = Class.forName("com.nexomc.nexo.api.NexoFurniture");
                Object furnitureId = nexoFurnitureClass.getMethod("furnitureId", org.bukkit.entity.Entity.class)
                        .invoke(null, entity);
                if (furnitureId != null) {
                    return "nexo:" + furnitureId;
                }
            } catch (Exception e) {
                plugin.debug("Error identifying Nexo furniture: " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * Get the dialogue selector instance
     */
    public DialogueSelector getDialogueSelector() {
        return dialogueSelector;
    }
}
