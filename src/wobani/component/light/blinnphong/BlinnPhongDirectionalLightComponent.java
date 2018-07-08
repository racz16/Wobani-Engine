package wobani.component.light.blinnphong;

import java.nio.*;
import org.joml.*;
import wobani.core.*;
import wobani.rendering.geometry.*;
import wobani.toolbox.annotation.*;

/**
 * Basic implementation of a directional light source.
 */
public class BlinnPhongDirectionalLightComponent extends BlinnPhongLightComponent {

    /**
     * Initializes a new BlinnPhongDirectionalLightComponent.
     */
    public BlinnPhongDirectionalLightComponent() {
    }

    /**
     * Initializes a new BlinnPhongDirectionalLightComponent to the given
     * values. All of the parameters's components must be min. 0.
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
    protected void refreshLightInVram() {
	BlinnPhongLightSources.refresh(this);
    }

    @Internal @NotNull
    @Override
    protected FloatBuffer computeLightParameters() {
	getHelper().setFloatBufferPosition(0);
	getHelper().setFloatBufferLimit(16);
	getHelper().setColor(getDiffuseColor(), getSpecularColor(), getAmbientColor());
	getHelper().setDirection(getGameObject().getTransform().getForwardVector());
	getHelper().setFloatBufferPosition(0);
	return getHelper().getFloatBuffer();
    }

    @Override
    protected int getLightShaderType() {
	return 0;
    }

    /**
     * Returns true if it's the Scene's main directional light.
     *
     * @return true if it's the Scene's main directional light, false otherwise
     */
    public boolean isTheMainDirectionalLight() {
	BlinnPhongDirectionalLightComponent light = Scene.getParameters().getValue(BlinnPhongRenderer.MAIN_DIRECTIONAL_LIGHT);
	return light == this;
    }

    @Override
    public String toString() {
	StringBuilder res = new StringBuilder()
		.append(super.toString()).append("\n")
		.append(BlinnPhongDirectionalLightComponent.class.getSimpleName()).append("(")
		.append(")");
	return res.toString();
    }

}
