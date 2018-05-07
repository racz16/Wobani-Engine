package wobani.core;

import wobani.toolbox.annotation.Nullable;
import wobani.toolbox.annotation.NotNull;
import java.util.logging.*;
import wobani.rendering.*;
import wobani.resources.*;
import wobani.toolbox.*;
import wobani.window.*;

/**
 * The engine's main game loop.
 */
public class GameLoop {

    /**
     * The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(GameLoop.class.getName());

    /**
     * To can't create GameLoop instance.
     */
    private GameLoop() {
    }

    /**
     * Initializes the engine including the windowing system, the input
     * handling, the rendering pipeline and much more. You should call it before
     * any OpenGL or OpenAL related code and before the GameLoop's run method.
     *
     * @param parameters parameters for the window
     */
    public static void initialize(@Nullable WindowParameters parameters) {
        try {
            initializeWithoutInspection(parameters);
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Initializes the engine including the windowing system, the input
     * handling, the rendering pipeline and much more. You should call it before
     * any OpenGL or OpenAL related code and before the GameLoop's run method.
     *
     * @param parameters parameters for the window
     */
    public static void initializeWithoutInspection(@Nullable WindowParameters parameters) {
        Utility.initializeLogging();
        initializeWindowAndInput(parameters);
        RenderingPipeline.initialize();
        initializeOpenGlOpenAl();
    }

    /**
     * Initializes the window and the input system.
     *
     * @param parameters parameters for the window
     */
    private static void initializeWindowAndInput(@Nullable WindowParameters parameters) {
        Window.initialize(parameters);
        Input.initialize();
    }

    /**
     * Initializes OpenGL and OpenAL.
     */
    private static void initializeOpenGlOpenAl() {
        OpenGl.initializeToDefaults();
        OpenAl.initialize();
    }

    /**
     * Handles the given exception. It logs the exception, releases the
     * resoruces and closes the program.
     *
     * @param ex Exception
     */
    private static void handleException(@NotNull Exception ex) {
        Utility.logException(ex);
        release();
        System.exit(1);
    }

    /**
     * Initializes the engine including the windowing system, the input
     * handling, the rendering pipeline and much more. You should call it before
     * any OpenGL or OpenAL related code and before the GameLoop's run method.
     */
    public static void initialize() {
        initialize(new WindowParameters());
    }

    /**
     * The engine's game loop. It updates all Components of the GameObjectContainer,
 updates the Resources, renders the scene, handles the input and swaps the
 buffers and handle exceptions. Before calling this method, you should
 initialize the engine. You can do it by by calling the initialize method.
     */
    public static void run() {
        try {
            gameLoop();
        } catch (Exception e) {
            Utility.logException(e);
        } finally {
            release();
        }
    }

    /**
     * The engine's game loop. It updates all Components of the GameObjectContainer,
 updates the resources, renders the scene, handles the input and swaps the
 buffers in every frame.
     */
    private static void gameLoop() {
        while (!Window.isWindowShouldClose()) {
            LOG.info("Frame started");
            update();
            RenderingPipeline.render();
            windowing();
        }
    }

    /**
     * Updates the Resources, the Components and update.
     */
    private static void update() {
        Time.update();
        LOG.info("Updating resources");
        ResourceManager.updateResources();
        LOG.info("Updating components");
        Scene.getGameObjects().updateComponents();
    }

    /**
     * Swaps the window's buffers and poll events.
     */
    private static void windowing() {
        Window.swapBuffers();
        Window.pollEvents();
    }

    /**
     * Releases all the Resources, the windowing system and the OpenAL. After
     * calling this method you can no longer use Meshes, Splines, Textures,
     * sounds, FBOs, the rendering pipeline or the window.
     */
    public static void release() {
        ResourceManager.releaseResources();
        Input.release();
        Window.release();
        OpenAl.release();
        LOG.info("All resources released");
    }

}
