package wobani.component.camera;

import org.joml.*;
import org.lwjgl.*;
import wobani.core.*;
import wobani.resource.opengl.buffer.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.nio.*;
import java.util.*;
import java.util.logging.*;

/**
 Implements basic camera functions. It has two projection modes: perspective and orthographic and you can switch between
 them anytime you want. Note that the field of view is only used in perspective mode and the scale is only used in
 orthographic mode. By the way you cen also get or set these values anytime. This Component offers you frustum culling
 methods in both modes. You can get the camera's projection and view matrices.

 @see GameObject */
public class CameraComponent extends Component implements Camera{

    /**
     The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(CameraComponent.class.getName());
    /**
     The Matrices UBO.
     */
    private static Ubo ubo;
    /**
     FloatBuffer for frequent UBO updates.
     */
    private static FloatBuffer temp;
    /**
     Reference for the main camera if it's a CameraComponent and needs to refresh it in the Matrices UBO.
     */
    private static CameraComponent camera;

    static{
        createUbo();
        temp = BufferUtils.createFloatBuffer(32);
    }

    /**
     Performs the frustum intersection tests.
     */
    private final FrustumIntersection frustum = new FrustumIntersection();
    /**
     View matrix.
     */
    private final Matrix4f viewMatrix = new Matrix4f();
    /**
     Projection matrix.
     */
    private final Matrix4f projectionMatrix = new Matrix4f();
    /**
     The corner points of the camera's frustum.
     */
    private final HashMap<CornerPoint, Vector3f> cornerPoints = new HashMap<>();
    /**
     Frustum's center.
     */
    private final Vector3f center = new Vector3f();
    /**
     Determines whether this Component's data is valid.
     */
    protected boolean valid;
    /**
     Near plane's distance from the eye position.
     */
    private float nearPlaneDistance = 0.1f;
    /**
     Far plane's distance from the eye position.
     */
    private float farPlaneDistance = 200;
    /**
     Vertical field of view value (in degrees).
     */
    private float fov = 45;
    /**
     The orthographic projection's scale.
     */
    private float scale = 10;
    /**
     Determines that the camera's projection mode is perspective or orthographic.
     */
    private ProjectionMode projectionMode;
    /**
     Determines whether the frustum culling is enabled.
     */
    private boolean frustumCulling = true;

    /**
     Initializes a new CameraComponent in perspective mode.

     @see ProjectionMode
     */
    public CameraComponent(){
        setProjectionMode(ProjectionMode.PERSPECTIVE);
        for(CornerPoint cp : CornerPoint.values()){
            cornerPoints.put(cp, new Vector3f());
        }
    }

    /**
     Initializes a new CameraComponent to the given values. Note that the near plane's distance must be higher than 0 and
     lower than the far plane's distance. Fov must be higher than 0 and lower than 180.

     @param fov       vertical field of view (in degrees)
     @param nearPlane near plane's distance
     @param farPlane  far plane's distance
     */
    public CameraComponent(float fov, float nearPlane, float farPlane){
        this();
        setFov(fov);
        setNearPlaneDistance(nearPlane);
        setFarPlaneDistance(farPlane);
    }

    /**
     Refreshes the Matrices UBO if it's needed. Recreates the whole UBO if it's released.
     */
    public static void refreshMatricesUbo(){
        if(camera != null){
            createUbo();
            refreshUboUnsafe();
        }
    }

    /**
     Refreshes the Matrices UBO.
     */
    private static void refreshUboUnsafe(){
        setMatricesBuffer();
        refreshUbo();
        camera = null;
    }

    /**
     Fills the temporary FloatBuffer with the main camera's matrices.
     */
    private static void setMatricesBuffer(){
        temp.position(0);
        camera.getViewMatrix().get(temp);
        camera.getProjectionMatrix().get(16, temp);
    }

    /**
     Refreshes the Matrices UBO with the temporary FloatBuffer.
     */
    private static void refreshUbo(){
        ubo.store(temp, 0);
        LOG.fine("Matrices UBO refreshed");
    }

    /**
     Recreates and fills the Matrices UBO it it's released.
     */
    public static void makeMatricesUboUsable(){
        createUbo();
        refreshMatricesUbo();
    }

    /**
     Invalidates the scene's main camera.
     */
    private static void invalidateMainCamera(){
        Camera cam = Scene.getParameters().getValue(Scene.MAIN_CAMERA);
        if(cam != null){
            cam.invalidate();
        }
    }

    /**
     Creates the Matrices UBO if it's not already created.
     */
    private static void createUbo(){
        if(!isMatricesUboUsable()){
            createUboUnsafe();
            LOG.fine("Matrices UBO created");
        }
    }

    /**
     Creates the Matrices UBO.
     */
    private static void createUboUnsafe(){
        ubo = new Ubo("Matrices");
        ubo.allocateImmutable(128);
        ubo.bindToBindingPoint(1);
    }

    /**
     Releases the Matrices UBO.

     @see #makeMatricesUboUsable()
     */
    public static void releaseMatricesUbo(){
        invalidateMainCamera();
        if(isMatricesUboUsable()){
            ubo.release();
            ubo = null;
            LOG.fine("Matrices UBO released");
        }
    }

    /**
     Returns true if the Matrices UBO is usable, and false if it's released.

     @return true if the Matrices UBO is usable, false otherwise
     */
    public static boolean isMatricesUboUsable(){
        return Utility.isUsable(ubo);
    }

    /**
     Determines whether frustum culling is enabled.

     @return true if frustum culling is enabled, false otherwise
     */
    @Override
    public boolean isFrustumCulling(){
        return frustumCulling;
    }

    /**
     Sets whether or not frustum culling is enabled.

     @param frustumCulling true if frustum culling should be enabled, false otherwise
     */
    @Override
    public void setFrustumCulling(boolean frustumCulling){
        this.frustumCulling = frustumCulling;
    }

    /**
     Returns the field of view. Note that this value is only used in perspective mode.

     @return vertical field of view (in degrees)

     @see #getProjectionMode()
     */
    public float getFov(){
        return fov;
    }

    /**
     Sets the field of view to the given value. Note the field of view value must be higher than 0, lower than 180 and
     this value is only used in perspective mode.

     @param fov vertical field of view (in degrees)

     @throws IllegalArgumentException field of view must be higher than 0 and lower than 180
     @see #getProjectionMode()
     */
    public void setFov(float fov){
        if(fov <= 0 || fov >= 180){
            throw new IllegalArgumentException("Field of view must be higher than 0 and lower than 180");
        }
        this.fov = fov;
        if(projectionMode == ProjectionMode.PERSPECTIVE){
            invalidate();
        }
    }

    /**
     Returns the near plane's distance.

     @return near plane's distance
     */
    public float getNearPlaneDistance(){
        return nearPlaneDistance;
    }

    /**
     Sets the near plane's distance to the given value. Note that the near plane's distance must be higher than 0 and
     lower than the far plane's distance.

     @param nearPlaneDistance near plane's distance

     @throws IllegalArgumentException near plane's distance must be higher than 0 and lower than the far plane distance
     */
    public void setNearPlaneDistance(float nearPlaneDistance){
        if(nearPlaneDistance <= 0 || nearPlaneDistance >= farPlaneDistance){
            throw new IllegalArgumentException("Near plane's distance must be higher than 0 and lower than the far plane distance");
        }
        this.nearPlaneDistance = nearPlaneDistance;
        invalidate();
    }

    /**
     Returns the far plane's distance.

     @return far plane's distance
     */
    public float getFarPlaneDistance(){
        return farPlaneDistance;
    }

    /**
     Sets the far plane's distance to the given value. Note that the far plane's distance must be higher than the near
     plane's distance.

     @param farPlaneDistance far plane's distance

     @throws IllegalArgumentException far plane's distance must be higher than near plane's distance
     */
    public void setFarPlaneDistance(float farPlaneDistance){
        if(nearPlaneDistance >= farPlaneDistance){
            throw new IllegalArgumentException("Near plane's distance must be lower than the far plane's distance");
        }
        this.farPlaneDistance = farPlaneDistance;
        invalidate();
    }

    /**
     Returns the scale value. Note that this value is only used in orthographic mode.

     @return scale value

     @see #getProjectionMode()
     */
    public float getScale(){
        return scale;
    }

    /**
     Sets the scale to the given value. Note that the scale must be higher than 0 and this value is only used in
     orthographic mode.

     @param scale scale

     @throws IllegalArgumentException scale must be higher than 0
     @see #getProjectionMode()
     */
    public void setScale(float scale){
        if(scale <= 0){
            throw new IllegalArgumentException("Scale must be higher than 0");
        }
        this.scale = scale;
        if(projectionMode == ProjectionMode.ORTHOGRAPHIC){
            invalidate();
        }
    }

    /**
     Returns the camera's projection mode.

     @return projection mode
     */
    @NotNull
    public ProjectionMode getProjectionMode(){
        return projectionMode;
    }

    /**
     Sets the camera's projection mode to the given value.

     @param projectionMode projection mode

     @throws NullPointerException projection mode can't be null
     */
    public void setProjectionMode(@NotNull ProjectionMode projectionMode){
        if(projectionMode == null){
            throw new NullPointerException();
        }
        if(this.projectionMode != projectionMode){
            this.projectionMode = projectionMode;
            invalidate();
        }
    }

    @Override
    public void invalidate(){
        valid = false;
        super.invalidate();
        if(isTheMainCamera()){
            camera = this;
        }
    }

    /**
     Refreshes this Component's data if it's invalid.
     */
    protected void refresh(){
        if(!valid){
            refreshProjectionMatrix();
            refreshViewMatrixAndFrustum();
            valid = true;
            LOG.finer("Camera refreshed");
        }
    }

    /**
     Refreshes the projection matrix.
     */
    private void refreshProjectionMatrix(){
        if(projectionMode == ProjectionMode.PERSPECTIVE){
            projectionMatrix.set(Utility.computePerspectiveProjectionMatrix(fov, nearPlaneDistance, farPlaneDistance));
        }else{
            projectionMatrix
                    .set(Utility.computeOrthographicProjectionMatrix(scale, nearPlaneDistance, farPlaneDistance));
        }
    }

    /**
     Refreshes the view matrix and the view frustum.
     */
    private void refreshViewMatrixAndFrustum(){
        if(getGameObject() != null){
            viewMatrix.set(Utility
                    .computeViewMatrix(getGameObject().getTransform().getAbsolutePosition(), getGameObject()
                            .getTransform().getAbsoluteRotation()));
            frustum.set(new Matrix4f(projectionMatrix).mul(viewMatrix));
            refreshFrustumVertices();
        }
    }

    /**
     Computes the frustum's corner points and center.
     */
    private void refreshFrustumVertices(){
        refreshFrustumCornerPoints();
        refreshFrustumCenterPoint();
    }

    /**
     Refreshes the frustum's corner points.
     */
    private void refreshFrustumCornerPoints(){
        Matrix4f inverseViewProjectionMatrix = computeInverseViewProjectionMatrix();
        Vector4f vec = new Vector4f();
        for(CornerPoint cp : CornerPoint.values()){
            vec.set(cp.getClipSpacePosition().mul(inverseViewProjectionMatrix));
            vec.div(vec.w);
            cornerPoints.get(cp).set(vec.x, vec.y, vec.z);
        }
    }

    /**
     Refreshes the frustum's center point.
     */
    private void refreshFrustumCenterPoint(){
        center.set(0, 0, 0);
        for(CornerPoint cp : CornerPoint.values()){
            center.add(cornerPoints.get(cp));
        }
        center.div(8);
    }

    /**
     Computes the inverse of the view projection matrix.

     @return the inverse of the view projection matrix
     */
    @NotNull
    private Matrix4f computeInverseViewProjectionMatrix(){
        if(projectionMode == ProjectionMode.PERSPECTIVE){
            return projectionMatrix.invertPerspectiveView(viewMatrix, new Matrix4f());
        }else{
            return projectionMatrix.mulAffine(viewMatrix, new Matrix4f()).invertAffine();
        }
    }

    //
    //UBO
    //

    /**
     Returns true if the sphere (determined by the given parameters) is inside, or intersects the frustum and returns
     false if it is fully outside. Note that if frustum culling is disabled, or this Component isn't connected to a
     GameObject this method always returns true.

     @param position position
     @param radius   radius

     @return false if the sphere is fully outside the frustum, true otherwise

     @throws NullPointerException     position can't be null
     @throws IllegalArgumentException radius can't be negative
     */
    @Override
    public boolean isInsideFrustum(@NotNull Vector3f position, float radius){
        if(position == null){
            throw new NullPointerException();
        }
        if(radius < 0){
            throw new IllegalArgumentException("Radius can't be negative");
        }
        return isInsideFrustumUnsafe(position, radius);
    }

    /**
     Returns true if the sphere (determined by the given parameters) is inside, or intersects the frustum and returns
     false if it is fully outside. Note that if frustum culling is disabled, or this Component isn't connected to a
     GameObject this method always returns true.

     @param position position
     @param radius   radius

     @return false if the sphere is fully outside the frustum, true otherwise
     */
    private boolean isInsideFrustumUnsafe(@NotNull Vector3f position, float radius){
        if(isFrustumCulling() && getGameObject() != null){
            refresh();
            return frustum.testSphere(position, radius);
        }else{
            return true;
        }
    }

    /**
     Returns true if the axis aligned bounding box (determined by the given parameters) is inside, or intersects the
     frustum and returns false if it is fully outside. Note that if frustum culling is disabled, or this Component isn't
     connected to a GameObject this method always returns true.

     @param aabbMin the axis aligned bounding box's minimum x, y and z values
     @param aabbMax the axis aligned bounding box's maximum x, y and z values

     @return false if the bounding box is fully outside the frustum, true otherwise

     @throws NullPointerException the parameters can't be null
     */
    @Override
    public boolean isInsideFrustum(@NotNull Vector3f aabbMin, @NotNull Vector3f aabbMax){
        if(aabbMin == null || aabbMax == null){
            throw new NullPointerException();
        }
        return isInsideFrustumUnsafe(aabbMin, aabbMax);
    }

    /**
     Returns true if the axis aligned bounding box (determined by the given parameters) is inside, or intersects the
     frustum and returns false if it is fully outside. Note that if frustum culling is disabled, or this Component isn't
     connected to a GameObject this method always returns true.

     @param aabbMin the axis aligned bounding box's minimum x, y and z values
     @param aabbMax the axis aligned bounding box's maximum x, y and z values

     @return false if the bounding box is fully outside the frustum, true otherwise
     */
    private boolean isInsideFrustumUnsafe(@NotNull Vector3f aabbMin, @NotNull Vector3f aabbMax){
        if(isFrustumCulling() && getGameObject() != null){
            refresh();
            return frustum.testAab(aabbMin, aabbMax);
        }else{
            return true;
        }
    }

    /**
     Returns the frustum's corner points. If this Component isn't connected to a GameObject, this method returns an empty
     Map.

     @return frustum's corner points
     */
    @NotNull
    @ReadOnly
    @Override
    public Map<CornerPoint, Vector3f> getFrustumCornerPoints(){
        Map<CornerPoint, Vector3f> ret = new HashMap<>(8);
        if(getGameObject() != null){
            ret.putAll(cornerPoints);
        }
        return ret;
    }

    /**
     Returns the frustum's specified corner point. If this Component isn't connected to a GameObject, this method returns
     null.

     @param cornerPoint corner point

     @return frustum's specified corner point

     @throws NullPointerException parameter can't be null
     */
    @Nullable
    @ReadOnly
    @Override
    public Vector3f getFrustumCornerPoint(@NotNull CornerPoint cornerPoint){
        if(cornerPoint == null){
            throw new NullPointerException();
        }
        return getFrustumCornerPointUnsafe(cornerPoint);
    }

    /**
     Returns the frustum's specified corner point. If this Component isn't connected to a GameObject, this method returns
     null.

     @param cornerPoint corner point

     @return frustum's specified corner point
     */
    @Nullable
    @ReadOnly
    private Vector3f getFrustumCornerPointUnsafe(@NotNull CornerPoint cornerPoint){
        if(getGameObject() != null){
            refresh();
            return new Vector3f(cornerPoints.get(cornerPoint));
        }else{
            return null;
        }
    }

    /**
     Returns the frustum's center point. If this Component isn't connected to a GameObject, this method returns null.

     @return frustum's center
     */
    @Nullable
    @ReadOnly
    @Override
    public Vector3f getFrustumCenter(){
        if(getGameObject() != null){
            refresh();
            return new Vector3f(center);
        }else{
            return null;
        }
    }

    /**
     Returns the view matrix. If this Component isn't connected to a GameObject, this method returns null.

     @return view matrix
     */
    @Nullable
    @ReadOnly
    @Override
    public Matrix4f getViewMatrix(){
        if(getGameObject() != null){
            refresh();
            return new Matrix4f(viewMatrix);
        }else{
            return null;
        }
    }

    /**
     Returns the projection matrix.

     @return projection matrix
     */
    @NotNull
    @ReadOnly
    @Override
    public Matrix4f getProjectionMatrix(){
        refresh();
        return new Matrix4f(projectionMatrix);
    }

    /**
     Returns true if it's the Scene's main Camera.

     @return true if it's the Scene's main Camera, false otherwise
     */
    public boolean isTheMainCamera(){
        Camera camera = Scene.getParameters().getValue(Scene.MAIN_CAMERA);
        return camera == this;
    }

    @Internal
    @Override
    protected void detachFromGameObject(){
        getGameObject().getTransform().removeInvalidatable(this);
        super.detachFromGameObject();
        invalidate();
    }

    @Internal
    @Override
    protected void attachToGameObject(@NotNull GameObject g){
        super.attachToGameObject(g);
        getGameObject().getTransform().addInvalidatable(this);
        invalidate();
    }

    @Override
    public int hashCode(){
        int hash = 5 + super.hashCode();
        hash = 67 * hash + Float.floatToIntBits(this.nearPlaneDistance);
        hash = 67 * hash + Float.floatToIntBits(this.farPlaneDistance);
        hash = 67 * hash + Float.floatToIntBits(this.fov);
        hash = 67 * hash + Float.floatToIntBits(this.scale);
        hash = 67 * hash + Objects.hashCode(this.projectionMode);
        hash = 67 * hash + (this.frustumCulling ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(!super.equals(obj)){
            return false;
        }
        final CameraComponent other = (CameraComponent) obj;
        if(Float.floatToIntBits(this.nearPlaneDistance) != Float.floatToIntBits(other.nearPlaneDistance)){
            return false;
        }
        if(Float.floatToIntBits(this.farPlaneDistance) != Float.floatToIntBits(other.farPlaneDistance)){
            return false;
        }
        if(Float.floatToIntBits(this.fov) != Float.floatToIntBits(other.fov)){
            return false;
        }
        if(Float.floatToIntBits(this.scale) != Float.floatToIntBits(other.scale)){
            return false;
        }
        if(this.frustumCulling != other.frustumCulling){
            return false;
        }
        if(this.projectionMode != other.projectionMode){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(super.toString()).append("\n")
                .append(CameraComponent.class.getSimpleName()).append("(").append(" projection mode: ")
                .append(projectionMode).append(", fov: ").append(fov).append(", scale: ").append(scale).append(")");
        return res.toString();
    }

}
