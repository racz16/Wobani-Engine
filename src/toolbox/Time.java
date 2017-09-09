package toolbox;

import org.lwjgl.glfw.*;

/**
 * By calling the timing method every frame, it computes the delta time factor
 * and the frame per sec value.
 *
 * @see #timing()
 * @see #getDeltaTimeFactor()
 * @see #getFps()
 */
public class Time {

    /**
     * Target FPS.
     */
    private static final int TARGET_FPS = 60;
    /**
     * One second divided into target FPS parts.
     */
    private static final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
    /**
     * Current sec's fps value.
     */
    private static int currentFps;
    /**
     * Last sec's fps value.
     */
    private static int lastFps;
    /**
     * Time elapsed since last frame.
     */
    private static long updateLength;
    /**
     * Sum of the update lengths until it reaches one sec.
     */
    private static long updateLengthSum;
    /**
     * The time when the last frame rendered.
     */
    private static long lastLoopTime = System.nanoTime();
    /**
     * Difference from the optimal FPS. If you multiply something (like movement
     * or rotation) with this value every frame, it'll be FPS independent.
     */
    private static float deltaTimeFactor;

    /**
     * To can't create Time instance.
     */
    private Time() {
    }

    /**
     * It computes the delta time factor and updates the FPS value.
     */
    public static void timing() {
        long now = System.nanoTime();
        updateLength = now - lastLoopTime;
        lastLoopTime = now;
        deltaTimeFactor = updateLength / ((float) OPTIMAL_TIME);

        updateLengthSum += updateLength;
        currentFps++;

        if (updateLengthSum >= 1000000000) {
            updateLengthSum = 0;
            lastFps = currentFps;
            currentFps = 0;
        }
    }

    /**
     * Returns the delta time factor. If you multiply something (like movement
     * or rotation) with this value every frame, it'll be FPS independent.
     *
     * @return delta time factor
     */
    public static float getDeltaTimeFactor() {
        return deltaTimeFactor;
    }

    /**
     * Returns the frame per sec value.
     *
     * @return fps
     */
    public static int getFps() {
        return lastFps;
    }

    /**
     * Retruns the elapsed time since the start.
     *
     * @return the elapsed time since the start (in seconds)
     */
    public static double getTime() {
        return GLFW.glfwGetTime();
    }

}
