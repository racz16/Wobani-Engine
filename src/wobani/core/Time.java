package wobani.core;

import wobani.toolbox.annotation.*;

/**
 It computes the delta time factor and the frame per sec value and other useful values.

 @see #getDeltaTimeFactor()
 @see #getFps()
 @see #getFrameCount() */
public class Time{

    /**
     One second in milseconds.
     */
    private static final long ONE_SECOND = 1000000000;
    /**
     Target FPS.
     */
    private static final int TARGET_FPS = 60;
    /**
     One second divided into target FPS parts.
     */
    private static final long OPTIMAL_TIME = ONE_SECOND / TARGET_FPS;
    /**
     The time when the engine started it's work (in miliseconds).
     */
    private static final long START_TIME;
    /**
     Current sec's fps value.
     */
    private static int currentFps;
    /**
     Last sec's fps value.
     */
    private static int lastFps;
    /**
     Time elapsed since last frame (in miliseconds).
     */
    private static long lastFrameInterval;
    /**
     Sum of this second's frame lengths.
     */
    private static long frameIntervalSum;
    /**
     The time when the last frame rendered (in miliseconds).
     */
    private static long lastFrameTime = System.nanoTime();
    /**
     Difference from the optimal FPS. If you multiply something (like movement or rotation) with this value every frame,
     it'll be FPS independent.
     */
    private static float deltaTimeFactor;
    /**
     The number of elapsed frames.
     */
    private static int frameCount;

    static{
        START_TIME = System.nanoTime();
    }

    /**
     To can't create Time instance.
     */
    private Time(){
    }

    /**
     It updates the delta time factor and other useful values.
     */
    @Internal
    static void update(){
        refreshDeltaTimeFactor();
        refreshFps();
    }

    /**
     Refreshes the delta time factor.
     */
    private static void refreshDeltaTimeFactor(){
        long currentTime = System.nanoTime();
        lastFrameInterval = currentTime - lastFrameTime;
        frameIntervalSum += lastFrameInterval;
        deltaTimeFactor = lastFrameInterval / ((float) OPTIMAL_TIME);
        lastFrameTime = currentTime;
    }

    /**
     Refreshes the FPS value.
     */
    private static void refreshFps(){
        frameCount++;
        currentFps++;
        if(frameIntervalSum >= ONE_SECOND){
            frameIntervalSum = 0;
            lastFps = currentFps;
            currentFps = 0;
        }
    }

    /**
     Returns the delta time factor. If you multiply something (like movement or rotation) with this value every frame,
     it'll be FPS independent.

     @return delta time factor
     */
    public static float getDeltaTimeFactor(){
        return deltaTimeFactor;
    }

    /**
     Returns the frame per sec value.

     @return fps
     */
    public static int getFps(){
        return lastFps;
    }

    /**
     Retruns the elapsed time since the start.

     @return the elapsed time since the start (in seconds)
     */
    public static double getTime(){
        return ((double) System.nanoTime() - START_TIME) / ONE_SECOND;
    }

    /**
     Returns the number of elapsed frames.

     @return the number of elapsed frames
     */
    public static int getFrameCount(){
        return frameCount;
    }

}
