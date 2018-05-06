package wobani.core;

import java.util.*;
import java.util.logging.*;
import org.joml.*;
import wobani.toolbox.*;
import wobani.toolbox.annotations.*;
import wobani.toolbox.invalidatable.*;

/**
 * Stores a GameObject's transformation data. It stores the position, the
 * rotation, the scale, both absolute and relative values. Also stores the model
 * matrix, the inverse model matrix and the direction vectors. If the GameObject
 * doesn't have parent, it's relative and absolute values are the same. All
 * rotation values are stored in degrees.
 *
 * @see GameObject
 * @see GameObject#setTransform(Transform transform)
 */
public class Transform implements Invalidatable {

    /**
     * The attached GameObject.
     */
    private GameObject gameObject;
    /**
     * Relative position.
     */
    private final Vector3f relativePosition = new Vector3f();
    /**
     * Relative rotation (in degrees).
     */
    private final Vector3f relativeRotation = new Vector3f();
    /**
     * Relative scale.
     */
    private final Vector3f relativeScale = new Vector3f(1);
    /**
     * Absolute position.
     */
    private final Vector3f absolutePosition = new Vector3f();
    /**
     * Absolute rotation (in degrees).
     */
    private final Vector3f absoluteRotation = new Vector3f();
    /**
     * Absolute scale.
     */
    private final Vector3f absoluteScale = new Vector3f();
    /**
     * Model matrix.
     */
    private final Matrix4f modelMatrix = new Matrix4f();
    /**
     * Model matrix's inverse.
     */
    private final Matrix4f inverseModelMatrix = new Matrix4f();
    /**
     * Forward direction vector (normalized).
     */
    private final Vector3f forward = new Vector3f();
    /**
     * Right direction vector (normalized).
     */
    private final Vector3f right = new Vector3f();
    /**
     * Up direction vector (normalized).
     */
    private final Vector3f up = new Vector3f();
    /**
     * Determines whether this Transform's data is valid.
     */
    private boolean valid;
    /**
     * Contains the Transform's Invalidatables.
     */
    private final InvalidatableContainer<Transform> invalidatables = new InvalidatableContainer<>(this);
    /**
     * The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(Transform.class.getName());

    /**
     * Initializes a new Transform.
     */
    public Transform() {
    }

    /**
     * Initializes a new Transform to the given values.
     *
     * @param position position
     * @param rotation rotation (in degrees)
     * @param scale    scale
     */
    public Transform(@NotNull Vector3f position, @NotNull Vector3f rotation, @NotNull Vector3f scale) {
        setRelativePosition(position);
        setRelativeRotation(rotation);
        setRelativeScale(scale);
    }

    /**
     * Returns the relative position.
     *
     * @return relative position
     */
    @NotNull @ReadOnly
    public Vector3f getRelativePosition() {
        return new Vector3f(relativePosition);
    }

    /**
     * Sets the relative position to the given value.
     *
     * @param position relative position
     */
    public void setRelativePosition(@NotNull Vector3f position) {
        relativePosition.set(position);
        invalidate();
    }

    /**
     * Returns the absolute position.
     *
     * @return absolute position
     */
    @NotNull @ReadOnly
    public Vector3f getAbsolutePosition() {
        refresh();
        return new Vector3f(absolutePosition);
    }

    /**
     * Sets the absolute position to the given value.
     *
     * @param position absolute position
     */
    public void setAbsolutePosition(@NotNull Vector3f position) {
        if (haveParent()) {
            Vector3f relPos = computeRelativePositionFromAbsolutePosition(position);
            setRelativePosition(relPos);
        } else {
            setRelativePosition(position);
        }
    }

    /**
     * Computes the realtive position based on the given absolute position.
     *
     * @param absolutePosition absolute position
     *
     * @return relative position
     */
    @NotNull
    private Vector3f computeRelativePositionFromAbsolutePosition(@NotNull Vector3f absolutePosition) {
        Vector3f parentPosition = new Vector3f(gameObject.getParent().getTransform().getAbsolutePosition());
        Vector3f parentRotation = new Vector3f(gameObject.getParent().getTransform().getAbsoluteRotation());
        Quaternionf rotation = new Quaternionf().rotation(
                Utility.toRadians(-parentRotation.x), Utility.toRadians(-parentRotation.y), Utility.toRadians(-parentRotation.z));
        Vector3f relPos = new Vector3f();
        absolutePosition.sub(parentPosition, relPos);
        return relPos.rotate(rotation);
    }

    /**
     * Moves the GameObject by the given movement.
     *
     * @param movement movement
     */
    public void move(@NotNull Vector3f movement) {
        relativePosition.add(movement);
        invalidate();
    }

    /**
     * Returns the relative rotation.
     *
     * @return relative rotation (in degrees)
     */
    @NotNull @ReadOnly
    public Vector3f getRelativeRotation() {
        return new Vector3f(relativeRotation);
    }

    /**
     * Sets the relative rotation to the given value.
     *
     * @param rotation relative rotation (in degrees)
     */
    public void setRelativeRotation(@NotNull Vector3f rotation) {
        relativeRotation.set(rotation);
        invalidate();
    }

    /**
     * Returns the absolute rotation.
     *
     * @return absolute rotation (in degrees)
     */
    @NotNull @ReadOnly
    public Vector3f getAbsoluteRotation() {
        refresh();
        return new Vector3f(absoluteRotation);
    }

    /**
     * Sets the absolute rotation to the given value.
     *
     * @param rotation absolute rotation (in degrees)
     */
    public void setAbsoluteRotation(@NotNull Vector3f rotation) {
        if (haveParent()) {
            Vector3f relRot = computeRelativeRotationFromAbsoluteRotation(rotation);
            setRelativeRotation(relRot);
        } else {
            setRelativeRotation(rotation);
        }
    }

    /**
     * Computes the realtive rotation based on the given absolute rotation.
     *
     * @param absoluteRotation absolute rotation
     *
     * @return relative rotation
     */
    @NotNull
    private Vector3f computeRelativeRotationFromAbsoluteRotation(@NotNull Vector3f absoluteRotation) {
        return absoluteRotation.sub(gameObject.getParent().getTransform().getAbsoluteRotation());
    }

    /**
     * Rotates the GameObject by the given rotation.
     *
     * @param rotation rotation (in degrees)
     */
    public void rotate(@NotNull Vector3f rotation) {
        relativeRotation.add(rotation);
        invalidate();
    }

    /**
     * Returns the relative scale.
     *
     * @return relative scale
     */
    @NotNull @ReadOnly
    public Vector3f getRelativeScale() {
        return new Vector3f(relativeScale);
    }

    /**
     * Sets the relative scale to the given value.
     *
     * @param scale relative scale
     */
    public void setRelativeScale(@NotNull Vector3f scale) {
        relativeScale.set(scale);
        invalidate();
    }

    /**
     * Returns the absolute scale.
     *
     * @return absolute scale
     */
    @NotNull @ReadOnly
    public Vector3f getAbsoluteScale() {
        refresh();
        return new Vector3f(absoluteScale);
    }

    /**
     * Sets the absolute scale to the given value.
     *
     * @param scale absolute scale
     */
    public void setAbsoluteScale(@NotNull Vector3f scale) {
        if (haveParent()) {
            Vector3f relScale = computeRelativeScaleFromAbsoluteScale(scale);
            setRelativeScale(relScale);
        } else {
            setRelativeScale(scale);
        }
    }

    /**
     * Computes the realtive scale based on the given absolute scale.
     *
     * @param absoluteScale absolute scale
     *
     * @return relative scale
     */
    @NotNull
    private Vector3f computeRelativeScaleFromAbsoluteScale(@NotNull Vector3f absoluteScale) {
        Vector3f parentAbsoluteScale = gameObject.getParent().getTransform().getAbsoluteScale();
        return absoluteScale.div(parentAbsoluteScale);
    }

    /**
     * Returns the model matrix.
     *
     * @return model matrix
     *
     * @see #getInverseModelMatrix()
     */
    @NotNull @ReadOnly
    public Matrix4f getModelMatrix() {
        refresh();
        return new Matrix4f(modelMatrix);
    }

    /**
     * Returns the model matrix's inverse.
     *
     * @return the model matrix's inverse
     *
     * @see #getModelMatrix()
     */
    @NotNull @ReadOnly
    public Matrix4f getInverseModelMatrix() {
        refresh();
        return new Matrix4f(inverseModelMatrix);
    }

    //
    //refreshing----------------------------------------------------------------
    //
    /**
     * Refreshes the data if it's invalid.
     */
    @Internal
    protected void refresh() {
        if (!valid) {
            refreshAbsoluteTransform();
            refreshMatrices();
            refreshDirectionVectors();
            valid = true;
            LOG.finer("Transform refreshed");
        }
    }

    /**
     * Refreshes the model and the inverse model matrices.
     */
    private void refreshMatrices() {
        modelMatrix.set(Utility.computeModelMatrix(absolutePosition, absoluteRotation, absoluteScale));
        inverseModelMatrix.set(Utility.computetInverseModelMatrix(absolutePosition, absoluteRotation, absoluteScale));
    }

    /**
     * Refreshes the absolute position, the absolute rotation and the absolute
     * scale.
     */
    private void refreshAbsoluteTransform() {
        if (!haveParent()) {
            refreshAbsoluteTransformWhenNoParent();
        } else {
            refreshAbsolutePosition();
            refreshAbsoluteRotation();
            refreshAbsoluteScale();
        }
    }

    /**
     * Refreshes the absolute position.
     */
    private void refreshAbsolutePosition() {
        Vector3f position = getRelativePosition();
        Vector3f parentRotation = gameObject.getParent().getTransform().getAbsoluteRotation();
        absolutePosition.set(position.rotate(new Quaternionf()
                .rotation(Utility.toRadians(parentRotation.x), Utility.toRadians(parentRotation.y), Utility.toRadians(parentRotation.z))));
        absolutePosition.add(gameObject.getParent().getTransform().getAbsolutePosition());
    }

    /**
     * Refreshes the absolute rotation.
     */
    private void refreshAbsoluteRotation() {
        Vector3f parentAbsoluteRotation = gameObject.getParent().getTransform().getAbsoluteRotation();
        absoluteRotation.set(parentAbsoluteRotation.add(relativeRotation));
    }

    /**
     * Refreshes the absolute scale.
     */
    private void refreshAbsoluteScale() {
        Vector3f parentAbsoluteScale = gameObject.getParent().getTransform().getAbsoluteScale();
        absoluteScale.set(parentAbsoluteScale.mul(relativeScale));
    }

    /**
     * Refreshes the absolute position, rotation and scale when the attached
     * GameObject is a root. In this case the relative and absolute values are
     * the same.
     */
    private void refreshAbsoluteTransformWhenNoParent() {
        absolutePosition.set(relativePosition);
        absoluteRotation.set(relativeRotation);
        absoluteScale.set(relativeScale);
    }

    //
    //direction vectors---------------------------------------------------------
    //
    /**
     * Returns the forward direction vector.
     *
     * @return forward direction vector (normalized)
     */
    @NotNull @ReadOnly
    public Vector3f getForwardVector() {
        refresh();
        return new Vector3f(forward);
    }

    /**
     * Returns the right direction vector.
     *
     * @return right direction vector (normalized)
     */
    @NotNull @ReadOnly
    public Vector3f getRightVector() {
        refresh();
        return new Vector3f(right);
    }

    /**
     * Returns the up direction vector.
     *
     * @return up direction vector (normalized)
     */
    @NotNull @ReadOnly
    public Vector3f getUpVector() {
        refresh();
        return new Vector3f(up);
    }

    /**
     * Refreshes the forward, right and up direction vectors.
     */
    private void refreshDirectionVectors() {
        Quaternionf rotation = new Quaternionf()
                .rotation(Utility.toRadians(absoluteRotation.x),
                        Utility.toRadians(absoluteRotation.y),
                        Utility.toRadians(absoluteRotation.z));
        forward.set(new Vector3f(0, 0, -1).rotate(rotation).normalize());
        right.set(new Vector3f(1, 0, 0).rotate(rotation).normalize());
        right.cross(forward, up);
    }

    //
    //GameObject related--------------------------------------------------------
    //
    /**
     * This method runs once per frame, before rendering.
     */
    @Internal
    protected void update() {
    }

    /**
     * Detaches the Transform from the GameObject.
     */
    @Internal
    protected void detacheFromGameObject() {
        this.gameObject = null;
        invalidate();
    }

    /**
     * Attaches the Transform to the given GameObject.
     *
     * @param object gameObject
     *
     * @throws NullPointerException object can't be null
     */
    @Internal
    protected void attachToGameObject(@NotNull GameObject object) {
        if (object == null) {
            throw new NullPointerException();
        }
        this.gameObject = object;
        invalidate();
    }

    /**
     * Returns the GameObject that attached to this Transform.
     *
     * @return attached GameObject
     */
    @Nullable
    public GameObject getGameObject() {
        return gameObject;
    }

    /**
     * Attaches this Transform to the specified GameObject.
     *
     * @param object GameObject
     */
    public void setGameObject(@NotNull GameObject object) {
        object.setTransform(this);
    }

    /**
     * Determines whether this Transform have a parent.
     *
     * @return true if the Transform is attached to a GameObject and this
     *         GameObject have a parent, false otherwise
     */
    private boolean haveParent() {
        return gameObject != null && gameObject.getParent() != null;
    }

    //
    //invaliadation-------------------------------------------------------------
    //
    /**
     * Invalidates this Transform's and the Transform's Invalidatables.
     */
    @Override
    public void invalidate() {
        invalidatables.invalidate();
        valid = false;
        LOG.finer("Transform invalidated");
    }

    /**
     * Adds the given Invalidatable to this Transform's Invalidatables.
     *
     * @param invalidatable Invalidatable
     */
    public void addInvalidatable(@NotNull Invalidatable invalidatable) {
        invalidatables.addInvalidatable(invalidatable);
    }

    /**
     * Returns true if the Transform's Invalidatables contains the specified
     * element.
     *
     * @param invalidatable Invalidatable
     *
     * @return true if this Transform's Invalidatables contains the specified
     *         element, false otherwise
     */
    public boolean containsInvalidatable(@Nullable Invalidatable invalidatable) {
        return invalidatables.containsInvalidatable(invalidatable);
    }

    /**
     * Removes the parameter from this Transform's Invalidatables.
     *
     * @param invalidatable Invalidatable
     */
    public void removeInvalidatable(@Nullable Invalidatable invalidatable) {
        invalidatables.removeInvalidatable(invalidatable);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.relativePosition);
        hash = 67 * hash + Objects.hashCode(this.relativeRotation);
        hash = 67 * hash + Objects.hashCode(this.relativeScale);
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
        final Transform other = (Transform) obj;
        if (!Objects.equals(this.relativePosition, other.relativePosition)) {
            return false;
        }
        if (!Objects.equals(this.relativeRotation, other.relativeRotation)) {
            return false;
        }
        if (!Objects.equals(this.relativeScale, other.relativeScale)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append("Transform(")
                .append(" position: ").append(getAbsolutePosition())
                .append(", rotation: ").append(getAbsoluteRotation())
                .append(", scale: ").append(getAbsoluteScale())
                .append(", forward: ").append(getForwardVector())
                .append(")");
        return res.toString();
    }

}
