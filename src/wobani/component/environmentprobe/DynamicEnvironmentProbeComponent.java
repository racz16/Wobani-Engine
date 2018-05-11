package wobani.component.environmentprobe;

import org.joml.*;
import wobani.resources.environmentprobe.*;
import wobani.toolbox.annotation.*;

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
		.append(DynamicEnvironmentProbeComponent.class.getSimpleName()).append("(")
		.append(")");
	return res.toString();
    }

}
