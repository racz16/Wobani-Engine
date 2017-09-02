package resources.textures;

import java.nio.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * Basic data and methods for implementing a texture.
 */
public abstract class AbstractTexture implements Texture2D {

    /**
     * Texture's id.
     */
    protected int textureId = 0;
    /**
     * Texture's width and height.
     */
    protected final Vector2i size = new Vector2i(-1);
    /**
     * Determines whether the texture is in sRGB color space.
     */
    protected boolean sRgb;
    /**
     * Texture wrap along the U direcion.
     */
    protected TextureWrap wrapingU = TextureWrap.REPEAT;
    /**
     * Texture wrap along the V direcion.
     */
    protected TextureWrap wrapingV = TextureWrap.REPEAT;
    /**
     * Texture's magnification filter.
     */
    protected TextureFilter magnification = TextureFilter.NEAREST;
    /**
     * Texture's minification filter.
     */
    protected TextureFilter minification = TextureFilter.NEAREST;
    /**
     * Texture's border color.
     */
    protected final Vector4f borderColor = new Vector4f(0);

    @NotNull @ReadOnly
    @Override
    public Vector2i getSize() {
        return new Vector2i(size);
    }

    @Override
    public void update() {
    }

    /**
     * Generates an id for the texture.
     */
    protected void glGenerateTextureId() {
        textureId = GL11.glGenTextures();
    }

    /**
     * Returns the textures's id.
     *
     * @return texture's id
     */
    protected int glGetId() {
        return textureId;
    }

    /**
     * Binds the texture.
     *
     * @param multisampled multisampled texture
     */
    protected void glBind(boolean multisampled) {
        GL11.glBindTexture(multisampled ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D, textureId);
    }

    /**
     * Activates the textture in the given texture unit.
     *
     * @param textureUnit texture unit (0;31)
     *
     * @throws IllegalArgumentException invalid texture unit
     */
    protected void glActivate(int textureUnit) {
        if (textureUnit < 0 || textureUnit > 31) {
            throw new IllegalArgumentException("Invalid texture unit");
        }

        GL13.glActiveTexture(textureUnit + 0x84C0);
    }

    /**
     * Unbinds the texture.
     *
     * @param multisampled multisampled texture
     */
    protected void glUnbind(boolean multisampled) {
        GL11.glBindTexture(multisampled ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D, 0);
    }

    /**
     * Generates the texture's mipmaps.
     *
     * @param multisampled multisampled texture
     */
    @Bind
    protected void glGenerateMipmaps(boolean multisampled) {
        GL30.glGenerateMipmap(multisampled ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D);
    }

    /**
     * Transfers image data to the texture based on the given values.
     *
     * @param internalFormat internal format
     * @param format format
     * @param type type
     * @param data image data
     */
    @Bind
    protected void glTexImage(int internalFormat, int format, int type, @Nullable ByteBuffer data) {
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, size.x, size.y, 0, format, type, data);
    }

    /**
     * Returns the texture's border color.
     *
     * @return the texture's border color
     */
    @NotNull
    protected Vector4f glGetBorderColor() {
        return borderColor;
    }

    /**
     * Sets the texture's border color to the given value.
     *
     * @param borderColor border color
     * @param multisampled multisampled texture
     *
     * @throws NullPointerException borderColor can't be null
     * @throws IllegalArgumentException border color can't be lower than 0
     */
    @Bind
    protected void glSetBorderColor(@NotNull Vector4f borderColor, boolean multisampled) {
        if (borderColor == null) {
            throw new NullPointerException();
        }
        if (!Utility.isHdrColor(new Vector3f(borderColor.x, borderColor.y, borderColor.z))) {
            throw new IllegalArgumentException("Border color can't be lower than 0");
        }
        this.borderColor.set(borderColor);
        float bc[] = {borderColor.x, borderColor.y, borderColor.z, borderColor.w};
        GL11.glTexParameterfv(multisampled ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_BORDER_COLOR, bc);

    }

    /**
     * Returns the texture's specified wrap mode.
     *
     * @param type texture wrap direction
     * @return the texture's specified wrap mode
     *
     * @throws NullPointerException parameter can't be null
     */
    @NotNull
    protected TextureWrap glGetWrap(@NotNull TextureWrapType2D type) {
        if (type == null) {
            throw new NullPointerException();
        }
        if (type == TextureWrapType2D.WRAP_U) {
            return wrapingU;
        } else {
            return wrapingV;
        }
    }

    /**
     * Sets the texture's specified wrap mode to the given value.
     *
     * @param type texture wrap direction
     * @param value texture wrap
     * @param multisampled multisampled texture
     *
     * @throws NullPointerException type and value can't be null
     */
    @Bind
    protected void glSetWrap(@NotNull TextureWrapType2D type, @NotNull TextureWrap value, boolean multisampled) {
        if (type == null || value == null) {
            throw new NullPointerException();
        }
        if (type == TextureWrapType2D.WRAP_U) {
            wrapingU = value;
        } else {
            wrapingV = value;
        }
        GL11.glTexParameteri(multisampled ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D, type.getOpenGlCode(), value.getOpenGlCode());
    }

    /**
     * Returns the texture's specified filter mode.
     *
     * @param type texture filter type
     * @return the texture's specified filter mode
     *
     * @throws NullPointerException parameter can't be null
     */
    @NotNull
    protected TextureFilter glGetFilter(@NotNull TextureFilterType type) {
        if (type == null) {
            throw new NullPointerException();
        }
        if (type == TextureFilterType.MAGNIFICATION) {
            return magnification;
        } else {
            return minification;
        }
    }

    /**
     * Sets the texture's specified filter to the given value.
     *
     * @param type texture filter type
     * @param value texture filter
     * @param multisampled multisampled texture
     *
     * @throws NullPointerException type and value can't be null
     */
    @Bind
    protected void glSetFilter(@NotNull TextureFilterType type, @NotNull TextureFilter value, boolean multisampled) {
        if (type == null || value == null) {
            throw new NullPointerException();
        }
        if (type == TextureFilterType.MAGNIFICATION) {
            magnification = value;
        } else {
            minification = value;
        }
        GL11.glTexParameteri(multisampled ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D, type.getOpenGlCode(), value.getOpenGlCode());
    }

    /**
     * Releases the texture's data.
     */
    protected void glRelease() {
        GL11.glDeleteTextures(textureId);
        textureId = 0;
    }

    @Override
    public String toString() {
        return "AbstractTexture{" + "textureId=" + textureId + ", size=" + size
                + ", sRgb=" + sRgb + ", wrapingU=" + wrapingU + ", wrapingV=" + wrapingV
                + ", magnification=" + magnification + ", minification=" + minification
                + ", borderColor=" + borderColor + '}';
    }

}
