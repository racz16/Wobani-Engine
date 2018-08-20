package wobani.resource.opengl.texture.texture2d;

import org.joml.*;
import org.lwjgl.opengl.*;
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
    protected final DataStoreManager meta = new DataStoreManager();
    /**
     The texture's default color space.
     */
    protected boolean basesRgb;

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
        super(new ResourceId(path));
        basesRgb = sRgb;
        meta.setPaths(Utility.wrapObjectByList(path));
        setsRgb(sRgb);
        meta.setLastActiveToNow();
        meta.setDataStorePolicy(ResourceState.ACTION);
        //filtering = ResourceManager.getTextureFiltering();

        hddToRam();
        ramToVram();

        meta.setDataSize(image.getData().capacity());
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
        //StaticTexture2D tex = (StaticTexture2D) ResourceManager.getTexture(new ResourceId(path));
        if(tex != null){
            return tex;
        }
        return new StaticTexture2D(path, sRgb);
    }

    protected void hddToRam(){
        image = new Image(meta.getPaths().get(0), true);
        setSize(image.getSize());

        meta.setState(ResourceState.RAM);
    }

    protected void ramToVram(){
        createTexture(getTarget(), getSampleCount());

        bind();

        if(issRgb()){
            allocateImmutable(TextureInternalFormat.SRGB8_A8, image.getSize(), true);
            //texImage(GL21.GL_SRGB8_ALPHA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image.getData());
        }else{
            allocateImmutable(TextureInternalFormat.RGBA8, image.getSize(), true);
            //texImage(GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image.getData());
        }
        store(new Vector2i(0), image.getSize(), TextureFormat.RGBA, image.getData());
        //clear(TextureFormat.RGBA);

        //GL45.glGenerateTextureMipmap(getId());

        /*setWrap(TextureWrapDirection.WRAP_U, getWrap(TextureWrapDirection.WRAP_U));
        setWrap(TextureWrapDirection.WRAP_V, getWrap(TextureWrapDirection.WRAP_V));
        setBorderColor(getBorderColor());
        setFilter(TextureFilterType.MINIFICATION, getFilter(TextureFilterType.MINIFICATION));
        setFilter(TextureFilterType.MAGNIFICATION, getFilter(TextureFilterType.MAGNIFICATION));*/
        //changeFiltering();

        //setFilter(TextureFilterType.MINIFICATION, TextureFilter.LINEAR);
        //setFilter(TextureFilterType.MAGNIFICATION, TextureFilter.LINEAR_MIPMAP_LINEAR);

        GL45.glTextureParameteri(getId(), GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GL45.glTextureParameteri(getId(), GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GL45.glGenerateTextureMipmap(getId());

        meta.setState(ResourceState.ACTION);
    }

    protected void vramToRam(){
        super.release();

        meta.setState(ResourceState.RAM);
    }

    protected void ramToHdd(){
        image.release();
        image = null;

        meta.setState(ResourceState.HDD);
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

    private int getTarget(){
        return GL11.GL_TEXTURE_2D;
    }

    @Override
    protected String getTypeName(){
        return "Static Texture2D";
    }


    //
    //
    //
    //


    @Override
    public void bindToTextureUnit(int textureUnit){
        if(meta.getState() != ResourceManager.ResourceState.ACTION){
            if(meta.getState() == ResourceManager.ResourceState.HDD){
                hddToRam();
            }
            ramToVram();
            bind();
        }
        super.bindToTextureUnit(textureUnit);
        meta.setLastActiveToNow();
    }


    /**
     Sets the texture's data store policy to the given value. ACTION means that the texture's data will be stored in
     ACTION. RAM means that the texture's data may be removed from ACTION to RAM if it's rarely used. HDD means that the
     texture's data may be removed from ACTION or even from RAM if it's rarely used. Later if you want to use this
     texture, it'll automatically load the data from file again.

     @param minState data store policy
     */
    public void setDataStorePolicy(@NotNull ResourceManager.ResourceState minState){
        meta.setDataStorePolicy(minState);

        if(minState != ResourceManager.ResourceState.HDD && meta.getState() == ResourceManager.ResourceState.HDD){
            hddToRam();
        }
        if(minState == ResourceManager.ResourceState.ACTION && meta.getState() != ResourceManager.ResourceState.ACTION){
            ramToVram();
        }
    }

    @Override
    public void update(){
        long elapsedTime = System.currentTimeMillis() - meta.getLastActive();
        if(elapsedTime > meta.getActionTimeLimit() && meta.getDataStorePolicy() != ResourceManager.ResourceState.ACTION && meta.getState() != ResourceManager.ResourceState.HDD){
            if(meta.getState() == ResourceManager.ResourceState.ACTION){
                vramToRam();
            }
            if(elapsedTime > meta.getCacheTimeLimit() && meta.getDataStorePolicy() == ResourceManager.ResourceState.HDD){
                ramToHdd();
            }
        }
    }


    /**
     Sets whether or not the texture is in sRGB color space. You can load sRGB color space textures in linear space if
     you want (eg. if you don't want to use gamma correction). But you can't change a default linear color space texture
     to sRGB color space because it's always used in linear color space. Note that this method reloads the texture from
     file if the color space changes.

     @param sRgb sRGB
     */
    public void setsRgb(boolean sRgb){
        if(!basesRgb && sRgb){
            return;
        }
        if(issRgb() != sRgb){
            super.setsRgb(sRgb);
            ResourceManager.ResourceState oldState = meta.getState();
            if(meta.getState() == ResourceManager.ResourceState.ACTION){
                vramToRam();
            }
            if(meta.getState() == ResourceManager.ResourceState.RAM){
                ramToHdd();
            }
            if(oldState != ResourceManager.ResourceState.HDD){
                hddToRam();
            }
            if(oldState == ResourceManager.ResourceState.ACTION){
                ramToVram();
            }
        }
    }

    /**
     Returns the texture's default color space. It doesn't have to be the same as the texture's current color space. You
     can load sRGB color space textures in linear space if you want (eg. if you don't want to use gamma correction).

     @return the texture's default color space

     @see #issRgb()
     @see #setsRgb(boolean sRgb)
     */
    public boolean isDefaultsRgb(){
        return basesRgb;
    }

    @Override
    public int getCachedDataSize(){
        return meta.getState() == ResourceManager.ResourceState.HDD ? 0 : meta.getDataSize();
    }

    @Override
    public int getActiveDataSize(){
        return meta.getState() == ResourceManager.ResourceState.ACTION ? meta.getDataSize() : 0;
    }

    @Override
    public void release(){
        if(meta.getState() == ResourceManager.ResourceState.ACTION){
            vramToRam();
        }
        if(meta.getState() == ResourceManager.ResourceState.RAM){
            ramToHdd();
        }
    }

    @Override
    public boolean isUsable(){
        return true;
    }

}
