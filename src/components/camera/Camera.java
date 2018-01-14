package components.camera;

import toolbox.invalidatable.Invalidatable;
import core.*;
import java.util.*;
import org.joml.*;
import toolbox.annotations.*;

/**
 * Base interface for Components with camera features.
 */
public interface Camera extends Invalidatable {

    /**
     * One of the camera frustum's corner points.
     */
    public enum CornerPoint {
        /**
         * Far-top-left corner point.
         */
        FAR_TOP_LEFT(new Vector4f(-1, 1, 1, 1)),
        /**
         * Far-top-right corner point.
         */
        FAR_TOP_RIGHT(new Vector4f(1, 1, 1, 1)),
        /**
         * Far-bottom-left corner point.
         */
        FAR_BOTTOM_LEFT(new Vector4f(-1, -1, 1, 1)),
        /**
         * Far-bottom-right corner point.
         */
        FAR_BOTTOM_RIGHT(new Vector4f(1, -1, 1, 1)),
        /**
         * Near-top-left corner point.
         */
        NEAR_TOP_LEFT(new Vector4f(-1, 1, -1, 1)),
        /**
         * Near-top-right corner point.
         */
        NEAR_TOP_RIGHT(new Vector4f(1, 1, -1, 1)),
        /**
         * Near-bottom-left corner point.
         */
        NEAR_BOTTOM_LEFT(new Vector4f(-1, -1, -1, 1)),
        /**
         * Near-bottom-right corner point.
         */
        NEAR_BOTTOM_RIGHT(new Vector4f(1, -1, -1, 1));

        /**
         * Initializes a new CornerPoint to the given value.
         *
         * @param pos position in clip space
         */
        CornerPoint(@NotNull Vector4f pos) {
            clipSpacePosition = new Vector4f(pos.x, pos.y, pos.z, pos.w);
        }

        /**
         * The corner point's position in clip space.
         */
        private final Vector4f clipSpacePosition;

        /**
         * Returns the corner point's position in clip space.
         *
         * @return corner point's position in clip space
         */
        @NotNull @ReadOnly
        public Vector4f getClipSpacePosition() {
            return new Vector4f(clipSpacePosition);
        }

    }

    /**
     * The camera can work in one of the following projection modes: perspective
     * or orthographic.
     */
    public enum ProjectionMode {
        /**
         * Perspective projection mode.
         */
        PERSPECTIVE,
        /**
         * Orthographic projection mode.
         */
        ORTHOGRAPHIC;
    }

    /**
     * Returns the view matrix.
     *
     * @return view matrix
     */
    public Matrix4f getViewMatrix();

    /**
     * Returns the projection matrix.
     *
     * @return projection matrix
     */
    public Matrix4f getProjectionMatrix();

    /**
     * Returns the frustum's center point.
     *
     * @return frustum's center
     */
    public Vector3f getFrustumCenter();

    /**
     * Returns the frustum's specified corner point.
     *
     * @param cornerPoint corner point
     *
     * @return frustum's specified corner point
     */
    public Vector3f getFrustumCornerPoint(@NotNull CornerPoint cornerPoint);

    /**
     * Returns the frustum's corner points.
     *
     * @return frustum's corner points
     */
    public List<Vector3f> getFrustumCornerPoints();

    /**
     * Determines whether frustum culling is enabled.
     *
     * @return true if frustum culling is enabled, false otherwise
     */
    public boolean isFrustumCulling();

    /**
     * Sets whether or not frustum culling is enabled.
     *
     * @param frustumCulling true if frustum culling should be enabled, false
     *                       otherwise
     */
    public void setFrustumCulling(boolean frustumCulling);

    /**
     * Returns true if the sphere (determined by the given parameters) is
     * inside, or intersects the frustum and returns false if it is fully
     * outside.
     *
     * @param position position
     * @param radius   radius
     *
     * @return false if the sphere is fully outside the frustum, true otherwise
     */
    public boolean isInsideFrustum(Vector3f position, float radius);

    /**
     * Returns true if the axis alligned bounding box (determined by the given
     * parameters) is inside, or intersects the frustum and returns false if it
     * is fully outside.
     *
     * @param aabbMin the axis alligned bounding box's minimum x, y and z values
     * @param aabbMax the axis alligned bounding box's maximum x, y and z values
     *
     * @return false if the bounding box is fully outside the frustum, true
     *         otherwise
     */
    public boolean isInsideFrustum(Vector3f aabbMin, Vector3f aabbMax);

    public GameObject getGameObject();

    public void addInvalidatable(@NotNull Invalidatable invalidatable);

    public void removeInvalidatable(@Nullable Invalidatable invalidatable);

    public boolean isActive();
}
