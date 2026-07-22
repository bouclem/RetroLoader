package retroloader.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parsed mod metadata from a {@code retroloader.mod.toml} file.
 *
 * @param id          unique mod identifier (lowercase, underscores)
 * @param version     mod version string
 * @param name        human-readable mod name
 * @param description short description
 * @param author      author name
 * @param dependencies version requirements keyed by dependency id
 *                    (e.g., "retroloader" -> ">=0.1.0", "minecraft" -> "c0.0.13a_03")
 * @param entrypoints entrypoint classes keyed by type (e.g., "main" -> "com.example.ExampleMod")
 * @param patches     list of patch definition files referenced by this mod
 */
public record ModMetadata(
        String id,
        String version,
        String name,
        String description,
        String author,
        Map<String, String> dependencies,
        Map<String, String> entrypoints,
        List<String> patches
) {

    public ModMetadata {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Mod metadata 'id' must not be empty");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("Mod metadata 'version' must not be empty");
        }
        dependencies = Collections.unmodifiableMap(new LinkedHashMap<>(dependencies));
        entrypoints = Collections.unmodifiableMap(new LinkedHashMap<>(entrypoints));
        patches = List.copyOf(patches);
    }

    /**
     * Returns the Minecraft version dependency, or empty if not specified.
     *
     * @return the required Minecraft version string, or empty
     */
    public String getMinecraftVersion() {
        return dependencies.getOrDefault("minecraft", "");
    }

    /**
     * Returns the RetroLoader version dependency, or empty if not specified.
     *
     * @return the required RetroLoader version string, or empty
     */
    public String getRetroLoaderVersion() {
        return dependencies.getOrDefault("retroloader", "");
    }

    /**
     * Returns the main entrypoint class name, or empty if not specified.
     *
     * @return the main entrypoint class FQN, or empty
     */
    public String getMainEntrypoint() {
        return entrypoints.getOrDefault("main", "");
    }
}
