package com.wynvers.wdialogues.addon.luxdialogues.listener;

import com.wynvers.wdialogues.addon.luxdialogues.LuxDialoguesAddon;
import com.wynvers.wdialogues.addon.luxdialogues.manager.DialogueManager;
import com.wynvers.wdialogues.addon.luxdialogues.model.DialogueEntry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * Listener for dialogue interactions
 * Validates that dialogues have valid targets before allowing interaction
 */
public class DialogueInteractionListener implements Listener {

    private final LuxDialoguesAddon plugin;

    public DialogueInteractionListener(LuxDialoguesAddon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // This is a placeholder for actual dialogue interaction logic
        // In a real implementation, this would integrate with LuxDialogues API
        // to intercept dialogue starts and validate targets

        plugin.debug("PlayerInteractEvent triggered - would validate dialogue here");

        // Example validation logic (would need actual LuxDialogues integration):
        // 1. Detect when player is about to start a dialogue
        // 2. Identify which dialogue is being triggered
        // 3. Look up the dialogue in our manager
        // 4. Validate the target exists
        // 5. Cancel event if validation fails and dialogue blocking is enabled
    }

    /**
     * Example method showing how validation would be used
     * This would be called when a dialogue is about to start
     */
    private boolean shouldAllowDialogue(String dialogueId) {
        DialogueManager manager = plugin.getDialogueManager();
        DialogueEntry dialogue = manager.getDialogue(dialogueId);

        if (dialogue == null) {
            plugin.debug("Dialogue not found: " + dialogueId);
            return true; // Allow if not managed by us
        }

        if (!dialogue.isEnabled()) {
            plugin.debug("Dialogue is disabled: " + dialogueId);
            return false;
        }

        // Check if validation is required
        boolean requireValid = plugin.getConfig().getBoolean("settings.validation.require-valid-targets", true);
        if (!requireValid) {
            return true;
        }

        // Validate the dialogue
        DialogueManager.ValidationResult result = manager.validateDialogue(dialogue);
        if (!result.isValid()) {
            plugin.debug("Dialogue validation failed: " + dialogueId + " - " + result.getReason());

            // Check if we should block invalid dialogues
            boolean blockInvalid = plugin.getConfig().getBoolean("settings.validation.block-invalid-dialogues", true);
            return !blockInvalid;
        }

        return true;
    }
}
