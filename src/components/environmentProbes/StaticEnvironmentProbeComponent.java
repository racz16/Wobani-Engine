package components.environmentProbes;

import resources.environmentProbes.*;
import toolbox.annotations.*;

/**
 * Stores a StaticEnvironmentProbe.
 */
public class StaticEnvironmentProbeComponent extends EnvironmentProbeComponent<StaticEnvironmentProbe> {

    /**
     * Initializes a new StaticEnvironmentProbeComponent to the given value.
     *
     * @param probe StaticEnvironmentProbe
     */
    public StaticEnvironmentProbeComponent(@NotNull StaticEnvironmentProbe probe) {
        super(probe);
    }

}
