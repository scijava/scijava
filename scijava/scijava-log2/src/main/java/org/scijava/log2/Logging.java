package org.scijava.log2;

/**
 * A static utility class serving as the access point for logging to the {@link Logger} agreed to by the framework.
 * This class maintains a shared
 *
 * @author Gabriel Selzer
 */
public final class Logging {

    /**
     * Stores the {@link Logger} used on each {@link Thread}.
     */
    private static final ThreadLocal<Logger> localLogger =
            new InheritableThreadLocal<>()
            {

                @Override
                protected Logger childValue( Logger parentValue) { return parentValue; }

                @Override
                protected Logger initialValue() {
                    return new StderrLoggerFactory().create();
                }
            };


    public static Logger getLogger() {
        return localLogger.get();
    }

    public static void setLogger(final Logger logger) {
        localLogger.set(logger);
    }

}
