package com.wynvers.wdialogues.addon.luxdialogues.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a dialogue configuration with its target entity or entities
 */
public class DialogueEntry {

    private final String id;
    private final List<String> targets;
    private final String luxDialoguesId;
    private final List<String> luxDialoguesIds;
    private final String description;
    private final boolean enabled;
    private final List<TargetInfo> targetInfos;

    public DialogueEntry(String id, List<String> targets, String luxDialoguesId, String description, boolean enabled) {
        this.id = id;
        this.targets = new ArrayList<>(targets);
        this.luxDialoguesId = luxDialoguesId;

        // Parse comma-separated luxdialogues IDs
        this.luxDialoguesIds = new ArrayList<>();
        if (luxDialoguesId != null && !luxDialoguesId.isEmpty()) {
            String[] ids = luxDialoguesId.split(",");
            for (String luxId : ids) {
                String trimmed = luxId.trim();
                if (!trimmed.isEmpty()) {
                    this.luxDialoguesIds.add(trimmed);
                }
            }
        }

        this.description = description;
        this.enabled = enabled;

        // Parse all target types and IDs
        this.targetInfos = new ArrayList<>();
        for (String target : targets) {
            this.targetInfos.add(parseTarget(target));
        }
    }

    /**
     * Constructor for backward compatibility with single target
     */
    public DialogueEntry(String id, String target, String luxDialoguesId, String description, boolean enabled) {
        this(id, Collections.singletonList(target), luxDialoguesId, description, enabled);
    }

    private TargetInfo parseTarget(String target) {
        if (target == null || target.isEmpty()) {
            return new TargetInfo(TargetType.UNKNOWN, "");
        }

        String lowerTarget = target.toLowerCase();

        if (lowerTarget.startsWith("mythicmobs:")) {
            return new TargetInfo(TargetType.MYTHICMOBS, target.substring("mythicmobs:".length()));
        } else if (lowerTarget.startsWith("nexo-block:")) {
            return new TargetInfo(TargetType.NEXO_BLOCK, target.substring("nexo-block:".length()));
        } else if (lowerTarget.startsWith("nexo-furniture:")) {
            return new TargetInfo(TargetType.NEXO_FURNITURE, target.substring("nexo-furniture:".length()));
        } else if (lowerTarget.startsWith("nexo:") || lowerTarget.startsWith("nexo-")) {
            // Simplified format - try as both block and furniture
            String id = lowerTarget.startsWith("nexo:") ?
                target.substring("nexo:".length()) :
                target.substring("nexo-".length());
            return new TargetInfo(TargetType.NEXO_SIMPLE, id);
        } else if (lowerTarget.startsWith("fancynpcs:")) {
            return new TargetInfo(TargetType.FANCYNPCS, target.substring("fancynpcs:".length()));
        }

        return new TargetInfo(TargetType.UNKNOWN, target);
    }

    // Getters
    public String getId() {
        return id;
    }

    /**
     * Get all targets for this dialogue
     */
    public List<String> getTargets() {
        return Collections.unmodifiableList(targets);
    }

    /**
     * Get the first target (for backward compatibility)
     */
    @Deprecated
    public String getTarget() {
        return targets.isEmpty() ? "" : targets.get(0);
    }

    public String getLuxDialoguesId() {
        return luxDialoguesId;
    }

    /**
     * Get all LuxDialogues IDs as a list
     */
    public List<String> getLuxDialoguesIds() {
        return Collections.unmodifiableList(luxDialoguesIds);
    }

    /**
     * Check if this dialogue has multiple LuxDialogues IDs
     */
    public boolean hasMultipleLuxDialoguesIds() {
        return luxDialoguesIds.size() > 1;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get all target info objects
     */
    public List<TargetInfo> getTargetInfos() {
        return Collections.unmodifiableList(targetInfos);
    }

    /**
     * Get the type of the first target (for backward compatibility)
     */
    @Deprecated
    public TargetType getTargetType() {
        return targetInfos.isEmpty() ? TargetType.UNKNOWN : targetInfos.get(0).type;
    }

    /**
     * Get the ID of the first target (for backward compatibility)
     */
    @Deprecated
    public String getTargetId() {
        return targetInfos.isEmpty() ? "" : targetInfos.get(0).id;
    }

    @Override
    public String toString() {
        return "DialogueEntry{" +
                "id='" + id + '\'' +
                ", targets=" + targets +
                ", luxDialoguesId='" + luxDialoguesId + '\'' +
                ", enabled=" + enabled +
                '}';
    }

    /**
     * Target type enumeration
     */
    public enum TargetType {
        MYTHICMOBS,
        NEXO_BLOCK,
        NEXO_FURNITURE,
        NEXO_SIMPLE,  // Can be either block or furniture
        FANCYNPCS,
        UNKNOWN
    }

    /**
     * Target information class
     */
    public static class TargetInfo {
        public final TargetType type;
        public final String id;

        TargetInfo(TargetType type, String id) {
            this.type = type;
            this.id = id;
        }

        public TargetType getType() {
            return type;
        }

        public String getId() {
            return id;
        }
    }
}
