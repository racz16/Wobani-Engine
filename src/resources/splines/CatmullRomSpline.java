package resources.splines;

import org.joml.*;
import toolbox.annotations.*;

/**
 * Implementation of the Catmull-Rom spline.
 * <p>
 */
public class CatmullRomSpline extends CubicSpline {

    /**
     * Catmull-Rom spline's tension.
     */
    private float tension;

    /**
     * Initializes a new CatmullRomSpline.
     */
    public CatmullRomSpline() {
        this(0.5f);
    }

    /**
     * Initializes a new CatmullRomSpline to the given value.
     *
     * @param tension tension
     */
    public CatmullRomSpline(float tension) {
        super();
        setTension(tension);
    }

    @Nullable
    @Override
    protected Vector3f getValue(int startIndex, float t) {
        if (getNumberOfControlPoints() < getRequiredControlPoints()) {
            return super.getValue(startIndex, t);
        } else {
            Vector4f vec = new Vector4f(t * t * t, t * t, t, 1).mul(basisMatrix);
            Vector3f[] cps = new Vector3f[4];
            for (int i = -1; i < 3; i++) {
                if (isLoopSpline()) {
                    if (startIndex + i == -1) {
                        cps[i + 1] = getControlPoint(getNumberOfControlPoints() - 1);
                    } else if (startIndex + i == getNumberOfControlPoints()) {
                        cps[i + 1] = getControlPoint(0);
                    } else if (startIndex + i == getNumberOfControlPoints() + 1) {
                        cps[i + 1] = getControlPoint(1);
                    } else {
                        cps[i + 1] = getControlPoint(startIndex + i);
                    }
                } else {
                    if (startIndex + i == -1) {
                        cps[i + 1] = getControlPoint(0).add(getControlPoint(0).sub(getControlPoint(1)));
                    } else if (startIndex + i == getNumberOfControlPoints()) {
                        cps[i + 1] = getControlPoint(startIndex + i - 1).add(getControlPoint(startIndex + i - 1).sub(getControlPoint(startIndex + i - 2)));
                    } else {
                        cps[i + 1] = getControlPoint(startIndex + i);
                    }
                }
            }

            Vector4f v1 = new Vector4f(cps[0].x, cps[1].x, cps[2].x, cps[3].x);
            Vector4f v2 = new Vector4f(cps[0].y, cps[1].y, cps[2].y, cps[3].y);
            Vector4f v3 = new Vector4f(cps[0].z, cps[1].z, cps[2].z, cps[3].z);

            return new Vector3f(vec.dot(v1), vec.dot(v2), vec.dot(v3));
        }
    }

    @Override
    protected void computeBasisMatrix() {
        basisMatrix.set(-tension, 2 - tension, tension - 2, tension,
                2 * tension, tension - 3, 3 - 2 * tension, -tension,
                -tension, 0, tension, 0,
                0, 1, 0, 0);
        valid = false;
    }

    /**
     * Returns the spline's tension.
     *
     * @return the spline's tension
     */
    public float getTension() {
        return tension;
    }

    /**
     * Sets the spline's tension to the given value.
     *
     * @param newTension tension
     */
    public void setTension(float newTension) {
        tension = newTension;
        computeBasisMatrix();
    }

    @Override
    public int getRequiredControlPoints() {
        return 4;
    }

    @Override
    public String toString() {
        return super.toString() + "\nCatmullRomSpline{" + "tension=" + tension + '}';
    }

}
