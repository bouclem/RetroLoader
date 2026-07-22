package retroloader.core;

import org.jetbrains.java.decompiler.api.Decompiler;
import org.jetbrains.java.decompiler.main.decompiler.SingleFileSaver;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Decompiles downloaded Minecraft client jars using Vineflower.
 *
 * The decompiled source is used for:
 * <ul>
 *   <li>Mod development — mod developers need readable Minecraft source to write mods</li>
 *   <li>Runtime — the game runs with the downloaded client jar</li>
 * </ul>
 *
 * Decompiled source is cached in the given output directory.
 */
public final class VersionDecompiler {

    private static final Logger LOGGER = Logger.getLogger("RetroLoader");

    private final Path outputDir;

    public VersionDecompiler(Path outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Decompiles a Minecraft client jar to source files.
     *
     * @param jarPath   path to the downloaded client jar
     * @param versionId the Minecraft version identifier (used for output naming)
     * @return path to the directory containing decompiled sources
     * @throws IOException if decompilation fails or output cannot be written
     */
    public Path decompile(Path jarPath, String versionId) throws IOException {
        Path versionOutputDir = outputDir.resolve(versionId + "-sources");

        if (Files.exists(versionOutputDir) && Files.isDirectory(versionOutputDir)) {
            try (var entries = Files.list(versionOutputDir)) {
                if (entries.findAny().isPresent()) {
                    LOGGER.info("Decompiled sources for " + versionId + " already exist at " + versionOutputDir);
                    return versionOutputDir;
                }
            }
        }

        Files.createDirectories(versionOutputDir);

        Path outputJar = outputDir.resolve(versionId + "-decompiled.jar");

        LOGGER.info("Decompiling " + versionId + " with Vineflower...");
        long startTime = System.currentTimeMillis();

        try {
            Decompiler decompiler = Decompiler.builder()
                    .inputs(jarPath.toFile())
                    .output(new SingleFileSaver(outputJar.toFile()))
                    .option(IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "true")
                    .build();

            decompiler.decompile();
        } catch (Exception e) {
            throw new IOException("Vineflower decompilation failed for " + versionId, e);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        LOGGER.info("Decompilation of " + versionId + " completed in " + elapsed + "ms");

        return versionOutputDir;
    }
}
