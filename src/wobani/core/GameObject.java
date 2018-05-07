package wobani.core;

import wobani.toolbox.annotation.Nullable;
import wobani.toolbox.annotation.NotNull;
import wobani.toolbox.annotation.Internal;
import java.util.*;
import java.util.logging.*;
import org.joml.*;

/**
 * Represents an entity which has it's own position, Mesh or any other
 * properties, you should create a new GameObject. A GameObjct can store any
 * number of Components, including Cameras, Meshes, Splines, Materials, light
 * sources etc. Of course you can create youe own Component. It also offers
 * parent-child relations between GameObjectContainer.
 *
 * @see Component
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
     * GameObject's name.
     */
    private String name;
    /**
     * GameObject's Transform.
     */
    private Transform transform = new Transform();
    /**
     * Contains the GameObject's Components.
     */
    private final ComponentContainer components;
    /**
     * Contains the GameObject's children.
     */
    private final ChildContainer children;
    /**
     * The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(GameObject.class.getName());

    /**
     * Initializes a new GameObject.
     */
    public GameObject() {
	transform.attachToGameObject(this);
	root = this;
	components = new ComponentContainer(this);
	children = new ChildContainer(this);
	Scene.getGameObjects().addGameObject(this);
	LOG.log(Level.FINEST, "GameObject {0} created", getName());
    }

    /**
     * Initializes a new GameObject to the given name.
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
     * Updates all the GameObject's Components and GameObject's Transform.
     *
     * @see Component#update()
     * @see Transform#update()
     */
    @Internal
    protected void update() {
	transform.update();
	components.update();
    }

    //
    //children------------------------------------------------------------------
    //
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
     * Sets the given value to this GameObect's (and all of it's descandent's)
     * root.
     *
     * @param root GameObject
     */
    private void setRoot(@NotNull GameObject root) {
	this.root = root;
	for (int i = 0; i < children.size(); i++) {
	    children.get(i).setRoot(root);
	}
    }

    /**
     * Sets the given parameter to this GameObject's parent. The parameter can't
     * be this, and the descendant of this.
     *
     * @param parent parent
     *
     * @throws IllegalArgumentException parent can't be this and the descendant
     *                                  of this
     */
    public void setParent(@Nullable GameObject parent) {
	if (parent == getParent()) {
	    return;
	}
	if (parent == this || children.containsDeep(parent)) {
	    throw new IllegalArgumentException("Parent can't be this and the descendant of this");
	}
	setParentWithoutInspection(parent);
    }

    /**
     * Sets the given parameter to this GameObject's parent.
     *
     * @param parent parent
     */
    private void setParentWithoutInspection(@Nullable GameObject parent) {
	TransformHolder holder = getCurrentTransformData();
	removeParent();
	addParent(parent);
	setTransform(holder);
	LOG.finest("GameObject parent set");
    }

    /**
     * Saves the Transform's current data to a TransformHolder.
     *
     * @return TransformHolder
     */
    @NotNull
    private TransformHolder getCurrentTransformData() {
	return new TransformHolder(transform.getAbsolutePosition(),
		transform.getAbsoluteRotation(),
		transform.getAbsoluteScale());
    }

    /**
     * Sets the Transform's data to the given value.
     *
     * @param holder TransformHolder
     */
    private void setTransform(@NotNull TransformHolder holder) {
	transform.setAbsolutePosition(holder.getPosition());
	transform.setAbsoluteRotation(holder.getRotation());
	transform.setAbsoluteScale(holder.getScale());
    }

    /**
     * Removes this GameObject from the children of it's parent.
     */
    private void removeParent() {
	if (parent != null) {
	    parent.getChildren().removeChild(this);
	    parent.getTransform().removeInvalidatable(transform);
	    parent = null;
	    setRoot(this);
	}
    }

    /**
     * Adds this GameObject to the children of the given parent.
     *
     * @param parent GameObject
     */
    private void addParent(@Nullable GameObject parent) {
	this.parent = parent;
	if (parent != null) {
	    setRoot(parent.getRoot());
	    parent.getChildren().addChild(this);
	    parent.getTransform().addInvalidatable(getTransform());
	}
    }

    //
    //transform, components, children-------------------------------------------
    //
    /**
     * Returns the GameObject's Transform.
     *
     * @return Transform
     */
    @NotNull
    public Transform getTransform() {
	return transform;
    }

    /**
     * Sets the GameObject's Transform to the given value.
     *
     * @param transform Transform
     *
     * @throws IllegalArgumentException Transform is already attached to a
     *                                  GameObject
     */
    public void setTransform(@NotNull Transform transform) {
	if (transform.getGameObject() != null) {
	    throw new IllegalArgumentException("Transform is already attached to a GameObject");
	}
	this.transform.detacheFromGameObject();
	transform.attachToGameObject(this);
	this.transform = transform;
    }

    /**
     * Returns the GameObject's Components.
     *
     * @return the GameObject's Components
     */
    @NotNull
    public ComponentContainer getComponents() {
	return components;
    }

    /**
     * Returns the GameObject's children.
     *
     * @return the GameObject's children
     */
    @NotNull
    public ChildContainer getChildren() {
	return children;
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
	StringBuilder res = new StringBuilder()
		.append("GameObject(")
		.append(" name: ").append(getName())
		.append(", parent: ").append(parentName)
		.append(",\ntransform: ").append(transform)
		.append(",\nchildren: ").append(getChildren().size())
		.append(",\ncomponents: ").append(components)
		.append(")");
	return res.toString();
    }

    /**
     * Holds a Transform's data.
     */
    private class TransformHolder {

	/**
	 * Position.
	 */
	private final Vector3f position;
	/**
	 * Rotation.
	 */
	private final Vector3f rotation;
	/**
	 * Scale.
	 */
	private final Vector3f scale;

	/**
	 * Initializes a new TransformHolder to the given values.
	 *
	 * @param position position
	 * @param rotation rotation
	 * @param scale    scale
	 *
	 * @throws NullPointerException parameters can't be null
	 */
	public TransformHolder(Vector3f position, Vector3f rotation, Vector3f scale) {
	    if (position == null || rotation == null || scale == null) {
		throw new NullPointerException();
	    }
	    this.position = new Vector3f(position);
	    this.rotation = new Vector3f(rotation);
	    this.scale = new Vector3f(scale);
	}

	/**
	 * Returns the position.
	 *
	 * @return position
	 */
	@NotNull
	public Vector3f getPosition() {
	    return position;
	}

	/**
	 * Returns the rotation.
	 *
	 * @return rotation
	 */
	@NotNull
	public Vector3f getRotation() {
	    return rotation;
	}

	/**
	 * Returns the scale.
	 *
	 * @return scale
	 */
	@NotNull
	public Vector3f getScale() {
	    return scale;
	}

	@Override
	public int hashCode() {
	    int hash = 3;
	    hash = 97 * hash + Objects.hashCode(this.position);
	    hash = 97 * hash + Objects.hashCode(this.rotation);
	    hash = 97 * hash + Objects.hashCode(this.scale);
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
	    final TransformHolder other = (TransformHolder) obj;
	    if (!Objects.equals(this.position, other.position)) {
		return false;
	    }
	    if (!Objects.equals(this.rotation, other.rotation)) {
		return false;
	    }
	    if (!Objects.equals(this.scale, other.scale)) {
		return false;
	    }
	    return true;
	}

	@Override
	public String toString() {
	    StringBuilder res = new StringBuilder()
		    .append("TransformHolder(")
		    .append(" position: ").append(position)
		    .append(", rotation: ").append(rotation)
		    .append(", scale: ").append(scale)
		    .append(")");
	    return res.toString();
	}

    }

}
