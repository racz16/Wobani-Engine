package wobani.components.light;

import org.joml.*;
import wobani.core.*;
import wobani.rendering.geometry.*;
import wobani.toolbox.annotations.*;

/**
 * Basic implementation of a directional light source.
 *
 * @see GameObject
 */
public class BlinnPhongDirectionalLightComponent extends BlinnPhongLightComponent {

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
	if (isTheMainDirectionalLight() && getUboIndex() == -1) {
	    BlinnPhongLightSources.addLight(this);
	}
    }

    @Internal
    @Override
    protected void removeLight() {
	if (!isTheMainDirectionalLight() && getUboIndex() != -1) {
	    BlinnPhongLightSources.removeLight(this);
	}
    }

    @Internal
    @Override
    protected void refreshUbo() {
	if (isTheMainDirectionalLight()) {
	    if (getUboIndex() == -1) {
		addLight();
	    } else {
		BlinnPhongLightSources.refreshLight(this);
	    }
	}
    }

    /**
     * Returns true if it's the Scene's main directional light.
     *
     * @return true if it's the Scene's main directional light, false otherwise
     */
    private boolean isTheMainDirectionalLight() {
	BlinnPhongDirectionalLightComponent light = Scene.getParameters().getValue(BlinnPhongRenderer.MAIN_DIRECTIONAL_LIGHT);
	return light == this;
    }

    @Override
    public String toString() {
	StringBuilder res = new StringBuilder()
		.append(super.toString()).append("\n")
		.append("BlinnPhongDirectionalLightComponent(")
		.append(")");
	return res.toString();
    }

}
