package components.light;

import static components.light.DefaultLightComponent.ubo;
import components.light.lightTypes.*;
import core.*;
import java.nio.*;
import org.joml.*;
import org.lwjgl.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * Basic implementation of a directional light source. It offers you methods for
 * frustum culling which can be useful for shadow mapping.
 *
 * @see GameObject
 * @see #isInsideFrustum(Vector3f position, float radius)
 */
public class DirectionalLightComponent extends DefaultLightComponent implements DirectionalLight {

    /**
     * Light's projection view matrix.
     */
    private final Matrix4f projectionViewMatrix = new Matrix4f();
    /**
     * Determines wheter the light's data is valid.
     */
    protected boolean valid;
    /**
     * Performs the frustum intersection tests for shadow mapping.
     */
    private final FrustumIntersection frustum = new FrustumIntersection();

    /**
     * Initializes a new DirectionalLightComponent.
     */
    public DirectionalLightComponent() {
    }

    /**
     * Initializes a new DirectionalLightComponent to the given values. All of
     * the parameters's components must be min. 0.
     *
     * @param diffuse diffuse color
     * @param specular specular color
     * @param ambient ambient color
     */
    public DirectionalLightComponent(@NotNull Vector3f diffuse, @NotNull Vector3f specular, @NotNull Vector3f ambient) {
        setDiffuseColor(diffuse);
        setSpecularColor(specular);
        setAmbientColor(ambient);
    }

    /**
     * Returns true if the sphere (determined by the given parameters) is
     * inside, or intersects the frustum and returns false if it is fully
     * outside. Note that if frustum culling is disabled, or this Component
     * isn't connected to a GameObject this method always returns true.
     *
     * @param position position
     * @param radius radius
     * @return false if the sphere is fully outside the frustum, true otherwise
     *
     * @throws NullPointerException position can't be null
     * @throws IllegalArgumentException radius can't be negative
     * @see Settings#isFrustumCulling()
     */
    public boolean isInsideFrustum(@NotNull Vector3f position, float radius) {
        if (position == null) {
            throw new NullPointerException();
        }
        if (radius < 0) {
            throw new IllegalArgumentException("Radius can't be negative");
        }
        if (Settings.isFrustumCulling() && getGameObject() != null) {
            refresh();
            return frustum.testSphere(position, radius);
        } else {
            return true;
        }
    }

    /**
     * Returns true if the axis alligned bounding box (determined by the given
     * parameters) is inside, or intersects the frustum and returns false if it
     * is fully outside. Note that if frustum culling is disabled, or this
     * Component isn't connected to a GameObject this method always returns
     * true.
     *
     * @param aabbMin the axis alligned bounding box's minimum x, y and z values
     * @param aabbMax the axis alligned bounding box's maximum x, y and z values
     * @return false if the bounding box is fully outside the frustum, true
     * otherwise
     *
     * @throws NullPointerException the parameters can't be null
     * @see Settings#isFrustumCulling()
     */
    public boolean isInsideFrustum(@NotNull Vector3f aabbMin, @NotNull Vector3f aabbMax) {
        if (aabbMin == null || aabbMax == null) {
            throw new NullPointerException();
        }
        if (Settings.isFrustumCulling() && getGameObject() != null) {
            refresh();
            return frustum.testAab(aabbMin, aabbMax);
        } else {
            return true;
        }
    }

    /**
     * Refreshes this Component's data if it's invalid.
     */
    private void refresh() {
        if (!valid) {
            if (getGameObject() != null) {
                projectionViewMatrix.set(Utility.computeDirectionalLightProjectionViewMatrix());
                frustum.set(projectionViewMatrix);
            }
            valid = true;
        }
    }

    /**
     * Returns the light's projection view matrix. If this Component isn't
     * connected to a GameObject, this method returns null.
     *
     * @return the light's projection view matrix
     */
    @Nullable @ReadOnly
    public Matrix4f getProjectionViewMatrix() {
        if (getGameObject() != null) {
            refresh();
            return new Matrix4f(projectionViewMatrix);
        } else {
            return null;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        valid = false;
    }

    @Override
    protected void addLightToUbo() {
        if (Scene.getDirectionalLight() != this || getUboIndex() != -1 || ubo == null || !ubo.isUsable()) {
            return;
        }
        setUboIndex(getMaxNumberOfLights());
        updateUbo();
    }

    @Override
    protected void removeLightFromUbo() {
        if (Scene.getDirectionalLight() == this || getUboIndex() == -1 || ubo == null || !ubo.isUsable()) {
            return;
        }
        IntBuffer ib = BufferUtils.createIntBuffer(1);
        ib.put(0);
        ubo.bind();
        ubo.storeData(ib, getUboIndex() * 112 + 108);
        ubo.unbind();
        setUboIndex(-1);
    }

    @Override
    protected void updateUbo() {
        if (this != Scene.getDirectionalLight() || ubo == null || !ubo.isUsable()) {
            return;
        }
        if (getUboIndex() == -1) {
            addLightToUbo();
        }
        temp.position(0);
        //position
        temp.put(-1);
        temp.put(-1);
        temp.put(-1);
        temp.put(-1);
        //direction
        for (int i = 0; i < 3; i++) {
            temp.put(getGameObject().getTransform().getForwardVector().get(i));
        }
        temp.put(-1);
        //attenutation
        temp.put(-1);
        temp.put(-1);
        temp.put(-1);
        temp.put(-1);
        //ambient
        for (int i = 0; i < 3; i++) {
            temp.put(getAmbientColor().get(i));
        }
        temp.put(-1);
        //diffuse
        for (int i = 0; i < 3; i++) {
            temp.put(getDiffuseColor().get(i));
        }
        temp.put(-1);
        //specular
        for (int i = 0; i < 3; i++) {
            temp.put(getSpecularColor().get(i));
        }
        temp.position(0);
        //type, active
        IntBuffer ib = BufferUtils.createIntBuffer(2);
        ib.put(0);
        ib.put(isActive() ? 1 : 0);
        ib.flip();
        ubo.bind();
        ubo.storeData(temp, getUboIndex() * 112);
        ubo.storeData(ib, getUboIndex() * 112 + 104);
        ubo.unbind();
    }

    @Override
    public String toString() {
        return super.toString() + "\nDirectionalLightComponent{"
                + "projectionViewMatrix=\n" + projectionViewMatrix
                + ", valid=" + valid + ", frustum=" + frustum + '}';
    }

}
