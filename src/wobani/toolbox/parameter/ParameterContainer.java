package wobani.toolbox.parameter;

import wobani.toolbox.annotation.Nullable;
import wobani.toolbox.annotation.NotNull;
import java.util.*;
import wobani.core.*;
import wobani.rendering.*;

/**
 * Contains Parameters for customize for example the Scene or the
 * RenderingPipeline.
 *
 * @see Parameter
 * @see Scene#getParameters()
 * @see RenderingPipeline#getParameters()
 */
public class ParameterContainer {

    /**
     * Stores the Parameters.
     */
    private final Map<Class<?>, ParameterContainerMap<?>> PARAMETERS = new HashMap<>();

    /**
     * Returns the specified Parameter.
     *
     * @param key the Parameter's key
     * @param <T> type of the Parameter's stored value
     *
     * @return the specified Parameter
     */
    @Nullable
    public <T> Parameter<T> get(@NotNull ParameterKey<T> key) {
        ParameterContainerMap<T> pcm = getParameterContainerMap(key);
        if (pcm == null) {
            return null;
        } else {
            return pcm.get(key.getKey());
        }
    }

    /**
     * Returns the specified Parameter's stored value.
     *
     * @param key the Parameter's key
     * @param <T> type of the Parameter's stored value
     *
     * @return the specified Parameter's stored value
     */
    @Nullable
    public <T> T getValue(@NotNull ParameterKey<T> key) {
        Parameter<T> param = get(key);
        return param == null ? null : param.getValue();
    }

    /**
     * Returns the specified Parameter's stored value if it exists, the given
     * default value otherwise.
     *
     * @param key          the Parameter's key
     * @param <T>          type of the Parameter's stored value
     * @param defaultValue default value if the Parameter doesn't exist
     *
     * @return the specified Parameter's stored value if it exists, the given
     *         default value otherwise
     */
    @Nullable
    public <T> T getValueOrDefault(@NotNull ParameterKey<T> key, @Nullable T defaultValue) {
        T value = getValue(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Sets the given value to the specified mapping.
     *
     * @param key       the Parameter's key
     * @param <T>       type of the Parameter's stored value
     * @param parameter Parameter
     *
     * @throws NullPointerException key can't be null
     */
    public <T> void set(@NotNull ParameterKey<T> key, @Nullable Parameter<T> parameter) {
        if (key == null) {
            throw new NullPointerException();
        }
        setWithoutInspection(key, parameter);
    }

    /**
     * Sets the given value to the specified mapping.
     *
     * @param key       the Parameter's key
     * @param <T>       type of the Parameter's stored value
     * @param parameter Parameter
     */
    private <T> void setWithoutInspection(@NotNull ParameterKey<T> key, @Nullable Parameter<T> parameter) {
        ParameterContainerMap<T> pcm = getParameterContainerMap(key);
        if (pcm == null) {
            pcm = new ParameterContainerMap<>();
            PARAMETERS.put(key.getReturnType(), pcm);
        }
        pcm.set(key.getKey(), parameter);
    }

    /**
     * Returns the specified ParameterContainerMap.
     *
     * @param key the Parameter's key
     * @param <T> type of the Parameter's stored value
     *
     * @return the specified ParameterContainerMap
     */
    @Nullable
    private <T> ParameterContainerMap<T> getParameterContainerMap(@NotNull ParameterKey<T> key) {
        return (ParameterContainerMap<T>) PARAMETERS.get(key.getReturnType());
    }

    /**
     * Returns the number of the stored Parameters including nulls.
     *
     * @return the number of the stored Parameters
     */
    public int size() {
        int paramCount = 0;
        for (Class<?> type : PARAMETERS.keySet()) {
            ParameterContainerMap<?> pcm = PARAMETERS.get(type);
            paramCount += pcm.size();
        }
        return paramCount;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.PARAMETERS);
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
        final ParameterContainer other = (ParameterContainer) obj;
        if (!Objects.equals(this.PARAMETERS, other.PARAMETERS)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append("ParameterContainer(")
                .append(" number of Parameters (including nulls): ").append(size())
                .append(")");
        return res.toString();
    }

    /**
     * Stores Parameters with the spcified type of data.
     *
     * @param <S> the type of the Parameters' data
     */
    private class ParameterContainerMap<S> {

        /**
         * Stores Parameters.
         */
        private final Map<String, Parameter<S>> PARAMETERS = new HashMap<>();

        /**
         * Returns the specified Parameter.
         *
         * @param key the Parameter's key
         *
         * @return the specified Parameter
         */
        @Nullable
        public Parameter<S> get(@Nullable String key) {
            return PARAMETERS.get(key);
        }

        /**
         * Sets the given value to the specified mapping.
         *
         * @param key       the Parameter's key
         * @param parameter Parameter
         */
        public void set(@NotNull String key, @Nullable Parameter<S> parameter) {
            if (key == null) {
                throw new NullPointerException();
            }
            Parameter<S> oldValue = get(key);
            PARAMETERS.put(key, parameter);
            addAndRemoveCallbacks(oldValue, parameter);
        }

        /**
         * Calls the add and remove callbacks on the given Parameters, so that
         * they can react to the changes.
         *
         * @param removed removed Parameter
         * @param added   added Parameter
         */
        private void addAndRemoveCallbacks(@Nullable Parameter<S> removed, @Nullable Parameter<S> added) {
            if (removed != null) {
                removed.removedFromParameters(added);
            }
            if (added != null) {
                added.addedToParameters(removed);
            }
        }

        /**
         * Returns the number of the stored Parameters including nulls.
         *
         * @return the number of the stored Parameters
         */
        public int size() {
            return PARAMETERS.size();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 43 * hash + Objects.hashCode(this.PARAMETERS);
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
            final ParameterContainerMap<?> other = (ParameterContainerMap<?>) obj;
            if (!Objects.equals(this.PARAMETERS, other.PARAMETERS)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder res = new StringBuilder()
                    .append("ParameterContainerMap(")
                    .append(" number of Parameters (including nulls): ").append(size())
                    .append(")");
            return res.toString();
        }

    }
}
