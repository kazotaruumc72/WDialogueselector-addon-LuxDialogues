package com.wynvers.wdialogues.addon.luxdialogues.manager;

import com.wynvers.wdialogues.addon.luxdialogues.LuxDialoguesAddon;

/**
 * Integration with FancyNpcs plugin
 * Validates if FancyNpcs entities exist
 */
public class FancyNpcsIntegration {

    private final LuxDialoguesAddon plugin;
    private boolean enabled = false;
    private Object npcAPI;

    public FancyNpcsIntegration(LuxDialoguesAddon plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            // Try to access FancyNpcs API
            Class<?> npcClass = Class.forName("de.oliver.fancynpcs.api.FancyNpcsPlugin");
            Object instance = npcClass.getMethod("get").invoke(null);
            this.npcAPI = instance.getClass().getMethod("getNpcAdapter").invoke(instance);
            this.enabled = true;
            plugin.debug("FancyNpcs API initialized successfully");
        } catch (Exception e) {
            plugin.debug("Failed to initialize FancyNpcs API: " + e.getMessage());
            this.enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Check if a FancyNpcs NPC exists
     * @param npcName The NPC name to check
     * @return true if the NPC exists, false otherwise
     */
    public boolean npcExists(String npcName) {
        if (!enabled || npcAPI == null) {
            return false;
        }

        try {
            // Use reflection to get all NPCs and check if one matches
            Object npcsCollection = npcAPI.getClass()
                    .getMethod("getAllNpcs")
                    .invoke(npcAPI);

            if (npcsCollection instanceof Iterable) {
                for (Object npc : (Iterable<?>) npcsCollection) {
                    try {
                        Object data = npc.getClass().getMethod("getData").invoke(npc);
                        String name = (String) data.getClass().getMethod("getName").invoke(data);
                        if (name != null && name.equalsIgnoreCase(npcName)) {
                            plugin.debug("FancyNpcs NPC found: " + npcName);
                            return true;
                        }
                    } catch (Exception e) {
                        plugin.debug("Error reading NPC data: " + e.getMessage());
                    }
                }
            }

            plugin.debug("FancyNpcs NPC not found: " + npcName);
            return false;
        } catch (Exception e) {
            plugin.debug("Error checking FancyNpcs NPC: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the FancyNpcs API object
     * @return the FancyNpcs API or null if not available
     */
    public Object getAPI() {
        return npcAPI;
    }
}
