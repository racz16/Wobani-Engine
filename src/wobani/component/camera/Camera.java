package wobani.component.camera;

import org.joml.*;
import wobani.component.*;
import wobani.toolbox.annotation.*;

import java.util.*;

/**
 Base interface for Components with camera features.
 */
public interface Camera extends ComponentBase{

    /**
     Returns the view matrix.

     @return view matrix
     */
    Matrix4f getViewMatrix();

    /**
     Returns the projection matrix.

     @return projection matrix
     */
    Matrix4f getProjectionMatrix();

    /**
     Returns the frustum's center point.

     @return frustum's center
     */
    Vector3f getFrustumCenter();

    /**
     Returns the frustum's specified corner point.

     @param cornerPoint corner point

     @return frustum's specified corner point
     */
    Vector3f getFrustumCornerPoint(@NotNull CornerPoint cornerPoint);

    /**
     Returns the frustum's corner points.

     @return frustum's corner points
     */
    Map<CornerPoint, Vector3f> getFrustumCornerPoints();

    /**
     Determines whether frustum culling is enabled.

     @return true if frustum culling is enabled, false otherwise
     */
    boolean isFrustumCulling();

    /**
     Sets whether or not frustum culling is enabled.

     @param frustumCulling true if frustum culling should be enabled, false otherwise
     */
    void setFrustumCulling(boolean frustumCulling);

    /**
     Returns true if the sphere (determined by the given parameters) is inside, or intersects the frustum and returns
     false if it is fully outside.

     @param position position
     @param radius   radius

     @return false if the sphere is fully outside the frustum, true otherwise
     */
    boolean isInsideFrustum(Vector3f position, float radius);

    /**
     Returns true if the axis aligned bounding box (determined by the given parameters) is inside, or intersects the
     frustum and returns false if it is fully outside.

     @param aabbMin the axis aligned bounding box's minimum x, y and z values
     @param aabbMax the axis aligned bounding box's maximum x, y and z values

     @return false if the bounding box is fully outside the frustum, true otherwise
     */
    boolean isInsideFrustum(Vector3f aabbMin, Vector3f aabbMax);

    /**
     One of the camera frustum's corner points.
     */
    enum CornerPoint{
        /**
         Far-top-left corner point.
         */
        FAR_TOP_LEFT(new Vector4f(-1, 1, 1, 1)), /**
         Far-top-right corner point.
         */
        FAR_TOP_RIGHT(new Vector4f(1, 1, 1, 1)), /**
         Far-bottom-left corner point.
         */
        FAR_BOTTOM_LEFT(new Vector4f(-1, -1, 1, 1)), /**
         Far-bottom-right corner point.
         */
        FAR_BOTTOM_RIGHT(new Vector4f(1, -1, 1, 1)), /**
         Near-top-left corner point.
         */
        NEAR_TOP_LEFT(new Vector4f(-1, 1, -1, 1)), /**
         Near-top-right corner point.
         */
        NEAR_TOP_RIGHT(new Vector4f(1, 1, -1, 1)), /**
         Near-bottom-left corner point.
         */
        NEAR_BOTTOM_LEFT(new Vector4f(-1, -1, -1, 1)), /**
         Near-bottom-right corner point.
         */
        NEAR_BOTTOM_RIGHT(new Vector4f(1, -1, -1, 1));

        /**
         The corner point's position in clip space.
         */
        private final Vector4f clipSpacePosition;

        /**
         Initializes a new CornerPoint to the given value.

         @param pos position in clip space
         */
        CornerPoint(@NotNull Vector4f pos){
            clipSpacePosition = new Vector4f(pos.x, pos.y, pos.z, pos.w);
        }

        /**
         Returns the corner point's position in clip space.

         @return corner point's position in clip space
         */
        @NotNull
        @ReadOnly
        public Vector4f getClipSpacePosition(){
            return new Vector4f(clipSpacePosition);
        }

    }

    /**
     The camera can work in one of the following projection modes: perspective or orthographic.
     */
    enum ProjectionMode{
        /**
         Perspective projection mode.
         */
        PERSPECTIVE, /**
         Orthographic projection mode.
         */
        ORTHOGRAPHIC
    }
}
