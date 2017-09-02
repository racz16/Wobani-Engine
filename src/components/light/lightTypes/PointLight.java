package components.light.lightTypes;

/**
 * This interface signs that the Component is a point light source.
 */
public interface PointLight extends Light {

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
