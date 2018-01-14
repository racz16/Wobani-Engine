package resources.audio;

import org.joml.*;
import org.lwjgl.openal.*;
import resources.*;
import toolbox.annotations.*;

/**
 * Audio sources can emit sound effect through the connected audio buffers.
 */
public class AudioSource implements Resource {

    /**
     * Audio source's native OpenAL id.
     */
    private int id = -1;
    /**
     * The connected audio buffer.
     */
    private AudioBuffer buffer;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     * The audio source's possible states.
     */
    public enum AudioSourceState {
        /**
         * Initial.
         */
        INITIAL(AL10.AL_INITIAL),
        /**
         * Playing.
         */
        PLAYING(AL10.AL_PLAYING),
        /**
         * Paused.
         */
        PAUSED(AL10.AL_PAUSED),
        /**
         * Stopped.
         */
        STOPPED(AL10.AL_STOPPED);

        /**
         * The state's native OpenAL code.
         */
        private final int code;

        /**
         * Initializes a new AudioSourceState to the given value.
         *
         * @param code state's native OpenAL code
         */
        private AudioSourceState(int code) {
            this.code = code;
        }

        /**
         * Returns the state's native OpenAL code.
         *
         * @return the state's native OpenAL code
         */
        public int getCode() {
            return code;
        }
    }

    /**
     * Initializes a new AudioSource to the given value.
     *
     * @param buffer audio buffer
     */
    public AudioSource(@NotNull AudioBuffer buffer) {
        id = AL10.alGenSources();
        setAudioBuffer(buffer);
        resourceId = new ResourceId();
        ResourceManager.addAudioSource(this);
    }

    /**
     * Returns the audio source's volume.
     *
     * @return the audio source's volume
     */
    public float getVolume() {
        return AL10.alGetSourcef(id, AL10.AL_GAIN);
    }

    /**
     * Sets the audio source's volume to the given value.
     *
     * @param volume audio source's new volume
     */
    public void setVolume(float volume) {
        AL10.alSourcef(id, AL10.AL_GAIN, volume);
    }

    /**
     * Returns the audio source's pitch.
     *
     * @return the audio source's pitch
     */
    public float getPitch() {
        return AL10.alGetSourcef(id, AL10.AL_PITCH);
    }

    /**
     * Sets the audio source's pitch to the given value.
     *
     * @param pitch audio source's new pitch
     */
    public void setPitch(float pitch) {
        AL10.alSourcef(id, AL10.AL_PITCH, pitch);
    }

    /**
     * Returns the audio source's rolloff factor. It affects the attenuation.
     *
     * @return the audio source's rolloff factor
     */
    public float getRolloff() {
        return AL10.alGetSourcef(id, AL10.AL_ROLLOFF_FACTOR);
    }

    /**
     * Sets the audio source's rolloff factor to the given value. It affects the
     * attenuation.
     *
     * @param rolloff audio source's new rolloff factor
     */
    public void setRolloff(float rolloff) {
        AL10.alSourcef(id, AL10.AL_ROLLOFF_FACTOR, rolloff);
    }

    /**
     * Returns the audio source's reference distance. It affects the
     * attenuation.
     *
     * @return the audio source's reference distance
     */
    public float getReferenceDistance() {
        return AL10.alGetSourcef(id, AL10.AL_REFERENCE_DISTANCE);
    }

    /**
     * Sets the audio source's reference distance to the given value. It affects
     * the attenuation.
     *
     * @param refDistance audio source's new reference distance
     */
    public void setReferenceDistance(float refDistance) {
        AL10.alSourcef(id, AL10.AL_REFERENCE_DISTANCE, refDistance);
    }

    /**
     * Returns the audio source's max distance. It affects the attenuation.
     *
     * @return the audio source's max distance
     */
    public float getMaxDistance() {
        return AL10.alGetSourcef(id, AL10.AL_MAX_DISTANCE);
    }

    /**
     * Sets the audio source's max distance to the given value. It affects the
     * attenuation.
     *
     * @param maxDistance audio source's new max distance
     */
    public void setMaxDistance(float maxDistance) {
        AL10.alSourcef(id, AL10.AL_MAX_DISTANCE, maxDistance);
    }

    /**
     * Connects the given audio buffer to this audio source.
     *
     * @param buffer audio buffer
     */
    public void setAudioBuffer(@NotNull AudioBuffer buffer) {
        this.buffer = buffer;
        stop();
        AL10.alSourcei(id, AL10.AL_BUFFER, buffer.getId());
    }

    /**
     * Plays the sounds effect.
     */
    public void play() {
        buffer.refreshStore();
        AL10.alSourcePlay(id);
        buffer.setLastActiveToNow();
    }

    /**
     * Pauses the sounds effect.
     */
    public void pause() {
        buffer.refreshStore();
        AL10.alSourcePause(id);
        buffer.setLastActiveToNow();
    }

    /**
     * Stops the sounds effect.
     */
    public void stop() {
        buffer.refreshStore();
        AL10.alSourceStop(id);
        buffer.setLastActiveToNow();
    }

    /**
     * Determines whether the audio source is a loop source.
     *
     * @return true if the audio source is loop source, false otherwise
     */
    public boolean isLoopAudio() {
        return AL10.alGetSourcei(id, AL10.AL_LOOPING) == AL10.AL_TRUE;
    }

    /**
     * Sets whether or not this audio source is a loop source.
     *
     * @param loop true if it should be a loop source, false otherwise
     */
    public void setLoopAuidio(boolean loop) {
        AL10.alSourcei(id, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    /**
     * Returns whether the audio source's position, velocity and direction is
     * relative to the audio listener.
     *
     * @return true if the audio source's position, velocity and direction is
     *         relative to the audio listener, false if they are absolute
     */
    public boolean isDataRelativeToTheListener() {
        return AL10.alGetSourcei(id, AL10.AL_SOURCE_RELATIVE) == AL10.AL_TRUE;
    }

    /**
     * Sets whether or not the audio source's position, velocity and direction
     * is relative to the audio listener.
     *
     * @param relative the audio source's position, velocity and direction
     *                 should be relative to the audio listener or not
     */
    public void setDataRelativeToTheListener(boolean relative) {
        AL10.alSourcei(id, AL10.AL_SOURCE_RELATIVE, relative ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    /**
     * Returns the audio source's status.
     *
     * @return the audio source's status
     */
    @NotNull
    public AudioSourceState getStatus() {
        int al_state = AL10.alGetSourcei(id, AL10.AL_SOURCE_STATE);
        for (AudioSourceState state : AudioSourceState.values()) {
            if (state.getCode() == al_state) {
                return state;
            }
        }
        return null;
    }

    /**
     * Returns the audio source's position.
     *
     * @return the audio source's position
     */
    @NotNull @ReadOnly
    public Vector3f getPosition() {
        float[] x = {0}, y = {0}, z = {0};
        AL10.alGetSource3f(id, AL10.AL_POSITION, x, y, z);
        return new Vector3f(x[0], y[0], z[0]);
    }

    /**
     * Sets the audio source's position to the given value.
     *
     * @param position audio source's new position
     */
    public void setPosition(@NotNull Vector3f position) {
        AL10.alSource3f(id, AL10.AL_POSITION, position.x, position.y, position.z);
    }

    /**
     * Returns the audio source's velocity.
     *
     * @return the audio source's velocity
     */
    @NotNull @ReadOnly
    public Vector3f getVelocity() {
        float[] x = {0}, y = {0}, z = {0};
        AL10.alGetSource3f(id, AL10.AL_VELOCITY, x, y, z);
        return new Vector3f(x[0], y[0], z[0]);
    }

    /**
     * Sets the audio source's velocity to the given value.
     *
     * @param velocity audio source's new velocity
     */
    public void setVelocity(@NotNull Vector3f velocity) {
        AL10.alSource3f(id, AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
    }

    /**
     * Returns the audio source's direction.
     *
     * @return the audio source's direction
     */
    @NotNull @ReadOnly
    public Vector3f getDirection() {
        float[] x = {0}, y = {0}, z = {0};
        AL10.alGetSource3f(id, AL10.AL_DIRECTION, x, y, z);
        return new Vector3f(x[0], y[0], z[0]);
    }

    /**
     * Sets the audio source's direction to the given value.
     *
     * @param direction audio source's new direction
     */
    public void setDirection(@NotNull Vector3f direction) {
        AL10.alSource3f(id, AL10.AL_DIRECTION, direction.x, direction.y, direction.z);
    }

    @Override
    public int getDataSizeInRam() {
        return 0;
    }

    @Override
    public int getDataSizeInAction() {
        return 0;
    }

    @Override
    public void update() {

    }

    @Override
    public void release() {
        stop();
        AL10.alDeleteSources(id);
        id = -1;
    }

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public boolean isUsable() {
        return id != -1;
    }

    @Override
    public String toString() {
        return "AudioSource{" + "id=" + id + ", buffer=" + buffer
                + ", resourceId=" + resourceId + '}';
    }

}
