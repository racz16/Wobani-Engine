package wobani.resources.splines;

import org.joml.*;
import wobani.resources.*;

/**
 * Interface for spline resources.
 */
public interface Spline extends Renderable {

    /**
     * Determines whether the spline is a loop spline.
     *
     * @return true if the spline is loop spline, false otherwise
     */
    public boolean isLoopSpline();

    /**
     * Returns the spline's approximated length.
     *
     * @return spline's approximated length
     */
    public float getApproximatedLength();

    /**
     * Returns the spline's forward vector at the given t "time", where t=0
     * means the start of the spline and t=1 means the end of the spline.
     *
     * @param t "time"
     *
     * @return forward vector (normalized)
     */
    public Vector3f getForwardVector(float t);

    /**
     * Returns the spline's approximated position at the given t "time", where
     * t=0 means the start of the spline and t=1 means the end of the spline.
     *
     * @param t "time"
     *
     * @return position
     */
    public Vector3f getApproximatedPosition(float t);
}
