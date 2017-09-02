package components.light;

import static components.light.DefaultLightComponent.ubo;
import components.light.lightTypes.*;
import core.*;

/**
 * Basic implementation of a spot light source.
 *
 * @see GameObject
 */
//TODO shadow mapping, frustum culling
public class SpotLightComponent extends DefaultLightComponent implements SpotLight {

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
        updateUbo();
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
        updateUbo();
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
        updateUbo();
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
     * and lower than the outer cutoff component
     */
    public void setCutoff(float cutoff) {
        if (cutoff <= 0 || cutoff >= outerCutoff) {
            throw new IllegalArgumentException("Cutoff component must be higher than 0 and lower than the outer cutoff component");
        }
        this.cutoff = cutoff;
        updateUbo();
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
     * than the cutoff component
     */
    public void setOuterCutoff(float outerCutoff) {
        if (cutoff >= outerCutoff) {
            throw new IllegalArgumentException("Cutoff component must be lower than the outer cutoff component");
        }
        this.outerCutoff = outerCutoff;
        updateUbo();
    }

    @Override
    protected void updateUbo() {
        if (getUboIndex() == -1 || getGameObject() == null || ubo == null || !ubo.isUsable()) {
            return;
        }
        floatBuffer.position(0);
        //position
        for (int i = 0; i < 3; i++) {
            floatBuffer.put(getGameObject().getTransform().getAbsolutePosition().get(i));
        }
        floatBuffer.put(-1);
        //direction
        for (int i = 0; i < 3; i++) {
            floatBuffer.put(getGameObject().getTransform().getForwardVector().get(i));
        }
        floatBuffer.put(-1);
        //attenutation
        floatBuffer.put(getConstant());
        floatBuffer.put(getLinear());
        floatBuffer.put(getQuadratic());
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
        floatBuffer.put(-1);
        //cutoff
        floatBuffer.put((float) java.lang.Math.cos(java.lang.Math.toRadians(getCutoff())));
        floatBuffer.put((float) java.lang.Math.cos(java.lang.Math.toRadians(getOuterCutoff())));
        floatBuffer.position(0);
        //type, active
        intBuffer.limit(2);
        intBuffer.position(0);
        intBuffer.put(2);
        intBuffer.put(isActive() ? 1 : 0);
        intBuffer.position(0);
        ubo.bind();
        ubo.storeData(floatBuffer, getUboIndex() * 112);
        ubo.storeData(intBuffer, getUboIndex() * 112 + 104);
        ubo.unbind();
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
        final SpotLightComponent other = (SpotLightComponent) obj;
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
