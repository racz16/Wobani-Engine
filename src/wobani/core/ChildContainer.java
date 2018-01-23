package wobani.core;

import java.util.*;
import wobani.toolbox.*;
import wobani.toolbox.annotations.*;

/**
 * Contains a GameObject's children.
 */
public class ChildContainer {

    /**
     * List of the GameObject's children.
     */
    private final List<GameObject> children = new ArrayList<>();
    /**
     * The class contains this GameObject's children.
     */
    private final GameObject gameObject;

    /**
     * Initializes a new GameObjectChildren to the given value.
     *
     * @param gameObject GameObject
     *
     * @throws NullPointerException parameter can't be null
     */
    public ChildContainer(@NotNull GameObject gameObject) {
        if (gameObject == null) {
            throw new NullPointerException();
        }
        this.gameObject = gameObject;
    }

    /**
     * Returns the GameObject's indexth child.
     *
     * @param index index
     *
     * @return GameObject's indexth child
     *
     * @see #size()
     */
    @NotNull
    public GameObject get(int index) {
        return children.get(index);
    }

    /**
     * Determines whether the given parameter is the child of the GameObject.
     *
     * @param child child
     *
     * @return true if the given parameter is the child of the GameObject, false
     *         otherwise
     *
     * @see #containsDeep(GameObject)
     */
    public boolean contains(@Nullable GameObject child) {
        return Utility.containsReference(children, child);
    }

    /**
     * Determines whether the given parameter is the child of the GameObject or
     * the descendant of it.
     *
     * @param child child
     *
     * @return true if the given parameter is the child of the GameObject or the
     *         descendant of it, false otherwise
     *
     * @see #contains(GameObject)
     */
    public boolean containsDeep(@Nullable GameObject child) {
        if (contains(child)) {
            return true;
        }
        return containsNonChildDescendant(child);
    }

    /**
     * Returns true if the given parameter is the descendant of the GameObject
     * but not the child of it.
     *
     * @param child GameObject
     *
     * @return true if the given parameter is the descendant of the GameObject
     *         but not the child of it, false otherwise
     */
    private boolean containsNonChildDescendant(@Nullable GameObject child) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getChildren().containsDeep(child)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the given parameter from the GameObject.
     *
     * @param child child to remove
     */
    public void remove(@Nullable GameObject child) {
        if (contains(child)) {
            child.setParent(null);
        }
    }

    /**
     * Removes the GameObject's indexth child.
     *
     * @param index index
     *
     * @return the removed child
     *
     * @see #size()
     */
    @NotNull
    public GameObject remove(int index) {
        GameObject child = children.get(index);
        child.setParent(null);
        return child;
    }

    /**
     * Removes the given GameObject from children. Note that this method doesn't
     * set the child's parent to null, it remains the old parent.
     *
     * @param child child
     */
    @Internal
    void removeChild(@Nullable GameObject child) {
        Utility.removeReference(children, child);
    }

    /**
     * Returns the number of the GameObject's children.
     *
     * @return number of the GameObject's children
     */
    public int size() {
        return children.size();
    }

    /**
     * Adds the given GameObject to the children. Note that this method doesn't
     * set the child's parent to the gameObject, it remains the old parent.
     *
     * @param child child
     */
    @Internal
    void addChild(@NotNull GameObject child) {
        children.add(child);
    }

    /**
     * Adds the given GameObject to the children.
     *
     * @param child GameObject you want to add
     */
    public void add(@NotNull GameObject child) {
        child.setParent(gameObject);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.children);
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
        final ChildContainer other = (ChildContainer) obj;
        if (!Objects.equals(this.children, other.children)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append("GameObjects(");
        for (GameObject go : children) {
            res.append(" ").append(go.getName()).append(",");
        }
        res.append(")");
        return res.toString();
    }

}
