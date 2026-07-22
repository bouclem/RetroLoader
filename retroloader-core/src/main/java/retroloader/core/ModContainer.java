package retroloader.core;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Holds a discovered mod's metadata, jar file path, and isolated classloader.
 *
 * @param metadata     parsed mod metadata
 * @param jarPath      path to the mod jar file on disk
 * @param classLoader  isolated classloader for this mod's classes
 */
public final class ModContainer {

    private final ModMetadata metadata;
    private final Path jarPath;
    private final URLClassLoader classLoader;

    public ModContainer(ModMetadata metadata, Path jarPath, ClassLoader parentClassLoader) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.jarPath = Objects.requireNonNull(jarPath, "jarPath");
        try {
            URL[] urls = {jarPath.toUri().toURL()};
            this.classLoader = new URLClassLoader(urls, parentClassLoader);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create classloader for mod '" + metadata.id() + "' from " + jarPath, e);
        }
    }

    public ModMetadata metadata() {
        return metadata;
    }

    public Path jarPath() {
        return jarPath;
    }

    public URLClassLoader classLoader() {
        return classLoader;
    }

    /**
     * Loads a class from this mod's classloader.
     *
     * @param className the fully qualified class name
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be found
     */
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return classLoader.loadClass(className);
    }

    @Override
    public String toString() {
        return "ModContainer[" + metadata.id() + " v" + metadata.version() + " @ " + jarPath + "]";
    }
}
