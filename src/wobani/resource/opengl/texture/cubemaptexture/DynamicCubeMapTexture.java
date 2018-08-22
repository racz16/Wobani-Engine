package wobani.resource.opengl.texture.cubemaptexture;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.annotation.*;

public class DynamicCubeMapTexture extends CubeMapTexture{

    public DynamicCubeMapTexture(@NotNull Vector2i size){
        super(new ResourceId());
        createTexture(GL13.GL_TEXTURE_CUBE_MAP, getSampleCount());
        //TODO: datasize now incorrect
        allocateImmutable2D(TextureInternalFormat.RGBA8, size, false);
        setWrap(TextureWrapDirection.WRAP_U, TextureWrap.CLAMP_TO_EDGE);
        setWrap(TextureWrapDirection.WRAP_V, TextureWrap.CLAMP_TO_EDGE);
        //TODO: default wrap to abstract method, override in texture2d and cubemaptexture
        setWrap(TextureWrapDirection.WRAP_U, TextureWrap.CLAMP_TO_EDGE);
        setWrap(TextureWrapDirection.WRAP_V, TextureWrap.CLAMP_TO_EDGE);
    }

    public void setSide(@NotNull CubeMapSide side, @NotNull DynamicTexture2D texture){
        //        GL11.glTexImage2D(side.getCode(), 0, GL11.GL_RGB, size.x, size.y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data[i]);
    }

    //TODO: copy texture2d to one side?

    @Override
    protected String getTypeName(){
        return "Dynamic CubeMap Texture";
    }

    @Override
    public boolean isUsable(){
        return isIdValid();
    }

    @Override
    public int getCacheDataSize(){
        return 0;
    }

    @Override
    public String toString(){
        return super.toString() + "\n" +
                DynamicCubeMapTexture.class.getSimpleName() + "(" + ")";
    }
}
