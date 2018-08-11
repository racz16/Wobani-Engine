package wobani.resource.environmentprobe;

import org.joml.*;

public interface EnvironmentProbe{

    public void bindToTextureUnit(int textureUnit);

    public Vector2i getSize();

    public boolean isParallaxCorrection();

    public float getParallaxCorrectionValue();

    public Vector3f getPosition();
}
