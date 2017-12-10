package core;

import com.sun.nio.sctp.*;
import java.util.*;
import org.joml.*;
import toolbox.*;
import toolbox.annotations.*;

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
//TODO lookat quaternions
//store only model matrix and get all data from it?
public class Transform implements Invalidatable {

    /**
     * The assigned GameObject.
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
     * List of invalidatables.
     */
    private final List<Invalidatable> invalidatables = new ArrayList<>();
    /**
     * Prevents invalidation mechanism from causing deadlock.
     */
    private boolean invalidatable = true;

    private BillboardingMode billboardingMode = BillboardingMode.NO_BILLBOARDING;
    private final Vector3f billboardingAxis = new Vector3f(0, 1, 0);

    public enum BillboardingMode {
        NO_BILLBOARDING,
        CYLINDRICAL_BILLBOARDING,
        SPHERICAL_BILLBOARDING;
    }

    @NotNull
    public BillboardingMode getBillboardingMode() {
        return billboardingMode;
    }

    public void setBillboardingMode(@NotNull BillboardingMode billboardingMode) {
        if (billboardingMode == null) {
            throw new NullPointerException();
        }
        this.billboardingMode = billboardingMode;
    }

    @NotNull @ReadOnly
    public Vector3f getBillboardingAxis() {
        return new Vector3f(billboardingAxis);
    }

    public void setBillboardingAxis(@NotNull Vector3f billboardingAxis) {
        if (billboardingAxis.x() == 0 && billboardingAxis.y() == 0 && billboardingAxis.z() == 0) {
            throw new IllegalReceiveException("Axis can't be nullvector");
        }
        billboardingAxis.set(billboardingAxis).normalize();
    }

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
     *
     * @throws NullPointerException position can't be null
     */
    public void setAbsolutePosition(@NotNull Vector3f position) {
        if (position == null) {
            throw new NullPointerException();
        }
        if (gameObject != null && gameObject.getParent() != null) {
            Vector3f parentPosition = new Vector3f(gameObject.getParent().getTransform().getAbsolutePosition());
            Vector3f parentRotation = new Vector3f(gameObject.getParent().getTransform().getAbsoluteRotation());
            Quaternionf rotation = new Quaternionf().rotation(
                    Utility.toRadians(-parentRotation.x),
                    Utility.toRadians(-parentRotation.y),
                    Utility.toRadians(-parentRotation.z));
            Vector3f relPos = new Vector3f();
            position.sub(parentPosition, relPos);
            setRelativePosition(relPos.rotate(rotation));
        } else {
            setRelativePosition(position);
        }
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
     *
     * @throws NullPointerException rotation can't be null
     */
    public void setAbsoluteRotation(@NotNull Vector3f rotation) {
        if (rotation == null) {
            throw new NullPointerException();
        }
        if (gameObject != null && gameObject.getParent() != null) {
            rotation.sub(gameObject.getParent().getTransform().getAbsoluteRotation(), relativeRotation);
            invalidate();
        } else {
            setRelativeRotation(rotation);
        }
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
     *
     * @throws NullPointerException scale can't be null
     */
    public void setAbsoluteScale(@NotNull Vector3f scale) {
        if (scale == null) {
            throw new NullPointerException();
        }
        if (gameObject != null && gameObject.getParent() != null) {
            Vector3f parentAbsoluteScale = gameObject.getParent().getTransform().getAbsoluteScale();
            scale.div(parentAbsoluteScale, relativeScale);
            invalidate();
        } else {
            setRelativeScale(scale);
        }
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
        if (getBillboardingMode() == BillboardingMode.NO_BILLBOARDING) {
            return new Matrix4f(modelMatrix);
        } else if (billboardingMode == BillboardingMode.CYLINDRICAL_BILLBOARDING) {
            Vector3f cameraPosition = Scene.getCamera().getGameObject().getTransform().getAbsolutePosition();
            return new Matrix4f().billboardCylindrical(absolutePosition, cameraPosition, billboardingAxis).scale(absoluteScale);
        } else {
            Vector3f cameraPosition = Scene.getCamera().getGameObject().getTransform().getAbsolutePosition();
            return new Matrix4f().billboardSpherical(absolutePosition, cameraPosition).scale(absoluteScale);
        }
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
        if (getBillboardingMode() == BillboardingMode.NO_BILLBOARDING) {
            return new Matrix4f(inverseModelMatrix);
        } else if (billboardingMode == BillboardingMode.CYLINDRICAL_BILLBOARDING) {
            Vector3f cameraPosition = Scene.getCamera().getGameObject().getTransform().getAbsolutePosition();
            return new Matrix4f().billboardCylindrical(absolutePosition, cameraPosition, billboardingAxis).invert();
        } else {
            Vector3f cameraPosition = Scene.getCamera().getGameObject().getTransform().getAbsolutePosition();
            return new Matrix4f().billboardSpherical(absolutePosition, cameraPosition).invert();
        }
    }

    /**
     * Refreshes the data if it's invalid.
     */
    protected void refresh() {
        if (!valid) {
            refreshAbsoluteTransform();
            modelMatrix.set(Utility.computeModelMatrix(absolutePosition, absoluteRotation, absoluteScale));
            inverseModelMatrix.set(Utility.computetInverseModelMatrix(new Vector3f(0), absoluteRotation, absoluteScale));
            refreshDirectionVectors();
            valid = true;
        }
    }

    /**
     * Invalidates this Transform's data and the Transform's invalidatables'
     * data. The Transform will automatically refresh itself when it needed.
     */
    @Override
    public void invalidate() {
        if (invalidatable) {
            invalidatable = false;
            for (Invalidatable inv : invalidatables) {
                inv.invalidate();
            }
            valid = false;
            invalidatable = true;
        }
    }

    /**
     * Refreshes the absolute position, the absolute rotation and the absolute
     * scale.
     */
    private void refreshAbsoluteTransform() {
        if (gameObject == null || gameObject.getParent() == null) {
            absolutePosition.set(relativePosition);
            absoluteRotation.set(relativeRotation);
            absoluteScale.set(relativeScale);
        } else {
            Vector3f position = new Vector3f(relativePosition);
            Vector3f parentRotation = new Vector3f(gameObject.getParent().getTransform().getAbsoluteRotation());
            absolutePosition.set(
                    position.rotate(
                            new Quaternionf()
                                    .rotation(Utility.toRadians(parentRotation.x),
                                            Utility.toRadians(parentRotation.y),
                                            Utility.toRadians(parentRotation.z))));
            absolutePosition.add(gameObject.getParent().getTransform().getAbsolutePosition());
            absoluteRotation.set(parentRotation.add(relativeRotation));
            gameObject.getParent().getTransform().getAbsoluteScale().mul(relativeScale, absoluteScale);
        }
    }

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

    /**
     * This method runs once per frame, before rendering.
     */
    protected void update() {
    }

    /**
     * Removes the Transform from the GameObject.
     */
    protected void removeFromGameObject() {
        this.gameObject = null;
        invalidate();
    }

    /**
     * Adds the Transform to the given GameObject.
     *
     * @param object gameObject
     *
     * @throws NullPointerException object can't be null
     */
    protected void addToGameObject(@NotNull GameObject object) {
        if (object == null) {
            throw new NullPointerException();
        }
        this.gameObject = object;
        invalidate();
    }

    /**
     * Returns the GameObject that assigned to this Transform.
     *
     * @return GameObject gameObject
     */
    @Nullable
    public GameObject getGameObject() {
        return gameObject;
    }

    /**
     * Assigns this Transform to the specified GameObject.
     *
     * @param object gameObject
     */
    public void setGameObject(@NotNull GameObject object) {
        object.setTransform(this);
    }

    /**
     * Adds the given Invalidatable to the list of invalidatables.
     *
     * @param invalidatable invalidatable
     *
     * @return true if the given parameter added successfully (the parameter
     *         isn't already in the list and if it isn't this Transform), false
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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.relativePosition);
        hash = 53 * hash + Objects.hashCode(this.relativeRotation);
        hash = 53 * hash + Objects.hashCode(this.relativeScale);
        hash = 53 * hash + Objects.hashCode(this.billboardingMode);
        hash = 53 * hash + Objects.hashCode(this.billboardingAxis);
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
        if (this.billboardingMode != other.billboardingMode) {
            return false;
        }
        if (!Objects.equals(this.billboardingAxis, other.billboardingAxis)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Transform{" + "gameObject=" + gameObject + ", relativePosition="
                + relativePosition + ", relativeRotation=" + relativeRotation
                + ", relativeScale=" + relativeScale + ", absolutePosition="
                + absolutePosition + ", absoluteRotation=" + absoluteRotation
                + ", absoluteScale=" + absoluteScale + ", modelMatrix=" + modelMatrix
                + ", inverseModelMatrix=" + inverseModelMatrix + ", forward="
                + forward + ", right=" + right + ", up=" + up + ", valid=" + valid
                + ", invalidatables=" + invalidatables + ", invalidatable="
                + invalidatable + ", bilboardingMode=" + billboardingMode
                + ", bilboardingAxis=" + billboardingAxis + '}';
    }

}
