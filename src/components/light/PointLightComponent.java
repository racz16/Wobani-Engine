package components.light;

import static components.light.DefaultLightComponent.ubo;
import components.light.lightTypes.*;
import core.*;

/**
 * Basic implementation of a point light source.
 *
 * @see GameObject
 */
//TODO shadow mapping, frustum culling
public class PointLightComponent extends DefaultLightComponent implements PointLight {

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
        floatBuffer.put(-1);
        floatBuffer.put(-1);
        floatBuffer.put(-1);
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
        floatBuffer.position(0);
        //type, active
        intBuffer.limit(2);
        intBuffer.position(0);
        intBuffer.put(1);
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
        final PointLightComponent other = (PointLightComponent) obj;
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
        return super.toString() + "\nPointLightComponent{"
                + "constant=" + constant + ", linear=" + linear
                + ", quadratic=" + quadratic + '}';
    }

}
