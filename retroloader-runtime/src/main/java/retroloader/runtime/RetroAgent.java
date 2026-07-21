package retroloader.runtime;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java agent entry point for RetroLoader.
 *
 * Loaded via -javaagent:RetroLoader.jar. Intercepts class loading
 * and applies registered transformers (mixin, mappings, etc).
 */
public final class RetroAgent {

    private static final Logger LOGGER = Logger.getLogger("RetroLoader");

    private static final List<ClassFileTransformer> transformers = new ArrayList<>();
    private static Instrumentation instrumentation;

    private RetroAgent() {
    }

    /**
     * Called by the JVM when the agent is loaded via -javaagent.
     *
     * @param agentArgs arguments passed after = on the -javaagent line
     * @param inst      the Instrumentation instance provided by the JVM
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        LOGGER.info("RetroLoader agent starting...");

        instrumentation = inst;

        inst.addTransformer(new RetroClassTransformer(), true);

        LOGGER.info("RetroLoader agent initialized. " + inst.getAllLoadedClasses().length + " classes already loaded.");
    }

    /**
     * Called when the agent is loaded dynamically at runtime (not via -javaagent).
     *
     * @param agentArgs arguments passed after = on the -javaagent line
     * @param inst      the Instrumentation instance provided by the JVM
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        LOGGER.info("RetroLoader agent loaded dynamically...");
        premain(agentArgs, inst);
    }

    /**
     * Returns the Instrumentation instance if the agent has been initialized.
     *
     * @return the Instrumentation, or null if not yet initialized
     */
    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    /**
     * Registers a class file transformer to participate in class loading.
     *
     * @param transformer the transformer to register
     */
    public static void registerTransformer(ClassFileTransformer transformer) {
        transformers.add(transformer);
        if (instrumentation != null) {
            instrumentation.addTransformer(transformer, true);
        }
    }

    /**
     * Returns the list of registered transformers.
     *
     * @return unmodifiable copy of the transformer list
     */
    static List<ClassFileTransformer> getTransformers() {
        return List.copyOf(transformers);
    }

    /**
     * Inner transformer that delegates to all registered transformers.
     * This is the single transformer registered with the JVM — it dispatches
     * to mod-registered transformers (mixin, mappings, etc).
     */
    private static final class RetroClassTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException {

            if (className == null || classfileBuffer == null) {
                return null;
            }

            byte[] result = classfileBuffer;

            for (ClassFileTransformer transformer : getTransformers()) {
                try {
                    byte[] transformed = transformer.transform(loader, className, classBeingRedefined,
                            protectionDomain, result);
                    if (transformed != null) {
                        result = transformed;
                    }
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE, "Transformer failed for class " + className, t);
                }
            }

            return result == classfileBuffer ? null : result;
        }
    }
}
