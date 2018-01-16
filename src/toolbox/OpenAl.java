package toolbox;

import java.nio.*;
import org.joml.*;
import org.lwjgl.openal.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import toolbox.annotations.*;

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
     * Initializes the OpenAL context and th edefault audio device.
     *
     * @throws IllegalStateException failed to open the default device
     */
    public static void initialize() {
        defaultAudioDevice = ALC10.alcOpenDevice((ByteBuffer) null);
        if (defaultAudioDevice == NULL) {
            throw new IllegalStateException("Failed to open the default device");
        }
        openAlContext = ALC10.alcCreateContext(defaultAudioDevice, (IntBuffer) null);
        if (openAlContext == NULL) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }
        ALCCapabilities deviceCaps = ALC.createCapabilities(defaultAudioDevice);
        ALC10.alcMakeContextCurrent(openAlContext);
        AL.createCapabilities(deviceCaps);
    }

    /**
     * Sets the sounds' attenuation model to the given value.
     *
     * @param distanceModel sounds' attenuation model
     */
    public static void setDistanceModel(@NotNull AudioDistanceModel distanceModel) {
        AL10.alDistanceModel(distanceModel.getCode());
    }

    public static void updateAudioListenerPosition(@NotNull Vector3f position) {
        AL10.alListener3f(AL10.AL_POSITION, position.x, position.y, position.z);
    }

    public static void updateAudioListenerVelocity(@NotNull Vector3f velocity) {
        AL10.alListener3f(AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
    }

    public static void updateAudioListenerOrientation(@NotNull Vector3f forward, @NotNull Vector3f up) {
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
