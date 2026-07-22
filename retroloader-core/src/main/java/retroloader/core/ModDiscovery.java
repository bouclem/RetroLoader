package retroloader.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Scans a {@code mods/} directory for mod jar files and parses their metadata.
 *
 * Each jar must contain a {@code retroloader.mod.toml} file to be recognized as a mod.
 * Jars without metadata are skipped with a warning.
 */
public final class ModDiscovery {

    private static final Logger LOGGER = Logger.getLogger("RetroLoader");
    private static final String JAR_EXTENSION = ".jar";

    private ModDiscovery() {
    }

    /**
     * Discovers all mods in the given mods directory.
     *
     * @param modsDir          path to the mods directory
     * @param parentClassLoader classloader to use as parent for mod classloaders
     * @return list of discovered mod containers (empty if none found)
     * @throws IOException if the directory cannot be read
     */
    public static List<ModContainer> discover(Path modsDir, ClassLoader parentClassLoader) throws IOException {
        List<ModContainer> mods = new ArrayList<>();

        if (!Files.isDirectory(modsDir)) {
            LOGGER.info("Mods directory does not exist: " + modsDir + " — no mods loaded.");
            return mods;
        }

        try (Stream<Path> entries = Files.list(modsDir)) {
            entries.filter(Files::isRegularFile)
                   .filter(p -> p.toString().endsWith(JAR_EXTENSION))
                   .sorted()
                   .forEach(jar -> {
                       try {
                           ModMetadata metadata = ModMetadataParser.parse(jar);
                           if (metadata == null) {
                               LOGGER.warning("Skipping " + jar.getFileName() + " — no retroloader.mod.toml found.");
                               return;
                           }
                           ModContainer container = new ModContainer(metadata, jar, parentClassLoader);
                           mods.add(container);
                           LOGGER.info("Discovered mod: " + metadata.id() + " v" + metadata.version()
                                   + " (" + jar.getFileName() + ")");
                       } catch (IOException e) {
                           LOGGER.log(Level.SEVERE, "Failed to parse metadata from " + jar, e);
                       }
                   });
        }

        LOGGER.info("Mod discovery complete: " + mods.size() + " mod(s) found in " + modsDir);
        return mods;
    }

    /**
     * Discovers all mods in the given mods directory using the system classloader as parent.
     *
     * @param modsDir path to the mods directory
     * @return list of discovered mod containers
     * @throws IOException if the directory cannot be read
     */
    public static List<ModContainer> discover(Path modsDir) throws IOException {
        return discover(modsDir, ClassLoader.getSystemClassLoader());
    }
}
