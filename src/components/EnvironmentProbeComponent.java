package components;

import core.*;
import org.joml.*;
import resources.environmentProbes.*;
import toolbox.annotations.*;

public class EnvironmentProbeComponent extends Component {

    private EnvironmentProbe probe;

    public EnvironmentProbeComponent(@NotNull EnvironmentProbe probe) {
        setProbe(probe);
    }

    @NotNull
    public EnvironmentProbe getProbe() {
        return probe;
    }

    public void setProbe(@NotNull EnvironmentProbe probe) {
        if (probe == null) {
            throw new NullPointerException();
        }
        this.probe = probe;
    }

    @Override
    public void update() {
        probe.setPosition(getGameObject() == null ? new Vector3f() : getGameObject().getTransform().getAbsolutePosition());
    }

}
