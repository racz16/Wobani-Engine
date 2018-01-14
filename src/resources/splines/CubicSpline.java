package resources.splines;

import org.joml.*;
import toolbox.annotations.*;

/**
 * This class extends SimpleSpline to it can store a cubic spline based on a 4x4
 * basis matrix.
 * <p>
 */
public abstract class CubicSpline extends SimpleSpline {

    /**
     * The spline's basis matrix.
     */
    protected Matrix4f basisMatrix = new Matrix4f();
    /**
     * Determines the spline's resolution between two control points. The lower
     * the number, the smoother the spline.
     */
    private float step = 0.1f;

    /**
     * Initializes a new a CubicSpline.
     */
    public CubicSpline() {
        super();
    }

    @NotNull
    @Override
    protected float[] computeSplineData() {
        Vector3f aabbMax = new Vector3f();
        Vector3f aabbMin = new Vector3f();
        Float max = 0f;

        int steps = (int) (1.0f / getStep());
        int index = 0;
        Vector3f pos;
        int size = isLoopSpline() ? 3 * steps * getNumberOfControlPoints() : 3 * steps * (getNumberOfControlPoints() - 1) + 3;
        float[] data = new float[size];
        for (int i = 0; i < getNumberOfControlPoints() - 1; i++) {
            for (int j = 0; j < steps; j++) {
                pos = getValue(i, j * getStep());
                data[index++] = pos.x;
                data[index++] = pos.y;
                data[index++] = pos.z;
                //frustum culling
                max = max < pos.length() ? pos.length() : max;
                refreshAabbs(aabbMax, aabbMin, pos);
            }
        }
        if (!isLoopSpline()) {
            pos = getControlPoint(getNumberOfControlPoints() - 1);
            data[index++] = pos.x;
            data[index++] = pos.y;
            data[index++] = pos.z;
            //frustum culling
            max = max < pos.length() ? pos.length() : max;
            refreshAabbs(aabbMax, aabbMin, pos);
        } else {
            for (int j = 0; j < steps; j++) {
                pos = getValue(getNumberOfControlPoints() - 1, j * getStep());
                data[index++] = pos.x;
                data[index++] = pos.y;
                data[index++] = pos.z;
                //frustum culling
                max = max < pos.length() ? pos.length() : max;
                refreshAabbs(aabbMax, aabbMin, pos);
            }
        }

        this.aabbMin.set(aabbMin);
        this.aabbMax.set(aabbMax);
        furthestVertexDistance = max;

        return data;
    }

    /**
     * Refreshes the given AABB accorading to the given position.
     *
     * @param aabbMax AABB's max values to refresh
     * @param aabbMin AABB's min values to refresh
     * @param pos     one of the spline's points
     */
    private void refreshAabbs(@NotNull Vector3f aabbMax, @NotNull Vector3f aabbMin, @NotNull Vector3f pos) {
        for (int i = 0; i < 3; i++) {
            if (pos.get(i) < aabbMin.get(i)) {
                aabbMin.setComponent(i, pos.get(i));
            }
            if (pos.get(i) > aabbMax.get(i)) {
                aabbMax.setComponent(i, pos.get(i));
            }
        }
    }

    /**
     * Returns the step. The lower the number, the smoother the spline.
     *
     * @return step
     */
    public float getStep() {
        return step;
    }

    /**
     * Sets the step to the given value. The lower the number, the smoother the
     * spline.
     *
     * @param step step
     *
     * @throws IllegalArgumentException step must be higher than 0 but it can't
     *                                  be higher than 1
     */
    public void setStep(float step) {
        if (step <= 0 || step > 1) {
            throw new IllegalArgumentException("Step must be higher than 0 but it can't be higher than 1");
        }
        this.step = step;
        valid = false;
    }

    /**
     * Computes the spline's new basis matrix.
     */
    protected abstract void computeBasisMatrix();

    /**
     * Returns the spline's basis matrix.
     *
     * @return the spline's basis matrix
     */
    @ReadOnly @NotNull
    public Matrix4f getBasisMatrix() {
        return new Matrix4f(basisMatrix);
    }

    @Override
    public String toString() {
        return super.toString() + "\nCubicSpline{" + "basisMatrix=" + basisMatrix
                + ", step=" + step + '}';
    }

}
