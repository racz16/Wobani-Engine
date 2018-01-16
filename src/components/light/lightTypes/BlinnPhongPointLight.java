package components.light.lightTypes;

/**
 * This interface signs that the Component is a Blinn-Phong point light source.
 */
public interface BlinnPhongPointLight extends BlinnPhongLight {

    /**
     * Returns the attenuation's constant component.
     *
     * @return attenuation's constant component
     */
    public float getConstant();

    /**
     * Returns the attenuation's linear component.
     *
     * @return attenuation's linear component
     */
    public float getLinear();

    /**
     * Returns the attenuation's quadratic component.
     *
     * @return attenuation's quadratic component
     */
    public float getQuadratic();
}
