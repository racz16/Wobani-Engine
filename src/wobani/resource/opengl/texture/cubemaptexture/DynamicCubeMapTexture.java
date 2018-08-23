package wobani.resource.opengl.texture.cubemaptexture;

import org.joml.*;
import wobani.resource.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.annotation.*;

public class DynamicCubeMapTexture extends CubeMapTexture{

    public DynamicCubeMapTexture(@NotNull Vector2i size){
        super(new ResourceId(), false);
        //TODO: datasize now incorrect
        allocateImmutable2D(TextureInternalFormat.RGBA8, size, false);
    }

    public void setSide(@NotNull CubeMapSide side, @NotNull DynamicTexture2D texture){
        //TODO: instead of this glCopyImageSubData? both dynamic textures
        //        GL11.glTexImage2D(side.getCode(), 0, GL11.GL_RGB, size.x, size.y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data[i]);
    }

    //TODO: make public clear?

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
