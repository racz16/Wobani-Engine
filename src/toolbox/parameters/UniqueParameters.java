package toolbox.parameters;

import java.util.*;
import toolbox.annotations.*;

public class UniqueParameters {

    private final Map<Class<? extends UniqueParameter<?>>, UniqueParameter<?>> PARAMETERS = new HashMap<>();

    private <T> void addRemove(@Nullable UniqueParameter<T> removed, @Nullable UniqueParameter<T> added) {
        if (removed != null) {
            removed.removedFromParameters(added);
        }
        if (added != null) {
            added.addedToParameters(removed);
        }
    }

    public <T> boolean contains(@NotNull Class<? extends UniqueParameter<T>> key) {
        return PARAMETERS.containsKey(key);
    }

    public <T> void setParameter(@NotNull UniqueParameter<T> param) {
        Class<UniqueParameter<T>> key = (Class<UniqueParameter<T>>) param.getClass();
        put(key, param);
    }

    public <T> void removeParameter(@NotNull Class<? extends UniqueParameter<T>> key) {
        put(key, null);
    }

    private <T> void put(@NotNull Class<? extends UniqueParameter<T>> key, @Nullable UniqueParameter<T> param) {
        UniqueParameter<T> oldValue = getParameter(key);
        PARAMETERS.put(key, param);
        addRemove(oldValue, param);
    }

//    @Nullable
//    public <T> UniqueParameter<T> getParameter(@NotNull Class<? extends UniqueParameter<?>> key) {
//        return (UniqueParameter<T>) PARAMETERS.get(key);
//    }
    @Nullable
    public <T extends UniqueParameter<?>> T getParameter(@NotNull Class<? extends UniqueParameter<?>> key) {
        return (T) PARAMETERS.get(key);
    }

    @NotNull
    public Set<Class<? extends UniqueParameter<?>>> getParameterKeys() {
        return PARAMETERS.keySet();
    }

}
