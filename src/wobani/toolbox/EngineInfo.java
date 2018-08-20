package wobani.toolbox;

import wobani.toolbox.annotation.*;

/**
 Gives information about the engine.
 */
public class EngineInfo{

    /**
     The engine's name.
     */
    private static final String NAME = "Wobani Engine";
    /**
     The engine's major version.
     */
    private static final int MAJOR_VERSION = 0;
    /**
     The engine's minor version.
     */
    private static final int MINOR_VERSION = 1;
    /**
     Determines whether the engine is in debug mode.
     */
    private static final boolean DEBUG = true;

    /**
     To can't initialize a new EngineInfo.
     */
    private EngineInfo(){
    }

    /**
     Returns the engine's name.

     @return the engine's name
     */
    @NotNull
    public static String getName(){
        return NAME;
    }

    /**
     Returns the engine's major version number.

     @return the engine's major version number
     */
    public static int getMajorVersion(){
        return MAJOR_VERSION;
    }

    /**
     Returns the engine's minor version number.

     @return the engine's minor version number
     */
    public static int getMinorVersion(){
        return MINOR_VERSION;
    }

    /**
     Returns the engine's full name.

     @return the engine's full name
     */
    @NotNull
    public static String getFullEngineName(){
        return getName() + " v" + getMajorVersion() + "." + getMinorVersion();
    }

    /**
     Determines whether the engine is in debug mode.

     @return true if the engine is in debug mode, false otherwise
     */
    public static boolean isDebugMode(){
        return DEBUG;
    }

    /**
     Contains all the used libraries.
     */
    public enum Library{
        /**
         The OpenGL library.
         */
        OPENGL,
        /**
         The OpenAL library.
         */
        OPENAL,
        /**
         The GLFW library.
         */
        GLFW,
        /**
         The STB library.
         */
        STB,
        /**
         The Assimp library.
         */
        ASSIMP,
        /**
         The JOML library.
         */
        JOML
    }

}
