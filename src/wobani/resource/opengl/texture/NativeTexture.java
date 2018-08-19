package wobani.resource.opengl.texture;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import wobani.resource.opengl.texture.Texture.TextureFilter;
import wobani.resource.opengl.texture.Texture.TextureFilterType;
import wobani.resource.opengl.texture.Texture.TextureWrap;
import wobani.resource.opengl.texture.Texture.TextureWrapDirection;
import wobani.toolbox.Utility;
import wobani.toolbox.annotation.Bind;
import wobani.toolbox.annotation.NotNull;
import wobani.toolbox.annotation.Nullable;
import wobani.toolbox.annotation.ReadOnly;

import java.nio.ByteBuffer;

public class NativeTexture {
    /**
     * Texture's id.
     */
    private int id;

    private int target;

    /**
     * Texture's width and height.
     */
    private final Vector2i size = new Vector2i(-1);
    /**
     * Texture's border color.
     */
    private final Vector4f borderColor = new Vector4f(0);
    /**
     * Determines whether the texture is in sRGB color space.
     */
    protected boolean sRgb;
    /**
     * Texture wrap along the U direction.
     */
    private TextureWrap wrappingU = TextureWrap.REPEAT;
    /**
     * Texture wrap along the V direction.
     */
    private TextureWrap wrappingV = TextureWrap.REPEAT;
    /**
     * Texture wrap along the W direction.
     */
    private TextureWrap wrappingW = TextureWrap.REPEAT;
    /**
     * Texture's magnification filter.
     */
    private TextureFilter magnification = TextureFilter.NEAREST;
    /**
     * Texture's minification filter.
     */
    private TextureFilter minification = TextureFilter.NEAREST;

    NativeTexture() {

    }

    public void createTexture(int target) {
        this.id = GL11.glGenTextures();
        this.target = target;
    }

    @NotNull
    @ReadOnly
    public Vector2i getSize() {
        return new Vector2i(size);
    }

    public void setSize(@NotNull Vector2i size) {
        //TODO: exceptions
        this.size.set(size);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        //FIXME: NO NO
        this.id = id;
    }

    /**
     * Binds the texture.
     */
    public void bind() {
        GL11.glBindTexture(getTarget(), id);
    }

    /**
     * Activates the texture in the given texture unit.
     *
     * @param textureUnit texture unit (0;31)
     * @throws IllegalArgumentException invalid texture unit
     */
    private void activate(int textureUnit) {
        if (textureUnit < 0 || textureUnit > 31) {
            throw new IllegalArgumentException("Invalid texture unit");
        }

        GL13.glActiveTexture(textureUnit + 0x84C0);
    }

    /**
     * Unbinds the texture.
     */
    public void unbind() {
        GL11.glBindTexture(getTarget(), 0);
    }

    public void bindToTextureUnit(int textureUnit) {
        //FIXME: to modern
        activate(textureUnit);
        bind();
    }

    /**
     * Generates the texture's mipmaps.
     */
    @Bind
    public void generateMipmaps() {
        GL30.glGenerateMipmap(getTarget());
    }

    /**
     * Transfers image data to the texture based on the given values.
     *
     * @param internalFormat internal format
     * @param format         format
     * @param type           type
     * @param data           image data
     */
    @Bind
    public void texImage(int internalFormat, int format, int type, @Nullable ByteBuffer data) {
        //FIXME: to modern
        GL11.glTexImage2D(getTarget(), 0, internalFormat, size.x, size.y, 0, format, type, data);
    }

    /**
     * Returns the texture's border color.
     *
     * @return the texture's border color
     */
    @NotNull
    public Vector4f getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the texture's border color to the given value.
     *
     * @param borderColor border color
     * @throws NullPointerException     borderColor can't be null
     * @throws IllegalArgumentException border color can't be lower than 0
     */
    @Bind
    public void setBorderColor(@NotNull Vector4f borderColor) {
        if (borderColor == null) {
            throw new NullPointerException();
        }
        if (!Utility.isHdrColor(new Vector3f(borderColor.x, borderColor.y, borderColor.z))) {
            throw new IllegalArgumentException("Border color can't be lower than 0");
        }
        this.borderColor.set(borderColor);
        float bc[] = {borderColor.x, borderColor.y, borderColor.z, borderColor.w};
        GL11.glTexParameterfv(getTarget(), GL11.GL_TEXTURE_BORDER_COLOR, bc);
    }

    /**
     * Returns the texture's specified wrap mode.
     *
     * @param type texture wrap direction
     * @return the texture's specified wrap mode
     * @throws NullPointerException parameter can't be null
     */
    @NotNull
    public TextureWrap getWrap(@NotNull TextureWrapDirection type) {
        if (type == null) {
            throw new NullPointerException();
        }
        switch (type) {
            case WRAP_U:
                return wrappingU;
            case WRAP_V:
                return wrappingV;
            case WRAP_W:
                return wrappingW;
        }
        return null;
    }

    /**
     * Sets the texture's specified wrap mode to the given value.
     *
     * @param type  texture wrap direction
     * @param value texture wrap
     * @throws NullPointerException type and value can't be null
     */
    @Bind
    public void setWrap(@NotNull TextureWrapDirection type, @NotNull TextureWrap value) {
        if (type == null || value == null) {
            throw new NullPointerException();
        }
        switch (type) {
            case WRAP_U:
                wrappingU = value;
                break;
            case WRAP_V:
                wrappingV = value;
                break;
            case WRAP_W:
                wrappingW = value;
                break;
        }
        GL11.glTexParameteri(getTarget(), type.getCode(), value.getCode());
    }

    /**
     * Returns the texture's specified filter mode.
     *
     * @param type texture filter type
     * @return the texture's specified filter mode
     * @throws NullPointerException parameter can't be null
     */
    @NotNull
    public TextureFilter getFilter(@NotNull TextureFilterType type) {
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
     * @param type  texture filter type
     * @param value texture filter
     * @throws NullPointerException type and value can't be null
     */
    @Bind
    public void setFilter(@NotNull TextureFilterType type, @NotNull TextureFilter value) {
        if (type == null || value == null) {
            throw new NullPointerException();
        }
        if (type == TextureFilterType.MAGNIFICATION) {
            magnification = value;
        } else {
            minification = value;
        }
        GL11.glTexParameteri(getTarget(), type.getCode(), value.getCode());
    }

    public boolean isSRgb() {
        return sRgb;
    }

    public void setsRgb(boolean sRgb) {
        this.sRgb = sRgb;
    }

    /**
     * Returns the texture's native OpenGL type.
     *
     * @return the texture's native OpenGL types
     */
    public int getTarget() {
        return target;
    }

    /**
     * Releases the texture's data.
     */
    public void release() {
        GL11.glDeleteTextures(id);
        id = 0;
    }
}
