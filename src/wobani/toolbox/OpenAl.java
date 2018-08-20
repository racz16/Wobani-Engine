package wobani.toolbox;

import org.joml.*;
import org.lwjgl.openal.*;
import wobani.toolbox.annotation.*;

import java.nio.*;
import java.util.logging.*;

import static org.lwjgl.system.MemoryUtil.*;

/**
 Set of commonly used OpenAL functions.
 */
public class OpenAl{

    /**
     The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(OpenAl.class.getName());
    /**
     Default audio device.
     */
    private static long defaultAudioDevice;
    /**
     OpenAL context.
     */
    private static long openAlContext;

    /**
     To can't create OpenAl instance.
     */
    private OpenAl(){
    }

    /**
     Initializes the OpenAL context and the default audio device.
     */
    public static void initialize(){
        initializeDefaultAudioDevice();
        initializeOpenAlContext();
        ALCCapabilities deviceCaps = ALC.createCapabilities(defaultAudioDevice);
        ALC10.alcMakeContextCurrent(openAlContext);
        AL.createCapabilities(deviceCaps);
        LOG.info("OpenAL initialized");
    }

    /**
     Initializes the default audio device.

     @throws IllegalStateException failed to open the default device
     */
    private static void initializeDefaultAudioDevice(){
        defaultAudioDevice = ALC10.alcOpenDevice((ByteBuffer) null);
        if(defaultAudioDevice == NULL){
            throw new IllegalStateException("Failed to open the default device");
        }
    }

    /**
     Initializes the OpenAL context.

     @throws IllegalStateException failed to create OpenAL context
     */
    private static void initializeOpenAlContext(){
        openAlContext = ALC10.alcCreateContext(defaultAudioDevice, (IntBuffer) null);
        if(openAlContext == NULL){
            throw new IllegalStateException("Failed to create OpenAL context");
        }
    }

    /**
     Returns the sounds' attenuation model.

     @return the sounds' attenuation model
     */
    @NotNull
    public static AudioDistanceModel getDistanceModel(){
        int code = AL10.alGetInteger(AL10.AL_DISTANCE_MODEL);
        for(AudioDistanceModel adm : AudioDistanceModel.values()){
            if(code == adm.getCode()){
                return adm;
            }
        }
        return null;
    }

    /**
     Sets the sounds' attenuation model to the given value.

     @param distanceModel sounds' attenuation model
     */
    public static void setDistanceModel(@NotNull AudioDistanceModel distanceModel){
        AL10.alDistanceModel(distanceModel.getCode());
    }

    /**
     Returns the audio listener's position.

     @return the audio listener's position
     */
    @NotNull
    @ReadOnly
    public static Vector3f getAudioListenerPosition(){
        float result[] = new float[3];
        AL10.alGetFloatv(AL10.AL_POSITION, result);
        return new Vector3f(result[0], result[1], result[2]);
    }

    /**
     Sets the audio listener's position to the given value.

     @param position audio listener's position
     */
    public static void setAudioListenerPosition(@NotNull Vector3f position){
        AL10.alListener3f(AL10.AL_POSITION, position.x, position.y, position.z);
    }

    /**
     Returns the audio listener's velocity.

     @return the audio listener's velocity
     */
    @NotNull
    @ReadOnly
    public static Vector3f getAudioListenerVelocity(){
        float result[] = new float[3];
        AL10.alGetFloatv(AL10.AL_VELOCITY, result);
        return new Vector3f(result[0], result[1], result[2]);
    }

    /**
     Sets the audio listener's velocity to the given value.

     @param velocity audio listener's velocity
     */
    public static void setAudioListenerVelocity(@NotNull Vector3f velocity){
        AL10.alListener3f(AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
    }

    /**
     Returns the audio listener's forward direction.

     @return the audio listener's forward direction
     */
    @NotNull
    @ReadOnly
    public static Vector3f getAudioListenerForward(){
        float result[] = new float[6];
        AL10.alGetFloatv(AL10.AL_ORIENTATION, result);
        return new Vector3f(result[0], result[1], result[2]);
    }

    /**
     Returns the audio listener's up direction.

     @return the audio listener's up direction
     */
    @NotNull
    @ReadOnly
    public static Vector3f getAudioListenerUp(){
        float result[] = new float[6];
        AL10.alGetFloatv(AL10.AL_ORIENTATION, result);
        return new Vector3f(result[3], result[4], result[5]);
    }

    /**
     Sets the audio listener's orientation to the given values.

     @param forward audio listener's forward direction
     @param up      audio listener's up direction
     */
    public static void setAudioListenerOrientation(@NotNull Vector3f forward, @NotNull Vector3f up){
        float[] orientation = {forward.x, forward.y, forward.z, up.x, up.y, up.z};
        AL10.alListenerfv(AL10.AL_ORIENTATION, orientation);
    }

    /**
     Returns the vendor.

     @return the vendor
     */
    @NotNull
    public static String getVendor(){
        return AL10.alGetString(AL10.AL_VENDOR);
    }

    /**
     Returns the version.

     @return the version
     */
    @NotNull
    public static String getVersion(){
        return AL10.alGetString(AL10.AL_VERSION);
    }

    /**
     Releases the OpenAL context and the used audio device. After calling this method, you can't use any audio related
     actions.
     */
    public static void release(){
        ALC10.alcMakeContextCurrent(NULL);
        ALC10.alcDestroyContext(openAlContext);
        ALC10.alcCloseDevice(defaultAudioDevice);
        LOG.info("OpenAL released");
    }

    /**
     Sounds' attenuation models.
     */
    public enum AudioDistanceModel{
        /**
         Inverse distance model.
         */
        INVERSE_DISTANCE(AL10.AL_INVERSE_DISTANCE),
        /**
         Inverse distance clamped model.
         */
        INVERSE_DISTANCE_CLAMPED(AL10.AL_INVERSE_DISTANCE_CLAMPED),
        /**
         Exponent distance model.
         */
        EXPONENT_DISTANCE(AL11.AL_EXPONENT_DISTANCE),
        /**
         Exponent distance clamped model.
         */
        EXPONENT_DISTANCE_CLAMPED(AL11.AL_EXPONENT_DISTANCE_CLAMPED),
        /**
         Linear distance model.
         */
        LINEAR_DISTANCE(AL11.AL_LINEAR_DISTANCE),
        /**
         Linear distance clamped model.
         */
        LINEAR_DISTANCE_CLAMPED(AL11.AL_LINEAR_DISTANCE_CLAMPED);

        /**
         The distance model's OpenAL code.
         */
        private final int code;

        /**
         Initializes a new AudioDistanceModel to the given value.

         @param code the distance model's OpenAL code
         */
        AudioDistanceModel(int code){
            this.code = code;
        }

        /**
         Returns the distance model's OpenAL code.

         @return the distance model's OpenAL code
         */
        public int getCode(){
            return code;
        }
    }
}
