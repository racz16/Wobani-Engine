package wobani.toolbox.parameter;

import java.util.*;
import wobani.toolbox.annotation.*;

/**
 * The key of a Parameter. It consists of two parts: the key and the return
 * type. This two identifies a Parameter, not only the String key. The return
 * type makes the ParameterContainer type safe.
 *
 * @param <T> Parameter's generic type
 */
public class ParameterKey<T> {

    /**
     * Parameter's return type.
     */
    private final Class<T> returnType;
    /**
     * Parameter's String key.
     */
    private final String key;

    /**
     * Initializes a new ParameterKey to the given values.
     *
     * @param returnType the Parameter's returny type
     * @param key        the Parameter's String key
     *
     * @throws NullPointerException parameters can't be null
     */
    public ParameterKey(@NotNull Class<T> returnType, @NotNull String key) {
	if (returnType == null || key == null) {
	    throw new NullPointerException();
	}
	this.returnType = returnType;
	this.key = key;
    }

    /**
     * Returns the Parameter's return type.
     *
     * @return the Parameter's return type
     */
    @NotNull
    public Class<T> getReturnType() {
	return returnType;
    }

    /**
     * Returns the Parameter's String key.
     *
     * @return the Parameter's String key
     */
    @NotNull
    public String getKey() {
	return key;
    }

    @Override
    public int hashCode() {
	int hash = 5;
	hash = 61 * hash + Objects.hashCode(this.returnType);
	hash = 61 * hash + Objects.hashCode(this.key);
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
	final ParameterKey<?> other = (ParameterKey<?>) obj;
	if (!Objects.equals(this.key, other.key)) {
	    return false;
	}
	if (!Objects.equals(this.returnType, other.returnType)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	StringBuilder res = new StringBuilder()
		.append(ParameterKey.class.getSimpleName()).append("(")
		.append(" return type: ").append(returnType)
		.append(", key: ").append(key)
		.append(")");
	return res.toString();
    }

}
