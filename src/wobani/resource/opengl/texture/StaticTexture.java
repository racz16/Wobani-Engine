package wobani.resource.opengl.texture;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.toolbox.annotation.*;

import static wobani.resource.opengl.texture.EasyFiltering.TextureFiltering.*;

/**
 Base class for static (loaded from file) textures.
 */
public abstract class StaticTexture extends AbstractTexture implements EasyFiltering, ChangableColorSpace{

    /**
     Stores meta data about this texture.
     */
    protected final DataStoreManager meta = new DataStoreManager();
    /**
     Texture's filtering mode.
     */
    protected EasyFiltering.TextureFiltering filtering;
    /**
     The texture's default color space.
     */
    protected boolean basesRgb;

    //
    //loading/saving------------------------------------------------------------
    //

    /**
     Loads the texture's data from file to the RAM.
     */
    protected abstract void hddToRam();

    /**
     Loads the texture's data from the RAM to the ACTION. It may cause errors if the data isn't in the RAM.
     */
    protected abstract void ramToVram();

    /**
     Removes the texture's data from the ACTION. It may cause errors if the data isn't in the ACTION.
     */
    protected abstract void vramToRam();

    /**
     Removes the texture's data from the RAM. It may cause errors if the data isn't in the RAM.
     */
    protected abstract void ramToHdd();

    //
    //texture wrapping----------------------------------------------------------
    //

    /**
     Returns the texture's specified wrap mode.

     @param type texture wrap direction

     @return the texture's specified wrap mode
     */
    @NotNull
    public TextureWrap getTextureWrap(@NotNull TextureWrapDirection type){
        return glGetWrap(type);
    }

    /**
     Sets the texture's specified wrap mode to the given value.

     @param type texture wrap direction
     @param tw   texture wrap
     */
    @Bind
    public void setTextureWrap(@NotNull TextureWrapDirection type, @NotNull TextureWrap tw){
        glSetWrap(type, tw);
    }

    /**
     Returns the texture's border color.

     @return the texture's border color
     */
    @NotNull
    @ReadOnly
    public Vector4f getBorderColor(){
        return new Vector4f(glGetBorderColor());
    }

    /**
     Sets the texture's border color to the given value.

     @param borderColor border color
     */
    @Bind
    public void setBorderColor(@NotNull Vector4f borderColor){
        glSetBorderColor(borderColor);
    }

    //
    //texture filtering---------------------------------------------------------
    //
    @NotNull
    @Override
    public TextureFiltering getTextureFiltering(){
        return filtering;
    }

    /**
     Sets the texture's filtering to the given value. Note that this method reloads the texture if the old or the new
     filtering mode is anisotropic.

     @param tf texture's filtering mode

     @throws NullPointerException parameter can't be null
     */
    @Bind
    @Override
    public void setTextureFiltering(@NotNull TextureFiltering tf){
        if(tf == null){
            throw new NullPointerException();
        }
        if(tf != filtering){
            boolean fastFilteringChange = tf.getIndex() < 3 && filtering.getIndex() < 3;
            filtering = tf;

            if(getState() == ResourceManager.ResourceState.ACTION){
                if(fastFilteringChange){
                    changeFiltering();
                }else{
                    vramToRam();
                    ramToVram();
                }
            }
        }
    }

    /**
     Changes the texture's filtering mode based on the filtering field. It may cause errors if the data isn't in the
     ACTION.
     */
    @Bind
    protected void changeFiltering(){
        glGenerateMipmaps();
        switch(filtering){
            case NONE:
                glSetFilter(TextureFilterType.MAGNIFICATION, TextureFilter.NEAREST);
                glSetFilter(TextureFilterType.MINIFICATION, TextureFilter.NEAREST_MIPMAP_NEAREST);
                break;
            case BILINEAR:
                glSetFilter(TextureFilterType.MAGNIFICATION, TextureFilter.LINEAR);
                glSetFilter(TextureFilterType.MINIFICATION, TextureFilter.LINEAR_MIPMAP_NEAREST);
                break;
            case TRILINEAR:
                glSetFilter(TextureFilterType.MAGNIFICATION, TextureFilter.LINEAR);
                glSetFilter(TextureFilterType.MINIFICATION, TextureFilter.LINEAR_MIPMAP_LINEAR);
                break;
            default:
                glSetFilter(TextureFilterType.MAGNIFICATION, TextureFilter.LINEAR);
                glSetFilter(TextureFilterType.MINIFICATION, TextureFilter.LINEAR_MIPMAP_LINEAR);
                if(GL.getCapabilities().GL_EXT_texture_filter_anisotropic){
                    float maxLevel = org.joml.Math.min(2 << filtering.getIndex() - 3, GL11
                            .glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
                    filtering = TextureFiltering.valueOf("ANISOTROPIC_" + (int) maxLevel + "X");
                    GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxLevel);
                    //TODO: 4.6-ban nem kell extension
                }else{
                    filtering = TRILINEAR;
                }
                break;
        }
    }

    //
    //opengl related------------------------------------------------------------
    //
    @Override
    public void bind(){
        glBind();
    }

    //TODO: to protected
    public int getId(){
        return id;
    }

    @Override
    public void unbind(){
        glUnbind();
    }

    @Override
    public void bindToTextureUnit(int textureUnit){
        if(getState() != ResourceManager.ResourceState.ACTION){
            if(getState() == ResourceManager.ResourceState.HDD){
                hddToRam();
            }
            ramToVram();
            bind();
        }

        glActivate(textureUnit);
        glBind();
        meta.setLastActiveToNow();
    }

    //
    //data store----------------------------------------------------------------
    //

    /**
     Returns the ACTION time limit. If the elapsed time since this texture's last use is higher than this value and the
     texture's data store policy is RAM or HDD, the texture's data may be removed from ACTION. Later if you want to use
     this texture, it'll automatically load the data from file again.

     @return ACTION time limit (in miliseconds)
     */
    public long getVramTimeLimit(){
        return meta.getActionTimeLimit();
    }

    /**
     Sets the ACTION time limit to the given value. If the elapsed time since this texture's last use is higher than this
     value and the texture's data store policy is RAM or HDD, the texture's data may be removed from ACTION. Later if you
     want to use this texture, it'll automatically load the data from file again.

     @param vramTimeLimit ACTION time limit (in miliseconds)
     */
    public void setVramTimeLimit(long vramTimeLimit){
        meta.setActionTimeLimit(vramTimeLimit);
    }

    /**
     Returns the RAM time limit. If the elapsed time since this texture's last use is higher than this value and the
     texture's data store policy is HDD, the texture's data may be removed from ACTION or even from RAM. Later if you
     want to use this texture, it'll automatically load the data from file again.

     @return RAM time limit (in miliseconds)
     */
    public long getRamTimeLimit(){
        return meta.getCacheTimeLimit();
    }

    /**
     Sets the RAM time limit to the given value. If the elapsed time since this texture's last use is higher than this
     value and the texture's data store policy is HDD, the texture's data may be removed from ACTION or even from RAM.
     Later if you want to use this texture, it'll automatically load the data from file again.

     @param ramTimeLimit RAM time limit (in miliseconds)
     */
    public void setRamTimeLimit(long ramTimeLimit){
        meta.setCacheTimeLimit(ramTimeLimit);
    }

    /**
     Returns the time when the texture last time used.

     @return the time when the texture last time used (in miliseconds)
     */
    public long getLastActive(){
        return meta.getLastActive();
    }

    /**
     Returns the texture's state. It determines where the texture is currently stored.

     @return the texture's state
     */
    @NotNull
    public ResourceManager.ResourceState getState(){
        return meta.getState();
    }

    /**
     Returns the texture's data store policy. ACTION means that the texture's data will be stored in ACTION. RAM means
     that the texture's data may be removed from ACTION to RAM if it's rarely used. HDD means that the texture's data may
     be removed from ACTION or even from RAM if it's rarely used. Later if you want to use this texture, it'll
     automatically load the data from file again.

     @return the texture's data store policy
     */
    @NotNull
    public ResourceManager.ResourceState getDataStorePolicy(){
        return meta.getDataStorePolicy();
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

        if(minState != ResourceManager.ResourceState.HDD && getState() == ResourceManager.ResourceState.HDD){
            hddToRam();
        }
        if(minState == ResourceManager.ResourceState.ACTION && getState() != ResourceManager.ResourceState.ACTION){
            ramToVram();
        }
    }

    @Override
    public void update(){
        long elapsedTime = System.currentTimeMillis() - getLastActive();
        if(elapsedTime > getVramTimeLimit() && getDataStorePolicy() != ResourceManager.ResourceState.ACTION && getState() != ResourceManager.ResourceState.HDD){
            if(getState() == ResourceManager.ResourceState.ACTION){
                vramToRam();
            }
            if(elapsedTime > getRamTimeLimit() && getDataStorePolicy() == ResourceManager.ResourceState.HDD){
                ramToHdd();
            }
        }
    }

    //
    //misc----------------------------------------------------------------------
    //
    @Override
    public boolean issRgb(){
        return sRgb;
    }

    /**
     Sets whether or not the texture is in sRGB color space. You can load sRGB color space textures in linear space if
     you want (eg. if you don't want to use gamma correction). But you can't change a default linear color space texture
     to sRGB color space because it's always used in linear color space. Note that this method reloads the texture from
     file if the color space changes.

     @param sRgb sRGB
     */
    @Override
    public void setsRgb(boolean sRgb){
        if(!basesRgb && sRgb){
            return;
        }
        if(issRgb() != sRgb){
            this.sRgb = sRgb;
            ResourceManager.ResourceState oldState = getState();
            if(getState() == ResourceManager.ResourceState.ACTION){
                vramToRam();
            }
            if(getState() == ResourceManager.ResourceState.RAM){
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
        return getState() == ResourceManager.ResourceState.HDD ? 0 : meta.getDataSize();
    }

    @Override
    public int getActiveDataSize(){
        return getState() == ResourceManager.ResourceState.ACTION ? meta.getDataSize() : 0;
    }

    @Override
    public void release(){
        if(getState() == ResourceManager.ResourceState.ACTION){
            vramToRam();
        }
        if(getState() == ResourceManager.ResourceState.RAM){
            ramToHdd();
        }
    }

    @Override
    public boolean isUsable(){
        return true;
    }

    @Override
    public String toString(){
        return super
                .toString() + "\nStaticTexture{" + "filtering=" + filtering + ", meta=" + meta + ", basesRgb=" + basesRgb + '}';
    }

}
