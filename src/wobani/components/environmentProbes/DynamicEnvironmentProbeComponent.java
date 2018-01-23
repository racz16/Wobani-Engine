package wobani.components.environmentProbes;

import org.joml.*;
import wobani.resources.environmentProbes.*;
import wobani.toolbox.annotations.*;

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

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append(super.toString()).append("\n")
                .append("DynamicEnvironmentProbeComponent(")
                .append(")");
        return res.toString();
    }

}
