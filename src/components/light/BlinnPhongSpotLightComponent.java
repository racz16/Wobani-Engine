package components.light;

import components.light.lightTypes.*;
import core.*;
import toolbox.annotations.*;

/**
 * Basic implementation of a spot light source.
 *
 * @see GameObject
 */
public class BlinnPhongSpotLightComponent extends BlinnPhongLightComponent implements BlinnPhongSpotLight {

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
     * Cutoff component (in degrees).
     */
    private float cutoff = 12.5f;
    /**
     * Outer cutoff component (in degrees).
     */
    private float outerCutoff = 15.0f;

    @Override
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

    @Override
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

    @Override
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

    /**
     * Returns the cutoff component.
     *
     * @return cutoff component (in degrees)
     */
    @Override
    public float getCutoff() {
        return cutoff;
    }

    /**
     * Sets the cutoff component to the given value. Note that the ccutoff
     * component must be higher than 0 and lower than the outer cutoff
     * component.
     *
     * @param cutoff cutoff component (in degrees)
     *
     * @throws IllegalArgumentException cutoff component must be higher than 0
     *                                  and lower than the outer cutoff
     *                                  component
     */
    public void setCutoff(float cutoff) {
        if (cutoff <= 0 || cutoff >= outerCutoff) {
            throw new IllegalArgumentException("Cutoff component must be higher than 0 and lower than the outer cutoff component");
        }
        this.cutoff = cutoff;
        refreshUbo();
    }

    /**
     * Returns the outer cutoff component.
     *
     * @return outer cutoff component (in degrees)
     */
    @Override
    public float getOuterCutoff() {
        return outerCutoff;
    }

    /**
     * Sets the outer cutoff component to the given value. Note that the outer
     * cutoff component must be higher than the cutoff component.
     *
     * @param outerCutoff outer cutoff component (in degrees)
     *
     * @throws IllegalArgumentException outer cutoff component must be higher
     *                                  than the cutoff component
     */
    public void setOuterCutoff(float outerCutoff) {
        if (cutoff >= outerCutoff) {
            throw new IllegalArgumentException("Cutoff component must be lower than the outer cutoff component");
        }
        this.outerCutoff = outerCutoff;
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
        hash = 61 * hash + Float.floatToIntBits(this.constant);
        hash = 61 * hash + Float.floatToIntBits(this.linear);
        hash = 61 * hash + Float.floatToIntBits(this.quadratic);
        hash = 61 * hash + Float.floatToIntBits(this.cutoff);
        hash = 61 * hash + Float.floatToIntBits(this.outerCutoff);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final BlinnPhongSpotLightComponent other = (BlinnPhongSpotLightComponent) obj;
        if (Float.floatToIntBits(this.constant) != Float.floatToIntBits(other.constant)) {
            return false;
        }
        if (Float.floatToIntBits(this.linear) != Float.floatToIntBits(other.linear)) {
            return false;
        }
        if (Float.floatToIntBits(this.quadratic) != Float.floatToIntBits(other.quadratic)) {
            return false;
        }
        if (Float.floatToIntBits(this.cutoff) != Float.floatToIntBits(other.cutoff)) {
            return false;
        }
        if (Float.floatToIntBits(this.outerCutoff) != Float.floatToIntBits(other.outerCutoff)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nSpotLightComponent{" + "constant=" + constant
                + ", linear=" + linear + ", quadratic=" + quadratic + ", cutoff=" + cutoff
                + ", outerCutoff=" + outerCutoff + '}';
    }

}
