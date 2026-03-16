package com.wynvers.wdialogues.addon.luxdialogues.command;

import com.wynvers.wdialogues.addon.luxdialogues.LuxDialoguesAddon;
import com.wynvers.wdialogues.addon.luxdialogues.model.DialogueEntry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for dialogue administration
 */
public class DialogueCommand implements CommandExecutor, TabCompleter {

    private final LuxDialoguesAddon plugin;

    public DialogueCommand(LuxDialoguesAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luxdialogue.admin")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;

            case "list":
                handleList(sender);
                break;

            case "info":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /luxdialogue info <dialogue_id>");
                    return true;
                }
                handleInfo(sender, args[1]);
                break;

            case "validate":
                handleValidate(sender);
                break;

            case "integrations":
                handleIntegrations(sender);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        try {
            plugin.reloadConfigs();
            sender.sendMessage(plugin.getMessage("reload-success"));
        } catch (Exception e) {
            sender.sendMessage(plugin.getMessage("reload-error", "error", e.getMessage()));
        }
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Loaded Dialogues ===");
        sender.sendMessage(ChatColor.GRAY + "Total: " + plugin.getDialogueManager().getDialogueCount());
        sender.sendMessage("");

        for (DialogueEntry dialogue : plugin.getDialogueManager().getAllDialogues()) {
            String status = dialogue.isEnabled() ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";

            // Show all targets
            List<String> targets = dialogue.getTargets();
            String targetDisplay = targets.size() == 1 ?
                targets.get(0) :
                targets.size() + " targets";

            sender.sendMessage(status + ChatColor.YELLOW + " " + dialogue.getId() +
                    ChatColor.GRAY + " (" + targetDisplay + ")");
        }
    }

    private void handleInfo(CommandSender sender, String dialogueId) {
        DialogueEntry dialogue = plugin.getDialogueManager().getDialogue(dialogueId);
        if (dialogue == null) {
            sender.sendMessage(ChatColor.RED + "Dialogue not found: " + dialogueId);
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Dialogue Info: " + dialogueId + " ===");

        // Display all targets
        List<String> targets = dialogue.getTargets();
        if (targets.size() == 1) {
            sender.sendMessage(ChatColor.YELLOW + "Target: " + ChatColor.WHITE + targets.get(0));
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Targets (" + targets.size() + "):");
            for (int i = 0; i < targets.size(); i++) {
                DialogueEntry.TargetInfo info = dialogue.getTargetInfos().get(i);
                sender.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + ChatColor.WHITE +
                    targets.get(i) + ChatColor.GRAY + " (" + info.getType() + ")");
            }
        }

        sender.sendMessage(ChatColor.YELLOW + "LuxDialogues ID: " + ChatColor.WHITE + dialogue.getLuxDialoguesId());

        // Display all LuxDialogues IDs if there are multiple
        if (dialogue.hasMultipleLuxDialoguesIds()) {
            List<String> ids = dialogue.getLuxDialoguesIds();
            sender.sendMessage(ChatColor.YELLOW + "LuxDialogues IDs (" + ids.size() + "):");
            for (int i = 0; i < ids.size(); i++) {
                sender.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + ChatColor.WHITE + ids.get(i));
            }
        }

        sender.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + dialogue.getDescription());
        sender.sendMessage(ChatColor.YELLOW + "Enabled: " + ChatColor.WHITE + dialogue.isEnabled());

        // Validate this dialogue
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Validation:");
        var result = plugin.getDialogueManager().validateDialogue(dialogue);
        if (result.isValid()) {
            sender.sendMessage(ChatColor.GREEN + "✓ Valid - All targets are valid");
        } else {
            sender.sendMessage(ChatColor.RED + "✗ Invalid - " + result.getReason());
        }
    }

    private void handleValidate(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Validating all dialogues...");
        int invalidCount = plugin.getDialogueManager().validateAllDialogues();

        if (invalidCount == 0) {
            sender.sendMessage(ChatColor.GREEN + "All dialogues are valid!");
        } else {
            sender.sendMessage(ChatColor.RED + "Found " + invalidCount + " invalid dialogue(s).");
            sender.sendMessage(ChatColor.GRAY + "Check console for details.");
        }
    }

    private void handleIntegrations(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Plugin Integrations ===");

        boolean mythicMobs = plugin.getIntegrationManager().isMythicMobsAvailable();
        boolean nexo = plugin.getIntegrationManager().isNexoAvailable();
        boolean fancyNpcs = plugin.getIntegrationManager().isFancyNpcsAvailable();

        sender.sendMessage((mythicMobs ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗") +
                ChatColor.YELLOW + " MythicMobs");

        if (nexo) {
            sender.sendMessage(ChatColor.GREEN + "✓" + ChatColor.YELLOW + " Nexo");
            var nexoInt = plugin.getIntegrationManager().getNexoIntegration();
            sender.sendMessage(ChatColor.GRAY + "  - Blocks API: " +
                    (nexoInt.hasBlocksAPI() ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗"));
            sender.sendMessage(ChatColor.GRAY + "  - Furniture API: " +
                    (nexoInt.hasFurnitureAPI() ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗"));
        } else {
            sender.sendMessage(ChatColor.RED + "✗" + ChatColor.YELLOW + " Nexo");
        }

        sender.sendMessage((fancyNpcs ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗") +
                ChatColor.YELLOW + " FancyNpcs");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== LuxDialogues Addon Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/luxdialogue reload" + ChatColor.GRAY + " - Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/luxdialogue list" + ChatColor.GRAY + " - List all dialogues");
        sender.sendMessage(ChatColor.YELLOW + "/luxdialogue info <id>" + ChatColor.GRAY + " - Show dialogue info");
        sender.sendMessage(ChatColor.YELLOW + "/luxdialogue validate" + ChatColor.GRAY + " - Validate all dialogues");
        sender.sendMessage(ChatColor.YELLOW + "/luxdialogue integrations" + ChatColor.GRAY + " - Show plugin integrations");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("luxdialogue.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("reload", "list", "info", "validate", "integrations")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            return plugin.getDialogueManager().getAllDialogues().stream()
                    .map(DialogueEntry::getId)
                    .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
