package materials;

import rendering.geometry.GeometryRenderer;
import java.util.*;
import org.joml.*;
import toolbox.annotations.*;

/**
 * Material for Splines and Meshes. It can work together with any kind of
 Renderers, you should only store the required slots in it. Note that not all
 Rendeers support all types of slots, but that isn't a problem if you add
 unsupported type of slot to a Material, the GeometryRenderer will ignore it. If you
 don't add a slot which is requred by a built-in GeometryRenderer, it will use some
 kind of a default value.
 */
public class Material {

    /**
     * The Material's GeometryRenderer class.
     */
    private Class<? extends GeometryRenderer> renderer;
    /**
     * The Material's slots.
     */
    private final Map<String, MaterialSlot> slots = new HashMap<>();
    /**
     * Slot's float parameters.
     */
    private final Map<String, Float> floatParameters = new HashMap<>();
    /**
     * Slot's vector parameters.
     */
    private final Map<String, Vector4f> vectorParameters = new HashMap<>();
    /**
     * Slot's String parameters.
     */
    private final Map<String, String> stringParameters = new HashMap<>();

    /**
     * Determines whether the shader use the specular map's or the specular
     * color's alpha chanel as a glossiness value.
     */
    public static final String PARAM_USE_GLOSSINESS_F = "PARAM_USE_GLOSSINESS_F";
    /**
     * Determines whether the shader use the normal map's alpha chanel as a
     * parallax map.
     */
    public static final String PARAM_USE_POM_F = "PARAM_USE_POM_F";
    /**
     * Parallax occlussion map's scale factor.
     */
    public static final String PARAM_POM_SCALE_F = "PARAM_POM_SCALE_F";
    /**
     * Parallax occlussion map's minimum layers.
     */
    public static final String PARAM_POM_MIN_LAYERS_F = "PARAM_POM_MIN_LAYERS_F";
    /**
     * Parallax occlussion map's maximum layers.
     */
    public static final String PARAM_POM_MAX_LAYERS_F = "PARAM_POM_MAX_LAYERS_F";
    /**
     * Refractive material's refraction index.
     */
    public static final String PARAM_REFRACTION_INDEX_F = "PARAM_REFRACTION_INDEX_F";

    /**
     * Diffuse slot's key.
     */
    public static final String DIFFUSE = "DIFFUSE";
    /**
     * Specular slot's key.
     */
    public static final String SPECULAR = "SPECULAR";
    /**
     * Normal slot's key.
     */
    public static final String NORMAL = "NORMAL";
    /**
     * Reflection slot's key.
     */
    public static final String REFLECTION = "REFLECTION";
    /**
     * Refraction slot's key.
     */
    public static final String REFRACTION = "REFRACTION";
    /**
     * Environtment intensity slot's key.
     */
    public static final String ENVIRONTMENT_INTENSITY = "ENVIRONTMENT_INTENSITY";
    /**
     * Metalness slot's key.
     */
    public static final String METALNESS = "METALNESS";
    /**
     * Roughness slot's key.
     */
    public static final String ROUGHNESS = "ROUGHNESS";
    /**
     * Ambient occlusion slot's key.
     */
    public static final String AMBIENT_OCCLUSION = "AMBIENT_OCCLUSION";

    /**
     * Initializes a new Material to the given value.
     *
     * @param renderer material's renderer
     *
     * @throws NullPointerException renderer can't be null
     */
    public Material(@NotNull Class<? extends GeometryRenderer> renderer) {
        if (renderer == null) {
            throw new NullPointerException();
        }
        this.renderer = renderer;
    }

    /**
     * Returns the Material's GeometryRenderer class.
     *
     * @return the Material's GeometryRenderer class
     */
    @NotNull
    public Class<? extends GeometryRenderer> getRenderer() {
        return renderer;
    }

    /**
     * Returns the Material's specified slot. If you use your own GeometryRenderer, you
 can use any String key you want. However if you use the built-in
 Renderers, you should use these Renderers' specified keys. You can reach
 these keys as this Material class's static final String values. Note that
 not all Rendeers support all types of slots, but that isn't a problem if
 you add unsupported type of slot to a Material, the GeometryRenderer will ignore
 it. If you don't add a slot which is requred by a built-in GeometryRenderer, it
 will use some kind of a default value.
     *
     * @param key slot's key
     *
     * @return the Material's specified slot
     *
     * @throws NullPointerException the key can't be null
     */
    @Nullable
    public MaterialSlot getSlot(@NotNull String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return slots.get(key);
    }

    /**
     * Sets the Material's specified slot to the given value. If you use your
 own GeometryRenderer, you can use any String key you want. However if you use the
 built-in Renderers, you should use these Renderers' specified keys. You
 can reach these keys as this Material class's static final String values.
 Note that not all Rendeers support all types of slots, but that isn't a
 problem if you add unsupported type of slot to a Material, the GeometryRenderer
 will ignore it. If you don't add a slot which is requred by a built-in
 GeometryRenderer, it will use some kind of a default value.
     *
     * @param key  slot's key
     * @param slot Material's slot
     *
     * @throws NullPointerException the key can't be null
     */
    public void setSlot(@NotNull String key, @Nullable MaterialSlot slot) {
        if (key == null) {
            throw new NullPointerException();
        }
        slots.put(key, slot);
    }

    //
    //parameters----------------------------------------------------------------
    //
    /**
     * Returns the specified key's Float value. Float, String and vector
     * parameters use distinct keys and values.
     *
     * @param key key
     *
     * @return the specified key's Float value
     *
     * @throws NullPointerException key can't be null
     * @see #PARAM_POM_SCALE_F
     * @see #PARAM_POM_MIN_LAYERS_F
     * @see #PARAM_POM_MAX_LAYERS_F
     */
    @Nullable
    public Float getFloatParameter(@NotNull String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return floatParameters.get(key);
    }

    /**
     * Sets the specified key's value to the given Float parameter. Note that
     * the it doesn't check whether or not given value is valid so try to give
     * sensable values. Float, String and vector parameters use distinct keys
     * and values.
     *
     * @param key   key
     * @param value value
     *
     * @throws NullPointerException key can't be null
     */
    public void setFloatParameter(@NotNull String key, @Nullable Float value) {
        if (key == null) {
            throw new NullPointerException();
        }
        floatParameters.put(key, value);
    }

    /**
     * Returns the specified key's String value. Float, String and vector
     * parameters use distinct keys and values.
     *
     * @param key key
     *
     * @return the specified key's String value
     *
     * @throws NullPointerException key can't be null
     */
    @Nullable
    public String getStringParameter(@NotNull String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return stringParameters.get(key);
    }

    /**
     * Sets the specified key's value to the given String parameter. Note that
     * the it doesn't check whether or not given value is valid so try to give
     * sensable values. Float, String and vector parameters use distinct keys
     * and values.
     *
     * @param key   key
     * @param value value
     *
     * @throws NullPointerException key can't be null
     */
    public void setStringParameter(@NotNull String key, @Nullable String value) {
        if (key == null) {
            throw new NullPointerException();
        }
        stringParameters.put(key, value);
    }

    /**
     * Returns the specified key's vector value. Float, String and vector
     * parameters use distinct keys and values.
     *
     * @param key key
     *
     * @return the specified key's vector value
     *
     * @throws NullPointerException key can't be null
     */
    @Nullable
    public Vector4f getVectorParameter(@NotNull String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return vectorParameters.get(key);
    }

    /**
     * Sets the specified key's value to the given vector parameter. Note that
     * the it doesn't check whether or not given value is valid so try to give
     * sensable values. Float, String and vector parameters use distinct keys
     * and values.
     *
     * @param key   key
     * @param value value
     *
     * @throws NullPointerException key can't be null
     */
    public void setVectorParameter(@NotNull String key, @Nullable Vector4f value) {
        if (key == null) {
            throw new NullPointerException();
        }
        vectorParameters.put(key, value);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.renderer);
        hash = 97 * hash + Objects.hashCode(this.slots);
        hash = 97 * hash + Objects.hashCode(this.floatParameters);
        hash = 97 * hash + Objects.hashCode(this.vectorParameters);
        hash = 97 * hash + Objects.hashCode(this.stringParameters);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Material other = (Material) obj;
        if (!Objects.equals(this.renderer, other.renderer)) {
            return false;
        }
        if (!Objects.equals(this.slots, other.slots)) {
            return false;
        }
        if (!Objects.equals(this.floatParameters, other.floatParameters)) {
            return false;
        }
        if (!Objects.equals(this.vectorParameters, other.vectorParameters)) {
            return false;
        }
        if (!Objects.equals(this.stringParameters, other.stringParameters)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Material{" + "renderer=" + renderer + ", slots=" + slots
                + ", floatParameters=" + floatParameters + ", vectorParameters="
                + vectorParameters + ", stringParameters=" + stringParameters + '}';
    }

}
