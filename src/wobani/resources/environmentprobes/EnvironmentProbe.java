package wobani.resources.environmentprobes;

import org.joml.*;

public interface EnvironmentProbe {

    public void bindToTextureUnit(int textureUnit);

    public Vector2i getSize();
}
