package resources.textures;

import core.*;
import java.io.*;
import java.nio.*;
import org.joml.Math;
import org.joml.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.*;
import resources.*;
import resources.ResourceManager.ResourceState;
import resources.textures.EasyFiltering.TextureFiltering;
import toolbox.*;
import toolbox.annotations.*;

/**
 * Stores data about a loaded texture. You can load a texture only once, if you
 * try to load it twice, you get reference to the already loaded one.
 *
 * @see #loadTexture(String path, boolean sRGB)
 */
public class StaticTexture extends AbstractTexture implements Texture2D, EasyFiltering, ChangableColorSpace {

    /**
     * Texture's filtering mode.
     */
    private TextureFiltering filtering;
    /**
     * Texture's pixel data.
     */
    private ByteBuffer data;
    /**
     * Stores meta data about this texture.
     */
    private final LoadableResourceMetaData meta = new LoadableResourceMetaData();
    /**
     * The texture's default color space.
     */
    private final boolean basesRgb;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     * Initializes a new StaticTexture to the given parameters.
     *
     * @param path texture's relative path (with extension like
     * "res/textures/myTexture.png")
     * @param sRgb determines whether the texture is in sRgb color space
     */
    private StaticTexture(@NotNull String path, boolean sRgb) {
        basesRgb = sRgb;
        this.sRgb = sRgb;
        meta.setPath(path);
        meta.setLastActiveToNow();
        meta.setDataStorePolicy(ResourceState.ACTION);
        filtering = Settings.getTextureFiltering();

        hddToRam();
        ramToVram();

        meta.setDataSize(data.capacity());
        resourceId = new ResourceId(new File(path));
        ResourceManager.addTexture(this);
    }

    //
    //loading/saving------------------------------------------------------------
    //
    /**
     * Loads a texture from the given path. You can load a texture only once, if
     * you try to load it twice, you get reference to the already loaded one.
     *
     * @param path texture's relative path (with extension like
     * "res/textures/myTexture.png")
     * @param sRgb determines whether the texture is in sRGB color space
     * @return texture
     */
    @NotNull
    public static StaticTexture loadTexture(@NotNull String path, boolean sRgb) {
        StaticTexture tex = (StaticTexture) ResourceManager.getTexture(path);
        if (tex != null) {
            return tex;
        }
        return new StaticTexture(path, sRgb);
    }

    /**
     * Loads the texture's data from file to the RAM.
     */
    private void hddToRam() {
        Image image = new Image(meta.getPath());
        size.set(image.getSize());
        data = image.getImage();

        meta.setState(ResourceState.RAM);
    }

    /**
     * Loads the texture's data from the RAM to the ACTION. It may cause errors
     * if the data isn't in the RAM.
     */
    private void ramToVram() {
        glGenerateTextureId();
        bind();

        if (sRgb) {
            glTexImage(GL21.GL_SRGB8_ALPHA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
        } else {
            glTexImage(GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
        }

        setTextureWrap(TextureWrapDirection.WRAP_U, wrapingU);
        setTextureWrap(TextureWrapDirection.WRAP_U, wrapingV);
        setBorderColor(borderColor);
        changeFiltering();

        meta.setState(ResourceState.ACTION);
    }

    /**
     * Removes the texture's data from the ACTION. It may cause errors if the
     * data isn't in the ACTION.
     */
    private void vramToRam() {
        glRelease();

        meta.setState(ResourceState.RAM);
    }

    /**
     * Removes the texture's data from the RAM. It may cause errors if the data
     * isn't in the RAM.
     */
    private void ramToHdd() {
        STBImage.stbi_image_free(data);
        data = null;

        meta.setState(ResourceState.HDD);
    }

    //
    //texture wrapping----------------------------------------------------------
    //
    /**
     * Returns the texture's specified wrap mode.
     *
     * @param type texture wrap direction
     * @return the texture's specified wrap mode
     *
     * @throws IllegalArgumentException w direction can't apply to a 2D texture
     */
    @NotNull
    public TextureWrap getTextureWrap(@NotNull TextureWrapDirection type) {
        if (type == TextureWrapDirection.WRAP_W) {
            throw new IllegalArgumentException("W direction can't apply to a 2D texture");
        }
        return glGetWrap(type);
    }

    /**
     * Sets the texture's specified wrap mode to the given value.
     *
     * @param type texture wrap direction
     * @param tw texture wrap
     *
     * @throws IllegalArgumentException w direction can't apply to a 2D texture
     */
    @Bind
    public void setTextureWrap(@NotNull TextureWrapDirection type, @NotNull TextureWrap tw) {
        if (type == TextureWrapDirection.WRAP_W) {
            throw new IllegalArgumentException("W direction can't apply to a 2D texture");
        }
        glSetWrap(type, tw);
    }

    /**
     * Returns the texture's border color.
     *
     * @return the texture's border color
     */
    @NotNull @ReadOnly
    public Vector4f getBorderColor() {
        return new Vector4f(glGetBorderColor());
    }

    /**
     * Sets the texture's border color to the given value.
     *
     * @param borderColor border color
     */
    @Bind
    public void setBorderColor(@NotNull Vector4f borderColor) {
        glSetBorderColor(borderColor);
    }

    //
    //texture filtering---------------------------------------------------------
    //
    @NotNull
    @Override
    public TextureFiltering getTextureFiltering() {
        return filtering;
    }

    /**
     * Sets the texture's filtering to the given value.
     *
     * @param tf texture's filtering mode
     *
     * @throws NullPointerException parameter can't be null
     */
    @Bind
    @Override
    public void setTextureFiltering(@NotNull TextureFiltering tf) {
        if (tf == null) {
            throw new NullPointerException();
        }
        if (tf != filtering) {
            boolean fastFilteringChange = tf.getIndex() < 3 && filtering.getIndex() < 3;
            filtering = tf;

            if (getState() == ResourceState.ACTION) {
                if (fastFilteringChange) {
                    changeFiltering();
                } else {
                    vramToRam();
                    ramToVram();
                }
            }
        }
    }

    /**
     * Changes the texture's filtering mode based on the filtering field. It may
     * cause errors if the data isn't in the ACTION.
     */
    @Bind
    private void changeFiltering() {
        glGenerateMipmaps();
        switch (filtering) {
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
                if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
                    float maxLevel = Math.min(2 << filtering.getIndex() - 3, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
                    filtering = TextureFiltering.valueOf("ANISOTROPIC_" + (int) maxLevel + "X");
                    GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxLevel);
                } else {
                    filtering = TextureFiltering.TRILINEAR;
                }
                break;
        }
    }

    //
    //opengl related------------------------------------------------------------
    //
    @Override
    public void bindToTextureUnit(int textureUnit) {
        if (getState() != ResourceState.ACTION) {
            if (getState() == ResourceState.HDD) {
                hddToRam();
            }
            ramToVram();
            bind();
        }

        glActivate(textureUnit);
        glBind();
        meta.setLastActiveToNow();
    }

    @Override
    public void bind() {
        glBind();
    }

    @Override
    public int getId() {
        return glGetId();
    }

    @Override
    public void unbind() {
        glUnbind();
    }

    //
    //data store----------------------------------------------------------------
    //
    /**
     * Returns the ACTION time limit. If the elapsed time since this texture's
     * last use is higher than this value and the texture's data store policy is
     * RAM or HDD, the texture's data may be removed from ACTION. Later if you
     * want to use this texture, it'll automatically load the data from file
     * again.
     *
     * @return ACTION time limit (in miliseconds)
     */
    public long getVramTimeLimit() {
        return meta.getActionTimeLimit();
    }

    /**
     * Sets the ACTION time limit to the given value. If the elapsed time since
     * this texture's last use is higher than this value and the texture's data
     * store policy is RAM or HDD, the texture's data may be removed from
     * ACTION. Later if you want to use this texture, it'll automatically load
     * the data from file again.
     *
     * @param vramTimeLimit ACTION time limit (in miliseconds)
     */
    public void setVramTimeLimit(long vramTimeLimit) {
        meta.setActionTimeLimit(vramTimeLimit);
    }

    /**
     * Returns the RAM time limit. If the elapsed time since this texture's last
     * use is higher than this value and the texture's data store policy is HDD,
     * the texture's data may be removed from ACTION or even from RAM. Later if
     * you want to use this texture, it'll automatically load the data from file
     * again.
     *
     * @return RAM time limit (in miliseconds)
     */
    public long getRamTimeLimit() {
        return meta.getRamTimeLimit();
    }

    /**
     * Sets the RAM time limit to the given value. If the elapsed time since
     * this texture's last use is higher than this value and the texture's data
     * store policy is HDD, the texture's data may be removed from ACTION or
     * even from RAM. Later if you want to use this texture, it'll automatically
     * load the data from file again.
     *
     * @param ramTimeLimit RAM time limit (in miliseconds)
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
     * data will be stored in ACTION. RAM means that the texture's data may be
     * removed from ACTION to RAM if it's rarely used. HDD means that the
     * texture's data may be removed from ACTION or even from RAM if it's rarely
     * used. Later if you want to use this texture, it'll automatically load the
     * data from file again.
     *
     * @return the texture's data store policy
     */
    @NotNull
    public ResourceState getDataStorePolicy() {
        return meta.getDataStorePolicy();
    }

    /**
     * Sets the texture's data store policy to the given value. ACTION means
     * that the texture's data will be stored in ACTION. RAM means that the
     * texture's data may be removed from ACTION to RAM if it's rarely used. HDD
     * means that the texture's data may be removed from ACTION or even from RAM
     * if it's rarely used. Later if you want to use this texture, it'll
     * automatically load the data from file again.
     *
     * @param minState data store policy
     */
    public void setDataStorePolicy(@NotNull ResourceState minState) {
        meta.setDataStorePolicy(minState);

        if (minState != ResourceState.HDD && getState() == ResourceState.HDD) {
            hddToRam();
        }
        if (minState == ResourceState.ACTION && getState() != ResourceState.ACTION) {
            ramToVram();
        }
    }

    @Override
    public void update() {
        long elapsedTime = System.currentTimeMillis() - getLastActive();
        if (elapsedTime > getVramTimeLimit() && getDataStorePolicy() != ResourceState.ACTION && getState() != ResourceState.HDD) {
            if (getState() == ResourceState.ACTION) {
                vramToRam();
            }
            if (elapsedTime > getRamTimeLimit() && getDataStorePolicy() == ResourceState.HDD) {
                ramToHdd();
            }
        }
    }

    //
    //misc----------------------------------------------------------------------
    //
    @Override
    protected int getTextureType() {
        return GL11.GL_TEXTURE_2D;
    }

    /**
     * Returns the texture's path.
     *
     * @return the texture's path
     */
    @NotNull
    public String getPath() {
        return meta.getPath();
    }

    /**
     * Returns the texture's default color space. It doesn't have to be the same
     * as the texture's current color space. You can load sRGB color space
     * textures in linear space if you want (eg. if you don't want to use gamma
     * correction).
     *
     * @return the texture's default color space
     *
     * @see #issRgb()
     * @see #setsRgb(boolean sRgb)
     */
    public boolean isDefaultsRgb() {
        return basesRgb;
    }

    @Override
    public boolean issRgb() {
        return sRgb;
    }

    /**
     * Sets whether or not the texture is in sRGB color space. You can load sRGB
     * color space textures in linear space if you want (eg. if you don't want
     * to use gamma correction). But you can't change a default linear color
     * space texture to sRGB color space because it's always used in linear
     * color space. Note that this method reloads the texture from file if the
     * color space changes.
     *
     * @param sRgb sRGB
     */
    @Override
    public void setsRgb(boolean sRgb) {
        if (!basesRgb && sRgb) {
            return;
        }
        if (issRgb() != sRgb) {
            this.sRgb = sRgb;
            ResourceState oldState = getState();
            if (getState() == ResourceState.ACTION) {
                vramToRam();
            }
            if (getState() == ResourceState.RAM) {
                ramToHdd();
            }
            if (oldState != ResourceState.HDD) {
                hddToRam();
            }
            if (oldState == ResourceState.ACTION) {
                ramToVram();
            }
        }
    }

    @Override
    public boolean isUsable() {
        return true;
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
            vramToRam();
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
    public String toString() {
        return super.toString() + "\nStaticTexture{" + "filtering=" + filtering
                + ", data=" + data + ", meta=" + meta + '}';
    }

}
