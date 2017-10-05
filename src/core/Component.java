package core;

import java.util.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * All of a GameObject's Components based on this abstract class. The GameLoop
 * call it's update method once a frame, before rendering.
 *
 * @see GameObject
 *
 */
public abstract class Component implements Invalidatable {

    /**
     * The assigned GameObject.
     */
    private GameObject gameObject;
    /**
     * Determines whether the Component is active. If it's not, your changes may
     * not affect the end result, and the data you get from this Component may
     * be invalid.
     */
    private boolean active = true;
    /**
     * List of invalidatables.
     */
    private final List<Invalidatable> invalidatables = new ArrayList<>();
    /**
     * Prevents invalidation mechanism from causing deadlock.
     */
    private boolean invalidatable = true;

    /**
     * Adds the given Invalidatable to the list of invalidatables.
     *
     * @param invalidatable invalidatable
     *
     * @return true if the given parameter added successfully (the parameter
     *         isn't already in the list and if it isn't this Component), false
     *         otherwise
     *
     * @throws NullPointerException can't add null to the list of invalidatables
     */
    public boolean addInvalidatable(@NotNull Invalidatable invalidatable) {
        if (invalidatable == null) {
            throw new NullPointerException();
        }
        if (!containsInvalidatable(invalidatable) && invalidatable != this) {
            invalidatables.add(invalidatable);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the list of invalidatables contains the specified
     * element.
     *
     * @param invalidatable invalidatable
     *
     * @return true if the list of invalidatables contains the specified
     *         element, false otherwise
     */
    public boolean containsInvalidatable(@Nullable Invalidatable invalidatable) {
        return Utility.containsReference(invalidatables, invalidatable);
    }

    /**
     * Removes the parameter from the list of invalidatables.
     *
     * @param invalidatable invalidatable
     */
    public void removeInvalidatable(@Nullable Invalidatable invalidatable) {
        Utility.removeReference(invalidatables, invalidatable);
    }

    /**
     * Determines whether the Component is active. If it's not, your changes may
     * not affect the end result, and the data you get from this Component may
     * be invalid.
     *
     * @return true if the Component is active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether or not the Component is active.
     *
     * @param active true if this Component should be active, false otherwise
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the GameObject that assigned to this Component.
     *
     * @return GameObject gameObject
     */
    @Nullable
    public GameObject getGameObject() {
        return gameObject;
    }

    /**
     * Adds this Component to the specified GameObject.
     *
     * @param object this Component's new GameObject
     *
     * @throws NullPointerException object can't be null
     */
    protected void addToGameObject(@NotNull GameObject object) {
        if (object == null) {
            throw new NullPointerException();
        }
        gameObject = object;
        Scene.addComponentToLists(this);
    }

    /**
     * Removes this Component from it's GameObject.
     */
    protected void removeFromGameObject() {
        Scene.removeComponentFromLists(this);
        gameObject = null;
    }

    /**
     * Assigns this Component to the specified GameObject.
     *
     * @param object gameObject
     */
    public void setGameObject(@Nullable GameObject object) {
        if (object == null) {
            if (gameObject != null) {
                gameObject.removeComponent(this);
            }
        } else {
            object.addComponent(this);
        }
    }

    /**
     * This method runs once per frame, before rendering.
     */
    protected void update() {
    }

    /**
     * Invalidates this Component's data and the Component's invalidatables'
     * data.
     */
    @Override
    public void invalidate() {
        if (invalidatable) {
            invalidatable = false;
            for (Invalidatable inv : invalidatables) {
                inv.invalidate();
            }
            invalidatable = true;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.active ? 1 : 0);
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
        final Component other = (Component) obj;
        if (this.active != other.active) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String gameObjectName = gameObject == null ? "null" : gameObject.getName();
        StringBuilder isb = new StringBuilder().append('[');
        for (Invalidatable inv : invalidatables) {
            isb.append(inv.getClass().getSimpleName()).append(", ");
        }
        isb.append(']');

        return "Component{" + "gameObject=" + gameObjectName + ", active=" + active
                + ", invalidatables=" + isb + ", invalidatable=" + invalidatable + '}';
    }

}
