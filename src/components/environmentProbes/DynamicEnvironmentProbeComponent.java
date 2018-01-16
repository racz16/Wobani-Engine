package components.environmentProbes;

import org.joml.*;
import resources.environmentProbes.*;
import toolbox.annotations.*;

/**
 * Stores a DynamicEnvironmentProbe.
 */
public class DynamicEnvironmentProbeComponent extends EnvironmentProbeComponent<DynamicEnvironmentProbe> {

    /**
     * Initializes a new DynamicEnvironmentProbeComponent to the given value.
     *
     * @param probe DynamicEnvironmentProbe
     */
    public DynamicEnvironmentProbeComponent(@NotNull DynamicEnvironmentProbe probe) {
        super(probe);
    }

    @Override
    public void update() {
        Vector3f position = getGameObject() == null ? new Vector3f() : getGameObject().getTransform().getAbsolutePosition();
        getProbe().setPosition(position);
    }

}
