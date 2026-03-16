# Required API JARs

This directory contains stub/placeholder JAR files that allow the project to compile. For production use, you should replace these with the actual API JAR files.

## About the Stub Files

The JAR files currently in this directory are minimal stub files created to allow compilation in CI/CD environments. They contain no actual code and should be replaced with the real API JARs before deployment to a production Minecraft server.

## Required Files

This directory must contain the following API JAR files for the project to build successfully:

## Required Files

1. **nexo-api.jar** (v0.1.0 or compatible)
   - Nexo custom blocks and furniture API
   - Source: [Nexo Documentation](https://docs.nexomc.com/)

2. **luxdialogues-api.jar** (v1.0.0 or compatible)
   - LuxDialogues API for dialogue management
   - Source: [LuxDialogues Wiki](https://wiki.aselstudios.com/luxdialogues/api)

3. **fancynpcs-api.jar** (v2.2.2 or compatible)
   - FancyNPCs API for NPC interactions
   - Source: [FancyNPCs Repository](https://github.com/FancyMcPlugins/FancyNpcs)

## How to Obtain the Files

These JAR files are typically obtained from:
- The respective plugin's releases/downloads page
- Your Minecraft server's `plugins/` directory (if you have these plugins installed)
- The plugin developers' official distribution channels

## Build Instructions

1. Download each of the required API JAR files
2. Place them in this `libs/` directory
3. Ensure the filenames match exactly as listed above
4. Run `mvn clean package` to build the project

## For CI/CD or Automated Builds

If you're setting up CI/CD pipelines or automated builds, you have several options:

### Option 1: Use Stub JARs (Compile-Only)
Create empty/stub JAR files for compilation purposes:
```bash
cd libs
jar cf nexo-api.jar -C /tmp .
jar cf luxdialogues-api.jar -C /tmp .
jar cf fancynpcs-api.jar -C /tmp .
```

These stub JARs will allow the project to compile, but the actual API JARs must be present on the Minecraft server at runtime.

### Option 2: Store in Private Repository
Upload the API JARs to a private Maven repository or artifact storage, and update the `pom.xml` to reference them.

### Option 3: Maven Local Repository
Install the JARs into your local Maven repository:
```bash
mvn install:install-file -Dfile=nexo-api.jar -DgroupId=com.nexomc -DartifactId=nexo-api -Dversion=0.1.0 -Dpackaging=jar
mvn install:install-file -Dfile=luxdialogues-api.jar -DgroupId=com.aselstudios -DartifactId=luxdialogues -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=fancynpcs-api.jar -DgroupId=de.oliver -DartifactId=FancyNpcs -Dversion=2.2.2 -Dpackaging=jar
```

## Note

This directory is excluded from version control (`.gitignore`) because:
- The API JARs are third-party dependencies
- They may have different licenses
- File size considerations
- Users should obtain them from official sources
