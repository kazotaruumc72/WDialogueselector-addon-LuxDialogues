package com.wynvers.wdialogues.addon.luxdialogues;

import com.wynvers.wdialogues.addon.luxdialogues.command.DialogueCommand;
import com.wynvers.wdialogues.addon.luxdialogues.listener.DialogueInteractionListener;
import com.wynvers.wdialogues.addon.luxdialogues.manager.DialogueManager;
import com.wynvers.wdialogues.addon.luxdialogues.manager.IntegrationManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

/**
 * Main plugin class for WDialogueselector-addon-LuxDialogues
 * Ensures each dialogue targets a valid MythicMobs model, Nexo block/furniture, or FancyNPCs entity
 */
public class LuxDialoguesAddon extends JavaPlugin {

    private static LuxDialoguesAddon instance;
    private IntegrationManager integrationManager;
    private DialogueManager dialogueManager;
    private DialogueInteractionListener dialogueListener;
    private FileConfiguration dialoguesConfig;
    private boolean debugMode;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();
        debugMode = getConfig().getBoolean("settings.debug", false);

        // Initialize dialogues configuration
        loadDialoguesConfig();

        // Check if LuxDialogues is available
        if (getServer().getPluginManager().getPlugin("LuxDialogues") == null) {
            getLogger().severe("LuxDialogues plugin not found! This addon requires LuxDialogues to function.");
            getLogger().severe("Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize integration manager
        this.integrationManager = new IntegrationManager(this);
        this.integrationManager.initialize();

        // Initialize dialogue manager
        this.dialogueManager = new DialogueManager(this);
        this.dialogueManager.loadDialogues();

        // Validate dialogues on startup
        if (getConfig().getBoolean("settings.validation.warn-on-invalid", true)) {
            dialogueManager.validateAllDialogues();
        }

        // Register listeners
        registerListeners();

        // Register commands
        registerCommands();

        getLogger().info("WDialogueselector-addon-LuxDialogues has been enabled!");
        getLogger().info("Loaded with " + integrationManager.getAvailableIntegrations() + " integration(s)");
    }

    @Override
    public void onDisable() {
        if (dialogueManager != null) {
            dialogueManager.cleanup();
        }
        if (dialogueListener != null) {
            dialogueListener.getDialogueSelector().cleanup();
        }
        getLogger().info("WDialogueselector-addon-LuxDialogues has been disabled!");
    }

    private void loadDialoguesConfig() {
        File dialoguesFile = new File(getDataFolder(), "dialogues.yml");
        if (!dialoguesFile.exists()) {
            saveResource("dialogues.yml", false);
        }
        this.dialoguesConfig = YamlConfiguration.loadConfiguration(dialoguesFile);
    }

    private void registerListeners() {
        DialogueInteractionListener listener = new DialogueInteractionListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        this.dialogueListener = listener;
        debug("Registered event listeners");
    }

    private void registerCommands() {
        DialogueCommand dialogueCommand = new DialogueCommand(this);
        getCommand("luxdialogue").setExecutor(dialogueCommand);
        getCommand("luxdialogue").setTabCompleter(dialogueCommand);
        debug("Registered commands");
    }

    public void reloadConfigs() {
        reloadConfig();
        loadDialoguesConfig();
        debugMode = getConfig().getBoolean("settings.debug", false);

        // Reload dialogue manager
        if (dialogueManager != null) {
            dialogueManager.loadDialogues();
        }

        debug("Configuration reloaded");
    }

    public void debug(String message) {
        if (debugMode) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    // Getters
    public static LuxDialoguesAddon getInstance() {
        return instance;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public DialogueManager getDialogueManager() {
        return dialogueManager;
    }

    public FileConfiguration getDialoguesConfig() {
        return dialoguesConfig;
    }

    public String getMessage(String path) {
        String prefix = getConfig().getString("messages.prefix", "");
        String message = getConfig().getString("messages." + path, path);
        return prefix + message;
    }

    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        return message;
    }
}
