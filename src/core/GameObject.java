package core;

import java.util.*;
import org.joml.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * If you would like to create a new entity which has it's own position, mesh or
 * any other properties, you should create a new GameObject. A GameObjct can
 * store any number of Components, including cameras, meshes and materials,
 * splines, light sources etc. Of course you can create youe own Component. It
 * also offers parent-child relations between GameObjects.
 *
 * @see Component
 *
 */
public class GameObject {

    /**
     * GameObject's parent.
     */
    private GameObject parent;
    /**
     * GameObject's root.
     */
    private GameObject root;
    /**
     * List of the GameObject's children.
     */
    private final ArrayList<GameObject> children = new ArrayList<>();
    /**
     * GameObject's name.
     */
    private String name;
    /**
     * GameObject's Transform.
     */
    private Transform transform = new Transform();
    /**
     * List of the GameObject's Components.
     */
    private final ArrayList<Component> components = new ArrayList<>();

    /**
     * Initializes a new GameObject.
     */
    public GameObject() {
        transform.addToGameObject(this);
        root = this;
        Scene.addGameObject(this);
    }

    /**
     * Initializes a new GameObject to the given value.
     *
     * @param name GameObject's name
     */
    public GameObject(@Nullable String name) {
        this();
        setName(name);
    }

    /**
     * Returns the GameObject's name. If it doesn't have name, the method
     * returns the hash code.
     *
     * @return name
     *
     * @see #hashCode()
     */
    @NotNull
    public String getName() {
        return name == null ? String.valueOf(hashCode()) : name;
    }

    /**
     * Sets the GameObject's name to the given value.
     *
     * @param name name
     */
    public void setName(@Nullable String name) {
        this.name = name;
    }

    /**
     * Calls the update method of all the GameObject's Components and
     * GameObject's Transform.
     *
     * @see Component#update()
     * @see Transform#update()
     */
    protected void update() {
        transform.update();
        for (Component component : components) {
            component.update();
        }
    }

    //
    //children------------------------------------------------------------------
    //
    /**
     * Returns the GameObject's indexth child.
     *
     * @param index index
     *
     * @return GameObject's indexth child
     *
     * @see #getNumberOfChildren()
     */
    @NotNull
    public GameObject getChild(int index) {
        return children.get(index);
    }

    /**
     * Determines whether the given GameObject is the child of this GameObject.
     *
     * @param child child
     *
     * @return true if the given GameObject is the child of this GameObject,
     *         false otherwise
     *
     * @see #containsChildDeep(GameObject child)
     */
    public boolean containsChild(@Nullable GameObject child) {
        return Utility.containsReference(children, child);
    }

    /**
     * Determines whether the given GameObject is the child of this GameObject
     * or descendant of it.
     *
     * @param child child
     *
     * @return true if the given GameObject is the child of this GameObject or
     *         descendant of it, false otherwise
     *
     * @see #containsChild(GameObject child)
     */
    public boolean containsChildDeep(@Nullable GameObject child) {
        if (containsChild(child)) {
            return true;
        }
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).containsChildDeep(child)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the specified GameObject from the GameObject's children.
     *
     * @param child child to remove
     *
     * @return true if the children contained the specified element, false
     *         otherwise
     */
    public boolean removeChild(@Nullable GameObject child) {
        if (containsChild(child)) {
            child.setParent(null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the GameObject's indexth child from children.
     *
     * @param index index
     *
     * @return the removed child
     *
     * @see #getNumberOfChildren()
     */
    @NotNull
    public GameObject removeChild(int index) {
        GameObject child = children.get(index);
        child.setParent(null);
        return child;
    }

    /**
     * Removes the given GameObject from children.
     *
     * @param child child
     *
     * @return true if this list contained the specified element, false
     *         otherwise
     */
    private boolean remove(@Nullable GameObject child) {
        boolean ret = Utility.removeReference(children, child);
        if (ret) {
            child.parent = null;
        }
        return ret;
    }

    /**
     * Returns the GameObject's parent.
     *
     * @return parent
     */
    @Nullable
    public GameObject getParent() {
        return parent;
    }

    /**
     * Returns the GameObject's root. If it doesn't have, returns this.
     *
     * @return the GameObject's root
     */
    @NotNull
    public GameObject getRoot() {
        return root;
    }

    /**
     * Sets the given value to this GameObect's root.
     *
     * @param root root
     *
     * @throws NullPointerException root can't be null
     */
    private void setRoot(@NotNull GameObject root) {
        if (root == null) {
            throw new NullPointerException();
        }
        this.root = root;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setRoot(root);
        }
    }

    /**
     * Returns the number of the GameObject's children.
     *
     * @return number of the GameObject's children
     */
    public int getNumberOfChildren() {
        return children.size();
    }

    /**
     * Adds the given GameObject to this GameObject's children. The parameter
     * can't be this, null, the ancestor of this or child of this.
     *
     * @param child add to children
     *
     * @return true if the parameter added successfully to the children, false
     *         otherwise
     */
    public boolean addChild(@NotNull GameObject child) {
        if (child == null || child == this || child.containsChildDeep(this) || containsChild(child)) {
            return false;
        }

        Vector3f absPos = new Vector3f(child.getTransform().getAbsolutePosition());
        Vector3f absRot = new Vector3f(child.getTransform().getAbsoluteRotation());
        Vector3f absScale = new Vector3f(child.getTransform().getAbsoluteScale());

        children.add(child);
        getTransform().addInvalidatable(child.getTransform());

        if (child.getParent() != null) {
            child.getParent().getTransform().removeInvalidatable(child.getTransform());
            child.getParent().remove(child);
        }
        child.parent = this;
        child.setRoot(root);

        child.getTransform().setAbsolutePosition(absPos);
        child.getTransform().setAbsoluteRotation(absRot);
        child.getTransform().setAbsoluteScale(absScale);

        return true;
    }

    /**
     * Sets the given GameObject to this GameObject's parent. The parameter
     * can't be this, the descendant of this or the parent of this.
     *
     * @param parent parent
     *
     * @return true if the parameter set successfully to the parent of this,
     *         false otherwise
     */
    public boolean setParent(@Nullable GameObject parent) {
        if (parent == this || containsChildDeep(parent) || parent == getParent()) {
            return false;
        }
        Vector3f absPos = new Vector3f(getTransform().getAbsolutePosition());
        Vector3f absRot = new Vector3f(getTransform().getAbsoluteRotation());
        Vector3f absScale = new Vector3f(getTransform().getAbsoluteScale());

        if (getParent() != null) {
            this.parent.remove(this);
            this.parent.getTransform().removeInvalidatable(getTransform());
        }
        this.parent = parent;
        if (parent == null) {
            setRoot(this);
        } else {
            setRoot(parent.getRoot());
            parent.children.add(this);
            parent.getTransform().addInvalidatable(getTransform());
        }

        getTransform().setAbsolutePosition(absPos);
        getTransform().setAbsoluteRotation(absRot);
        getTransform().setAbsoluteScale(absScale);

        return true;
    }

    //
    //transform-----------------------------------------------------------------
    //
    /**
     * Returns the GameObject's Transform.
     *
     * @return transform
     */
    @NotNull
    public Transform getTransform() {
        return transform;
    }

    /**
     * Sets the GameObject's Transform to the given value.
     *
     * @param transform transform
     *
     * @throws IllegalArgumentException you can't assign a Transform to multiple
     *                                  GameObjects
     */
    public void setTransform(@NotNull Transform transform) {
        if (transform.getGameObject() != null) {
            throw new IllegalArgumentException("You can't assign a Transform to multiple GameObjects");
        }
        this.transform.removeFromGameObject();
        transform.addToGameObject(this);
        this.transform = transform;
    }

    //
    //components----------------------------------------------------------------
    //
    /**
     * Adds the given Component to this GameObject.
     *
     * @param component component
     *
     * @return false if the GameObject already contained the given Component,
     *         true otherwise
     *
     * @throws NullPointerException     Component can't be null
     * @throws IllegalArgumentException you can't assign a Component to multiple
     *                                  GameObjects
     */
    public boolean addComponent(@NotNull Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        if (Utility.containsReference(components, component)) {
            return false;
        }
        if (component.getGameObject() != null) {
            throw new IllegalArgumentException("You can't assign a Component to multiple GameObjects");
        }
        component.addToGameObject(this);
        components.add(component);
        return true;
    }

    /**
     * Removes the given Component from the GameObject's components.
     *
     * @param component component
     *
     * @return true if the GameObject's components contained the given
     *         Component, false otherwise
     */
    public boolean removeComponent(@Nullable Component component) {
        if (component == Scene.getDirectionalLight() || component == Scene.getCamera()) {
            return false;
        }
        boolean result = Utility.removeReference(components, component);
        if (result) {
            component.removeFromGameObject();
        }
        return result;
    }

    /**
     * Removes the GameObject's indexth Component.
     *
     * @param index index
     *
     * @return true if the component removed successfully, false otherwise
     *
     * @see #getNumberOfComponents()
     */
    public boolean removeComponent(int index) {
        Component component = components.get(index);
        if (component == Scene.getDirectionalLight() || component == Scene.getCamera()) {
            return false;
        } else {
            components.remove(index).removeFromGameObject();
            return true;
        }
    }

    /**
     * Removes from the GameObject all the Components that's class is the given
     * type or extends it.
     *
     * @param type type
     */
    public void removeComponents(@NotNull Class<?> type) {
        for (Component comp : components) {
            if (type.isInstance(comp) && comp != Scene.getDirectionalLight() && comp != Scene.getCamera()) {
                comp.removeFromGameObject();
                Utility.removeReference(components, comp);
            }
        }
    }

    /**
     * Removes all the Components from the GameObject.
     * <p>
     */
    public void clearComponents() {
        removeComponents(Component.class);
    }

    /**
     * Returns the GameObject's indexth Component.
     *
     * @param index index
     *
     * @return indexth Component
     *
     * @see #getNumberOfComponents()
     */
    @NotNull
    public Component getComponent(int index) {
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
     * @see #getComponents(Class type)
     */
    @Nullable
    public <T> T getComponent(@NotNull Class<T> type) {
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
     * @see #getComponent(Class type)
     */
    @NotNull
    public <T> List<T> getComponents(@NotNull Class<T> type) {
        List<T> list = new ArrayList<>();
        for (Component comp : components) {
            if (type.isInstance(comp)) {
                list.add(type.cast(comp));
            }
        }
        return list;
    }

    /**
     * Returns the number of Components.
     *
     * @return number of Components
     */
    public int getNumberOfComponents() {
        return components.size();
    }

    //
    //misc----------------------------------------------------------------------
    //
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.children);
        hash = 89 * hash + Objects.hashCode(this.name);
        hash = 89 * hash + Objects.hashCode(this.components);
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
        final GameObject other = (GameObject) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.children, other.children)) {
            return false;
        }
        if (!Objects.equals(this.components, other.components)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String parentName = parent == null ? "null" : parent.getName();
        String rootName = root == this ? "this" : root.getName();
        StringBuilder childrenName = new StringBuilder("[");
        for (GameObject child : children) {
            childrenName.append(child.getName()).append(", ");
        }
        childrenName.append("]");
        StringBuilder componentsName = new StringBuilder("[");
        for (Component component : components) {
            componentsName.append(component).append(", \n");
        }
        componentsName.append("]");
        return "GameObject{" + "parent=" + parentName + ", root=" + rootName
                + ", children=" + childrenName + ", name=" + name
                + ", transform=\n" + transform + ", \ncomponents=\n" + componentsName + '}';
    }

}
