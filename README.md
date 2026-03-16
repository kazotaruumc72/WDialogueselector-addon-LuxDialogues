# WDialogueselector-addon-LuxDialogues

A Minecraft plugin addon that ensures each LuxDialogues dialogue targets a valid entity from MythicMobs, Nexo, or FancyNPCs. This addon provides configuration-driven dialogue management with automatic validation and enforcement of target requirements.

## Features

- **Target Validation**: Ensures every dialogue has a valid target from one of the supported plugins
- **Multiple Integration Support**:
  - MythicMobs models
  - Nexo custom blocks
  - Nexo furniture
  - FancyNPCs entities
- **Configuration-Driven**: Easy YAML-based dialogue configuration
- **Validation Commands**: Admin commands to validate and manage dialogues
- **Flexible Targeting**: Support for multiple target formats (explicit and simplified)
- **Debug Mode**: Detailed logging for troubleshooting
- **Hot Reload**: Reload configurations without restarting the server

## Requirements

- **Required**:
  - Java 21 or higher
  - Paper/Spigot 1.21+ (or compatible fork)
  - LuxDialogues plugin

- **Optional** (at least one recommended):
  - MythicMobs 5.6.1+
  - Nexo (with Blocks and/or Furniture API)
  - FancyNpcs 2.2.2+
  - PlaceholderAPI 2.11.6+ (for future features)

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Ensure LuxDialogues is installed
4. Install at least one of the optional plugins (MythicMobs, Nexo, or FancyNPCs)
5. Start/restart your server
6. Configure your dialogues in `plugins/WDialogueselector-addon-LuxDialogues/dialogues.yml`

## Configuration

### Target Format

Each dialogue can specify one or multiple targets using these formats:

| Format | Description | Example |
|--------|-------------|---------|
| `mythicmobs:ModelName` | MythicMobs mob model | `mythicmobs:AncientGuardian` |
| `nexo-block:block_id` | Nexo custom block | `nexo-block:magic_altar` |
| `nexo-furniture:furniture_id` | Nexo furniture | `nexo-furniture:royal_throne` |
| `nexo:item_id` | Nexo item (block or furniture) | `nexo:magic_statue_5` |
| `fancynpcs:npc_name` | FancyNPCs entity | `fancynpcs:VillageTrader` |

### Single vs Multiple Targets

**Single Target** (use `target:`):
```yaml
dialogue_example:
  target: 'nexo:magic_statue_5'
  luxdialogues-id: 'greeting'
```

**Multiple Targets** (use `targets:` with a list):
```yaml
dialogue_example:
  targets:
    - 'nexo:magic_statue_5'
    - 'nexo:magic_statue_6'
    - 'fancynpcs:Merchant'
  luxdialogues-id: 'greeting'
```

With multiple targets, the same dialogue will be available for all listed entities. This is useful for:
- Sharing common dialogues across similar NPCs
- Applying the same dialogue to multiple furniture pieces
- Creating universal interactions that work with different entity types

### Example Configuration

**dialogues.yml**:
```yaml
dialogues:
  # Single target (original format)
  dialogue_statue_1:
    target: 'nexo:magic_statue_5'
    luxdialogues-id: 'statue_greeting'
    description: 'Magic statue dialogue 1'
    enabled: true

  # Multiple targets (NEW - same dialogue for multiple entities)
  dialogue_multi_statue:
    targets:
      - 'nexo:magic_statue_5'
      - 'nexo:magic_statue_6'
      - 'nexo:magic_statue_7'
    luxdialogues-id: 'statue_quest'
    description: 'Quest dialogue for multiple statues'
    enabled: true

  # Mixed target types
  dialogue_boss_and_altar:
    targets:
      - 'mythicmobs:AncientGuardian'
      - 'nexo-block:magic_altar'
    luxdialogues-id: 'boss_altar_interaction'
    description: 'Works on both boss and altar'
    enabled: true

  # Multiple NPCs
  dialogue_village_npcs:
    targets:
      - 'fancynpcs:VillageTrader'
      - 'fancynpcs:VillageBlacksmith'
      - 'fancynpcs:VillageInnkeeper'
    luxdialogues-id: 'village_greeting'
    description: 'Common greeting for village NPCs'
    enabled: true
```

### Settings

**config.yml**:
```yaml
settings:
  debug: false  # Enable debug logging

  validation:
    require-valid-targets: true      # Require all dialogues to have valid targets
    warn-on-invalid: true            # Warn on startup about invalid targets
    block-invalid-dialogues: true    # Prevent dialogue interaction if target is invalid
```

## Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/luxdialogue reload` | `luxdialogue.admin` | Reload configuration |
| `/luxdialogue list` | `luxdialogue.admin` | List all dialogues |
| `/luxdialogue info <id>` | `luxdialogue.admin` | Show dialogue details |
| `/luxdialogue validate` | `luxdialogue.admin` | Validate all dialogues |
| `/luxdialogue integrations` | `luxdialogue.admin` | Show available integrations |

**Aliases**: `/luxd`, `/lxd`

## Permissions

| Permission | Default | Description |
|-----------|---------|-------------|
| `luxdialogue.*` | op | All permissions |
| `luxdialogue.admin` | op | Admin commands |
| `luxdialogue.use` | true | Use dialogue system |

## Building from Source

### Prerequisites

- Java 21 JDK
- Maven 3.6+
- Git

### Build Steps

1. Clone the repository:
```bash
git clone https://github.com/kazotaruumc72/WDialogueselector-addon-LuxDialogues.git
cd WDialogueselector-addon-LuxDialogues
```

2. **(Optional)** Replace stub JAR files with actual API JARs:
   - The `libs/` directory contains stub JAR files for compilation
   - For production use, replace these with actual API JARs (see `libs/README.md`)
   - For development/testing, the stub files are sufficient for compilation

3. Build the plugin:
```bash
mvn clean package
```

4. Find the compiled JAR in `target/` directory

## How It Works

### Validation Process

1. **On Startup**: The plugin loads all dialogues from `dialogues.yml`
2. **Integration Check**: Verifies which integration plugins are available
3. **Target Parsing**: Parses each dialogue's target string to identify the type and ID
4. **Validation**: Checks if the target exists in the respective plugin:
   - MythicMobs: Queries mob manager for model existence
   - Nexo: Checks blocks and furniture APIs
   - FancyNPCs: Searches for NPC by name
5. **Reporting**: Logs validation results and warnings

### Integration Architecture

The plugin uses a manager pattern with reflection-based integration:

```
LuxDialoguesAddon (Main)
├── IntegrationManager
│   ├── MythicMobsIntegration
│   ├── NexoIntegration
│   └── FancyNpcsIntegration
├── DialogueManager
│   ├── Load dialogues
│   ├── Validate targets
│   └── Manage lookup
└── DialogueInteractionListener
    └── Monitor interactions
```

## Use Cases

### Example 1: Magic Statue Quest
Multiple dialogues targeting the same Nexo furniture:
```yaml
dialogues:
  dialogue_statue_1:
    target: 'nexo:magic_statue_5'
    luxdialogues-id: 'statue_greeting'
    description: 'Initial greeting'

  dialogue_statue_2:
    target: 'nexo:magic_statue_5'
    luxdialogues-id: 'statue_quest_start'
    description: 'Quest introduction'

  dialogue_statue_3:
    target: 'nexo:magic_statue_5'
    luxdialogues-id: 'statue_quest_complete'
    description: 'Quest completion'
```

### Example 2: Boss Battle
Dialogue triggered by MythicMobs entity:
```yaml
dialogues:
  dialogue_boss_taunt:
    target: 'mythicmobs:AncientGuardian'
    luxdialogues-id: 'boss_battle_start'
    description: 'Boss pre-battle dialogue'
```

### Example 3: NPC Merchant
Dialogue for FancyNPCs character:
```yaml
dialogues:
  dialogue_merchant:
    target: 'fancynpcs:VillageTrader'
    luxdialogues-id: 'merchant_shop'
    description: 'Trading dialogue'
```

## Troubleshooting

### Issue: Dialogues not validating
- Check that the required plugins (MythicMobs, Nexo, or FancyNPCs) are installed
- Verify the target ID matches exactly (case-sensitive)
- Enable debug mode: `debug: true` in config.yml
- Run `/luxdialogue validate` to see detailed errors

### Issue: Integration not working
- Run `/luxdialogue integrations` to check which plugins are detected
- Ensure plugin versions are compatible
- Check console for initialization errors
- Verify API JARs are present in `libs/` directory (for building)

### Issue: Build fails
- Ensure Java 21 is installed: `java -version`
- Check Maven version: `mvn -version`
- The `libs/` directory with stub JAR files is included in the repository
- For production builds, replace stub JARs with actual API JARs (see `libs/README.md`)
- If you get "Could not resolve dependencies" errors, ensure internet connectivity for Maven to download Paper, MythicMobs, and PlaceholderAPI dependencies

## API Usage (For Developers)

### Accessing the Dialogue Manager

```java
LuxDialoguesAddon plugin = LuxDialoguesAddon.getInstance();
DialogueManager manager = plugin.getDialogueManager();

// Get a dialogue
DialogueEntry dialogue = manager.getDialogue("dialogue_statue_1");

// Validate a dialogue
DialogueManager.ValidationResult result = manager.validateDialogue(dialogue);
if (result.isValid()) {
    // Dialogue is valid
} else {
    // Handle invalid dialogue
    String reason = result.getReason();
}
```

### Checking Integrations

```java
IntegrationManager integrations = plugin.getIntegrationManager();

if (integrations.isMythicMobsAvailable()) {
    boolean exists = integrations.getMythicMobsIntegration()
        .mobExists("AncientGuardian");
}

if (integrations.isNexoAvailable()) {
    boolean exists = integrations.getNexoIntegration()
        .itemExists("magic_statue_5");
}
```

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For issues, questions, or suggestions:
- GitHub Issues: [Report an issue](https://github.com/kazotaruumc72/WDialogueselector-addon-LuxDialogues/issues)
- Wiki: [Documentation](https://wiki.aselstudios.com/luxdialogues/)

## Credits

- **Author**: Kazotaruu_
- **LuxDialogues**: AselStudios
- **MythicMobs**: Lumine
- **Nexo**: NexoMC
- **FancyNPCs**: Oliver

## Links

- [LuxDialogues Wiki](https://wiki.aselstudios.com/luxdialogues/api)
- [Nexo Documentation](https://docs.nexomc.com/community-guides/api)
- [MythicMobs Documentation](https://git.lumine.io/mythiccraft/MythicMobs)
