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

     @throws NullPointerException     attachmentType and size can't be null
     @throws IllegalArgumentException width and height must be positive
     @throws IllegalArgumentException samples can't be lower than 1
     */
    public DynamicTexture2D(@NotNull Vector2i size, @NotNull TextureInternalFormat internalFormat, int samples, boolean mipmaps){
        super(new ResourceId());
        createTexture(getTarget(samples), samples);
        allocateImmutable2D(internalFormat, size, mipmaps);
    }

    @Override
    public void store2D(@NotNull TextureFormat format, @NotNull ByteBuffer data){
        super.store2D(format, data);
    }

    @Override
    public void store2D(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull TextureFormat format, @NotNull ByteBuffer data){
        super.store2D(offset, size, format, data);
    }

    @Override
    public void clear(@NotNull Vector3f clearColor){
        super.clear(clearColor);
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
    public boolean isUsable(){
        return isIdValid();
    }

    private int getTarget(int samples){
        return samples > 1 ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D;
    }

    @Override
    protected String getTypeName(){
        return "Dynamic Texture2D";
    }

    @Override
    public int getCacheDataSize(){
        return 0;
    }

    @Override
    public String toString(){
        return super.toString() + "\n" +
                DynamicTexture2D.class.getSimpleName() + "(" + ")";
    }
}
