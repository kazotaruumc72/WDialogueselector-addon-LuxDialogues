package com.wynvers.wdialogues.addon.luxdialogues.model;

/**
 * Represents a dialogue configuration with its target entity
 */
public class DialogueEntry {

    private final String id;
    private final String target;
    private final String luxDialoguesId;
    private final String description;
    private final boolean enabled;
    private final TargetType targetType;
    private final String targetId;

    public DialogueEntry(String id, String target, String luxDialoguesId, String description, boolean enabled) {
        this.id = id;
        this.target = target;
        this.luxDialoguesId = luxDialoguesId;
        this.description = description;
        this.enabled = enabled;

        // Parse target type and ID
        TargetInfo info = parseTarget(target);
        this.targetType = info.type;
        this.targetId = info.id;
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

    public String getTarget() {
        return target;
    }

    public String getLuxDialoguesId() {
        return luxDialoguesId;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    @Override
    public String toString() {
        return "DialogueEntry{" +
                "id='" + id + '\'' +
                ", target='" + target + '\'' +
                ", luxDialoguesId='" + luxDialoguesId + '\'' +
                ", targetType=" + targetType +
                ", targetId='" + targetId + '\'' +
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

    private static class TargetInfo {
        final TargetType type;
        final String id;

        TargetInfo(TargetType type, String id) {
            this.type = type;
            this.id = id;
        }
    }
}
