package wobani.toolbox.parameters;

import java.util.*;
import wobani.core.*;
import wobani.rendering.*;
import wobani.toolbox.annotations.*;

/**
 * A Parameter stores some kind of a data. With Parameters you can customize the
 * engine's work. For example you can specify the main camera, directional
 * light, audio listener, skybox, gamma value, msaa value etc. Of course you can
 * create your own Parameter and use as you want.
 *
 * @param <T> the data'ss type
 *
 * @see Scene#getParameters()
 * @see RenderingPipeline#getParameters()
 */
public class Parameter<T> {

    /**
     * The stored value.
     */
    private T value;

    /**
     * Initializes a new Parameter to the given value.
     *
     * @param value data you want to store
     */
    public Parameter(@NotNull T value) {
        setValue(value);
    }

    /**
     * Returns the stored data.
     *
     * @return the stored data
     */
    @NotNull
    public T getValue() {
        return value;
    }

    /**
     * Sets the stored data to the given value.
     *
     * @param value data you want to store
     *
     * @throws NullPointerException value can't be null
     */
    public void setValue(@NotNull T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        T old = this.value;
        this.value = value;
        refresh(old);
    }

    /**
     * Called when the stored data changed.
     *
     * @param old previously stored data
     */
    @Internal
    protected void refresh(@NotNull T old) {

    }

    /**
     * Called when the Parameter added to a ParameterContainer.
     *
     * @param removed previously stored Parameter
     */
    @Internal
    protected void addedToParameters(@Nullable Parameter<T> removed) {
    }

    /**
     * Called when the Parameter removed from a ParameterContainer.
     *
     * @param added newly stored Parameter
     */
    @Internal
    protected void removedFromParameters(@Nullable Parameter<T> added) {
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Parameter<?> other = (Parameter<?>) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append("Parameter(")
                .append(" value: ").append(value)
                .append(")");
        return res.toString();
    }
}
