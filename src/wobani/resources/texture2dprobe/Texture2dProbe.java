package wobani.resources.texture2dprobe;

import org.joml.*;

public interface Texture2dProbe {

    public void bindToTextureUnit(int textureUnit);

    public Vector2i getSize();
}
