package wobani.component.light.blinnphong;

import java.nio.*;
import java.util.*;
import org.joml.*;
import wobani.core.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 * This abstract class stores the light's diffuse, specular and ambient
 * components.
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
     * The light's index in the shader.
     */
    private int shaderIndex = -1;
    /**
     * Helps in the communication with the VGA.
     */
    private static final BlinnPhongShaderHelper HELPER = new BlinnPhongShaderHelper();

    /**
     * Returns a helper object which can help in the communication with the VGA.
     *
     * @return helper obeject
     */
    @NotNull
    protected BlinnPhongShaderHelper getHelper() {
	return HELPER;
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
	makeDirty();
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
	makeDirty();
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
	makeDirty();
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
	makeDirty();
    }

    @Override
    public void invalidate() {
	super.invalidate();
	makeDirty();
    }

    /**
     * Returns the light's index in the shader.
     *
     * @return the light's index in the shader
     */
    @Internal
    protected int getShaderIndex() {
	return shaderIndex;
    }

    /**
     * Sets the light's shader index to the given value.
     *
     * @param index new shader index
     */
    @Internal
    protected void setShaderIndex(int index) {
	shaderIndex = index;
    }

    /**
     * Signs that the at least one of the light's properties changed and have to
     * update it in the shader.
     */
    @Internal
    protected void makeDirty() {
	BlinnPhongLightSources.makeDirty(this);
    }

    /**
     * Refreshes the light in the VRAM.
     */
    @Internal
    protected abstract void refreshLightInVram();

    /**
     * Creates a FloatBuffer which contains all the light's parameters like
     * diffuse color, position etc. The FloatBuffer's content must follow the
     * memory layout of the shader's light struct.
     *
     * @return the light's parameters
     */
    @Internal
    protected abstract FloatBuffer computeLightParameters();

    /**
     * Creates an IntBuffer which contains all the light's meta data like the
     * light's type etc. The IntBuffer's content must follow the memory layout
     * of the shader's light struct.
     *
     * @return the light's meta data
     */
    @Internal @NotNull
    protected IntBuffer computeLightMetadata() {
	getHelper().setIntBufferPosition(0);
	getHelper().setIntBufferLimit(2);
	getHelper().setMetaData(getLightShaderType(), isActive());
	getHelper().setIntBufferPosition(0);
	return getHelper().getIntBuffer();
    }

    /**
     * Returns the light's integer type used in the shader.
     *
     * @return the light's integer type
     */
    @Internal
    protected abstract int getLightShaderType();

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
