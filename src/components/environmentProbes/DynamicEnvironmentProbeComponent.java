package components.environmentProbes;

import org.joml.*;
import resources.environmentProbes.*;

public class DynamicEnvironmentProbeComponent extends EnvironmentProbeComponent<DynamicEnvironmentProbe> {

    public DynamicEnvironmentProbeComponent(DynamicEnvironmentProbe probe) {
        super(probe);
    }

    @Override
    public void update() {
        getProbe().setPosition(getGameObject() == null ? new Vector3f() : getGameObject().getTransform().getAbsolutePosition());
    }

}
