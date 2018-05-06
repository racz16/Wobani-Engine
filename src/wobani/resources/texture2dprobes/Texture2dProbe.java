package wobani.resources.texture2dprobes;

import org.joml.*;

public interface Texture2dProbe {

    public void bindToTextureUnit(int textureUnit);

    public Vector2i getSize();
}
