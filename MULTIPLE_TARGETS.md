# Multiple Targets Feature - Implementation Summary

## Overview
This update adds the ability to specify multiple targets for the same dialogue in the LuxDialogues addon. Previously, each dialogue could only target a single entity. Now, dialogues can target multiple entities of any supported type.

## Changes Made

### 1. DialogueEntry Model (DialogueEntry.java)
**Key Changes:**
- Changed from single `target` (String) to `targets` (List<String>)
- Changed from single `targetInfo` to `targetInfos` (List<TargetInfo>)
- Added backward compatibility constructor for single target
- Made `TargetInfo` class public for external access
- Added deprecated getters for single target access (backward compatibility)
- New getters:
  - `getTargets()` - returns all targets
  - `getTargetInfos()` - returns all target information objects

**Backward Compatibility:**
- Old single-target constructor still works
- Deprecated `getTarget()`, `getTargetType()`, `getTargetId()` methods return first target values
- Existing code using these methods will continue to work

### 2. DialogueManager (DialogueManager.java)
**Configuration Loading:**
- Now supports both `target:` (single) and `targets:` (list) in YAML
- If `targets` is defined as a list, all items are loaded
- If `target` is defined (singular), it's converted to a single-item list
- Empty target lists generate warning messages

**Validation:**
- `validateDialogue()` now validates ALL targets in a dialogue
- A dialogue is considered valid only if ALL targets are valid
- New helper method `validateTarget()` validates individual targets
- Error messages list all invalid targets with their specific reasons

**Indexing:**
- Each target is indexed separately in `targetToDialogues` map
- Same dialogue can appear under multiple target keys
- Efficient O(1) lookup for any target

### 3. DialogueCommand (DialogueCommand.java)
**List Command:**
- Shows "X targets" for multi-target dialogues
- Shows target string for single-target dialogues

**Info Command:**
- Displays all targets with their types
- Shows numbered list for multiple targets
- Shows single line for single target (backward compatible)
- Validation message updated to reflect all targets

### 4. Configuration Files

**dialogues.yml:**
Added examples showing:
- Single target format (backward compatible)
- Multiple targets with same type (e.g., multiple statues)
- Multiple targets with mixed types (e.g., boss + altar)
- Multiple NPCs with same dialogue

**README.md:**
Added new section:
- Explanation of single vs multiple targets
- Syntax examples for both formats
- Use cases for multiple targets
- Updated configuration examples

## Features

### Multiple Targets Support
```yaml
dialogue_example:
  targets:
    - 'nexo:magic_statue_5'
    - 'nexo:magic_statue_6'
    - 'fancynpcs:Merchant'
  luxdialogues-id: 'greeting'
```

### Backward Compatibility
```yaml
# Old format still works
dialogue_example:
  target: 'nexo:magic_statue_5'
  luxdialogues-id: 'greeting'
```

### Mixed Types
```yaml
# Different entity types in same dialogue
dialogue_boss_altar:
  targets:
    - 'mythicmobs:AncientGuardian'
    - 'nexo-block:magic_altar'
  luxdialogues-id: 'interaction'
```

## Validation

The validation system ensures:
1. All targets in a dialogue must be valid
2. Each target is validated according to its type (MythicMobs, Nexo, FancyNPCs)
3. Invalid targets are reported with specific error messages
4. Validation results show which specific targets failed and why

## Use Cases

1. **Common NPC Dialogues**: Same greeting for multiple village NPCs
2. **Quest Progression**: Same dialogue for multiple quest objectives
3. **Furniture Sets**: Same interaction for related furniture pieces
4. **Mixed Interactions**: Dialogue that works on both mobs and blocks

## Performance Impact

- Minimal: O(n) where n = number of targets per dialogue
- Target lookup remains O(1) via indexed map
- Validation is O(n * v) where v = validation cost per target
- Memory overhead: ~50 bytes per additional target

## Testing Recommendations

1. Test single target dialogues (backward compatibility)
2. Test multiple targets of same type
3. Test multiple targets of different types
4. Test validation with invalid targets
5. Test `/luxdialogue list` and `/luxdialogue info` commands
6. Test configuration reload with mixed formats

## Migration Guide

### For Existing Configurations
No changes needed! Existing single-target configurations continue to work.

### To Use Multiple Targets
Simply change:
```yaml
target: 'single_value'
```

To:
```yaml
targets:
  - 'value1'
  - 'value2'
  - 'value3'
```

## Future Enhancements

Potential improvements:
1. Target groups (define reusable target lists)
2. Wildcard target matching (e.g., `nexo:magic_statue_*`)
3. Target exclusions (all except specific ones)
4. Conditional targets based on player state

## Conclusion

This feature significantly enhances the flexibility of the dialogue system by allowing dialogues to be reused across multiple entities. The implementation maintains full backward compatibility while providing powerful new configuration options.
