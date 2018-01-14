package components.light;

import static components.light.DefaultLightComponent.ubo;
import components.light.lightTypes.*;
import core.*;
import org.joml.*;
import toolbox.annotations.*;
import toolbox.parameters.*;

/**
 * Basic implementation of a directional light source. It offers you methods for
 * frustum culling which can be useful for shadow mapping.
 *
 * @see GameObject
 * @see #isInsideFrustum(Vector3f position, float radius)
 */
public class DirectionalLightComponent extends DefaultLightComponent implements DirectionalLight {

    /**
     * Determines wheter the light's data is valid.
     */
    protected boolean valid;

    /**
     * Initializes a new DirectionalLightComponent.
     */
    public DirectionalLightComponent() {
    }

    /**
     * Initializes a new DirectionalLightComponent to the given values. All of
     * the parameters's components must be min. 0.
     *
     * @param diffuse  diffuse color
     * @param specular specular color
     * @param ambient  ambient color
     */
    public DirectionalLightComponent(@NotNull Vector3f diffuse, @NotNull Vector3f specular, @NotNull Vector3f ambient) {
        setDiffuseColor(diffuse);
        setSpecularColor(specular);
        setAmbientColor(ambient);
    }

    @Override
    protected void addLightToUbo() {
        MainDirectionalLight dirLight = Scene.getParameters().getParameter(MainDirectionalLight.class);
        DirectionalLight light = dirLight == null ? null : dirLight.getValue();
        if (light != this || getUboIndex() != -1 || ubo == null || !ubo.isUsable()) {
            return;
        }
        setUboIndex(getMaxNumberOfLights());
        updateUbo();
    }

    @Override
    protected void removeLightFromUbo() {
        MainDirectionalLight dirLight = Scene.getParameters().getParameter(MainDirectionalLight.class);
        DirectionalLight light = dirLight == null ? null : dirLight.getValue();
        if (light == this || getUboIndex() == -1 || ubo == null || !ubo.isUsable()) {
            return;
        }
        intBuffer.limit(1);
        intBuffer.position(0);
        intBuffer.put(0);
        intBuffer.position(0);
        ubo.bind();
        ubo.storeData(intBuffer, getUboIndex() * 112 + 108);
        ubo.unbind();
        setUboIndex(-1);
    }

    @Override
    protected void updateUbo() {
        MainDirectionalLight dirLight = Scene.getParameters().getParameter(MainDirectionalLight.class);
        DirectionalLight light = dirLight == null ? null : dirLight.getValue();
        if (this != light || ubo == null || !ubo.isUsable()) {
            return;
        }
        if (getUboIndex() == -1) {
            addLightToUbo();
        }
        floatBuffer.position(0);
        //position
        floatBuffer.put(-1);
        floatBuffer.put(-1);
        floatBuffer.put(-1);
        floatBuffer.put(-1);
        //direction
        for (int i = 0; i < 3; i++) {
            floatBuffer.put(getGameObject().getTransform().getForwardVector().get(i));
        }
        floatBuffer.put(-1);
        //attenutation
        floatBuffer.put(-1);
        floatBuffer.put(-1);
        floatBuffer.put(-1);
        floatBuffer.put(-1);
        //ambient
        for (int i = 0; i < 3; i++) {
            floatBuffer.put(getAmbientColor().get(i));
        }
        floatBuffer.put(-1);
        //diffuse
        for (int i = 0; i < 3; i++) {
            floatBuffer.put(getDiffuseColor().get(i));
        }
        floatBuffer.put(-1);
        //specular
        for (int i = 0; i < 3; i++) {
            floatBuffer.put(getSpecularColor().get(i));
        }
        floatBuffer.position(0);
        //type, active
        intBuffer.limit(2);
        intBuffer.position(0);
        intBuffer.put(0);
        intBuffer.put(isActive() ? 1 : 0);
        intBuffer.position(0);
        ubo.bind();
        ubo.storeData(floatBuffer, getUboIndex() * 112);
        ubo.storeData(intBuffer, getUboIndex() * 112 + 104);
        ubo.unbind();
    }

    @Override
    public String toString() {
        return "";
        //FIXME dirlight toString
//        return super.toString() + "\nDirectionalLightComponent{"
//                + "projectionViewMatrix=\n" + projectionViewMatrix
//                + ", valid=" + valid + ", frustum=" + frustum + '}';
    }

}
