package toolbox.parameters;

import toolbox.annotations.*;

public abstract class UniqueParameter<T> {

    private T value;

    public UniqueParameter(@NotNull T value) {
        setValue(value);
    }

    @NotNull
    public T getValue() {
        return value;
    }

    protected void setValue(@NotNull T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        this.value = value;
    }

    protected void addedToParameters(@Nullable UniqueParameter<T> removed) {
    }

    protected void removedFromParameters(@Nullable UniqueParameter<T> added) {
    }
}
