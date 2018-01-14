package core;

import java.util.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * Contains all of a GameObject's Components.
 */
public class GameObjectComponents {

    /**
     * List of the GameObject's Components.
     */
    private final List<Component> components = new ArrayList<>();
    /**
     * The class contains this GameObject's Components.
     */
    private final GameObject gameObject;

    /**
     * Initializes a new GameObjectComponents to the given value.
     *
     * @param gameObject GameObject
     */
    public GameObjectComponents(@NotNull GameObject gameObject) {
        if (gameObject == null) {
            throw new NullPointerException();
        }
        this.gameObject = gameObject;
    }

    /**
     * Updates all Components.
     */
    @Internal
    void update() {
        for (Component component : components) {
            component.update();
        }
    }

    /**
     * Adds the given Component to the GameObject's Components.
     *
     * @param component Component you want to add
     *
     * @throws NullPointerException Component can't be null
     * @throws AttachmentException  you can't assign a Component to multiple
     *                              GameObjects
     */
    public void add(@NotNull Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        if (component.getGameObject() != null) {
            throw new AttachmentException(gameObject, component);
        }
        addIfNotContained(component);
    }

    /**
     * Adds the given Component to the GameObject's Components if it's not
     * already in it.
     *
     * @param component Component
     */
    private void addIfNotContained(@NotNull Component component) {
        if (!Utility.containsReference(components, component)) {
            component.attachToGameObject(gameObject);
            components.add(component);
        }
    }

    /**
     * Removes the given Component from the GameObject's Components.
     *
     * @param component Component you want to remove
     */
    public void remove(@Nullable Component component) {
        boolean result = Utility.removeReference(components, component);
        if (result) {
            component.detachFromGameObject();
        }
    }

    /**
     * Removes the GameObject's indexth Component.
     *
     * @param index index
     *
     * @see #size()
     */
    public void remove(int index) {
        components.remove(index).detachFromGameObject();
    }

    /**
     * Removes from the GameObject all the Components that's class is the given
     * type or extends it.
     *
     * @param type type
     */
    public void remove(@NotNull Class<?> type) {
        for (Component comp : components) {
            if (type.isInstance(comp)) {
                comp.detachFromGameObject();
                Utility.removeReference(components, comp);
            }
        }
    }

    /**
     * Removes all the Components from the GameObject.
     */
    public void clear() {
        remove(Component.class);
    }

    /**
     * Returns the GameObject's indexth Component.
     *
     * @param index index
     *
     * @return indexth Component
     *
     * @see #size()
     */
    @NotNull
    public Component get(int index) {
        return components.get(index);
    }

    /**
     * Returns one of the GameObject's Components that's class is the given type
     * or extends it.
     *
     * @param <T>  type
     * @param type type
     *
     * @return one of the GameObject's Components that's class is the given type
     *         or extends it
     *
     * @see #getAll(Class)
     */
    @Nullable
    public <T> T getOne(@NotNull Class<T> type) {
        for (Component comp : components) {
            if (type.isInstance(comp)) {
                return (T) comp;
            }
        }
        return null;
    }

    /**
     * Returns a list containing all the GameObject's Components that's class is
     * the given type or extends it. If there's no such a Component, it returns
     * an empty list.
     *
     * @param <T>  type
     * @param type type
     *
     * @return a list containing all the GameObject's Components that's class is
     *         the given type or extends it
     *
     * @see #getOne(Class)
     */
    @NotNull @ReadOnly
    public <T> List<T> getAll(@NotNull Class<T> type) {
        List<T> list = new ArrayList<>();
        for (Component comp : components) {
            if (type.isInstance(comp)) {
                list.add(type.cast(comp));
            }
        }
        return list;
    }

    /**
     * Returns the number of Components attached to the GameObject.
     *
     * @return number of Components
     */
    public int size() {
        return components.size();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.components);
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
        final GameObjectComponents other = (GameObjectComponents) obj;
        if (!Objects.equals(this.components, other.components)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Components{" + "components=" + components + ", gameObject=" + gameObject.getName() + '}';
    }

}