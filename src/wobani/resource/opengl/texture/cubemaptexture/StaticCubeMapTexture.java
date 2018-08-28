package wobani.resource.opengl.texture.cubemaptexture;

import org.joml.*;
import wobani.resource.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.io.*;
import java.util.*;

/**
 Stores data about a loaded texture. You can load a texture only once, if you try to load it twice, you get reference to
 the already loaded one.

 @see #loadTexture(List paths, boolean sRgb) */
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
     Initializes a new StaticCubeMapTexture to the given parameters.

     @param paths textures' relative path (with extension like "res/textures/myTexture.png")
     @param sRgb  determines whether the texture is in sRgb color space
     */
    public StaticCubeMapTexture(@NotNull List<File> paths, boolean sRgb){
        super(new ResourceId(paths), false);
        meta.setPaths(paths);
        meta.setLastActiveToNow();
        meta.setDataStorePolicy(ResourceManager.ResourceState.ACTIVE);
        setInternalFormat(sRgb ? TextureInternalFormat.SRGB8_A8 : TextureInternalFormat.RGBA8);

        hddToRam();
        ramToVram();

        int size = 0;
        for(int i = 0; i < 6; i++){
            size += images[i].getData().capacity();
        }
        meta.setDataSize(size);
    }

    //
    //loading/saving------------------------------------------------------------
    //

    /**
     Loads a texture from the given path. You can load a texture only once, if you try to load it twice, you get
     reference to the already loaded one.

     @param paths texture's relative path (with extension like "res/textures/myTexture.png")
     @param sRgb  determines whether the texture is in sRGB color space

     @return texture
     */
    @NotNull
    public static StaticCubeMapTexture loadTexture(@NotNull List<File> paths, boolean sRgb){
        StaticCubeMapTexture tex = ResourceManager.getResource(new ResourceId(paths), StaticCubeMapTexture.class);
        if(tex != null){
            return tex;
        }
        return new StaticCubeMapTexture(paths, sRgb);
    }

    /**
     Loads the texture's data from file to the CACHE.

     @throws IllegalStateException each image have to be the same size
     */
    private void hddToRam(){
        for(int i = 0; i < 6; i++){
            images[i] = new Image(getPath(i), false);
            if(i == 0){
                setSize(images[i].getSize());
            }else{
                if(!getSize().equals(images[i].getSize())){
                    throw new IllegalStateException("Each image have to be the same size");
                }
            }
        }
        //TODO compute data size !! *6 !!
        //setCachedDataSize(-1);
        meta.setState(ResourceManager.ResourceState.CACHE);
    }

    private void ramToVram(){
        createTextureId();
        TextureInternalFormat internalFormat = issRgb() ? TextureInternalFormat.SRGB8_A8 : TextureInternalFormat.RGBA8;
        allocateImmutable2D(internalFormat, getSize(), false);
        for(CubeMapSide side : CubeMapSide.values()){
            storeCubeMapSide(new Vector2i(0, 0), side, getSize(), TextureFormat.RGBA, images[side.getIndex()].getData());
        }
        generateMipmaps();
        meta.setState(ResourceManager.ResourceState.ACTIVE);
        meta.setLastActiveToNow();
    }

    private void vramToRam(){
        super.release();
        meta.setState(ResourceManager.ResourceState.CACHE);
    }

    private void ramToHdd(){
        for(int i = 0; i < 6; i++){
            images[i].release();
            images[i] = null;
        }
        //setCachedDataSize(0);
        meta.setState(ResourceManager.ResourceState.STORAGE);
    }

    //
    //misc----------------------------------------------------------------------
    //


    @Override
    public void copyTo(@NotNull DynamicTexture2D destination, @NotNull Vector2i destinationOffset, @NotNull CubeMapSide side, @NotNull Vector2i sourceOffset, @NotNull Vector2i size){
        loadFromCache();
        super.copyTo(destination, destinationOffset, side, sourceOffset, size);
    }

    @Override
    public void copyTo(@NotNull CubeMapTexture destination, @NotNull Vector2i destinationOffset, @NotNull CubeMapSide destinationSide, @NotNull Vector2i sourceOffset, @NotNull CubeMapSide sourceSide, @NotNull Vector2i size){
        loadFromCache();
        super.copyTo(destination, destinationOffset, destinationSide, sourceOffset, sourceSide, size);
    }

    private void loadFromCache(){
        if(meta.getState() == ResourceManager.ResourceState.STORAGE){
            hddToRam();
        }
        if(meta.getState() == ResourceManager.ResourceState.CACHE){
            ramToVram();
        }
    }

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
        if(meta.getState() != ResourceManager.ResourceState.ACTIVE){
            if(meta.getState() == ResourceManager.ResourceState.STORAGE){
                hddToRam();
            }
            ramToVram();
        }
        super.bindToTextureUnit(textureUnit);
        meta.setLastActiveToNow();
    }

    /**
     Sets the texture's data store policy to the given value. ACTIVE means that the texture's data will be stored in
     ACTIVE. CACHE means that the texture's data may be removed from ACTIVE to CACHE if it's rarely used. STORAGE means
     that the texture's data may be removed from ACTIVE or even from CACHE if it's rarely used. Later if you want to use
     this texture, it'll automatically load the data from file again.

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

    @Override
    public void update(){
        long elapsedTime = System.currentTimeMillis() - meta.getLastActive();
        if(meta.getDataStorePolicy() != ResourceManager.ResourceState.ACTIVE && meta.getState() != ResourceManager.ResourceState.STORAGE){
            if(elapsedTime > meta.getActiveTimeLimit() && meta.getState() == ResourceManager.ResourceState.ACTIVE){
                vramToRam();
            }
            if(elapsedTime > meta.getCacheTimeLimit() && meta.getDataStorePolicy() == ResourceManager.ResourceState.STORAGE){
                ramToHdd();
            }
        }
    }

    @Override
    public void release(){
        if(meta.getState() == ResourceManager.ResourceState.ACTIVE){
            vramToRam();
        }
        if(meta.getState() == ResourceManager.ResourceState.CACHE){
            ramToHdd();
        }
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
                "images: " + Utility.toString(images) + ")";
    }
}
