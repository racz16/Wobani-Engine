package resources.audio;

import org.joml.*;
import org.lwjgl.openal.*;
import resources.*;
import toolbox.annotations.*;

public class AudioSource implements Resource {

    private int id = -1;
    private AudioBuffer buffer;

    public enum AudioSourceState {
        INITIAL(AL10.AL_INITIAL),
        PLAYING(AL10.AL_PLAYING),
        PAUSED(AL10.AL_PAUSED),
        STOPPED(AL10.AL_STOPPED);

        private final int code;

        private AudioSourceState(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public AudioSource(@NotNull AudioBuffer buffer) {
        id = AL10.alGenSources();
        setAudioBuffer(buffer);
    }

    public float getVolume() {
        return AL10.alGetSourcef(id, AL10.AL_GAIN);
    }

    public void setVolume(float volume) {
        AL10.alSourcef(id, AL10.AL_GAIN, volume);
    }

    public float getPitch() {
        return AL10.alGetSourcef(id, AL10.AL_PITCH);
    }

    public void setPitch(float pitch) {
        AL10.alSourcef(id, AL10.AL_PITCH, pitch);
    }

    public float getRolloff() {
        return AL10.alGetSourcef(id, AL10.AL_ROLLOFF_FACTOR);
    }

    public void setRolloff(float rolloff) {
        AL10.alSourcef(id, AL10.AL_ROLLOFF_FACTOR, rolloff);
    }

    public float getReferenceDistance() {
        return AL10.alGetSourcef(id, AL10.AL_REFERENCE_DISTANCE);
    }

    public void setReferenceDistance(float refDistance) {
        AL10.alSourcef(id, AL10.AL_REFERENCE_DISTANCE, refDistance);
    }

    public float getMaxDistance() {
        return AL10.alGetSourcef(id, AL10.AL_MAX_DISTANCE);
    }

    public void setMaxDistance(float maxDistance) {
        AL10.alSourcef(id, AL10.AL_MAX_DISTANCE, maxDistance);
    }

    public void setAudioBuffer(@NotNull AudioBuffer buffer) {
        AL10.alSourcei(id, AL10.AL_BUFFER, buffer.getId());
        this.buffer = buffer;
    }

    public void play() {
        buffer.refreshStore();
        AL10.alSourcePlay(id);
        buffer.setLastActiveToNow();
    }

    public void pause() {
        buffer.refreshStore();
        AL10.alSourcePause(id);
        buffer.setLastActiveToNow();
    }

    public void stop() {
        buffer.refreshStore();
        AL10.alSourceStop(id);
        buffer.setLastActiveToNow();
    }

    public boolean isLoopAudio() {
        return AL10.alGetSourcei(id, AL10.AL_LOOPING) == AL10.AL_TRUE;
    }

    public void setLoopAuidio(boolean loop) {
        AL10.alSourcei(id, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

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

    public void setPosition(@NotNull Vector3f position) {
        AL10.alSource3f(id, AL10.AL_POSITION, position.x, position.y, position.z);
    }

    public void setVelocity(@NotNull Vector3f velocity) {
        AL10.alSource3f(id, AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
    }

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

    @Override
    public boolean isUsable() {
        return id != -1;
    }
}
