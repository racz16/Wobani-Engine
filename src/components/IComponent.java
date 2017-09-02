package components;

import core.*;
import toolbox.annotations.*;

/**
 * Makes possible that Component's interface can act as Components.
 */
public interface IComponent {

    /**
     * Adds the given Invalidatable to the list of invalidatables.
     *
     * @param invalidatable invalidatable
     * @return true if the given parameter added successfully (the parametern
     * isn't already in the list and if it isn't this Component), false
     * otherwise
     *
     * @throws NullPointerException can't add null to the list of invalidatables
     */
    public boolean addInvalidatable(@NotNull Invalidatable invalidatable);

    /**
     * Returns true if the list of invalidatables contains the specified
     * element.
     *
     * @param invalidatable invalidatable
     * @return true if the list of invalidatables contains the specified
     * element, false otherwise
     */
    public boolean containsInvalidatable(@Nullable Invalidatable invalidatable);

    /**
     * Removes the parameter from the list of invalidatables.
     *
     * @param invalidatable invalidatable
     */
    public void removeInvalidatable(@Nullable Invalidatable invalidatable);

    /**
     * Determines whether the Component is active. If it's not, your changes may
     * not affect the end result, and the data you get from this Component may
     * be invalid.
     *
     * @return true if the Component is active, false otherwise
     */
    public boolean isActive();

    /**
     * Sets whether or not the Component is active.
     *
     * @param active true if this Component should be active, false otherwise
     */
    public void setActive(boolean active);

    /**
     * Returns the GameObject that assigned to this Component.
     *
     * @return GameObject gameObject
     */
    @Nullable
    public GameObject getGameObject();

    /**
     * Assigns this Component to the specified GameObject.
     *
     * @param object object
     */
    public void setGameObject(@Nullable GameObject object);

    /**
     * Invalidates this Component's data and the Component's invalidatables'
     * data.
     */
    public void invalidate();

}
