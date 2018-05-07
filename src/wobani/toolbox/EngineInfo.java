package wobani.toolbox;

import wobani.toolbox.annotation.NotNull;

/**
 * Gives information about the engine.
 */
public class EngineInfo {

    /**
     * The engine's name.
     */
    private static final String NAME = "Wobani Engine";
    /**
     * The engine's major version.
     */
    private static final int MAJOR_VERSION = 0;
    /**
     * The engine's minor version.
     */
    private static final int MINOR_VERSION = 1;

    /**
     * To can't initialize a new EngineInfo.
     */
    private EngineInfo() {
    }

    /**
     * Returns the engine's name.
     *
     * @return the engine's name
     */
    @NotNull
    public static String getName() {
        return NAME;
    }

    /**
     * Returns the engine's major version number.
     *
     * @return the engine's major version number
     */
    public static int getMajorVersion() {
        return MAJOR_VERSION;
    }

    /**
     * Returns the engine's minor version number.
     *
     * @return the engine's minor version number
     */
    public static int getMinorVersion() {
        return MINOR_VERSION;
    }

    /**
     * Returns the engine's full name.
     *
     * @return the engine's full name
     */
    @NotNull
    public static String getFullEngineName() {
        return getName() + " v" + getMajorVersion() + "." + getMinorVersion();
    }

}
