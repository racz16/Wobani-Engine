package core;

import org.joml.*;
import renderers.*;
import resources.*;
import toolbox.*;
import window.*;

/**
 * The engine's main game loop.
 */
public class GameLoop {

    /**
     * To can't create GameLoop instance.
     */
    private GameLoop() {
    }

    /**
     * The number of elapsed frames.
     */
    private static int frameCount;

    /**
     * Initializes the engine including the windowing system, the input
     * handling, the rendering pipeline and much more. You should call it before
     * any OpenGL related code and before the GameLoop's run method.
     *
     * @param parameters parameters for the window
     */
    public static void initialize(WindowParameters parameters) {
        try {
            Window.initialize(parameters);
            Input.initialize();
            RenderingPipeline.initialize();
            OpenGl.initializeToDefaults();
            Scene.setEnvironmentColor(new Vector3f(0, 1, 0));
            OpenAl.initialize();
        } catch (Exception e) {
            Utility.logException(e);
            ResourceManager.releaseResources();
            OpenAl.release();
            System.exit(1);
        }
    }

    /**
     * Initializes the engine including the windowing system, the input
     * handling, the rendering pipeline and much more. You should call it before
     * any OpenGL related code and before the GameLoop's run method.
     */
    public static void initialize() {
        initialize(new WindowParameters());
    }

    /**
     * The engine's game loop. It updates all Components of the GameObjects,
     * updates the resources ,renders the scene, handles the input and swaps the
     * buffers in every frame.
     */
    public static void run() {
        try {
            while (!Window.isWindowShouldClose()) {
                Time.timing();
                ResourceManager.updateResources();
                Scene.updateComponents();
                RenderingPipeline.render();
                Window.swapBuffers();
                Window.pollEvents();
                frameCount++;
            }
        } catch (Exception e) {
            Utility.logException(e);
        } finally {
            ResourceManager.releaseResources();
            OpenAl.release();
        }
    }

    /**
     * Returns the number of elapsed frames.
     *
     * @return the number of elapsed frames
     */
    public static int getFrameCount() {
        return frameCount;
    }

}
