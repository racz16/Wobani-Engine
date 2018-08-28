package wobani.resource.opengl.texture.texture2d;

import org.joml.*;
import wobani.resource.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.io.*;
import java.nio.*;

/**
 Dynamic texture, usually for FBO attachments.
 */
public class DynamicTexture2D extends Texture2D{

    /**
     Initializes a new DynamicTexture to the given values.

     @param size           texture's width and height
     @param internalFormat texture's internal format
     @param samples        number of samples
     */
    public DynamicTexture2D(@NotNull Vector2i size, @NotNull TextureInternalFormat internalFormat, int samples){
        super(new ResourceId(), samples > 1);
        allocateImmutable2D(internalFormat, size, samples);
    }

    /**
     Initializes a new DynamicTexture to the given values.

     @param size           texture's width and height
     @param internalFormat texture's internal format
     @param mipmaps        true if this texture should use mipmaps, false otherwise
     */
    public DynamicTexture2D(@NotNull Vector2i size, @NotNull TextureInternalFormat internalFormat, boolean mipmaps){
        super(new ResourceId(), false);
        allocateImmutable2D(internalFormat, size, mipmaps);
    }

    /**
     Initializes a new DynamicTexture to the given values.

     @param path    texture's relative path (with extension like "res/textures/myTexture.png")
     @param sRgb    determines whether the texture is in sRGB color space
     @param samples number of samples
     */
    public DynamicTexture2D(@NotNull File path, boolean sRgb, int samples){
        super(new ResourceId(), samples > 1);
        Image image = new Image(path, true);
        TextureInternalFormat internalFormat = sRgb ? TextureInternalFormat.SRGB8_A8 : TextureInternalFormat.RGBA8;
        allocateImmutable2D(internalFormat, image.getSize(), samples);
        store(TextureFormat.RGBA, image.getData());
        generateMipmaps();
        image.release();
    }

    /**
     Initializes a new DynamicTexture to the given values.

     @param path    texture's relative path (with extension like "res/textures/myTexture.png")
     @param sRgb    determines whether the texture is in sRGB color space
     @param mipmaps true if this texture should use mipmaps, false otherwise
     */
    public DynamicTexture2D(@NotNull File path, boolean sRgb, boolean mipmaps){
        super(new ResourceId(), false);
        Image image = new Image(path, true);
        TextureInternalFormat internalFormat = sRgb ? TextureInternalFormat.SRGB8_A8 : TextureInternalFormat.RGBA8;
        allocateImmutable2D(internalFormat, image.getSize(), mipmaps);
        store(TextureFormat.RGBA, image.getData());
        generateMipmaps();
        image.release();
    }

    @Override
    public void store(@NotNull TextureFormat format, @NotNull ByteBuffer data){
        super.store(format, data);
    }

    @Override
    public void store(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull TextureFormat format, @NotNull ByteBuffer data){
        super.store(offset, size, format, data);
    }

    @Override
    protected void createTextureId(){
        if(!isAvailable()){
            if(isMultisampled()){
                setId(getTexture2DMultisampledPool().getResource());
            }else{
                setId(getTexture2DPool().getResource());
            }
        }
    }

    @Override
    public boolean isUsable(){
        return isAvailable();
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
