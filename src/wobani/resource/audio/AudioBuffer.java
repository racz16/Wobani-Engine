package wobani.resource.audio;

import org.lwjgl.openal.*;
import org.lwjgl.stb.*;
import org.lwjgl.system.*;
import wobani.resource.*;
import wobani.resource.ResourceManager.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.exceptions.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.*;
import static wobani.toolbox.EngineInfo.Library.*;

/**
 Stores a sound effect and can play through an AudioSource.
 */
public class AudioBuffer implements Resource{

    /**
     Stores meta data about this audio buffer.
     */
    private final DataStoreManager meta = new DataStoreManager();
    /**
     The resource's unique id.
     */
    private final ResourceId resourceId;
    /**
     The audio buffer's native OpenAL id.
     */
    private int id;
    /**
     The sound effect's data.
     */
    private ShortBuffer data;
    /**
     Number of the audio buffer's channels.
     */
    private int channels;
    /**
     Audio buffer's frequency.
     */
    private int frequency;

    /**
     Initializes a new AudioBuffer to the given value.

     @param path sound file's relative path (with extension like "res/sounds/mySound.ogg")
     */
    private AudioBuffer(@NotNull File path){
        meta.setPaths(Utility.wrapObjectByList(path));
        meta.setLastActiveToNow();
        meta.setDataStorePolicy(ResourceManager.ResourceState.ACTIVE);

        hddToRam();
        ramToAction();

        meta.setDataSize(data.capacity());
        resourceId = new ResourceId(path);
        ResourceManager.addResource(this);
    }

    /**
     Loads a sound effect from the given path. You can load a sound effect only once, if you try to load it twice, you
     get reference to the already loaded one.

     @param path sound effect's relative path (with extension like "res/sounds/mySound.ogg")

     @return audio buffer
     */
    public static AudioBuffer loadSound(@NotNull File path){
        AudioBuffer sound = ResourceManager.getResource(new ResourceId(path), AudioBuffer.class);
        //AudioBuffer sound = ResourceManager.getAudioBuffer(new ResourceId(path));
        if(sound != null){
            return sound;
        }
        return new AudioBuffer(path);
    }

    /**
     Returns the audio buffer's native OpenAL id. You should use it if you can't do anything else.

     @return the audio buffer's native OpenAL id
     */
    public int getId(){
        return id;
    }

    /**
     Sets the audio buffer's last activation time to now.
     */
    public void setLastActiveToNow(){
        meta.setLastActiveToNow();
    }

    /**
     Loads the sound effect to the sound system.
     */
    public void refreshStore(){
        if(getState() == ResourceState.STORAGE){
            hddToRam();
        }
        if(getState() == ResourceState.CACHE){
            ramToAction();
        }
    }

    /**
     Loads the audio buffer's data from file to the CACHE.

     @throws NativeException failed to load the sound effect
     */
    private void hddToRam(){
        try(STBVorbisInfo info = STBVorbisInfo.malloc()){
            ByteBuffer vorbis;
            try{
                FileInputStream fis = new FileInputStream(getPath());
                FileChannel fc = fis.getChannel();
                vorbis = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                fc.close();
                fis.close();
            }catch(IOException e){
                //WTF?
                throw new RuntimeException(e);
            }

            IntBuffer error = MemoryUtil.memAllocInt(1);
            long decoder = stb_vorbis_open_memory(vorbis, error, null);
            if(decoder == NULL){
                throw new NativeException(OPENAL, "Failed to open Ogg Vorbis file.\n Error: " + error.get(0));
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

        meta.setState(ResourceState.CACHE);
    }

    /**
     Loads the audio buffer's data from the CACHE to the sound system. It may cause errors if the data isn't in the CACHE.
     */
    private void ramToAction(){
        id = AL10.alGenBuffers();
        AL10.alBufferData(id, channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, data, frequency);

        meta.setState(ResourceState.ACTIVE);
    }

    /**
     Removes the audio buffer's data from the sound system. It may cause errors if the data isn't in the sound system.
     */
    private void actionToRam(){
        AL10.alDeleteBuffers(id);
        id = -1;

        meta.setState(ResourceState.CACHE);
    }

    /**
     Removes the audio buffer's data from the CACHE. It may cause errors if the data isn't in the CACHE.
     */
    private void ramToHdd(){
        MemoryUtil.memFree(data);
        data = null;

        meta.setState(ResourceState.STORAGE);
    }

    /**
     Returns the number of the audio buffer's channels.

     @return the number of the audio buffer's channels
     */
    public int getChannels(){
        return channels;
    }

    /**
     Returns the audio buffer's frequency.

     @return the audio buffer's frequency
     */
    public int getFrequency(){
        return frequency;
    }

    //
    //data store----------------------------------------------------------------
    //

    /**
     Returns the ACTIVE time limit. If the elapsed time since this audio buffer's last use is higher than this value and
     the audio buffer's data store policy is CACHE or STORAGE, the audio buffer's data may be removed from the sound system.
     Later if you want to use this audio buffer, you should call the refreshStore method to load the data from file
     again.

     @return ACTIVE time limit (in milliseconds)

     @see #refreshStore()
     */
    public long getActionTimeLimit(){
        return meta.getActiveTimeLimit();
    }

    /**
     Sets the ACTIVE time limit to the given value. If the elapsed time since this audio buffer's last use is higher than
     this value and the audio buffer's data store policy is CACHE or STORAGE, the audio buffer's data may be removed from the
     sound system. Later if you want to use this audio buffer, you should call the refreshStore method to load the data
     from file again.

     @param actionTimeLimit ACTIVE time limit (in milliseconds)

     @see #refreshStore()
     */
    public void setActionTimeLimit(long actionTimeLimit){
        meta.setActionTimeLimit(actionTimeLimit);
    }

    /**
     Returns the CACHE time limit. If the elapsed time since this audio buffer's last use is higher than this value and the
     audio buffer's data store policy is STORAGE, the audio buffer's data may be removed from the sound system or even from
     CACHE. Later if you want to use this texture, you should call the refreshStore method to load the data from file
     again.

     @return CACHE time limit (in milliseconds)

     @see #refreshStore()
     */
    public long getRamTimeLimit(){
        return meta.getCacheTimeLimit();
    }

    /**
     Sets the CACHE time limit to the given value. If the elapsed time since this audio buffer's last use is higher than
     this value and the audio buffer's data store policy is STORAGE, the audio buffer's data may be removed from the sounds
     system or even from CACHE. Later if you want to use this audio buffer, you should call the refreshStore method to load
     the data from file again.

     @param ramTimeLimit CACHE time limit (in milliseconds)

     @see #refreshStore()
     */
    public void setRamTimeLimit(long ramTimeLimit){
        meta.setCacheTimeLimit(ramTimeLimit);
    }

    /**
     Returns the time when the audio buffer last time used.

     @return the time when the audio buffer last time used (in milliseconds)
     */
    public long getLastActive(){
        return meta.getLastActive();
    }

    /**
     Returns the audio buffer's state. It determines where the sound is currently stored.

     @return the audio buffer's state
     */
    @NotNull
    public ResourceState getState(){
        return meta.getState();
    }

    /**
     Returns the audio buffer's data store policy. ACTIVE means that the audio buffer's data will be stored in the sound
     system. CACHE means that the audio buffer's data may be removed from the sound system to CACHE if it's rarely used. STORAGE
     means that the audio buffer's data may be removed from the sound system or even from CACHE if it's rarely used. Later
     if you want to use this audio buffer, you should call the refreshStore method to load the data from file again.

     @return the texture's data store policy

     @see #refreshStore()
     */
    @NotNull
    public ResourceState getDataStorePolicy(){
        return meta.getDataStorePolicy();
    }

    /**
     Sets the audio buffer's data store policy to the given value. ACTIVE means that the sound's data will be stored in
     the sound system. CACHE means that the audio buffer's data may be removed from the sound system to CACHE if it's rarely
     used. STORAGE means that the audio buffer's data may be removed from the sound system or even from CACHE if it's rarely
     used. Later if you want to use this audio buffer, you should call the refreshStore method to load the data from file
     again.

     @param minState data store policy

     @see #refreshStore()
     */
    public void setDataStorePolicy(@NotNull ResourceState minState){
        meta.setDataStorePolicy(minState);

        if(minState != ResourceState.STORAGE && getState() == ResourceState.STORAGE){
            hddToRam();
        }
        if(minState == ResourceState.ACTIVE && getState() != ResourceState.ACTIVE){
            ramToAction();
        }
    }

    @Override
    public void update(){
        long elapsedTime = System.currentTimeMillis() - getLastActive();
        if(elapsedTime > getActionTimeLimit() && getDataStorePolicy() != ResourceState.ACTIVE && getState() != ResourceState.STORAGE){
            if(getState() == ResourceState.ACTIVE){
                actionToRam();
            }
            if(elapsedTime > getRamTimeLimit() && getDataStorePolicy() == ResourceState.STORAGE){
                ramToHdd();
            }
        }
    }

    //
    //misc----------------------------------------------------------------------
    //

    /**
     Returns the sound's path.

     @return the sound's path
     */
    @NotNull
    public File getPath(){
        return meta.getPaths().get(0);
    }

    @Override
    public int getCacheDataSize(){
        return getState() == ResourceState.STORAGE ? 0 : meta.getDataSize();
    }

    @Override
    public int getActiveDataSize(){
        return getState() == ResourceState.ACTIVE ? meta.getDataSize() : 0;
    }

    @Override
    public void release(){
        if(getState() == ResourceState.ACTIVE){
            actionToRam();
        }
        if(getState() == ResourceState.CACHE){
            ramToHdd();
        }
    }

    @NotNull
    @Override
    public ResourceId getResourceId(){
        return resourceId;
    }

    @Override
    public boolean isUsable(){
        return true;
    }

    @Override
    public String toString(){
        return "AudioBuffer{" + "id=" + id + ", meta=" + meta + ", data=" + data + ", channels=" + channels + ", frequency=" + frequency + ", resourceId=" + resourceId + '}';
    }

}
