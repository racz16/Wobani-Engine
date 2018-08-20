package wobani.resource.opengl.texture.cubemaptexture;

import org.lwjgl.opengl.*;
import org.lwjgl.stb.*;
import wobani.resource.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.io.*;
import java.nio.*;
import java.util.*;

/**
 Stores data about a loaded texture. You can load a texture only once, if you try to load it twice, you get reference to
 the already loaded one.

 @see #loadTexture(List paths, boolean sRgb) */
public class StaticCubeMapTexture extends CubeMapTexture{

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
    private final ByteBuffer[] data = new ByteBuffer[6];

    /**
     Initializes a new StaticCubeMapTexture to the given parameters.

     @param paths textures' relative path (with extension like "res/textures/myTexture.png")
     @param sRgb  determines whether the texture is in sRgb color space
     */
    public StaticCubeMapTexture(@NotNull List<File> paths, boolean sRgb){
        super(new ResourceId(paths));
        basesRgb = sRgb;
        meta.setPaths(paths);
        meta.setLastActiveToNow();
        meta.setDataStorePolicy(ResourceManager.ResourceState.ACTION);
        setsRgb(sRgb);
        //filtering = ResourceManager.getTextureFiltering();

        hddToRam();
        ramToVram();

        int size = 0;
        for(int i = 0; i < 6; i++){
            size += data[i].capacity();
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
        //StaticCubeMapTexture tex = (StaticCubeMapTexture) ResourceManager.getTexture(new ResourceId(paths));
        if(tex != null){
            return tex;
        }
        return new StaticCubeMapTexture(paths, sRgb);
    }

    /**
     Loads the texture's data from file to the RAM.

     @throws IllegalStateException each image have to be the same size
     */

    protected void hddToRam(){
        for(int i = 0; i < 6; i++){
            Image image = new Image(getPath(i), false);
            if(i == 0){
                setSize(image.getSize());
            }else{
                if(!getSize().equals(image.getSize())){
                    throw new IllegalStateException("Each image have to be the same size");
                }
            }
            data[i] = image.getData();
        }

        meta.setState(ResourceManager.ResourceState.RAM);
    }

    protected void ramToVram(){
        createTexture(getTarget(), getSampleCount());
        bind();

        for(int i = 0; i < 6; i++){
            if(issRgb()){
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL21.GL_SRGB, getSize().x, getSize().y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data[i]);
            }else{
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGB, getSize().x, getSize().y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data[i]);
            }
        }

        setWrap(TextureWrapDirection.WRAP_U, TextureWrap.CLAMP_TO_EDGE);
        setWrap(TextureWrapDirection.WRAP_V, TextureWrap.CLAMP_TO_EDGE);
        setBorderColor(getBorderColor());
        //changeFiltering();
        setFilter(TextureFilterType.MINIFICATION, getFilter(TextureFilterType.MINIFICATION));
        setFilter(TextureFilterType.MAGNIFICATION, getFilter(TextureFilterType.MAGNIFICATION));

        meta.setState(ResourceManager.ResourceState.ACTION);
    }

    protected void vramToRam(){
        super.release();

        meta.setState(ResourceManager.ResourceState.RAM);
    }

    protected void ramToHdd(){
        for(int i = 0; i < 6; i++){
            STBImage.stbi_image_free(data[i]);
            data[i] = null;
        }

        meta.setState(ResourceManager.ResourceState.HDD);
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

    private int getTarget(){
        return GL13.GL_TEXTURE_CUBE_MAP;
    }

    @Override
    protected String getTypeName(){
        return "Static CubeMap Texture";
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
