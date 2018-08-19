package wobani.resource.opengl.texture.texture2d;

import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;
import wobani.resource.ResourceId;
import wobani.resource.opengl.buffer.Fbo.FboAttachmentSlot;
import wobani.resource.opengl.texture.DynamicTexture;
import wobani.toolbox.annotation.Bind;
import wobani.toolbox.annotation.NotNull;
import wobani.toolbox.annotation.Nullable;

import java.nio.ByteBuffer;

/**
 * Dynamic texture for FBO attachments.
 */
public class DynamicTexture2D extends DynamicTexture implements Texture2D {

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
     * @param size           texture's width and height
     * @param floatingPoint  texture store color attachments as floating point values or not
     * @param multisampled   multisampled
     * @param samples        number of samples, if the texture isn't multisampled, it can be anything
     * @param image          texture's image data, if the texture is multisampled, it doesn't used
     * @throws NullPointerException     attachmentType and size can't be null
     * @throws IllegalArgumentException width and height must be positive
     * @throws IllegalArgumentException samples can't be lower than 1
     */
    public DynamicTexture2D(@NotNull FboAttachmentSlot attachmentType, @NotNull Vector2i size, boolean floatingPoint, boolean multisampled, int samples, @Nullable ByteBuffer image) {
        super(new ResourceId());
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
        getTexture().createTexture(getTarget());
        getTexture().setSize(size);
        if (multisampled) {
            this.samples = samples;
        } else {
            this.samples = 1;
        }
        dataSize = size.x * size.y * 4 * 4 * samples;

        bind();
        if (multisampled) {
            GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, attachmentType
                    .getInternalFormat(floatingPoint), size.x, size.y, true);
        } else {
            getTexture().texImage(attachmentType.getInternalFormat(floatingPoint), attachmentType.getFormat(), attachmentType.getType(), image);
        }
        //FIXME: filter, texture wrap és border color nem álltható, ha multisampled a textura
        setFilter(TextureFilterType.MINIFICATION, getTexture().getFilter(TextureFilterType.MINIFICATION));
        setFilter(TextureFilterType.MAGNIFICATION, getTexture().getFilter(TextureFilterType.MAGNIFICATION));
        setTextureWrap(TextureWrapDirection.WRAP_U, getTexture().getWrap(TextureWrapDirection.WRAP_U));
        setTextureWrap(TextureWrapDirection.WRAP_V, getTexture().getWrap(TextureWrapDirection.WRAP_V));
        setBorderColor(getTexture().getBorderColor());
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
     * @throws NullPointerException parameter can't be null
     */
    private void setAttachmentType(@NotNull FboAttachmentSlot attachmentType) {
        if (attachmentType == null) {
            throw new NullPointerException();
        }
        this.attachmentType = attachmentType;
    }

    /**
     * Returns the texture's specified wrap mode.
     *
     * @param type texture wrap direction
     * @return the texture's specified wrap mode
     * @throws IllegalArgumentException w direction can't apply to a 2D texture
     */
    @NotNull
    @Override
    public TextureWrap getTextureWrap(@NotNull TextureWrapDirection type) {
        if (type == TextureWrapDirection.WRAP_W) {
            throw new IllegalArgumentException("W direction can't apply to a 2D texture");
        }
        return super.getTextureWrap(type);
    }

    /**
     * Sets the texture's specified wrap mode to the given value.
     *
     * @param type texture wrap direction
     * @param tw   texture wrap
     * @throws IllegalArgumentException w direction can't apply to a 2D texture
     */
    @Bind
    @Override
    public void setTextureWrap(@NotNull TextureWrapDirection type, @NotNull TextureWrap tw) {
        if (type == TextureWrapDirection.WRAP_W) {
            throw new IllegalArgumentException("W direction can't apply to a 2D texture");
        }
        super.setTextureWrap(type, tw);
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

    private int getTarget() {
        return multisampled ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D;
    }

    @Override
    protected String getTypeName() {
        return "Dynamic Texture2D";
    }


}
