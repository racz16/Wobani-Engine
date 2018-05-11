package wobani.core;

import java.util.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 * Stores all the GameObjects.
 */
public class GameObjectContainer {

    /**
     * Contains all the GameObjects.
     */
    private final List<GameObject> OBJECTS = new ArrayList<>();

    /**
     * Updates all GameObject's Components.
     */
    @Internal
    void updateComponents() {
	for (GameObject gameObject : OBJECTS) {
	    gameObject.update();
	}
    }

    /**
     * Returns the number of the GameObjectContainer.
     *
     * @return the number of the GameObjectContainer
     */
    public int size() {
	return OBJECTS.size();
    }

    /**
     * Adds the given GameObject to the list of GameObjectContainer.
     *
     * @param gameObject GameObject you want to add
     *
     * @throws NullPointerException gameObject can't be null
     */
    @Internal
    void addGameObject(@NotNull GameObject gameObject) {
	if (gameObject == null) {
	    throw new NullPointerException();
	}
	if (!Utility.containsReference(OBJECTS, gameObject)) {
	    OBJECTS.add(gameObject);
	}
    }

    /**
     * Returns the specified GameObject.
     *
     * @param index index
     *
     * @return specified GameObject
     */
    @NotNull
    public GameObject getGameObject(int index) {
	return OBJECTS.get(index);
    }

    @Override
    public int hashCode() {
	int hash = 5;
	hash = 37 * hash + Objects.hashCode(this.OBJECTS);
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
	final GameObjectContainer other = (GameObjectContainer) obj;
	if (!Objects.equals(this.OBJECTS, other.OBJECTS)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	StringBuilder res = new StringBuilder()
		.append(GameObjectContainer.class.getSimpleName()).append("(")
		.append(" size: ").append(OBJECTS.size())
		.append(")");
	return res.toString();
    }

}
