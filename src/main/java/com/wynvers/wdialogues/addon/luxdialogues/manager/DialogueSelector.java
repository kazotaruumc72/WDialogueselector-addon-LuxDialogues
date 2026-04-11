package com.wynvers.wdialogues.addon.luxdialogues.manager;

import com.wynvers.wdialogues.addon.luxdialogues.LuxDialoguesAddon;
import com.wynvers.wdialogues.addon.luxdialogues.model.DialogueEntry;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles dialogue selection when a dialogue entry has multiple LuxDialogues IDs.
 * Supports random and sequential selection modes.
 */
public class DialogueSelector {

    private final LuxDialoguesAddon plugin;

    /**
     * Tracks sequential index per player per dialogue for sequential mode.
     * Key: "playerUUID:dialogueId", Value: next index to use
     */
    private final Map<String, Integer> sequentialTracker = new ConcurrentHashMap<>();

    public DialogueSelector(LuxDialoguesAddon plugin) {
        this.plugin = plugin;
    }

    /**
     * Select a LuxDialogues ID from the dialogue entry's list of IDs
     * @param player The player interacting
     * @param dialogue The dialogue entry with one or more LuxDialogues IDs
     * @return The selected LuxDialogues ID, or null if none available
     */
    public String selectDialogueId(Player player, DialogueEntry dialogue) {
        List<String> ids = dialogue.getLuxDialoguesIds();
        if (ids.isEmpty()) {
            return null;
        }

        if (ids.size() == 1) {
            return ids.get(0);
        }

        String mode = plugin.getConfig().getString("settings.selection-mode", "random").toLowerCase();

        switch (mode) {
            case "sequential":
                return selectSequential(player, dialogue, ids);
            case "random":
            default:
                return selectRandom(ids);
        }
    }

    /**
     * Select a random ID from the list
     */
    private String selectRandom(List<String> ids) {
        return ids.get(ThreadLocalRandom.current().nextInt(ids.size()));
    }

    /**
     * Select the next sequential ID for this player and dialogue
     */
    private String selectSequential(Player player, DialogueEntry dialogue, List<String> ids) {
        String key = player.getUniqueId().toString() + ":" + dialogue.getId();
        int index = sequentialTracker.getOrDefault(key, 0);

        // Wrap around if past the end
        if (index >= ids.size()) {
            index = 0;
        }

        String selected = ids.get(index);
        sequentialTracker.put(key, index + 1);

        return selected;
    }

    /**
     * Reset the sequential tracker for a player
     */
    public void resetPlayer(UUID playerId) {
        sequentialTracker.entrySet().removeIf(entry -> entry.getKey().startsWith(playerId.toString() + ":"));
    }

    /**
     * Clean up all tracking data
     */
    public void cleanup() {
        sequentialTracker.clear();
    }
}
