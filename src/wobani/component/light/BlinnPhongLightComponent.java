package wobani.component.light;

import java.util.*;
import org.joml.*;
import wobani.core.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 * This abstract class stores the light's diffuse, specular and ambient
 * components.
 *
 * @see GameObject
 */
public abstract class BlinnPhongLightComponent extends Component {

    /**
     * Diffuse color.
     */
    private final Vector3f diffuseColor = new Vector3f(1);
    /**
     * Specular color.
     */
    private final Vector3f specularColor = new Vector3f(1);
    /**
     * Ambient color.
     */
    private final Vector3f ambientColor = new Vector3f(0.1f);
    /**
     * The light's index in the UBO.
     */
    private int shaderIndex = -1;
    
    static{
	BlinnPhongLightSources.initialize();
    }

    /**
     * Returns the diffuse color.
     *
     * @return diffuse color
     */
    @NotNull @ReadOnly
    public Vector3f getDiffuseColor() {
	return new Vector3f(diffuseColor);
    }

    /**
     * Sets the diffuse color to the given value. All of diffuse color's
     * components must be min. 0.
     *
     * @param diffuse diffuse color
     *
     * @throws IllegalArgumentException color can't be lower than 0
     */
    public void setDiffuseColor(@NotNull Vector3f diffuse) {
	if (!Utility.isHdrColor(diffuse)) {
	    throw new IllegalArgumentException("Diffuse color can't be lower than 0");
	}
	this.diffuseColor.set(diffuse);
	refreshShader();
    }

    /**
     * Returns the specular color.
     *
     * @return specular color
     */
    @NotNull @ReadOnly
    public Vector3f getSpecularColor() {
	return new Vector3f(specularColor);
    }

    /**
     * Sets the specular color to the given value. All of specular color's
     * components must be min. 0.
     *
     * @param specular specular color
     *
     * @throws IllegalArgumentException color can't be lower than 0
     */
    public void setSpecularColor(@NotNull Vector3f specular) {
	if (!Utility.isHdrColor(specular)) {
	    throw new IllegalArgumentException("Specular color can't be lower than 0");
	}
	this.specularColor.set(specular);
	refreshShader();
    }

    /**
     * Returns the ambient color.
     *
     * @return ambient color
     */
    @NotNull @ReadOnly
    public Vector3f getAmbientColor() {
	return new Vector3f(ambientColor);
    }

    /**
     * Sets the ambient color to the given value. All of ambient color's
     * components must be min. 0.
     *
     * @param ambient ambient color
     *
     * @throws IllegalArgumentException color can't be lower than 0
     */
    public void setAmbientColor(@NotNull Vector3f ambient) {
	if (!Utility.isHdrColor(ambient)) {
	    throw new IllegalArgumentException("Ambient color can't be lower than 0");
	}
	this.ambientColor.set(ambient);
	refreshShader();
    }

    @Override
    protected void detachFromGameObject() {
	getGameObject().getTransform().removeInvalidatable(this);
	super.detachFromGameObject();
	invalidate();
    }

    @Override
    protected void attachToGameObject(@NotNull GameObject g) {
	super.attachToGameObject(g);
	getGameObject().getTransform().addInvalidatable(this);
	invalidate();
    }

    @Override
    public void setActive(boolean active) {
	super.setActive(active);
	refreshShader();
    }

    @Override
    public void invalidate() {
	super.invalidate();
	refreshShader();
    }

    /**
     * Returns the light's UBO index.
     *
     * @return the light's UBO index.
     */
    @Internal
    int getShaderIndex() {
	return shaderIndex;
    }

    /**
     * Sets the light's UBO index to the given value.
     *
     * @param index new UBO index
     */
    @Internal
    void setShaderIndex(int index) {
	shaderIndex = index;

    }

    /**
     * Refreshes the light in the UBO.
     */
    @Internal
    protected abstract void refreshShader();

    @Override
    public int hashCode() {
	int hash = 7 + super.hashCode();
	hash = 11 * hash + Objects.hashCode(this.diffuseColor);
	hash = 11 * hash + Objects.hashCode(this.specularColor);
	hash = 11 * hash + Objects.hashCode(this.ambientColor);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (!super.equals(obj)) {
	    return false;
	}
	final BlinnPhongLightComponent other = (BlinnPhongLightComponent) obj;
	if (!Objects.equals(this.diffuseColor, other.diffuseColor)) {
	    return false;
	}
	if (!Objects.equals(this.specularColor, other.specularColor)) {
	    return false;
	}
	if (!Objects.equals(this.ambientColor, other.ambientColor)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	StringBuilder res = new StringBuilder()
		.append(super.toString()).append("\n")
		.append(BlinnPhongLightComponent.class.getSimpleName()).append("(")
		.append(" diffuse color: ").append(diffuseColor)
		.append(", specular color: ").append(specularColor)
		.append(", ambient color: ").append(ambientColor)
		.append(")");
	return res.toString();
    }

}
