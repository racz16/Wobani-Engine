package wobani.resource.opengl.texture.texture2d;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.toolbox.annotation.*;

import java.nio.*;

/**
 Dynamic texture for FBO attachments.
 */
public class DynamicTexture2D extends Texture2D{

    /**
     Initializes a new DynamicTexture to the given parameter.

     @param size    texture's width and height
     @param samples number of samples, if the texture isn't multisampled, it can be anything
     @param image   texture's image data, if the texture is multisampled, it doesn't used

     @throws NullPointerException     attachmentType and size can't be null
     @throws IllegalArgumentException width and height must be positive
     @throws IllegalArgumentException samples can't be lower than 1
     */
    public DynamicTexture2D(@NotNull Vector2i size, @NotNull TextureInternalFormat internalFormat, @NotNull TextureFormat format, int samples, @Nullable ByteBuffer image, boolean mipmaps){
        super(new ResourceId());
        if(internalFormat == null || format == null || size == null){
            throw new NullPointerException();
        }
        createTexture(getTarget(samples), samples);

        bind();
        allocateImmutable(internalFormat, size, mipmaps);
        if(image != null){
            store(new Vector2i(0), size, format, image);
        }

        //FIXME: filter, texture wrap és border color nem állítható, ha multisampled a textura (???)
        setFilter(TextureFilterType.MINIFICATION, getFilter(TextureFilterType.MINIFICATION));
        setFilter(TextureFilterType.MAGNIFICATION, getFilter(TextureFilterType.MAGNIFICATION));
        setWrap(TextureWrapDirection.WRAP_U, getWrap(TextureWrapDirection.WRAP_U));
        setWrap(TextureWrapDirection.WRAP_V, getWrap(TextureWrapDirection.WRAP_V));
        setBorderColor(getBorderColor());
    }

    @Override
    protected int createTextureId(){
        if(getSampleCount() > 1){
            return getTexture2DMultisampledPool().getResource();
        }else{
            return getTexture2DPool().getResource();
        }
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
