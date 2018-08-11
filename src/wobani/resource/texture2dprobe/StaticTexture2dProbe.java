package wobani.resource.texture2dprobe;

import org.joml.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.annotation.*;

public class StaticTexture2dProbe implements Texture2dProbe{

    private StaticTexture2D texture;

    public StaticTexture2dProbe(@NotNull StaticTexture2D texture){
        setTexture(texture);
    }

    @NotNull
    public StaticTexture2D getTexture(){
        return texture;
    }

    public void setTexture(@NotNull StaticTexture2D texture){
        if(texture == null){
            throw new NullPointerException();
        }
        this.texture = texture;
    }

    @Override
    public void bindToTextureUnit(int textureUnit){
        texture.bindToTextureUnit(textureUnit);
    }

    @NotNull
    @ReadOnly
    @Override
    public Vector2i getSize(){
        return texture.getSize();
    }
}
