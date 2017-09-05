package resources.audio;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import org.lwjgl.*;
import org.lwjgl.openal.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_close;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_info;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_samples_short_interleaved;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_open_memory;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_stream_length_in_samples;
import org.lwjgl.stb.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import resources.*;
import resources.ResourceManager.ResourceState;
import toolbox.annotations.*;

public class AudioBuffer implements Resource {

    private int id;
    /**
     * Stores meta data about this sound.
     */
    private final LoadableResourceMetaData meta = new LoadableResourceMetaData();
    private ShortBuffer data;
    private int channels;
    private int frequency;

    private AudioBuffer(@NotNull String path) {
        meta.setPath(path);
        meta.setLastActiveToNow();
        meta.setDataStorePolicy(ResourceManager.ResourceState.ACTION);

        hddToRam();
        ramToAction();

        meta.setDataSize(data.capacity());
        ResourceManager.addAudioBuffer(path, this);
    }

    public int getId() {
        return id;
    }

    public void setLastActiveToNow() {
        meta.setLastActiveToNow();
    }

    public void refreshStore() {
        if (getState() == ResourceState.HDD) {
            hddToRam();
        }
        if (getState() == ResourceState.RAM) {
            ramToAction();
        }
    }

    /**
     * Loads the texture's data from file to the RAM.
     */
    private void hddToRam() {
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            ByteBuffer vorbis = null;
            try {
                File file = new File(getPath());
                if (file.isFile()) {
                    FileInputStream fis = new FileInputStream(file);
                    FileChannel fc = fis.getChannel();
                    vorbis = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                    fc.close();
                    fis.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            IntBuffer error = BufferUtils.createIntBuffer(1);
            long decoder = stb_vorbis_open_memory(vorbis, error, null);
            if (decoder == NULL) {
                throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
            }

            stb_vorbis_get_info(decoder, info);

            channels = info.channels();
            frequency = info.sample_rate();

            int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

            ShortBuffer pcm = BufferUtils.createShortBuffer(lengthSamples);

            pcm.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);
            stb_vorbis_close(decoder);

            data = pcm;
        }

        meta.setState(ResourceState.RAM);
    }

    /**
     * Loads the texture's data from the RAM to the ACTION. It may cause errors if
     * the data isn't in the RAM.
     */
    private void ramToAction() {
        id = AL10.alGenBuffers();
        AL10.alBufferData(id, channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, data, frequency);

        meta.setState(ResourceState.ACTION);
    }

    /**
     * Removes the texture's data from the ACTION. It may cause errors if the data
 isn't in the ACTION.
     */
    private void actionToRam() {
        AL10.alDeleteBuffers(id);

        meta.setState(ResourceState.RAM);
    }

    /**
     * Removes the texture's data from the RAM. It may cause errors if the data
     * isn't in the RAM.
     */
    private void ramToHdd() {
        data = null;

        meta.setState(ResourceState.HDD);
    }

    public static AudioBuffer loadSound(@NotNull String path) {
        AudioBuffer sound = ResourceManager.getAudioBuffer(path);
        if (sound != null) {
            return sound;
        }
        return new AudioBuffer(path);
    }

    public int getChannels() {
        return channels;
    }

    public int getFrequency() {
        return frequency;
    }

    //
    //data store----------------------------------------------------------------
    //
    /**
     * Returns the ACTION time limit. If the elapsed time since this texture's
 last use is higher than this value and the texture's data store policy is
 RAM or HDD, the texture's data may be removed from ACTION. Later if you
 want to use this texture, it'll automatically load the data from file
 again.
     *
     * @return ACTION time limit (in miliseconds)
     */
    public long getVramTimeLimit() {
        return meta.getActionTimeLimit();
    }

    /**
     * Sets the ACTION time limit to the given value. If the elapsed time since
 this texture's last use is higher than this value and the texture's data
 store policy is RAM or HDD, the texture's data may be removed from ACTION.
 Later if you want to use this texture, it'll automatically load the data
 from file again.
     *
     * @param vramTimeLimit ACTION time limit (in miliseconds)
     * @throws IllegalArgumentException ACTION time limit have to be higher than 0
 and lower than RAM time limit
     */
    public void setVramTimeLimit(long vramTimeLimit) {
        meta.setActionTimeLimit(vramTimeLimit);
    }

    /**
     * Returns the RAM time limit. If the elapsed time since this texture's last
 use is higher than this value and the texture's data store policy is HDD,
 the texture's data may be removed from ACTION or even from RAM. Later if
 you want to use this texture, it'll automatically load the data from file
 again.
     *
     * @return RAM time limit (in miliseconds)
     */
    public long getRamTimeLimit() {
        return meta.getRamTimeLimit();
    }

    /**
     * Sets the RAM time limit to the given value. If the elapsed time since
 this texture's last use is higher than this value and the texture's data
 store policy is HDD, the texture's data may be removed from ACTION or even
 from RAM. Later if you want to use this texture, it'll automatically load
 the data from file again.
     *
     * @param ramTimeLimit RAM time limit (in miliseconds)
     * @throws IllegalArgumentException RAM time limit have to be higher than
 ACTION time limit
     */
    public void setRamTimeLimit(long ramTimeLimit) {
        meta.setRamTimeLimit(ramTimeLimit);
    }

    /**
     * Returns the time when the texture last time used.
     *
     * @return the time when the texture last time used (in miliseconds)
     */
    public long getLastActive() {
        return meta.getLastActive();
    }

    /**
     * Returns the texture's state. It determines where the texture is currently
     * stored.
     *
     * @return the texture's state
     */
    @NotNull
    public ResourceState getState() {
        return meta.getState();
    }

    /**
     * Returns the texture's data store policy. ACTION means that the texture's
 data will be stored in ACTION. RAM means that the texture's data may be
 removed from ACTION to RAM if it's rarely used. HDD means that the
 texture's data may be removed from ACTION or even from RAM if it's rarely
 used. Later if you want to use this texture, it'll automatically load the
 data from file again.
     *
     * @return the texture's data store policy
     */
    @NotNull
    public ResourceState getDataStorePolicy() {
        return meta.getDataStorePolicy();
    }

    /**
     * Sets the sound's data store policy to the given value. ACTION means that
 the sound's data will be stored in ACTION. RAM means that the texture's
 data may be removed from ACTION to RAM if it's rarely used. HDD means that
 the texture's data may be removed from ACTION or even from RAM if it's
 rarely used. Later if you want to use this texture, it'll automatically
 load the data from file again.
     *
     * @param minState data store policy
     *
     * @throws NullPointerException minState can't be null
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
        if (elapsedTime > getVramTimeLimit() && getDataStorePolicy() != ResourceState.ACTION && getState() != ResourceState.HDD) {
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
    public String getPath() {
        return meta.getPath();
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

    @Override
    public boolean isUsable() {
        return true;
    }
}
