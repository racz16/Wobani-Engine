package wobani.resource.opengl.texture.texture2d;

import org.joml.*;
import wobani.resource.*;
import wobani.resource.ResourceManager.*;
import wobani.resource.opengl.texture.cubemaptexture.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.io.*;

import static wobani.resource.ExceptionHelper.*;

/**
 Stores data about a loaded texture. You can load a texture only once, if you try to load it twice, you get reference to
 the already loaded one.

 @see #loadTexture(File path, boolean sRgb, boolean mipmaps) */
public class StaticTexture2D extends Texture2D{
    /**
     Stores meta data about this texture.
     */
    private final DataStoreManager meta = new DataStoreManager();
    /**
     Texture's pixel data.
     */
    private Image image;
    /**
     Determines whether the texture should use the default settings instead of it's own.
     */
    private boolean useDefaults = true;

    /**
     Initializes a new StaticTexture to the given parameters.

     @param path    texture's relative path (with extension like "res/textures/myTexture.png")
     @param sRgb    determines whether the texture is in sRgb color space
     @param mipmaps true if this texture should use mipmaps, false otherwise
     */
    private StaticTexture2D(@NotNull File path, boolean sRgb, boolean mipmaps){
        super(new ResourceId(path), false);
        setInternalFormat(sRgb ? TextureInternalFormat.SRGB8_A8 : TextureInternalFormat.RGBA8);
        setMipmapCount(mipmaps);
        meta.setPaths(Utility.wrapObjectByList(path));
        meta.setDataStorePolicy(ResourceState.ACTIVE);
        meta.setState(ResourceState.STORAGE);
        loadData();
    }

    @Override
    protected void createTextureId(){
        if(!isAvailable()){
            setId(getTexture2DPool().getResource());
        }
    }

    @Override
    protected void initializeAfterAllocation(){
        if(useDefaults){
            super.initializeAfterAllocation();
        }else{
            setFilter(getFilter());
            setWrapU(getWrapU());
            setWrapV(getWrapV());
            setBorderColor(getBorderColor());
            setAnisotropicLevel(getAnisotropicLevel());
        }
    }

    //
    //loading/saving----------------------------------------------------------------------------------------------------
    //

    /**
     Loads a texture from the given path. You can load a texture only once, if you try to load it twice, you get
     reference to the already loaded one.

     @param path    texture's relative path (with extension like "res/textures/myTexture.png")
     @param sRgb    determines whether the texture is in sRGB color space
     @param mipmaps true if this texture should use mipmaps, false otherwise

     @return texture
     */
    @NotNull
    public static StaticTexture2D loadTexture(@NotNull File path, boolean sRgb, boolean mipmaps){
        exceptionIfNull(path);
        StaticTexture2D tex = ResourceManager.getResource(new ResourceId(path), StaticTexture2D.class);
        if(tex != null){
            return tex;
        }
        return new StaticTexture2D(path, sRgb, mipmaps);
    }

    /**
     Loads the texture's data from the storage to the RAM.
     */
    private void hddToRam(){
        image = new Image(meta.getPaths().get(0), true);
        meta.setDataSize(image.getData().limit());
        meta.setState(ResourceState.CACHE);
    }

    /**
     Loads the texture's data from the RAM to the VRAM.
     */
    private void ramToVram(){
        createTextureId();
        TextureInternalFormat internalFormat = issRgb() ? TextureInternalFormat.SRGB8_A8 : TextureInternalFormat.RGBA8;
        allocateImmutable2D(internalFormat, image.getSize(), isMipmapped());
        store(TextureFormat.RGBA, image.getData());
        generateMipmaps();
        meta.setState(ResourceState.ACTIVE);
        meta.setLastActiveToNow();
    }

    /**
     Removes the texture's data from the VRAM.
     */
    private void vramToRam(){
        super.release();
        meta.setState(ResourceState.CACHE);
    }

    /**
     Removes the texture's data from the RAM.
     */
    private void ramToHdd(){
        image.release();
        image = null;
        meta.setDataSize(0);
        meta.setState(ResourceState.STORAGE);
    }

    //
    //misc--------------------------------------------------------------------------------------------------------------
    //

    @Override
    public void copyTo(@NotNull DynamicTexture2D destination, @NotNull Vector2i destinationOffset, @NotNull Vector2i sourceOffset, @NotNull Vector2i size){
        loadData();
        super.copyTo(destination, destinationOffset, sourceOffset, size);
    }

    @Override
    public void copyTo(@NotNull DynamicCubeMapTexture destination, @NotNull Vector2i destinationOffset, @NotNull CubeMapTexture.CubeMapSide side, @NotNull Vector2i sourceOffset, @NotNull Vector2i size){
        loadData();
        super.copyTo(destination, destinationOffset, side, sourceOffset, size);
    }

    //
    //cache-------------------------------------------------------------------------------------------------------------
    //

    /**
     Returns the texture's data store policy. Active means that the texture's data will be stored in VRAM. Cache means
     that the texture's data may be removed from VRAM to RAM if it's rarely used. Storage means that the texture's data
     may be removed from VRAM or even from RAM if it's rarely used. Later if you want to use this texture, it'll
     automatically load the data from file again.

     @return the texture's data store policy
     */
    @NotNull
    public ResourceState getDataStorePolicy(){
        return meta.getDataStorePolicy();
    }

    /**
     Sets the texture's data store policy to the given value. Active means that the texture's data will be stored in
     VRAM. Cache means that the texture's data may be removed from VRAM to RAM if it's rarely used. Storage means that
     the texture's data may be removed from VRAM or even from RAM if it's rarely used. Later if you want to use this
     texture, it'll automatically load the data from file again.

     @param minState data store policy
     */
    public void setDataStorePolicy(@NotNull ResourceManager.ResourceState minState){
        meta.setDataStorePolicy(minState);
        if(minState != ResourceManager.ResourceState.STORAGE && meta.getState() == ResourceManager.ResourceState.STORAGE){
            hddToRam();
        }
        if(minState == ResourceManager.ResourceState.ACTIVE && meta.getState() != ResourceManager.ResourceState.ACTIVE){
            ramToVram();
        }
    }

    /**
     Returns the active time limit. If the elapsed time since this resource's last use is higher than this value and the
     resource's data store policy is cache or storage, the resource's data may be removed from the active state.

     @return active time limit (in milliseconds)
     */
    public long getActiveTimeLimit(){
        return meta.getActiveTimeLimit();
    }

    /**
     Sets the active time limit to the given value. If the elapsed time since this resource's last use is higher than
     this value and the resource's data store policy is cache or storage, the resource's data may be removed from the
     active state.

     @param actionTimeLimit active time limit (in milliseconds)
     */
    public void setActionTimeLimit(long actionTimeLimit){
        meta.setActionTimeLimit(actionTimeLimit);
    }

    /**
     Returns the cache time limit. If the elapsed time since this resource's last use is higher than this value and the
     resource's data store policy is storage, the resource's data may be removed from active or even from cache state.

     @return cache time limit (in milliseconds)
     */
    public long getCacheTimeLimit(){
        return meta.getCacheTimeLimit();
    }

    /**
     Sets the cache time limit to the given value. If the elapsed time since this resource's last use is higher than this
     value and the resource's data store policy is storage, the resource's data may be removed from active or even from
     cache state.

     @param cacheTimeLimit cache time limit (in milliseconds)
     */
    public void setCacheTimeLimit(long cacheTimeLimit){
        meta.setCacheTimeLimit(cacheTimeLimit);
    }

    /**
     Loads data from the cache or the storage the texture's data.
     */
    private void loadData(){
        if(meta.getState() == ResourceState.STORAGE){
            hddToRam();
        }
        if(meta.getState() == ResourceState.CACHE){
            ramToVram();
        }
        meta.setLastActiveToNow();
    }

    /**
     Removes the texture's data from the VRAM and RAM.
     */
    private void removeData(){
        if(meta.getState() == ResourceManager.ResourceState.ACTIVE){
            vramToRam();
        }
        if(meta.getState() == ResourceManager.ResourceState.CACHE){
            ramToHdd();
        }
    }

    //
    //misc--------------------------------------------------------------------------------------------------------------
    //

    /**
     Returns the texture's path.

     @return the texture's path
     */
    @NotNull
    public File getPath(){
        return meta.getPaths().get(0);
    }

    @Override
    protected String getTypeName(){
        return "Static Texture2D";
    }

    @Override
    public void bindToTextureUnit(int textureUnit){
        loadData();
        super.bindToTextureUnit(textureUnit);
    }

    @Override
    public void update(){
        long elapsedTime = System.currentTimeMillis() - meta.getLastActive();
        if(meta.getDataStorePolicy() != ResourceManager.ResourceState.ACTIVE && meta.getState() != ResourceManager.ResourceState.STORAGE){
            if(elapsedTime > meta.getActiveTimeLimit() && meta.getState() == ResourceManager.ResourceState.ACTIVE){
                useDefaults = false;
                vramToRam();
            }
            if(elapsedTime > meta.getCacheTimeLimit() && meta.getDataStorePolicy() == ResourceManager.ResourceState.STORAGE){
                ramToHdd();
            }
        }
    }

    @Override
    public void release(){
        useDefaults = true;
        removeData();
    }

    @Override
    public boolean isUsable(){
        return true;
    }

    @Override
    public int getCacheDataSize(){
        return meta.getDataSize();
    }

    @Override
    public String toString(){
        return super.toString() + "\n" +
                StaticTexture2D.class.getSimpleName() + "(" +
                "meta: " + meta + ", " +
                "image: " + image + ", " +
                "useDefaults: " + useDefaults + ")";
    }
}
