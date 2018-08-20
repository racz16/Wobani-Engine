package wobani.resource.opengl.texture.texture2d;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.buffer.Fbo.*;
import wobani.toolbox.annotation.*;

import java.nio.*;

/**
 Dynamic texture for FBO attachments.
 */
public class DynamicTexture2D extends Texture2D{

    /**
     Texture's attachment type.
     */
    private FboAttachmentSlot attachmentType;

    /**
     Initializes a new DynamicTexture to the given parameter.

     @param attachmentType texture's attachment type
     @param size           texture's width and height
     @param floatingPoint  texture store color attachments as floating point values or not
     @param samples        number of samples, if the texture isn't multisampled, it can be anything
     @param image          texture's image data, if the texture is multisampled, it doesn't used

     @throws NullPointerException     attachmentType and size can't be null
     @throws IllegalArgumentException width and height must be positive
     @throws IllegalArgumentException samples can't be lower than 1
     */
    public DynamicTexture2D(@NotNull FboAttachmentSlot attachmentType, @NotNull Vector2i size, boolean floatingPoint, int samples, @Nullable ByteBuffer image, boolean mipmaps){
        super(new ResourceId());
        if(attachmentType == null || size == null){
            throw new NullPointerException();
        }
        setAttachmentType(attachmentType);
        createTexture(getTarget(samples), samples);

        bind();
        allocateImmutable(attachmentType.getInternalFormat(floatingPoint), size, mipmaps);
        if(image != null){
            store(new Vector2i(0), size, attachmentType.getFormat(), image);
        }

        //FIXME: filter, texture wrap és border color nem állítható, ha multisampled a textura
        //setFilter(TextureFilterType.MINIFICATION, getFilter(TextureFilterType.MINIFICATION));
        //setFilter(TextureFilterType.MAGNIFICATION, getFilter(TextureFilterType.MAGNIFICATION));
        //setWrap(TextureWrapDirection.WRAP_U, getWrap(TextureWrapDirection.WRAP_U));
        //setWrap(TextureWrapDirection.WRAP_V, getWrap(TextureWrapDirection.WRAP_V));
        //setBorderColor(getBorderColor());
    }

    /**
     Returns the texture's attachment type.

     @return the texture's attachment type.
     */
    @NotNull
    public FboAttachmentSlot getAttachmentType(){
        return attachmentType;
    }

    /**
     Sets the texture's attachment type to the given value.

     @param attachmentType texture' attachment type

     @throws NullPointerException parameter can't be null
     */
    private void setAttachmentType(@NotNull FboAttachmentSlot attachmentType){
        if(attachmentType == null){
            throw new NullPointerException();
        }
        this.attachmentType = attachmentType;
    }

    @Override
    public int getCachedDataSize(){
        return 0;
    }

    /**
     Releases the texture's data. After calling this method, you can't use this texture for anything.
     */
    @Override
    public void release(){
        super.release();
    }

    @Override
    public boolean isUsable(){
        return getId() != -1;
    }

    private int getTarget(int samples){
        return samples > 1 ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D;
    }

    @Override
    protected String getTypeName(){
        return "Dynamic Texture2D";
    }


}
