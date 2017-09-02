package resources.splines;

import java.util.*;
import org.joml.*;
import toolbox.annotations.*;

/**
 * This class provides a left and a right helper point to a control point. It
 * could be helpful for example for Bezier curves.
 */
public class SplinePoint {

    /**
     * Control point.
     */
    private final Vector3f point = new Vector3f();
    /**
     * Left helper point.
     */
    private final Vector3f left = new Vector3f();
    /**
     * Right helper point.
     */
    private final Vector3f right = new Vector3f();

    /**
     * Initializes a new SplinePoint to the given value.
     *
     * @param point control point
     */
    public SplinePoint(@NotNull Vector3f point) {
        setPoint(point);
    }

    /**
     * Retursn the control point.
     *
     * @return control point
     */
    @NotNull
    public Vector3f getPoint() {
        return point;
    }

    /**
     * Sets the control point to the given value.
     *
     * @param point control point
     */
    public void setPoint(@NotNull Vector3f point) {
        this.point.set(point);
    }

    /**
     * Retursn the left helper point.
     *
     * @return left helper point
     */
    @NotNull
    public Vector3f getLeft() {
        return left;
    }

    /**
     * Sets the left helper point to the given value. It changes the right
     * helper point's position to be symmetric to the left.
     *
     * @param left left helper point
     */
    public void setLeft(@NotNull Vector3f left) {
        this.left.set(left);
        Vector3f direction = point.sub(left, new Vector3f());
        point.add(direction, right);
    }

    /**
     * Retursn the right helper point.
     *
     * @return right helper point
     */
    @NotNull
    public Vector3f getRight() {
        return right;
    }

    /**
     * Sets the right helper point to the given value. It changes the left
     * helper point's position to be symmetric to the right.
     *
     * @param right right helper point
     */
    public void setRight(@NotNull Vector3f right) {
        this.right.set(right);
        Vector3f direction = point.sub(right, new Vector3f());
        point.add(direction, left);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.point);
        hash = 89 * hash + Objects.hashCode(this.left);
        hash = 89 * hash + Objects.hashCode(this.right);
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
        final SplinePoint other = (SplinePoint) obj;
        if (!Objects.equals(this.point, other.point)) {
            return false;
        }
        if (!Objects.equals(this.left, other.left)) {
            return false;
        }
        if (!Objects.equals(this.right, other.right)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SplinePoint{" + "point=" + point + ", left=" + left
                + ", right=" + right + '}';
    }

}
