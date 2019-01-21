package wobani.resource.opengl.texture.cubemaptexture;

import org.joml.*;
import wobani.resource.*;
import wobani.resource.opengl.fbo.*;
import wobani.resource.opengl.texture.*;
import wobani.toolbox.annotation.*;

public class CubeMapSideTexture implements FboAttachment{

    private final DynamicCubeMapTexture cubeMapTexture;
    private final CubeMapTexture.CubeMapSide side;

    CubeMapSideTexture(@NotNull DynamicCubeMapTexture cubeMapTexture, @NotNull CubeMapTexture.CubeMapSide side){
        ExceptionHelper.exceptionIfNull(cubeMapTexture, side);
        this.cubeMapTexture = cubeMapTexture;
        this.side = side;
    }

    public int getId(){
        return cubeMapTexture.getId();
    }

    @NotNull
    public DynamicCubeMapTexture getCubeMapTexture(){
        return cubeMapTexture;
    }

    @NotNull
    public CubeMapTexture.CubeMapSide getSide(){
        return side;
    }

    @NotNull
    @ReadOnly
    @Override
    public Vector2i getSize(){
        return cubeMapTexture.getSize();
    }

    @NotNull
    @Override
    public Texture.TextureInternalFormat getInternalFormat(){
        return cubeMapTexture.getInternalFormat();
    }

    @Override
    public boolean isAllocated(){
        return cubeMapTexture.isAllocated();
    }

    @Override
    public boolean isMultisampled(){
        return cubeMapTexture.isMultisampled();
    }

    @Override
    public int getSampleCount(){
        return cubeMapTexture.getSampleCount();
    }

    @Override
    public boolean isUsable(){
        return cubeMapTexture.isUsable();
    }
}
