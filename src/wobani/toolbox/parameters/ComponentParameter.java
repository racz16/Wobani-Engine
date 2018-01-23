package wobani.toolbox.parameters;

import wobani.components.*;
import wobani.toolbox.annotations.*;

/**
 * Stores a Component as a Parameter.
 *
 * @param <T> stored data's type must extends the IComponent interface
 */
public class ComponentParameter<T extends IComponent> extends Parameter<T> {

    /**
     * Initializes a new ComponentParameter to the given value.
     *
     * @param value data you want to store
     */
    public ComponentParameter(T value) {
        super(value);
    }

    @Override
    public T getValue() {
        T ret = super.getValue();
        if (ret.getGameObject() == null) {
            throw new RuntimeException("Component not attached to any GameObject");
        }
        return ret;
    }

    @Internal
    @Override
    protected void addedToParameters(Parameter<T> removed) {
        if (getValue().getGameObject() == null) {
            throw new NullPointerException();
        }
        getValue().invalidate();
    }

    @Internal
    @Override
    protected void refresh(@NotNull T old) {
        getValue().invalidate();
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append(super.toString()).append("\n")
                .append("ComponentParameter(")
                .append(")");
        return res.toString();
    }
}
