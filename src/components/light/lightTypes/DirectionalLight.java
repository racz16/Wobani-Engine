package components.light.lightTypes;

import org.joml.*;

/**
 * This interface signs that the Component is a directional light source.
 */
public interface DirectionalLight extends Light {

    /**
     * Returns true if the sphere (determined by the given parameters) is
     * inside, or intersects the frustum and returns false if it is fully
     * outside.
     *
     * @param position position
     * @param radius radius
     * @return false if the sphere is fully outside the frustum, true otherwise
     *
     * @see Settings#isFrustumCulling()
     */
    public boolean isInsideFrustum(Vector3f position, float radius);

    /**
     * Returns true if the axis alligned bounding box (determined by the given
     * parameters) is inside, or intersects the frustum and returns false if it
     * is fully outside.
     *
     * @param aabbMin the axis alligned bounding box's minimum x, y and z values
     * @param aabbMax the axis alligned bounding box's maximum x, y and z values
     * @return false if the bounding box is fully outside the frustum, true
     * otherwise
     *
     * @see Settings#isFrustumCulling()
     */
    public boolean isInsideFrustum(Vector3f aabbMin, Vector3f aabbMax);
}
