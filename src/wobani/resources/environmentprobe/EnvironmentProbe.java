package wobani.resources.environmentprobe;

import org.joml.*;

public interface EnvironmentProbe {

    public void bindToTextureUnit(int textureUnit);

    public Vector2i getSize();
}
