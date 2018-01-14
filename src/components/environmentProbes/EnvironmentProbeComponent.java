package components.environmentProbes;

import core.*;
import resources.environmentProbes.*;
import toolbox.annotations.*;

public abstract class EnvironmentProbeComponent<T extends EnvironmentProbe> extends Component {

    private T probe;

    public EnvironmentProbeComponent(@NotNull T probe) {
        setProbe(probe);
    }

    @NotNull
    public T getProbe() {
        return probe;
    }

    public void setProbe(@NotNull T probe) {
        if (probe == null) {
            throw new NullPointerException();
        }
        this.probe = probe;
    }

}
