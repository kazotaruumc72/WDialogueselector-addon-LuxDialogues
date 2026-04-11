package com.wynvers.wdialogues.addon.luxdialogues.manager;

import com.wynvers.wdialogues.addon.luxdialogues.LuxDialoguesAddon;
import org.bukkit.entity.Player;

/**
 * Integration with LuxDialogues plugin
 * Uses reflection to start dialogues and check if dialogue IDs exist
 */
public class LuxDialoguesIntegration {

    private final LuxDialoguesAddon plugin;
    private boolean enabled = false;
    private Object luxDialoguesAPI;

    public LuxDialoguesIntegration(LuxDialoguesAddon plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            // Try to access LuxDialogues API
            Class<?> apiClass = Class.forName("com.aselstudios.luxdialogues.api.LuxDialoguesAPI");
            this.luxDialoguesAPI = apiClass.getMethod("getInstance").invoke(null);
            this.enabled = true;
            plugin.debug("LuxDialogues API initialized successfully");
        } catch (Exception e) {
            // Try alternative API access pattern
            try {
                Class<?> pluginClass = Class.forName("com.aselstudios.luxdialogues.LuxDialogues");
                Object instance = pluginClass.getMethod("getInstance").invoke(null);
                this.luxDialoguesAPI = instance.getClass().getMethod("getAPI").invoke(instance);
                this.enabled = true;
                plugin.debug("LuxDialogues API initialized via plugin instance");
            } catch (Exception e2) {
                plugin.debug("Failed to initialize LuxDialogues API: " + e.getMessage());
                this.enabled = false;
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Start a dialogue for a player using the LuxDialogues API
     * @param player The player to start the dialogue for
     * @param dialogueId The LuxDialogues dialogue ID to start
     * @return true if the dialogue was started successfully
     */
    public boolean startDialogue(Player player, String dialogueId) {
        if (!enabled || luxDialoguesAPI == null) {
            plugin.debug("Cannot start dialogue - LuxDialogues API not available");
            return false;
        }

        try {
            // Try common API method names for starting a dialogue
            try {
                luxDialoguesAPI.getClass()
                        .getMethod("startDialogue", Player.class, String.class)
                        .invoke(luxDialoguesAPI, player, dialogueId);
                plugin.debug("Started dialogue '" + dialogueId + "' for player " + player.getName());
                return true;
            } catch (NoSuchMethodException e) {
                // Try alternative method name
                try {
                    luxDialoguesAPI.getClass()
                            .getMethod("openDialogue", Player.class, String.class)
                            .invoke(luxDialoguesAPI, player, dialogueId);
                    plugin.debug("Opened dialogue '" + dialogueId + "' for player " + player.getName());
                    return true;
                } catch (NoSuchMethodException e2) {
                    // Try playDialogue
                    luxDialoguesAPI.getClass()
                            .getMethod("playDialogue", Player.class, String.class)
                            .invoke(luxDialoguesAPI, player, dialogueId);
                    plugin.debug("Played dialogue '" + dialogueId + "' for player " + player.getName());
                    return true;
                }
            }
        } catch (Exception e) {
            plugin.debug("Error starting dialogue '" + dialogueId + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a LuxDialogues dialogue ID exists
     * @param dialogueId The dialogue ID to check
     * @return true if the dialogue exists
     */
    public boolean dialogueExists(String dialogueId) {
        if (!enabled || luxDialoguesAPI == null) {
            return false;
        }

        try {
            try {
                Object result = luxDialoguesAPI.getClass()
                        .getMethod("hasDialogue", String.class)
                        .invoke(luxDialoguesAPI, dialogueId);
                return result instanceof Boolean && (Boolean) result;
            } catch (NoSuchMethodException e) {
                // Try alternative: getDialogue returns non-null if exists
                try {
                    Object dialogue = luxDialoguesAPI.getClass()
                            .getMethod("getDialogue", String.class)
                            .invoke(luxDialoguesAPI, dialogueId);
                    return dialogue != null;
                } catch (NoSuchMethodException e2) {
                    plugin.debug("Cannot check dialogue existence - no suitable API method found");
                    return true; // Assume it exists if we can't verify
                }
            }
        } catch (Exception e) {
            plugin.debug("Error checking dialogue existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the LuxDialogues API object
     * @return the LuxDialogues API or null if not available
     */
    public Object getAPI() {
        return luxDialoguesAPI;
    }
}
