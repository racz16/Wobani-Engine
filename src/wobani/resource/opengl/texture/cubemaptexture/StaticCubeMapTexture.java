package wobani.resource.opengl.texture.cubemaptexture;

import org.joml.*;
import wobani.resource.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.io.*;
import java.util.*;

import static wobani.resource.ExceptionHelper.*;

/**
 Stores data about a loaded texture. You can load a texture only once, if you try to load it twice, you get reference to
 the already loaded one.

 @see #loadTexture(List paths, boolean sRgb, boolean mipmaps) */
public class StaticCubeMapTexture extends CubeMapTexture{
    /**
     Stores meta data about this texture.
     */
    private final DataStoreManager meta = new DataStoreManager();
    /**
     Texture's pixel data.
     */
    private final Image[] images = new Image[6];
    /**
     Determines whether the texture should use the default settings instead of it's own.
     */
    private boolean useDefaults = true;

    /**
     Initializes a new StaticCubeMapTexture to the given parameters.

     @param paths   textures' relative path (with extension like "res/textures/myTexture.png")
     @param sRgb    determines whether the textures are in sRgb color space
     @param mipmaps true if this texture should use mipmaps, false otherwise
     */
    private StaticCubeMapTexture(@NotNull List<File> paths, boolean sRgb, boolean mipmaps){
        super(new ResourceId(paths), false);
        setInternalFormat(sRgb ? TextureInternalFormat.SRGB8_A8 : TextureInternalFormat.RGBA8);
        setMipmapCount(mipmaps);
        meta.setPaths(paths);
        meta.setDataStorePolicy(ResourceManager.ResourceState.ACTIVE);
        meta.setState(ResourceManager.ResourceState.STORAGE);
        loadData();
    }

    @Override
    protected void initializeAfterAllocation(){
        if(useDefaults){
            super.initializeAfterAllocation();
        }else{
            setFilter(getFilter());
            setWrapU(getWrapU());
            setWrapV(getWrapV());
            setWrapW(getWrapW());
            setBorderColor(getBorderColor());
            setAnisotropicLevel(getAnisotropicLevel());
        }
    }

    //
    //loading/saving------------------------------------------------------------
    //

    /**
     Loads a texture from the given path. You can load a texture only once, if you try to load it twice, you get
     reference to the already loaded one.

     @param paths   textures' relative path (with extension like "res/textures/myTexture.png")
     @param sRgb    determines whether the textures are in sRGB color space
     @param mipmaps true if this texture should use mipmaps, false otherwise

     @return texture

     @throws IllegalArgumentException if number of given paths is not 6
     */
    @NotNull
    public static StaticCubeMapTexture loadTexture(@NotNull List<File> paths, boolean sRgb, boolean mipmaps){
        exceptionIfNull(paths);
        if(paths.size() != SIDE_COUNT){
            throw new IllegalArgumentException("Number of given paths is not 6");
        }
        StaticCubeMapTexture tex = ResourceManager.getResource(new ResourceId(paths), StaticCubeMapTexture.class);
        if(tex != null){
            return tex;
        }
        return new StaticCubeMapTexture(paths, sRgb, mipmaps);
    }

    /**
     Loads the texture's data from the storage to the RAM.
     */
    private void hddToRam(){
        int dataSize = 0;
        for(int i = 0; i < SIDE_COUNT; i++){
            images[i] = new Image(getPath(i), false);
            dataSize += images[i].getData().capacity();
        }
        exceptionIfNotAllSidesHaveTheSameSize();
        meta.setDataSize(dataSize);
        meta.setState(ResourceManager.ResourceState.CACHE);
    }

    /**
     Throws an exception if not all sides of the cube map texture have the same width and height.

     @throws IllegalStateException if not all sides of the cube map texture have the same width and height
     */
    private void exceptionIfNotAllSidesHaveTheSameSize(){
        for(int i = 1; i < SIDE_COUNT; i++){
            if(!images[0].getSize().equals(images[i].getSize())){
                throw new IllegalStateException();
            }
        }
    }

    /**
     Loads the texture's data from the RAM to the VRAM.
     */
    private void ramToVram(){
        createTextureId();
        TextureInternalFormat internalFormat = issRgb() ? TextureInternalFormat.SRGB8_A8 : TextureInternalFormat.RGBA8;
        allocateImmutable2D(internalFormat, images[0].getSize(), isMipmapped());
        for(CubeMapSide side : CubeMapSide.values()){
            storeCubeMapSide(new Vector2i(0, 0), side, images[0].getSize(), TextureFormat.RGBA, images[side.getIndex()].getData());
        }
        generateMipmaps();
        meta.setState(ResourceManager.ResourceState.ACTIVE);
        meta.setLastActiveToNow();
    }

    /**
     Removes the texture's data from the VRAM.
     */
    private void vramToRam(){
        super.release();
        meta.setState(ResourceManager.ResourceState.CACHE);
    }

    /**
     Removes the texture's data from the RAM.
     */
    private void ramToHdd(){
        for(int i = 0; i < SIDE_COUNT; i++){
            images[i].release();
            images[i] = null;
        }
        meta.setDataSize(0);
        meta.setState(ResourceManager.ResourceState.STORAGE);
    }

    //
    //copy--------------------------------------------------------------------------------------------------------------
    //

    @Override
    public void copyTo(@NotNull DynamicTexture2D destination, @NotNull Vector2i destinationOffset, @NotNull CubeMapSide side, @NotNull Vector2i sourceOffset, @NotNull Vector2i size){
        loadData();
        super.copyTo(destination, destinationOffset, side, sourceOffset, size);
    }

    @Override
    public void copyTo(@NotNull CubeMapTexture destination, @NotNull Vector2i destinationOffset, @NotNull CubeMapSide destinationSide, @NotNull Vector2i sourceOffset, @NotNull CubeMapSide sourceSide, @NotNull Vector2i size){
        loadData();
        super.copyTo(destination, destinationOffset, destinationSide, sourceOffset, sourceSide, size);
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
    public ResourceManager.ResourceState getDataStorePolicy(){
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
        if(meta.getState() == ResourceManager.ResourceState.STORAGE){
            hddToRam();
        }
        if(meta.getState() == ResourceManager.ResourceState.CACHE){
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
    //misc----------------------------------------------------------------------
    //

    /**
     Returns the texture's specified path.

     @param index the method returns the indexth path, it must be in the (0;6) interval

     @return the texture's specified path
     */
    @NotNull
    public File getPath(int index){
        return meta.getPaths().get(index);
    }

    @Override
    protected String getTypeName(){
        return "Static CubeMap Texture";
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
                StaticCubeMapTexture.class.getSimpleName() + "(" +
                "meta: " + meta + ", " +
                "images: " + Utility.toString(images) + ", " +
                "useDefaults: " + useDefaults + ")";
    }
}
