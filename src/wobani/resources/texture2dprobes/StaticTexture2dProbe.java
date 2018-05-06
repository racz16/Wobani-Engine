package wobani.resources.texture2dprobes;

import wobani.resources.textures.texture2d.StaticTexture2D;
import org.joml.*;
import wobani.toolbox.annotations.*;

public class StaticTexture2dProbe implements Texture2dProbe {

    private StaticTexture2D texture;

    public StaticTexture2dProbe(@NotNull StaticTexture2D texture) {
        setTexture(texture);
    }

    @NotNull
    public StaticTexture2D getTexture() {
        return texture;
    }

    public void setTexture(@NotNull StaticTexture2D texture) {
        if (texture == null) {
            throw new NullPointerException();
        }
        this.texture = texture;
    }

    @Override
    public void bindToTextureUnit(int textureUnit) {
        texture.bindToTextureUnit(textureUnit);
    }

    @NotNull @ReadOnly
    @Override
    public Vector2i getSize() {
        return texture.getSize();
    }
}
