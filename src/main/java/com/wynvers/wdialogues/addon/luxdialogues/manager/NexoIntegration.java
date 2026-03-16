package com.wynvers.wdialogues.addon.luxdialogues.manager;

import com.wynvers.wdialogues.addon.luxdialogues.LuxDialoguesAddon;

/**
 * Integration with Nexo plugin
 * Validates if Nexo blocks and furniture exist
 */
public class NexoIntegration {

    private final LuxDialoguesAddon plugin;
    private boolean enabled = false;
    private Object nexoAPI;
    private Object blocksAPI;
    private Object furnitureAPI;

    public NexoIntegration(LuxDialoguesAddon plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            // Try to access Nexo API
            Class<?> nexoClass = Class.forName("com.nexomc.nexo.api.NexoAPI");

            // Get blocks API
            try {
                Object blocksInstance = nexoClass.getMethod("getBlocks").invoke(null);
                this.blocksAPI = blocksInstance;
                plugin.debug("Nexo Blocks API initialized");
            } catch (Exception e) {
                plugin.debug("Nexo Blocks API not available: " + e.getMessage());
            }

            // Get furniture API
            try {
                Object furnitureInstance = nexoClass.getMethod("getFurniture").invoke(null);
                this.furnitureAPI = furnitureInstance;
                plugin.debug("Nexo Furniture API initialized");
            } catch (Exception e) {
                plugin.debug("Nexo Furniture API not available: " + e.getMessage());
            }

            // Mark as enabled if at least one API is available
            this.enabled = (blocksAPI != null || furnitureAPI != null);
            if (enabled) {
                plugin.debug("Nexo API initialized successfully");
            }
        } catch (Exception e) {
            plugin.debug("Failed to initialize Nexo API: " + e.getMessage());
            this.enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Check if a Nexo block exists
     * @param blockId The block ID to check
     * @return true if the block exists, false otherwise
     */
    public boolean blockExists(String blockId) {
        if (!enabled || blocksAPI == null) {
            return false;
        }

        try {
            // Use reflection to check if block exists
            Object block = blocksAPI.getClass()
                    .getMethod("block", String.class)
                    .invoke(blocksAPI, blockId);

            boolean exists = block != null;
            plugin.debug("Nexo block check: " + blockId + " = " + exists);
            return exists;
        } catch (Exception e) {
            plugin.debug("Error checking Nexo block: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a Nexo furniture exists
     * @param furnitureId The furniture ID to check
     * @return true if the furniture exists, false otherwise
     */
    public boolean furnitureExists(String furnitureId) {
        if (!enabled || furnitureAPI == null) {
            return false;
        }

        try {
            // Use reflection to check if furniture exists
            Object furniture = furnitureAPI.getClass()
                    .getMethod("furniture", String.class)
                    .invoke(furnitureAPI, furnitureId);

            boolean exists = furniture != null;
            plugin.debug("Nexo furniture check: " + furnitureId + " = " + exists);
            return exists;
        } catch (Exception e) {
            plugin.debug("Error checking Nexo furniture: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if an item exists as either a block or furniture
     * @param itemId The item ID to check
     * @return true if exists as block or furniture, false otherwise
     */
    public boolean itemExists(String itemId) {
        return blockExists(itemId) || furnitureExists(itemId);
    }

    public boolean hasBlocksAPI() {
        return blocksAPI != null;
    }

    public boolean hasFurnitureAPI() {
        return furnitureAPI != null;
    }
}
