package wobani.component;

import wobani.core.*;
import wobani.toolbox.invalidatable.*;

/**
 * All public methods of the Component class.
 */
public interface ComponentBase extends Invalidatable {

    /**
     * Adds the given Invalidatable to this Component's Invalidatables.
     *
     * @param invalidatable Invalidatable
     */
    public void addInvalidatable(Invalidatable invalidatable);

    /**
     * Returns true if this Component's Invalidatables contains the specified
     * element.
     *
     * @param invalidatable Invalidatable
     *
     * @return true if this Component's Invalidatables contains the specified
     *         element, false otherwise
     */
    public boolean containsInvalidatable(Invalidatable invalidatable);

    /**
     * Removes the parameter from this Component's Invalidatables.
     *
     * @param invalidatable Invalidatable
     */
    public void removeInvalidatable(Invalidatable invalidatable);

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
     * Returns the GameObject that attached to this Component.
     *
     * @return GameObject
     */
    public GameObject getGameObject();

    /**
     * Attaches this Component to the specified GameObject.
     *
     * @param object GameObject
     */
    public void setGameObject(GameObject object);

}
