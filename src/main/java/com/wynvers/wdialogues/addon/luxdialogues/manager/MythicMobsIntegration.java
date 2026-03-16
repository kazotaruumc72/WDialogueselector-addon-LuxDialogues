package com.wynvers.wdialogues.addon.luxdialogues.manager;

import com.wynvers.wdialogues.addon.luxdialogues.LuxDialoguesAddon;

/**
 * Integration with MythicMobs plugin
 * Validates if MythicMobs models exist
 */
public class MythicMobsIntegration {

    private final LuxDialoguesAddon plugin;
    private boolean enabled = false;
    private Object mythicMobsAPI;

    public MythicMobsIntegration(LuxDialoguesAddon plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            // Try to access MythicMobs API
            Class<?> mythicBukkit = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            Object instance = mythicBukkit.getMethod("inst").invoke(null);
            this.mythicMobsAPI = instance.getClass().getMethod("getMobManager").invoke(instance);
            this.enabled = true;
            plugin.debug("MythicMobs API initialized successfully");
        } catch (Exception e) {
            plugin.debug("Failed to initialize MythicMobs API: " + e.getMessage());
            this.enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Check if a MythicMobs mob type exists
     * @param mobId The mob ID to check
     * @return true if the mob exists, false otherwise
     */
    public boolean mobExists(String mobId) {
        if (!enabled || mythicMobsAPI == null) {
            return false;
        }

        try {
            // Use reflection to call getMythicMob method
            Object mythicMob = mythicMobsAPI.getClass()
                    .getMethod("getMythicMob", String.class)
                    .invoke(mythicMobsAPI, mobId);

            boolean exists = mythicMob != null;
            plugin.debug("MythicMobs mob check: " + mobId + " = " + exists);
            return exists;
        } catch (Exception e) {
            plugin.debug("Error checking MythicMobs mob: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the MythicMobs API object
     * @return the MythicMobs API or null if not available
     */
    public Object getAPI() {
        return mythicMobsAPI;
    }
}
