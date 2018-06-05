package wobani.toolbox;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.logging.*;
import org.joml.*;
import org.lwjgl.*;
import wobani.component.camera.*;
import wobani.component.camera.Camera.CornerPoint;
import wobani.component.renderable.*;
import wobani.core.*;
import wobani.resources.*;
import wobani.toolbox.annotation.*;
import wobani.window.*;

/**
 * Useful static methods for logging, creating various matrices etc.
 */
public class Utility {

    /**
     * PI, mathematical constant.
     */
    public static final float PI = 3.14159265358979323846f;
    /**
     * For logging errors both to console and file.
     */
    private static final Logger ERROR_LOG = Logger.getLogger("ERROR");
    /**
     * The engine's root logger.
     */
    private static final Logger WOBANI_LOG = Logger.getLogger("wobani");

    /**
     * To can't create Utility instance.
     */
    private Utility() {
    }

    //
    //logging-------------------------------------------------------------------
    //
    /**
     * Returns the engine's root logger. You get the same result if call
     * Logger.getLogger("wobani").
     *
     * @return the engine's root logger
     */
    @NotNull
    public static Logger getWobaniLogger() {
	return WOBANI_LOG;
    }

    /**
     * Sets the logging level for the engine's root logger and all of it's
     * handlers to the given value.
     * <p>
     * Info level used when big systems like OpenGL start or end, Fine used when
     * Resources created or released, Finer used when computation intensive
     * tasks like matrix calculation ran, Finer used for other fairly important
     * events.
     *
     * @param level logging level
     *
     * @see #getWobaniLogger()
     */
    public static void setLoggingLevel(@NotNull Level level) {
	WOBANI_LOG.setLevel(level);
	for (Handler handler : WOBANI_LOG.getHandlers()) {
	    handler.setLevel(level);
	}
    }

    /**
     * Initializes the engine's root logger and the exception logger.
     */
    public static void initializeLogging() {
	initializeWobaniLogger();
	initializeErrorLogger();
	setLoggingLevel(Level.SEVERE);
    }

    /**
     * Initializes the engine's root logger.
     */
    private static void initializeWobaniLogger() {
	WOBANI_LOG.setUseParentHandlers(false);
	WOBANI_LOG.addHandler(new ConsoleHandler());
    }

    /**
     * Initializes the engine's error logger.
     */
    private static void initializeErrorLogger() {
	try {
	    ERROR_LOG.setUseParentHandlers(false);
	    ERROR_LOG.addHandler(createFileHandler("errors.log"));
	    ERROR_LOG.addHandler(new ConsoleHandler());
	} catch (IOException | SecurityException ex) {
	    Logger.getLogger("").severe(ex.toString());
	}
    }

    /**
     * Creates a new FileHandler to the given path.
     *
     * @param fileName file name to log
     *
     * @return a new FileHandler to log
     *
     * @throws IOException if there are IO problems opening the file
     */
    @NotNull
    private static FileHandler createFileHandler(@NotNull String fileName) throws IOException {
	FileHandler fh = new FileHandler(fileName, true);
	fh.setFormatter(new SimpleFormatter());
	return fh;
    }

    /**
     * Prints the exception and the method stack to console and file.
     *
     * @param ex exception
     */
    public static void logException(@NotNull Throwable ex) {
	ERROR_LOG.log(Level.SEVERE, ex.toString(), ex);
	System.err.println(ex.getMessage());
    }

    /**
     * Prints the error message to console and file.
     *
     * @param ex error message
     */
    public static void logError(@NotNull String ex) {
	ERROR_LOG.severe(ex);
    }

    //
    //matrices------------------------------------------------------------------
    //
    /**
     * Returns the model matrix, based on the given values.
     *
     * @param position position
     * @param rotation rotation (in degrees)
     * @param scale    scale
     *
     * @return model matrix
     */
    @NotNull
    public static Matrix4f computeModelMatrix(@NotNull Vector3f position, @NotNull Vector3f rotation, @NotNull Vector3f scale) {
	return new Matrix4f().translationRotateScale(
		position,
		new Quaternionf()
			.rotation(Utility.toRadians(rotation.x), Utility.toRadians(rotation.y), Utility.toRadians(rotation.z)),
		scale);
    }

    /**
     * Returns the inverse of the model matrix based on the given values.
     *
     * @param position position
     * @param rotation rotation (in degrees)
     * @param scale    scale
     *
     * @return inverse of the model matrix
     */
    @NotNull
    public static Matrix4f computetInverseModelMatrix(@NotNull Vector3f position, @NotNull Vector3f rotation, @NotNull Vector3f scale) {
	return new Matrix4f().translationRotateScaleInvert(
		position,
		new Quaternionf()
			.rotation(Utility.toRadians(rotation.x), Utility.toRadians(rotation.y), Utility.toRadians(rotation.z)),
		scale);
    }

    /**
     * Returns the view matrix based on the given values.
     *
     * @param position position
     * @param rotation rotation (in degrees)
     *
     * @return view matrix
     */
    @NotNull
    public static Matrix4f computeViewMatrix(@NotNull Vector3f position, @NotNull Vector3f rotation) {
	return computetInverseModelMatrix(position, rotation, new Vector3f(1));
    }

    /**
     * Returns the perspective projection matrix based on the given values.
     *
     * @param fov       vertical field of view (in degrees)
     * @param nearPlane near plane
     * @param farPlane  far plane
     *
     * @return perspective projection matrix
     *
     * @throws IllegalArgumentException fov must be in the (1;179) interval, the
     *                                  near plane must be higher than 0 and the
     *                                  far plane higher than the near plane
     */
    @NotNull
    public static Matrix4f computePerspectiveProjectionMatrix(float fov, float nearPlane, float farPlane) {
	if (fov <= 0 || fov >= 180 || nearPlane <= 0 || farPlane <= nearPlane) {
	    throw new IllegalArgumentException("Fov must be in the (0;180) interval, the near plane must be higher than 0 and the far plane higher than the near plane");
	}
	return new Matrix4f().setPerspective(toRadians(fov),
		Window.getAspectRatio(),
		nearPlane,
		farPlane);
    }

    /**
     * Returns the orthographic projection matrix based on the given values.
     *
     * @param scale     scale
     * @param nearPlane near plane
     * @param farPlane  far plane
     *
     * @return orthographic projection matrix
     *
     * @throws IllegalArgumentException scale must be higher than 0, the near
     *                                  plane must be higher than 0 and the far
     *                                  plane higher than the near plane
     */
    @NotNull
    public static Matrix4f computeOrthographicProjectionMatrix(float scale, float nearPlane, float farPlane) {
	if (scale <= 0 || nearPlane <= 0 || farPlane <= nearPlane) {
	    throw new IllegalArgumentException("Scale must be higher than 0, the near plane must be higher than 0 and the far plane higher than the near plane");
	}
	float ar = Window.getAspectRatio();
	return new Matrix4f().setOrtho(-scale * ar, scale * ar, -scale, scale, nearPlane, farPlane);
    }

    /**
     * Computes the main directional light's projection view matrix for shadow
     * mapping.
     *
     * @param dirLight     the directional light's GameObject
     * @param distance     distance from the camera frustum center
     * @param nearDistance near plane distance
     * @param farDistance  far plane distance
     *
     * @return the main directional light's projection view matrix
     *
     * @throws NullPointerException     dirLight can't be null
     * @throws IllegalArgumentException all parameters must be positive and
     *                                  nearDistance must be lower than
     *                                  farDistance
     */
    @NotNull
    public static Matrix4f computeShadowMapProjectionViewMatrix(@NotNull GameObject dirLight, float distance, float nearDistance, float farDistance) {
	if (dirLight == null) {
	    throw new NullPointerException();
	}
	if (distance <= 0 || nearDistance <= 0 || nearDistance >= farDistance) {
	    throw new IllegalArgumentException("All parameters must be positive and nearDistance must be lower than farDistance");
	}
	return ShadowMapMatrixSolver.computeMatrix(dirLight, distance, nearDistance, farDistance);
    }

    //
    //references----------------------------------------------------------------
    //
    /**
     * Returns whether the specified Collections contains reference to the given
     * object.
     *
     * @param collection collection
     * @param object     object
     *
     * @return true if the given collection contains reference to the given
     *         object, false otherwise
     */
    public static boolean containsReference(@NotNull Collection<?> collection, @Nullable Object object) {
	for (Object collectionObject : collection) {
	    if (collectionObject == object) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Removes the given object from the given Collection if the Collection
     * stored reference to the object.
     *
     * @param <T>        type
     * @param collection collection
     * @param object     object
     *
     * @return true if the given object successfully removed from the
     *         Collection, false otherwise
     */
    public static <T> boolean removeReference(@NotNull Collection<T> collection, @Nullable T object) {
	return collection.removeIf((T t) -> t == object);

    }

    //
    //math----------------------------------------------------------------------
    //
    /**
     * Converts an angle measured in degrees to an approximately equivalent
     * angle measured in radians. The conversion from degrees to radians is
     * generally inexact.
     *
     * @param angle an angle, in degrees
     *
     * @return the measurement of the angle angdeg in radians.
     */
    public static float toRadians(float angle) {
	return angle / 180 * PI;
    }

    /**
     * Converts the given vector's all coordinates from degrees to radians.
     *
     * @param angles vector contains angles in degrees
     *
     * @return vector contains angles in radians
     */
    @NotNull
    public static Vector3f toRadians(@NotNull Vector3f angles) {
	return new Vector3f(toRadians(angles.x), toRadians(angles.y), toRadians(angles.z));
    }

    /**
     * Converts an angle measured in radians to an approximately equivalent
     * angle measured in degrees. The conversion from radians to degrees is
     * generally inexact; users should
     * <i>not</i> expect {@code cos(toRadians(90.0))} to exactly equal
     * {@code 0.0}.
     *
     * @param angle an angle, in radians
     *
     * @return the measurement of the angle {@code angrad} in degrees.
     */
    public static float toDegrees(float angle) {
	return angle * 180 / PI;
    }

    /**
     * Converts the given vector's all coordinates from radians to degrees.
     *
     * @param angles vector contains angles in radians
     *
     * @return vector contains angles in degrees
     */
    @NotNull
    public static Vector3f toDegrees(@NotNull Vector3f angles) {
	return new Vector3f(toDegrees(angles.x), toDegrees(angles.y), toDegrees(angles.z));
    }

    //
    //misc----------------------------------------------------------------------
    //
    /**
     * Determines whether all of the given vector's cordinates are equals or
     * higher than zero.
     *
     * @param color color
     *
     * @return true if all of the given vector's cordinates are equals or higher
     *         than zero, false otherwise
     */
    public static boolean isHdrColor(@NotNull Vector3f color) {
	return color.get(color.minComponent()) >= 0;
    }

    /**
     * Determines whether all of the given vector's cordinates are between zero
     * and one.
     *
     * @param color color
     *
     * @return true if all of the given vector's cordinates are between zero and
     *         one, false otherwise
     */
    public static boolean isLdrColor(@NotNull Vector3f color) {
	return color.get(color.minComponent()) >= 0 && color.get(color.maxComponent()) <= 1;
    }

    /**
     * Wraps the given object by a list.
     *
     * @param <T>    type
     * @param object object to wrap
     *
     * @return list contains the given object
     */
    @NotNull
    public static <T> List<T> wrapObjectByList(@Nullable T object) {
	List<T> list = new ArrayList<>(1);
	list.add(object);
	return list;
    }

    /**
     * Creates an int buffer and stores the given data in it.
     *
     * @param data data to store
     *
     * @return int buffer containing the given data
     */
    @NotNull
    public static IntBuffer storeDataInIntBuffer(@NotNull int[] data) {
	IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
	buffer.put(data);
	buffer.flip();
	return buffer;
    }

    /**
     * Creates a float buffer and stores the given data in it.
     *
     * @param data data to store
     *
     * @return float buffer containing the given data
     */
    @NotNull
    public static FloatBuffer storeDataInFloatBuffer(@NotNull float[] data) {
	FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
	buffer.put(data);
	buffer.flip();
	return buffer;
    }

    /**
     * Determines whether the given RenderableComponent is inside the main
     * camera's view frustum (computing by AABB).
     *
     * @param renderableComponent RenderableComponent
     *
     * @return true if the RenderableComponent is inside the main camera's view
     *         frustum, false otherwise
     */
    public static boolean isInsideMainCameraFrustumAabb(@NotNull RenderableComponent<?> renderableComponent) {
	Camera camera = Scene.getParameters().getValue(Scene.MAIN_CAMERA);
	if (camera != null) {
	    return camera.isInsideFrustum(renderableComponent.getBoundingShape().getRealAabbMin(), renderableComponent.getBoundingShape().getRealAabbMax());
	} else {
	    return false;
	}
    }

    /**
     * Determines whether the given RenderableComponent is inside the main
     * camera's view frustum (computing by bounding sphere).
     *
     * @param renderableComponent RenderableComponent
     *
     * @return true if the RenderableComponent is inside the main camera's view
     *         frustum, false otherwise
     */
    public static boolean isInsideMainCameraFrustumSphere(@NotNull RenderableComponent<?> renderableComponent) {
	Camera camera = Scene.getParameters().getValue(Scene.MAIN_CAMERA);
	if (camera != null) {
	    return camera.isInsideFrustum(renderableComponent.getGameObject().getTransform().getAbsolutePosition(), renderableComponent.getBoundingShape().getRealRadius());
	} else {
	    return false;
	}
    }

    /**
     * Returns true if the given Resource isn't null and is usable.
     *
     * @param resource Resource
     *
     * @return true if the given Resource isn't null and is usable, false
     *         otherwise
     */
    public static boolean isUsable(@Nullable Resource resource) {
	return resource != null && resource.isUsable();
    }

    /**
     * This class can compute the main directional light's projection view
     * matrix for shadow mapping. It tries to compute the view matrix to best
     * fit to the main camera's view frustum.
     */
    private static class ShadowMapMatrixSolver {

	/**
	 * Scene's main camera.
	 */
	private static Camera camera;
	/**
	 * Main directional light's right direction.
	 */
	private static Vector3f lightRight;
	/**
	 * Main directional light's up direction.
	 */
	private static Vector3f lightUp;
	/**
	 * Main directional light's computed position.
	 */
	private static Vector3f lightPosition;
	/**
	 * Main directional light's absolute rotation.
	 */
	private static Vector3f lightRotation;
	/**
	 * Maximum x coordinate of the main camera's frustum corner points in
	 * light space.
	 */
	private static float lightSpaceXMax;
	/**
	 * Minimum x coordinate of the main camera's frustum corner points in
	 * light space.
	 */
	private static float lightSpaceXMin;
	/**
	 * Maximum y coordinate of the main camera's frustum corner points in
	 * light space.
	 */
	private static float lightSpaceYMax;
	/**
	 * Minimum y coordinate of the main camera's frustum corner points in
	 * light space.
	 */
	private static float lightSpaceYMin;

	/**
	 * To can't create ShadowMapMatrixSolver instance.
	 */
	private ShadowMapMatrixSolver() {
	}

	/**
	 * Computes the main directional light's projection view matrix based on
	 * the given data.
	 *
	 * @param dirLight     directional light source's GameObject
	 * @param distance     distance from the camera frustum center
	 * @param nearDistance near plane distance
	 * @param farDistance  far plane distance
	 *
	 * @return the main directional light's projection view matrix
	 */
	public static Matrix4f computeMatrix(@NotNull GameObject dirLight, float distance, float nearDistance, float farDistance) {
	    initializeCamera();
	    initializeLight(dirLight, distance);
	    initializeMinMax();
	    refreshMinMaxValues();
	    refreshLightPosition();
	    return computeMatrix(nearDistance, farDistance);
	}

	/**
	 * Initializes the main camera related fields.
	 */
	private static void initializeCamera() {
	    camera = Scene.getParameters().getValue(Scene.MAIN_CAMERA);
	}

	/**
	 * Initializes the main directional light related fields.
	 *
	 * @param lightGameObject directional light source's GameObject
	 * @param distance        distance from the camera frustum center
	 */
	private static void initializeLight(@NotNull GameObject lightGameObject, float distance) {
	    lightRight = lightGameObject.getTransform().getRightVector();
	    lightUp = lightGameObject.getTransform().getUpVector();
	    lightRotation = lightGameObject.getTransform().getAbsoluteRotation();
	    lightPosition = camera.getFrustumCenter().add(lightGameObject.getTransform().getForwardVector().negate().mul(distance));
	}

	/**
	 * Initializes the light space minimum an maximum values.
	 */
	private static void initializeMinMax() {
	    lightSpaceXMax = Float.NEGATIVE_INFINITY;
	    lightSpaceXMin = Float.POSITIVE_INFINITY;
	    lightSpaceYMax = Float.NEGATIVE_INFINITY;
	    lightSpaceYMin = Float.POSITIVE_INFINITY;
	}

	/**
	 * Refreshes the light space minimum and maximum values based on the
	 * camera's frustum corner points.
	 */
	private static void refreshMinMaxValues() {
	    Vector4f vec = new Vector4f();
	    Matrix4f lightSpaceMatrix = computeViewMatrix(lightPosition, lightRotation);
	    for (CornerPoint cp : Camera.CornerPoint.values()) {
		vec.set(camera.getFrustumCornerPoint(cp), 1).mul(lightSpaceMatrix);
		refreshMinValues(vec.x(), vec.y());
		refreshMaxValues(vec.x(), vec.y());
	    }
	}

	/**
	 * Refreshes the light space minimum values based on the camera's given
	 * frustum corner point.
	 *
	 * @param x one of the main camera's frustum corner point's x coordinate
	 * @param y one of the main camera's frustum corner point's y coordinate
	 */
	private static void refreshMinValues(float x, float y) {
	    if (x < lightSpaceXMin) {
		lightSpaceXMin = x;
	    }
	    if (y < lightSpaceYMin) {
		lightSpaceYMin = y;
	    }
	}

	/**
	 * Refreshes the light space maximum values based on the camera's given
	 * frustum corner point.
	 *
	 * @param x one of the main camera's frustum corner point's x coordinate
	 * @param y one of the main camera's frustum corner point's y coordinate
	 */
	private static void refreshMaxValues(float x, float y) {
	    if (x > lightSpaceXMax) {
		lightSpaceXMax = x;
	    }
	    if (y > lightSpaceYMax) {
		lightSpaceYMax = y;
	    }
	}

	/**
	 * Refreshes the light's position based on the light space minimum and
	 * maximum values.
	 */
	private static void refreshLightPosition() {
	    float compensation = (lightSpaceXMax + lightSpaceXMin) / 2;
	    lightPosition.add(lightRight.mul(compensation));
	    compensation = (lightSpaceYMax + lightSpaceYMin) / 2;
	    lightPosition.add(lightUp.mul(compensation));
	}

	/**
	 * Computes the main directional light's projection view matrix based on
	 * the already computed light space min, max and position values.
	 *
	 * @param near near plane distance
	 * @param far  far plane distance
	 *
	 * @return the main directional light's projection view
	 */
	@NotNull
	private static Matrix4f computeMatrix(float near, float far) {
	    float horizontalScale = (lightSpaceXMax - lightSpaceXMin) / 2;
	    float verticalScale = (lightSpaceYMax - lightSpaceYMin) / 2;
	    Matrix4f lightProjectionMatrix = new Matrix4f().setOrtho(-horizontalScale, horizontalScale, -verticalScale, verticalScale, near, far);
	    Matrix4f lightViewMatrix = Utility.computeViewMatrix(lightPosition, lightRotation);
	    return lightProjectionMatrix.mulOrthoAffine(lightViewMatrix);
	}

    }

}
