package wobani.component.light.blinnphong;

import org.joml.*;
import wobani.toolbox.annotation.*;

public abstract class BlinnPhongPositionalLightComponent extends BlinnPhongLightComponent {

    /**
     * Attenuation's constant component.
     */
    private float constant = 1.0f;
    /**
     * Attenuation's linear component.
     */
    private float linear = 0.022f;
    /**
     * Attenuation's quadratic component.
     */
    private float quadratic = 0.0019f;

    /**
     * Returns the attenuation's constant component.
     *
     * @return attenuation's constant component
     */
    public float getConstant() {
	return constant;
    }

    /**
     * Sets the attenuation's constant component to the given value. In the most
     * cases it's one.
     *
     * @param constant attenuation's constant component
     */
    public void setConstant(float constant) {
	this.constant = constant;
	makeDirty();
    }

    /**
     * Returns the attenuation's linear component.
     *
     * @return attenuation's linear component
     */
    public float getLinear() {
	return linear;
    }

    /**
     * Sets the attenuation's linear component to the given value.
     *
     * @param linear attenuation's linear component
     */
    public void setLinear(float linear) {
	this.linear = linear;
	makeDirty();
    }

    /**
     * Returns the attenuation's quadratic component.
     *
     * @return attenuation's quadratic component
     */
    public float getQuadratic() {
	return quadratic;
    }

    /**
     * Sets the attenuation's quadratic component to the given value.
     *
     * @param quadratic attenuation's quadratic component
     */
    public void setQuadratic(float quadratic) {
	this.quadratic = quadratic;
	makeDirty();
    }

    @Internal
    @Override
    protected void refreshShader() {
	BlinnPhongLightSources.refresh(this);
    }

    private Vector2i tilePosition;

    @Internal @Nullable
    Vector2i getTilePosition() {
	return tilePosition;
    }

    @Internal
    void setTilePosition(@Nullable Vector2i tilePosition) {
	this.tilePosition = tilePosition;
    }

    @Override
    public int hashCode() {
	int hash = 3 + super.hashCode();
	hash = 67 * hash + Float.floatToIntBits(this.constant);
	hash = 67 * hash + Float.floatToIntBits(this.linear);
	hash = 67 * hash + Float.floatToIntBits(this.quadratic);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (!super.equals(obj)) {
	    return false;
	}
	final BlinnPhongPositionalLightComponent other = (BlinnPhongPositionalLightComponent) obj;
	if (Float.floatToIntBits(this.constant) != Float.floatToIntBits(other.constant)) {
	    return false;
	}
	if (Float.floatToIntBits(this.linear) != Float.floatToIntBits(other.linear)) {
	    return false;
	}
	if (Float.floatToIntBits(this.quadratic) != Float.floatToIntBits(other.quadratic)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	StringBuilder res = new StringBuilder()
		.append(super.toString()).append("\n")
		.append(BlinnPhongPositionalLightComponent.class.getSimpleName()).append("(")
		.append("constant: ").append(constant)
		.append(", linear: ").append(linear)
		.append(", quadratic: ").append(quadratic)
		.append(")");
	return res.toString();
    }

}
