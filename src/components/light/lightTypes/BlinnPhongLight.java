package components.light.lightTypes;

import components.*;
import org.joml.*;

/**
 * This interface signs that the Component is a Blinn-Phong light source.
 */
public interface BlinnPhongLight extends IComponent {

    /**
     * Returns the diffuse color.
     *
     * @return diffuse color
     */
    public Vector3f getDiffuseColor();

    /**
     * Returns the specular color.
     *
     * @return specular color
     */
    public Vector3f getSpecularColor();

    /**
     * Returns the ambient color.
     *
     * @return ambient color
     */
    public Vector3f getAmbientColor();

}
