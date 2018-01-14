package core;

import toolbox.annotations.*;
import toolbox.invalidatable.*;

/**
 * All of a GameObject's Components based on this abstract class. The GameLoop
 * call it's update method once a frame, before rendering.
 */
public abstract class Component implements Invalidatable {

    /**
     * The attached GameObject.
     */
    private GameObject gameObject;
    /**
     * Determines whether the Component is active. If it's not, your changes may
     * not affect the end result, and the data you get from this Component may
     * be invalid.
     */
    private boolean active = true;
    /**
     * Contains the Component's Invalidatables.
     */
    private final InvalidatableContainer invalidatables = new InvalidatableContainer(this);

    /**
     * Adds the given Invalidatable to this Component's Invalidatables.
     *
     * @param invalidatable Invalidatable
     */
    public void addInvalidatable(@NotNull Invalidatable invalidatable) {
        invalidatables.addInvalidatable(invalidatable);
    }

    /**
     * Returns true if this Component's Invalidatables contains the specified
     * element.
     *
     * @param invalidatable Invalidatable
     *
     * @return true if this Component's Invalidatables contains the specified
     *         element, false otherwise
     */
    public boolean containsInvalidatable(@Nullable Invalidatable invalidatable) {
        return invalidatables.containsInvalidatable(invalidatable);
    }

    /**
     * Removes the parameter from this Component's Invalidatables.
     *
     * @param invalidatable Invalidatable
     */
    public void removeInvalidatable(@Nullable Invalidatable invalidatable) {
        invalidatables.removeInvalidatable(invalidatable);
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
     * Returns the GameObject that attached to this Component.
     *
     * @return GameObject
     */
    @Nullable
    public GameObject getGameObject() {
        return gameObject;
    }

    /**
     * Attaches this Component to the given GameObject.
     *
     * @param object this Component's new GameObject
     *
     * @throws NullPointerException object can't be null
     */
    @Internal
    protected void attachToGameObject(@NotNull GameObject object) {
        if (object == null) {
            throw new NullPointerException();
        }
        gameObject = object;
        Scene.getComponentLists().addComponentToLists(this);
    }

    /**
     * Detaches this Component from it's GameObject.
     */
    @Internal
    protected void detachFromGameObject() {
        Scene.getComponentLists().removeComponentFromLists(this);
        gameObject = null;
    }

    /**
     * Attaches this Component to the specified GameObject.
     *
     * @param object GameObject
     */
    public void setGameObject(@Nullable GameObject object) {
        if (object == null) {
            if (gameObject != null) {
                gameObject.getComponents().remove(this);
            }
        } else {
            object.getComponents().add(this);
        }
    }

    /**
     * This method runs once per frame, before rendering.
     */
    @Internal
    protected void update() {
    }

    /**
     * Invalidates this Component and the Component's Invalidatables.
     */
    @Override
    public void invalidate() {
        invalidatables.invalidate();
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
        return "Component{" + "gameObject=" + gameObjectName + ", active="
                + active + ", invalidatables=" + invalidatables + '}';
    }

}
