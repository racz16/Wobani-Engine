package resources.audio;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import org.lwjgl.openal.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_close;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_info;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_samples_short_interleaved;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_open_memory;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_stream_length_in_samples;
import org.lwjgl.stb.*;
import org.lwjgl.system.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import resources.*;
import resources.ResourceManager.ResourceState;
import toolbox.*;
import toolbox.annotations.*;

/**
 * Stores a sound effect and can play through an AudioSource.
 */
public class AudioBuffer implements Resource {

    /**
     * The audio buffer's native OpenAL id.
     */
    private int id;
    /**
     * Stores meta data about this audio buffer.
     */
    private final LoadableResourceMetaData meta = new LoadableResourceMetaData();
    /**
     * The sound effect's data.
     */
    private ShortBuffer data;
    /**
     * Number of the audio buffer's channels.
     */
    private int channels;
    /**
     * Audio buffer's frequency.
     */
    private int frequency;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     * Initializes a new AudioBuffer to the given value.
     *
     * @param path sound file's relative path (with extension like
     * "res/sounds/mySound.ogg")
     */
    private AudioBuffer(@NotNull File path) {
        meta.setPaths(Utility.wrapObjectByList(path));
        meta.setLastActiveToNow();
        meta.setDataStorePolicy(ResourceManager.ResourceState.ACTION);

        hddToRam();
        ramToAction();

        meta.setDataSize(data.capacity());
        resourceId = new ResourceId(path);
        ResourceManager.addAudioBuffer(this);
    }

    /**
     * Returns the audio buffer's native OpenAL id. You should use it if you
     * can't do anything else.
     *
     * @return the audio buffer's native OpenAL id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the audio buffer's last activation time to now.
     */
    public void setLastActiveToNow() {
        meta.setLastActiveToNow();
    }

    /**
     * Loads the sound effect to the sound system.
     */
    public void refreshStore() {
        if (getState() == ResourceState.HDD) {
            hddToRam();
        }
        if (getState() == ResourceState.RAM) {
            ramToAction();
        }
    }

    /**
     * Loads the audio buffer's data from file to the RAM.
     *
     * @throws RuntimeException failed to load the sound effect
     */
    private void hddToRam() {
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            ByteBuffer vorbis = null;
            try {
                FileInputStream fis = new FileInputStream(getPath());
                FileChannel fc = fis.getChannel();
                vorbis = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                fc.close();
                fis.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            IntBuffer error = MemoryUtil.memAllocInt(1);
            long decoder = stb_vorbis_open_memory(vorbis, error, null);
            if (decoder == NULL) {
                throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
            }
            stb_vorbis_get_info(decoder, info);
            channels = info.channels();
            frequency = info.sample_rate();
            int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);
            ShortBuffer pcm = MemoryUtil.memAllocShort(lengthSamples);
            pcm.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);
            stb_vorbis_close(decoder);
            data = pcm;
            MemoryUtil.memFree(error);
        }

        meta.setState(ResourceState.RAM);
    }

    /**
     * Loads the audio buffer's data from the RAM to the sound system. It may
     * cause errors if the data isn't in the RAM.
     */
    private void ramToAction() {
        id = AL10.alGenBuffers();
        AL10.alBufferData(id, channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, data, frequency);

        meta.setState(ResourceState.ACTION);
    }

    /**
     * Removes the audio buffer's data from the sound system. It may cause
     * errors if the data isn't in the sound system.
     */
    private void actionToRam() {
        AL10.alDeleteBuffers(id);
        id = -1;

        meta.setState(ResourceState.RAM);
    }

    /**
     * Removes the audio buffer's data from the RAM. It may cause errors if the
     * data isn't in the RAM.
     */
    private void ramToHdd() {
        MemoryUtil.memFree(data);
        data = null;

        meta.setState(ResourceState.HDD);
    }

    /**
     * Loads a sound effect from the given path. You can load a sound effect
     * only once, if you try to load it twice, you get reference to the already
     * loaded one.
     *
     * @param path sound effect's relative path (with extension like
     * "res/sounds/mySound.ogg")
     * @return audio buffer
     */
    public static AudioBuffer loadSound(@NotNull File path) {
        AudioBuffer sound = ResourceManager.getAudioBuffer(new ResourceId(path));
        if (sound != null) {
            return sound;
        }
        return new AudioBuffer(path);
    }

    /**
     * Returns the number of the audio buffer's channels.
     *
     * @return the number of the audio buffer's channels
     */
    public int getChannels() {
        return channels;
    }

    /**
     * Returns the audio buffer's frequency.
     *
     * @return the audio buffer's frequency
     */
    public int getFrequency() {
        return frequency;
    }

    //
    //data store----------------------------------------------------------------
    //
    /**
     * Returns the ACTION time limit. If the elapsed time since this audio
     * buffer's last use is higher than this value and the audio buffer's data
     * store policy is RAM or HDD, the audio buffer's data may be removed from
     * the sound system. Later if you want to use this audio buffer, you should
     * call the refreshStore method to load the data from file again.
     *
     * @return ACTION time limit (in miliseconds)
     *
     * @see #refreshStore()
     */
    public long getActionTimeLimit() {
        return meta.getActionTimeLimit();
    }

    /**
     * Sets the ACTION time limit to the given value. If the elapsed time since
     * this audio buffer's last use is higher than this value and the audio
     * buffer's data store policy is RAM or HDD, the audio buffer's data may be
     * removed from the sound system. Later if you want to use this audio
     * buffer, you should call the refreshStore method to load the data from
     * file again.
     *
     * @param actionTimeLimit ACTION time limit (in miliseconds)
     *
     * @see #refreshStore()
     */
    public void setActionTimeLimit(long actionTimeLimit) {
        meta.setActionTimeLimit(actionTimeLimit);
    }

    /**
     * Returns the RAM time limit. If the elapsed time since this audio buffer's
     * last use is higher than this value and the audio buffer's data store
     * policy is HDD, the audio buffer's data may be removed from the sound
     * system or even from RAM. Later if you want to use this texture, you
     * should call the refreshStore method to load the data from file again.
     *
     * @return RAM time limit (in miliseconds)
     *
     * @see #refreshStore()
     */
    public long getRamTimeLimit() {
        return meta.getRamTimeLimit();
    }

    /**
     * Sets the RAM time limit to the given value. If the elapsed time since
     * this audio buffer's last use is higher than this value and the audio
     * buffer's data store policy is HDD, the audio buffer's data may be removed
     * from the sounds system or even from RAM. Later if you want to use this
     * audio buffer, you should call the refreshStore method to load the data
     * from file again.
     *
     * @param ramTimeLimit RAM time limit (in miliseconds)
     *
     * @see #refreshStore()
     */
    public void setRamTimeLimit(long ramTimeLimit) {
        meta.setRamTimeLimit(ramTimeLimit);
    }

    /**
     * Returns the time when the audio buffer last time used.
     *
     * @return the time when the audio buffer last time used (in miliseconds)
     */
    public long getLastActive() {
        return meta.getLastActive();
    }

    /**
     * Returns the audio buffer's state. It determines where the sound is
     * currently stored.
     *
     * @return the audio buffer's state
     */
    @NotNull
    public ResourceState getState() {
        return meta.getState();
    }

    /**
     * Returns the audio buffer's data store policy. ACTION means that the audio
     * buffer's data will be stored in the sound system. RAM means that the
     * audio buffer's data may be removed from the sound system to RAM if it's
     * rarely used. HDD means that the audio buffer's data may be removed from
     * the sound system or even from RAM if it's rarely used. Later if you want
     * to use this audio buffer, you should call the refreshStore method to load
     * the data from file again.
     *
     * @return the texture's data store policy
     *
     * @see #refreshStore()
     */
    @NotNull
    public ResourceState getDataStorePolicy() {
        return meta.getDataStorePolicy();
    }

    /**
     * Sets the audio buffer's data store policy to the given value. ACTION
     * means that the sound's data will be stored in the sound system. RAM means
     * that the audio buffer's data may be removed from the sound system to RAM
     * if it's rarely used. HDD means that the audio buffer's data may be
     * removed from the sound system or even from RAM if it's rarely used. Later
     * if you want to use this audio buffer, you should call the refreshStore
     * method to load the data from file again.
     *
     * @param minState data store policy
     *
     * @see #refreshStore()
     */
    public void setDataStorePolicy(@NotNull ResourceState minState) {
        meta.setDataStorePolicy(minState);

        if (minState != ResourceState.HDD && getState() == ResourceState.HDD) {
            hddToRam();
        }
        if (minState == ResourceState.ACTION && getState() != ResourceState.ACTION) {
            ramToAction();
        }
    }

    @Override
    public void update() {
        long elapsedTime = System.currentTimeMillis() - getLastActive();
        if (elapsedTime > getActionTimeLimit() && getDataStorePolicy() != ResourceState.ACTION && getState() != ResourceState.HDD) {
            if (getState() == ResourceState.ACTION) {
                actionToRam();
            }
            if (elapsedTime > getRamTimeLimit() && getDataStorePolicy() == ResourceState.HDD) {
                ramToHdd();
            }
        }
    }

    //
    //misc----------------------------------------------------------------------
    //
    /**
     * Returns the sound's path.
     *
     * @return the sound's path
     */
    @NotNull
    public File getPath() {
        return meta.getPaths().get(0);
    }

    @Override
    public int getDataSizeInRam() {
        return getState() == ResourceState.HDD ? 0 : meta.getDataSize();
    }

    @Override
    public int getDataSizeInAction() {
        return getState() == ResourceState.ACTION ? meta.getDataSize() : 0;
    }

    @Override
    public void release() {
        if (getState() == ResourceState.ACTION) {
            actionToRam();
        }
        if (getState() == ResourceState.RAM) {
            ramToHdd();
        }
    }

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public boolean isUsable() {
        return true;
    }

    @Override
    public String toString() {
        return "AudioBuffer{" + "id=" + id + ", meta=" + meta + ", data=" + data
                + ", channels=" + channels + ", frequency=" + frequency
                + ", resourceId=" + resourceId + '}';
    }

}
