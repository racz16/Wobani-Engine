package resources.textures;

import java.nio.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import resources.Fbo.FboAttachmentSlot;
import resources.*;
import toolbox.annotations.*;

/**
 * Dynamic texture for FBO attachments.
 */
public class DynamicTexture2D extends AbstractTexture implements Texture2D {

    /**
     * The texture data size (in bytes).
     */
    private int dataSize;
    /**
     * Texture's attachment type.
     */
    private FboAttachmentSlot attachmentType;
    /**
     * Determines whether the texture is multisampled.
     */
    private boolean multisampled;
    /**
     * Number of the texture's samples.
     */
    private int samples;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     * Initializes a new DynamicTexture to the given parameter.
     *
     * @param attachmentType texture's attachment type
     * @param size texture's width and height
     * @param floatingPoint texture store color attachments as floating point
     * values or not
     * @param multisampled multisampled
     * @param samples number of samples, if the texture isn't multisampled, it
     * can be anything
     * @param image texture's image data, if the texture is multisampled, it
     * doesn't used
     *
     * @throws NullPointerException attachmentType and size can't be null
     * @throws IllegalArgumentException width and height must be positive
     * @throws IllegalArgumentException samples can't be lower than 1
     */
    public DynamicTexture2D(@NotNull FboAttachmentSlot attachmentType, @NotNull Vector2i size, boolean floatingPoint, boolean multisampled, int samples, @Nullable ByteBuffer image) {
        if (attachmentType == null || size == null) {
            throw new NullPointerException();
        }
        if (size.x <= 0 || size.y <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        if (multisampled && samples < 1) {
            throw new IllegalArgumentException("Samples can't be lower than 1");
        }
        setAttachmentType(attachmentType);
        this.multisampled = multisampled;
        this.size.set(size);
        if (multisampled) {
            this.samples = samples;
        } else {
            this.samples = 1;
        }
        dataSize = size.x * size.y * 4 * 4 * samples;

        glGenerateTextureId();
        bind();
        if (multisampled) {
            GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, attachmentType.getInternalFormat(floatingPoint), size.x, size.y, true);
        } else {
            glTexImage(attachmentType.getInternalFormat(floatingPoint), attachmentType.getFormat(), attachmentType.getType(), image);
        }

        setFilter(TextureFilterType.MINIFICATION, minification);
        setFilter(TextureFilterType.MAGNIFICATION, magnification);
        setTextureWrap(TextureWrapDirection.WRAP_U, wrapingU);
        setTextureWrap(TextureWrapDirection.WRAP_V, wrapingV);
        setBorderColor(borderColor);

        resourceId = new ResourceId();
        ResourceManager.addTexture(this);
    }

    /**
     * Returns the texture's attachment type.
     *
     * @return the texture's attachment type.
     */
    @NotNull
    public FboAttachmentSlot getAttachmentType() {
        return attachmentType;
    }

    /**
     * Sets the texture's attachment type to the given value.
     *
     * @param attachmentType texture' attachment type
     *
     * @throws NullPointerException parameter can't be null
     */
    private void setAttachmentType(@NotNull FboAttachmentSlot attachmentType) {
        if (attachmentType == null) {
            throw new NullPointerException();
        }
        this.attachmentType = attachmentType;
    }

    /**
     * Returns the texture's specified filter mode.
     *
     * @param type texture filter type
     * @return the texture's specified filter mode
     */
    @NotNull
    public TextureFilter getFilter(@NotNull TextureFilterType type) {
        return glGetFilter(type);
    }

    /**
     * Sets the texture's specified filter to the given value.
     *
     * @param type texture filter type
     * @param value texture filter
     */
    @Bind
    public void setFilter(@NotNull TextureFilterType type, @NotNull TextureFilter value) {
        glSetFilter(type, value);
    }

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

    @Override
    protected int getTextureType() {
        return multisampled ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D;
    }

    @Override
    public boolean isUsable() {
        return getId() != 0;
    }

    @Override
    public void bindToTextureUnit(int textureUnit) {
        glActivate(textureUnit);
        glBind();
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

    @Override
    public boolean issRgb() {
        return false;
    }

    /**
     * Determines whether the texture is multisampled.
     *
     * @return true if the texture is multisampled, false otherwise
     */
    public boolean isMultisampled() {
        return multisampled;
    }

    /**
     * Returns the number of the texture's samples.
     *
     * @return the number of the texture's samples
     */
    public int getNumberOfSamples() {
        return samples;
    }

    @Override
    public int getDataSizeInRam() {
        return 0;
    }

    @Override
    public int getDataSizeInAction() {
        return dataSize;
    }

    /**
     * Releases the texture's data. After calling this method, you can't use
     * this texture for anything.
     */
    @Override
    public void release() {
        glRelease();
        dataSize = 0;
    }

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public String toString() {
        return super.toString() + "\nDynamicTexture{" + "dataSize=" + dataSize
                + ", attachmentType=" + attachmentType + ", multisampled="
                + multisampled + ", samples=" + samples + ", resourceId="
                + resourceId + '}';
    }

}
