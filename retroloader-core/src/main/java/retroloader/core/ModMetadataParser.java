package retroloader.core;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Parses {@code retroloader.mod.toml} from inside a mod jar file.
 *
 * Expected TOML structure:
 * <pre>{@code
 * id = "example_mod"
 * version = "1.0.0"
 * name = "Example Mod"
 * description = "An example mod"
 * author = "bouclem"
 *
 * [dependencies]
 * retroloader = ">=0.1.0"
 * minecraft = "c0.0.13a_03"
 *
 * [entrypoints]
 * main = "com.example.ExampleMod"
 *
 * [[patches]]
 * file = "patches.example_mod.json"
 * }</pre>
 */
public final class ModMetadataParser {

    private static final Logger LOGGER = Logger.getLogger("RetroLoader");
    private static final String METADATA_FILE = "retroloader.mod.toml";

    private ModMetadataParser() {
    }

    /**
     * Parses mod metadata from a jar file.
     *
     * @param jarPath path to the mod jar
     * @return parsed metadata, or null if the jar has no metadata file
     * @throws IOException if the jar cannot be read
     */
    public static ModMetadata parse(Path jarPath) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(jarPath, (ClassLoader) null)) {
            Path metaPath = fs.getPath(METADATA_FILE);
            if (!Files.exists(metaPath)) {
                LOGGER.fine("No " + METADATA_FILE + " found in " + jarPath);
                return null;
            }

            try (InputStream in = Files.newInputStream(metaPath)) {
                TomlParser parser = new TomlParser();
                Config config = parser.parse(in);
                return fromConfig(config);
            }
        }
    }

    /**
     * Converts a parsed TOML config into a {@link ModMetadata} record.
     *
     * @param config the parsed TOML config
     * @return the mod metadata
     */
    private static ModMetadata fromConfig(Config config) {
        String id = config.get("id");
        String version = config.get("version");
        String name = config.getOrElse("name", id);
        String description = config.getOrElse("description", "");
        String author = config.getOrElse("author", "");

        Map<String, String> dependencies = new LinkedHashMap<>();
        Config depConfig = config.get("dependencies");
        if (depConfig != null) {
            for (String key : depConfig.valueMap().keySet()) {
                Object value = depConfig.get(key);
                if (value != null) {
                    dependencies.put(key, value.toString());
                }
            }
        }

        Map<String, String> entrypoints = new LinkedHashMap<>();
        Config entryConfig = config.get("entrypoints");
        if (entryConfig != null) {
            for (String key : entryConfig.valueMap().keySet()) {
                Object value = entryConfig.get(key);
                if (value != null) {
                    entrypoints.put(key, value.toString());
                }
            }
        }

        List<String> patches = new ArrayList<>();
        Object patchesObj = config.get("patches");
        if (patchesObj instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Config patchConfig) {
                    String file = patchConfig.get("file");
                    if (file != null) {
                        patches.add(file);
                    }
                }
            }
        }

        return new ModMetadata(id, version, name, description, author, dependencies, entrypoints, patches);
    }
}
