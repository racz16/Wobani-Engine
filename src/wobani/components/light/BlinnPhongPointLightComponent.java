package wobani.components.light;

import wobani.core.*;
import wobani.toolbox.annotations.*;

/**
 * Basic implementation of a point light source.
 *
 * @see GameObject
 */
public class BlinnPhongPointLightComponent extends BlinnPhongLightComponent {

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
        refreshUbo();
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
        refreshUbo();
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
        refreshUbo();
    }

    @Internal
    @Override
    protected void refreshUbo() {
        if (getGameObject() != null && getUboIndex() != -1) {
            BlinnPhongLightSources.refreshLight(this);
        }
    }

    @Internal
    @Override
    protected void removeLight() {
        if (getGameObject() == null && getUboIndex() != -1) {
            BlinnPhongLightSources.removeLight(this);
        }
    }

    @Internal
    @Override
    protected void addLight() {
        if (getGameObject() != null && getUboIndex() == -1) {
            BlinnPhongLightSources.addLight(this);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5 + super.hashCode();
        hash = 43 * hash + Float.floatToIntBits(this.constant);
        hash = 43 * hash + Float.floatToIntBits(this.linear);
        hash = 43 * hash + Float.floatToIntBits(this.quadratic);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final BlinnPhongPointLightComponent other = (BlinnPhongPointLightComponent) obj;
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
                .append("BlinnPhongPointLightComponent(")
                .append(")");
        return res.toString();
    }

}
