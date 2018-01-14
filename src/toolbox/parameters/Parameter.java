package toolbox.parameters;

import toolbox.annotations.*;

public class Parameter<T> {

    private T value;

    public Parameter(@NotNull T value) {
        setValue(value);
    }

    @NotNull
    public T getValue() {
        return value;
    }

    public void setValue(@NotNull T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        this.value = value;
    }

    public static <T> T getValueOrDefault(@Nullable Parameter<T> param, @Nullable T defaultValue) {
        return param == null ? defaultValue : param.getValue();
    }

    protected void addedToParameters(@Nullable Parameter<T> removed) {
    }

    protected void removedFromParameters(@Nullable Parameter<T> added) {
    }
}
