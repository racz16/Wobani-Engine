package toolbox;

import components.camera.*;
import components.camera.Camera.CornerPoint;
import components.renderables.*;
import core.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.logging.*;
import org.joml.*;
import org.lwjgl.*;
import toolbox.annotations.*;
import window.*;

/**
 * Useful static methods mostly for creating various matrices.
 */
public class Utility {

    /**
     * PI, mathematical constant.
     */
    public static final float PI = 3.14159265358979323846f;
    /**
     * For logging errors both to console and file.
     */
    private final static Logger errorLogger = Logger.getLogger("errorLogger");
    /**
     * For logging to the console.
     */
    private final static Logger logger = Logger.getLogger("logger");

    /**
     * To can't create Utility instance.
     */
    private Utility() {
    }

    static {
        try {
            ConsoleHandler ch = new ConsoleHandler();
            ch.setLevel(Level.ALL);
            FileHandler fh = new FileHandler("errors.log", true);
            fh.setFormatter(new SimpleFormatter());

            errorLogger.setUseParentHandlers(false);
            errorLogger.setLevel(Level.SEVERE);
            errorLogger.addHandler(fh);
            errorLogger.addHandler(ch);

            logger.setUseParentHandlers(false);
            logger.setLevel(Level.ALL);
            logger.addHandler(ch);
        } catch (IOException | SecurityException ex) {
            System.out.println(ex);
        }
    }

    //
    //logging-------------------------------------------------------------------
    //
    /**
     * Prints the exception and the method stack to console and file.
     *
     * @param ex exception
     */
    public static void logException(@NotNull Throwable ex) {
        errorLogger.log(Level.SEVERE, ex.toString(), ex);
    }

    /**
     * Prints the error message to console and file.
     *
     * @param ex error message
     */
    public static void logError(@NotNull String ex) {
        errorLogger.log(Level.SEVERE, ex);
    }

    /**
     * Logs a message to the console.
     *
     * @param obj what you want to log
     */
    public static void log(@NotNull Object obj) {
        logger.log(Level.INFO, obj.toString());
    }

    /**
     * This method logs to the console that you entered to the given menthod.
     *
     * @param sourceClass name of the class
     * @param sourceMethod name of the method
     */
    public static void logEntering(@NotNull String sourceClass, @NotNull String sourceMethod) {
        logger.entering(sourceClass, sourceMethod);
    }

    /**
     * This method logs to the console that you exited from the given menthod.
     *
     * @param sourceClass name of the class
     * @param sourceMethod name of the method
     */
    public static void logExiting(@NotNull String sourceClass, @NotNull String sourceMethod) {
        logger.exiting(sourceClass, sourceMethod);
    }

    //
    //matrices------------------------------------------------------------------
    //
    /**
     * Returns the model matrix, based on the given values.
     *
     * @param position position
     * @param rotation rotation (in degrees)
     * @param scale scale
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
     * @param scale scale
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
     * @return view matrix
     */
    @NotNull
    public static Matrix4f computeViewMatrix(@NotNull Vector3f position, @NotNull Vector3f rotation) {
        return computetInverseModelMatrix(position, rotation, new Vector3f(1));
    }

    /**
     * Returns the perspective projection matrix based on the given values.
     *
     * @param fov vertical field of view (in degrees)
     * @param nearPlane near plane
     * @param farPlane far plane
     * @return perspective projection matrix
     */
    @NotNull
    public static Matrix4f computePerspectiveProjectionMatrix(float fov, float nearPlane, float farPlane) {
        return new Matrix4f().setPerspective(toRadians(fov),
                Window.getAspectRatio(),
                nearPlane,
                farPlane);
    }

    /**
     * Returns the orthographic projection matrix based on the given values.
     *
     * @param scale scale
     * @param nearPlane near plane
     * @param farPlane far plane
     * @return orthographic projection matrix
     */
    @NotNull
    public static Matrix4f computeOrthographicProjectionMatrix(float scale, float nearPlane, float farPlane) {
        float ar = Window.getAspectRatio();
        return new Matrix4f().setOrtho(-scale * ar,
                scale * ar,
                -scale,
                scale,
                nearPlane,
                farPlane
        );
    }

    /**
     * Computes the main directional light's projection view matrix.
     *
     * @return the main directional light's projection view matrix
     */
    @NotNull
    public static Matrix4f computeDirectionalLightProjectionViewMatrix() {
        Camera camera = Scene.getCamera();
        GameObject lightGameObject = Scene.getDirectionalLight().getGameObject();
        Vector3f right = lightGameObject.getTransform().getRightVector();
        Vector3f up = lightGameObject.getTransform().getUpVector();
        Vector3f lightPosition = camera.getFrustumCenter().add(lightGameObject.getTransform().getForwardVector().negate().mul(Settings.getShadowCameraDistance()));
        Matrix4f lightSpaceMatrix = computeViewMatrix(lightPosition, lightGameObject.getTransform().getAbsoluteRotation());
        float maxX = Float.NEGATIVE_INFINITY;
        float minX = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        Vector4f vec = new Vector4f();
        for (CornerPoint cp : Camera.CornerPoint.values()) {
            vec.set(camera.getFrustumCornerPoint(cp), 1).mul(lightSpaceMatrix);
            if (vec.x > maxX) {
                maxX = vec.x;
            }
            if (vec.x < minX) {
                minX = vec.x;
            }
            if (vec.y > maxY) {
                maxY = vec.y;
            }
            if (vec.y < minY) {
                minY = vec.y;
            }
        }
        float compensation = (maxX + minX) / 2;
        float rightLeft = (maxX - minX) / 2;
        lightPosition.add(right.mul(compensation));

        compensation = (maxY + minY) / 2;
        float topBottom = (maxY - minY) / 2;
        lightPosition.add(up.mul(compensation));

        Matrix4f lightProjectionMatrix = new Matrix4f().setOrtho(-rightLeft, rightLeft, -topBottom, topBottom, Settings.getShadowCameraNearDistance(), Settings.getShadowCameraFarDistance());
        Vector3f rotation = new Vector3f(lightGameObject.getTransform().getAbsoluteRotation());
        Matrix4f lightViewMatrix = Utility.computeViewMatrix(lightPosition, rotation);
        return lightProjectionMatrix.mulOrthoAffine(lightViewMatrix);
    }

    //
    //misc----------------------------------------------------------------------
    //
    /**
     * Returns whether the specified Collections contains reference to the given
     * object.
     *
     * @param collection collection
     * @param object object
     * @return true if the given collection contains reference to the given
     * object, false otherwise
     */
    public static boolean containsReference(@NotNull Collection collection, @Nullable Object object) {
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
     * @param collection collection
     * @param object object
     * @return true if the given object successfully removed from the
     * Collection, false otherwise
     */
    public static boolean removeReference(@NotNull Collection collection, @Nullable Object object) {
        return collection.removeIf((Object t) -> t == object);

    }

    /**
     * Converts an angle measured in degrees to an approximately equivalent
     * angle measured in radians. The conversion from degrees to radians is
     * generally inexact.
     *
     * @param angle an angle, in degrees
     * @return the measurement of the angle angdeg in radians.
     */
    public static float toRadians(float angle) {
        return angle / 180 * PI;
    }

    /**
     * Converts an angle measured in radians to an approximately equivalent
     * angle measured in degrees. The conversion from radians to degrees is
     * generally inexact; users should
     * <i>not</i> expect {@code cos(toRadians(90.0))} to exactly equal
     * {@code 0.0}.
     *
     * @param angle an angle, in radians
     * @return the measurement of the angle {@code angrad} in degrees.
     */
    public static float toDegrees(float angle) {
        return angle * 180 / PI;
    }

    /**
     * Determines whether all of the given vector's cordinates are equals or
     * higher than zero.
     *
     * @param color color
     * @return true if all of the given vector's cordinates are equals or higher
     * than zero, false otherwise
     */
    public static boolean isHdrColor(@NotNull Vector3f color) {
        return color.get(color.minComponent()) >= 0;
    }

    /**
     * Determines whether all of the given vector's cordinates are between zero
     * and one.
     *
     * @param color color
     * @return true if all of the given vector's cordinates are between zero and
     * one, false otherwise
     */
    public static boolean isColor(@NotNull Vector3f color) {
        return color.get(color.minComponent()) >= 0 && color.get(color.maxComponent()) <= 1;
    }

    /**
     * Wraps the given object by a list.
     *
     * @param <T> type
     * @param object object to wrap
     * @return list
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
     * Determines whether the given mesh component is inside the main camera's
     * view frustum.
     *
     * @param meshComponent mesh component
     * @return true if the mesh component is inside the main camera's view
     * frustum, false otherwise
     */
    public static boolean isInsideFrustum(@NotNull MeshComponent meshComponent) {
        Camera camera = Scene.getCamera();
        Transform transform = meshComponent.getGameObject().getTransform();
        if (transform.getBillboardingMode() == Transform.BillboardingMode.NO_BILLBOARDING) {
            return camera.isInsideFrustum(meshComponent.getRealAabbMin(), meshComponent.getRealAabbMax());
        } else {
            return camera.isInsideFrustum(transform.getAbsolutePosition(), meshComponent.getRealFurthestVertexDistance());
        }
    }

    /**
     * Determines whether the given spline component is inside the main camera's
     * view frustum.
     *
     * @param splineComponent spline component
     * @return true if the spline component is inside the main camera's view
     * frustum, false otherwise
     */
    public static boolean isInsideFrustum(@NotNull SplineComponent splineComponent) {
        Camera camera = Scene.getCamera();
        Transform transform = splineComponent.getGameObject().getTransform();
        if (transform.getBillboardingMode() == Transform.BillboardingMode.NO_BILLBOARDING) {
            return camera.isInsideFrustum(splineComponent.getRealAabbMin(), splineComponent.getRealAabbMax());
        } else {
            return camera.isInsideFrustum(transform.getAbsolutePosition(), splineComponent.getRealFurthestVertexDistance());
        }
    }

}
