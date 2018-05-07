package wobani.component.environmentprobe;

import wobani.toolbox.annotation.NotNull;
import wobani.resources.environmentprobe.EnvironmentProbe;
import java.util.*;
import wobani.core.*;

/**
 * Base class which contains an EnvironmentProbe.
 *
 * @param <T> type
 */
public abstract class EnvironmentProbeComponent<T extends EnvironmentProbe> extends Component {

    /**
     * The EnvironmentProbe.
     */
    private T probe;

    /**
     * Initializes a new EnvironmentProbeComponent to the given value.
     *
     * @param probe EnvironmentProbe
     *
     */
    public EnvironmentProbeComponent(@NotNull T probe) {
        setProbe(probe);
    }

    /**
     * Returns the EnvironmentProbe.
     *
     * @return the EnvironmentProbe
     */
    @NotNull
    public T getProbe() {
        return probe;
    }

    /**
     * Sets the EnvironmentProbe to the given value.
     *
     * @param probe EnvironmentProbe
     *
     * @throws NullPointerException parameter can't be null
     */
    public void setProbe(@NotNull T probe) {
        if (probe == null) {
            throw new NullPointerException();
        }
        this.probe = probe;
    }

    @Override
    public int hashCode() {
        int hash = 3 + super.hashCode();
        hash = 61 * hash + Objects.hashCode(this.probe);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final EnvironmentProbeComponent<?> other = (EnvironmentProbeComponent<?>) obj;
        if (!Objects.equals(this.probe, other.probe)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append(super.toString()).append("\n")
                .append("EnvironmentProbeComponent(")
                .append(" probe: ").append(probe)
                .append(")");
        return res.toString();
    }

}
