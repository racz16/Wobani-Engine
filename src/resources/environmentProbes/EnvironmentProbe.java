package resources.environmentProbes;

import org.joml.*;

public interface EnvironmentProbe {

    public void update();

    public void bindToTextureUnit(int textureUnit);

    public void setPosition(Vector3f position);
}
