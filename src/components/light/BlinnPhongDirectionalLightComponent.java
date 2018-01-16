package components.light;

import components.light.lightTypes.*;
import core.*;
import org.joml.*;
import toolbox.annotations.*;
import toolbox.parameters.*;

/**
 * Basic implementation of a directional light source.
 *
 * @see GameObject
 */
public class BlinnPhongDirectionalLightComponent extends BlinnPhongLightComponent implements BlinnPhongDirectionalLight {

    /**
     * Initializes a new DirectionalLightComponent.
     */
    public BlinnPhongDirectionalLightComponent() {
    }

    /**
     * Initializes a new DirectionalLightComponent to the given values. All of
     * the parameters's components must be min. 0.
     *
     * @param diffuse  diffuse color
     * @param specular specular color
     * @param ambient  ambient color
     */
    public BlinnPhongDirectionalLightComponent(@NotNull Vector3f diffuse, @NotNull Vector3f specular, @NotNull Vector3f ambient) {
        setDiffuseColor(diffuse);
        setSpecularColor(specular);
        setAmbientColor(ambient);
    }

    @Internal
    @Override
    protected void addLight() {
        if (getMainLight() == this && getUboIndex() == -1) {
            BlinnPhongLightSources.addLight(this);
        }
    }

    @Internal
    @Override
    protected void removeLight() {
        if (getMainLight() != this && getUboIndex() != -1) {
            BlinnPhongLightSources.removeLight(this);
        }
    }

    @Internal
    @Override
    protected void refreshUbo() {
        if (getMainLight() == this) {
            if (getUboIndex() == -1) {
                addLight();
            } else {
                BlinnPhongLightSources.refreshLight(this);
            }
        }
    }

    /**
     * Returns the Scene's main directional light.
     *
     * @return the Scene's main directional light
     */
    private BlinnPhongDirectionalLight getMainLight() {
        BlinnPhongMainDirectionalLight dirLight = Scene.getParameters().getParameter(BlinnPhongMainDirectionalLight.class);
        BlinnPhongDirectionalLight light = dirLight == null ? null : dirLight.getValue();
        return light;
    }

}
