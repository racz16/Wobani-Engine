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
public class DynamicTexture extends AbstractTexture {

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
    public DynamicTexture(@NotNull FboAttachmentSlot attachmentType, @NotNull Vector2i size, boolean floatingPoint, boolean multisampled, int samples, @Nullable ByteBuffer image) {
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
        setTextureWrap(TextureWrapType2D.WRAP_U, wrapingU);
        setTextureWrap(TextureWrapType2D.WRAP_V, wrapingV);
        setBorderColor(borderColor);

        ResourceManager.addTexture("." + ResourceManager.getNextId(), this);
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
        glSetFilter(type, value, multisampled);
    }

    /**
     * Returns the texture's specified wrap mode.
     *
     * @param type texture wrap direction
     * @return the texture's specified wrap mode
     */
    @NotNull
    public TextureWrap getTextureWrap(@NotNull TextureWrapType2D type) {
        return glGetWrap(type);
    }

    /**
     * Sets the texture's specified wrap mode to the given value.
     *
     * @param type texture wrap direction
     * @param tw texture wrap
     */
    @Bind
    public void setTextureWrap(@NotNull TextureWrapType2D type, @NotNull TextureWrap tw) {
        glSetWrap(type, tw, multisampled);
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
        glSetBorderColor(borderColor, multisampled);
    }

    @Override
    public boolean isUsable() {
        return getTextureId() != 0;
    }

    @Override
    public void bindToTextureUnit(int textureUnit) {
        glActivate(textureUnit);
        bind();
    }

    @Override
    public void bind() {
        glBind(multisampled);
    }

    @Override
    public int getTextureId() {
        return glGetId();
    }

    @Override
    public void unbind() {
        glUnbind(multisampled);
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
    public int getDataSizeInVram() {
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

    @Override
    public String toString() {
        return super.toString() + "\nDynamicTexture{" + "dataSize=" + dataSize
                + ", attachmentType=" + attachmentType
                + ", multisampled=" + multisampled + ", samples=" + samples + '}';
    }

}
