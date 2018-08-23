package wobani.resource.opengl.texture.texture2d;

import org.joml.*;
import wobani.resource.*;
import wobani.resource.ResourceManager.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.io.*;

/**
 Stores data about a loaded texture. You can load a texture only once, if you try to load it twice, you get reference to
 the already loaded one.

 @see #loadTexture(File path, boolean sRgb) */
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
     Initializes a new StaticTexture to the given parameters.

     @param path texture's relative path (with extension like "res/textures/myTexture.png")
     @param sRgb determines whether the texture is in sRgb color space
     */
    private StaticTexture2D(@NotNull File path, boolean sRgb){
        super(new ResourceId(path), false);
        setsRgb(sRgb);
        meta.setPaths(Utility.wrapObjectByList(path));
        meta.setDataStorePolicy(ResourceState.ACTIVE);

        hddToRam();
        ramToVram();

        meta.setDataSize(image.getData().capacity());
    }

    @Override
    protected void createTextureId(){
        setId(getTexture2DPool().getResource());
    }

    //
    //loading/saving------------------------------------------------------------
    //

    /**
     Loads a texture from the given path. You can load a texture only once, if you try to load it twice, you get
     reference to the already loaded one.

     @param path texture's relative path (with extension like "res/textures/myTexture.png")
     @param sRgb determines whether the texture is in sRGB color space

     @return texture
     */
    @NotNull
    public static StaticTexture2D loadTexture(@NotNull File path, boolean sRgb){
        StaticTexture2D tex = ResourceManager.getResource(new ResourceId(path), StaticTexture2D.class);
        if(tex != null){
            return tex;
        }
        return new StaticTexture2D(path, sRgb);
    }

    private void hddToRam(){
        image = new Image(meta.getPaths().get(0), true);
        //setCachedDataSize(image.getData().limit());
        meta.setState(ResourceState.CACHE);
    }

    private void ramToVram(){
        createTextureId();
        TextureInternalFormat internalFormat = issRgb() ? TextureInternalFormat.SRGB8_A8 : TextureInternalFormat.RGBA8;
        allocateImmutable2D(internalFormat, image.getSize(), true);
        store(new Vector2i(0), image.getSize(), TextureFormat.RGBA, image.getData());
        meta.setState(ResourceState.ACTIVE);
        meta.setLastActiveToNow();
    }

    private void vramToRam(){
        super.release();
        meta.setState(ResourceState.CACHE);
    }

    private void ramToHdd(){
        image.release();
        image = null;
        //setCachedDataSize(0);
        meta.setState(ResourceState.STORAGE);
    }

    //
    //misc----------------------------------------------------------------------
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
        if(meta.getState() != ResourceManager.ResourceState.ACTIVE){
            if(meta.getState() == ResourceManager.ResourceState.STORAGE){
                hddToRam();
            }
            ramToVram();
        }
        meta.setLastActiveToNow();
        super.bindToTextureUnit(textureUnit);
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
                StaticTexture2D.class.getSimpleName() + "(" +
                "meta: " + meta + ", " +
                "image: " + image + ")";
    }
}
