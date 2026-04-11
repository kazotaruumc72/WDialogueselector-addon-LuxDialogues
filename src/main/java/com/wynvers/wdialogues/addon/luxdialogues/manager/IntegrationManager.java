package com.wynvers.wdialogues.addon.luxdialogues.manager;

import com.wynvers.wdialogues.addon.luxdialogues.LuxDialoguesAddon;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages integrations with external plugins (MythicMobs, Nexo, FancyNPCs)
 */
public class IntegrationManager {

    private final LuxDialoguesAddon plugin;
    private LuxDialoguesIntegration luxDialoguesIntegration;
    private MythicMobsIntegration mythicMobsIntegration;
    private NexoIntegration nexoIntegration;
    private FancyNpcsIntegration fancyNpcsIntegration;

    public IntegrationManager(LuxDialoguesAddon plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        plugin.debug("Initializing integrations...");

        // Initialize LuxDialogues integration
        try {
            this.luxDialoguesIntegration = new LuxDialoguesIntegration(plugin);
            this.luxDialoguesIntegration.initialize();
            if (luxDialoguesIntegration.isEnabled()) {
                plugin.getLogger().info("LuxDialogues API integration enabled!");
            } else {
                plugin.debug("LuxDialogues API not accessible via reflection - dialogue triggering will use commands");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize LuxDialogues integration: " + e.getMessage());
            this.luxDialoguesIntegration = null;
        }

        // Initialize MythicMobs integration
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            try {
                this.mythicMobsIntegration = new MythicMobsIntegration(plugin);
                this.mythicMobsIntegration.initialize();
                plugin.getLogger().info("MythicMobs integration enabled!");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to initialize MythicMobs integration: " + e.getMessage());
                this.mythicMobsIntegration = null;
            }
        } else {
            plugin.debug("MythicMobs not found - integration disabled");
        }

        // Initialize Nexo integration
        if (Bukkit.getPluginManager().getPlugin("Nexo") != null) {
            try {
                this.nexoIntegration = new NexoIntegration(plugin);
                this.nexoIntegration.initialize();
                plugin.getLogger().info("Nexo integration enabled!");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to initialize Nexo integration: " + e.getMessage());
                this.nexoIntegration = null;
            }
        } else {
            plugin.debug("Nexo not found - integration disabled");
        }

        // Initialize FancyNPCs integration
        if (Bukkit.getPluginManager().getPlugin("FancyNpcs") != null) {
            try {
                this.fancyNpcsIntegration = new FancyNpcsIntegration(plugin);
                this.fancyNpcsIntegration.initialize();
                plugin.getLogger().info("FancyNpcs integration enabled!");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to initialize FancyNpcs integration: " + e.getMessage());
                this.fancyNpcsIntegration = null;
            }
        } else {
            plugin.debug("FancyNpcs not found - integration disabled");
        }

        plugin.debug("Integration initialization complete");
    }

    public boolean isMythicMobsAvailable() {
        return mythicMobsIntegration != null && mythicMobsIntegration.isEnabled();
    }

    public boolean isNexoAvailable() {
        return nexoIntegration != null && nexoIntegration.isEnabled();
    }

    public boolean isFancyNpcsAvailable() {
        return fancyNpcsIntegration != null && fancyNpcsIntegration.isEnabled();
    }

    public boolean isLuxDialoguesAvailable() {
        return luxDialoguesIntegration != null && luxDialoguesIntegration.isEnabled();
    }

    public LuxDialoguesIntegration getLuxDialoguesIntegration() {
        return luxDialoguesIntegration;
    }

    public MythicMobsIntegration getMythicMobsIntegration() {
        return mythicMobsIntegration;
    }

    public NexoIntegration getNexoIntegration() {
        return nexoIntegration;
    }

    public FancyNpcsIntegration getFancyNpcsIntegration() {
        return fancyNpcsIntegration;
    }

    public int getAvailableIntegrations() {
        int count = 0;
        if (isMythicMobsAvailable()) count++;
        if (isNexoAvailable()) count++;
        if (isFancyNpcsAvailable()) count++;
        return count;
    }

    public List<String> getAvailableIntegrationsList() {
        List<String> integrations = new ArrayList<>();
        if (isMythicMobsAvailable()) integrations.add("MythicMobs");
        if (isNexoAvailable()) integrations.add("Nexo");
        if (isFancyNpcsAvailable()) integrations.add("FancyNpcs");
        return integrations;
    }
}
