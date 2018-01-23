package wobani.toolbox;

import java.nio.*;
import java.util.logging.*;
import org.joml.*;
import org.lwjgl.openal.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import wobani.toolbox.annotations.*;

/**
 * Set of commonly used OpenAL functions.
 */
public class OpenAl {

    /**
     * Default audio device.
     */
    private static long defaultAudioDevice;
    /**
     * OpenAL context.
     */
    private static long openAlContext;
    /**
     * The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(OpenAl.class.getName());

    /**
     * Sounds' attenuation models.
     */
    public enum AudioDistanceModel {
        /**
         * Inverse distance model.
         */
        INVERSE_DISTANCE(AL10.AL_INVERSE_DISTANCE),
        /**
         * Inverse distance clamped model.
         */
        INVERSE_DISTANCE_CLAMPED(AL10.AL_INVERSE_DISTANCE_CLAMPED),
        /**
         * Exponent distance model.
         */
        EXPONENT_DISTANCE(AL11.AL_EXPONENT_DISTANCE),
        /**
         * Exponent distance clamped model.
         */
        EXPONENT_DISTANCE_CLAMPED(AL11.AL_EXPONENT_DISTANCE_CLAMPED),
        /**
         * Linear distance model.
         */
        LINEAR_DISTANCE(AL11.AL_LINEAR_DISTANCE),
        /**
         * Linear distance clamped model.
         */
        LINEAR_DISTANCE_CLAMPED(AL11.AL_LINEAR_DISTANCE_CLAMPED);

        /**
         * The distance model's OpenAL code.
         */
        private final int code;

        /**
         * Initializes a new AudioDistanceModel to the given value.
         *
         * @param code the distance model's OpenAL code
         */
        private AudioDistanceModel(int code) {
            this.code = code;
        }

        /**
         * Returns the distance model's OpenAL code.
         *
         * @return the distance model's OpenAL code
         */
        public int getCode() {
            return code;
        }
    }

    /**
     * To can't create OpenAl instance.
     */
    private OpenAl() {
    }

    /**
     * Initializes the OpenAL context and the default audio device.
     */
    public static void initialize() {
        initializeDefaultAudioDevice();
        initializeOpenAlContext();
        ALCCapabilities deviceCaps = ALC.createCapabilities(defaultAudioDevice);
        ALC10.alcMakeContextCurrent(openAlContext);
        AL.createCapabilities(deviceCaps);
        LOG.info("OpenAL initialized");
    }

    /**
     * Initializes the default audio device.
     *
     * @throws IllegalStateException failed to open the default device
     */
    private static void initializeDefaultAudioDevice() {
        defaultAudioDevice = ALC10.alcOpenDevice((ByteBuffer) null);
        if (defaultAudioDevice == NULL) {
            throw new IllegalStateException("Failed to open the default device");
        }
    }

    /**
     * Initializes the OpenAL context.
     *
     * @throws IllegalStateException failed to create OpenAL context
     */
    private static void initializeOpenAlContext() {
        openAlContext = ALC10.alcCreateContext(defaultAudioDevice, (IntBuffer) null);
        if (openAlContext == NULL) {
            throw new IllegalStateException("Failed to create OpenAL context");
        }
    }

    /**
     * Sets the sounds' attenuation model to the given value.
     *
     * @param distanceModel sounds' attenuation model
     */
    public static void setDistanceModel(@NotNull AudioDistanceModel distanceModel) {
        AL10.alDistanceModel(distanceModel.getCode());
    }

    /**
     * Sets the audio listener's position to the given value.
     *
     * @param position audio listener's position
     */
    public static void setAudioListenerPosition(@NotNull Vector3f position) {
        AL10.alListener3f(AL10.AL_POSITION, position.x, position.y, position.z);
    }

    /**
     * Sets the audio listener's velocity to the given value.
     *
     * @param velocity audio listener's velocity
     */
    public static void setAudioListenerVelocity(@NotNull Vector3f velocity) {
        AL10.alListener3f(AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
    }

    /**
     * Sets the audio listener's orientation to the given values.
     *
     * @param forward audio listener's forward direction
     * @param up      audio listener's up direction
     */
    public static void setAudioListenerOrientation(@NotNull Vector3f forward, @NotNull Vector3f up) {
        float[] orientation = {forward.x, forward.y, forward.z, up.x, up.y, up.z};
        AL10.alListenerfv(AL10.AL_ORIENTATION, orientation);
    }

    /**
     * Releases the OpenAL context and the used audio device. After calling this
     * method, you can't use any audio related actions.
     */
    public static void release() {
        ALC10.alcMakeContextCurrent(NULL);
        ALC10.alcDestroyContext(openAlContext);
        ALC10.alcCloseDevice(defaultAudioDevice);
    }
}
