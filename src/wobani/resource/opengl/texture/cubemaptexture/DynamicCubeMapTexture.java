package wobani.resource.opengl.texture.cubemaptexture;

import org.joml.*;
import wobani.resource.*;
import wobani.toolbox.annotation.*;

import java.nio.*;

/**
 Dynamic cube map texture usually for rendering to it.
 */
public class DynamicCubeMapTexture extends CubeMapTexture{

    /**
     Initializes a new DynamicCubeMapTexture to the given values.

     @param size           one side's width and height
     @param internalFormat texture's internal format
     @param samples        number of samples
     */
    public DynamicCubeMapTexture(@NotNull Vector2i size, @NotNull TextureInternalFormat internalFormat, int samples){
        super(new ResourceId(), samples > 1);
        allocateImmutable2D(internalFormat, size, samples);
    }

    /**
     Initializes a new DynamicCubeMapTexture to the given values.

     @param size           one side's width and height
     @param internalFormat texture's internal format
     @param mipmaps        true if this texture should use mipmaps, false otherwise
     */
    public DynamicCubeMapTexture(@NotNull Vector2i size, @NotNull TextureInternalFormat internalFormat, boolean mipmaps){
        super(new ResourceId(), false);
        allocateImmutable2D(internalFormat, size, mipmaps);
    }

    @Override
    public void storeCubeMapSide(@NotNull Vector2i offset, @NotNull CubeMapSide side, @NotNull Vector2i size, @NotNull TextureFormat format, @NotNull ByteBuffer data){
        super.storeCubeMapSide(offset, side, size, format, data);
    }

    @Override
    protected String getTypeName(){
        return "Dynamic CubeMap Texture";
    }

    @Override
    public boolean isUsable(){
        return isAvailable();
    }

    @Override
    public int getCacheDataSize(){
        return 0;
    }

    @NotNull
    public CubeMapSideTexture getSideTexture(@NotNull CubeMapSide side){
        ExceptionHelper.exceptionIfNotUsable(this);
        ExceptionHelper.exceptionIfNotAllocated(this);
        ExceptionHelper.exceptionIfNull(side);
        return new CubeMapSideTexture(this, side);
    }

    @Override
    public String toString(){
        return super.toString() + "\n" +
                DynamicCubeMapTexture.class.getSimpleName() + "(" + ")";
    }
}
