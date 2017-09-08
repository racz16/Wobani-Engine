package materials;

import java.util.*;
import renderers.*;
import toolbox.annotations.*;

/**
 * Material for Splines and Meshes. It can work together with any kind of
 * Renderers, you should only store the required slots in it. Note that not all
 * Rendeers support all types of slots, but that isn't a problem if you add
 * unsupported type of slot to a Material, the Renderer will ignore it. If you
 * don't add a slot which is requred by a built-in Renderer, it will use some
 * kind of a default value.
 */
public class Material {

    /**
     * The Material's Renderer class.
     */
    private Class<? extends Renderer> renderer;
    /**
     * The Material's slots.
     */
    private final Map<String, MaterialSlot> slots = new HashMap<>();

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
    public Material(@NotNull Class<? extends Renderer> renderer) {
        if (renderer == null) {
            throw new NullPointerException();
        }
        this.renderer = renderer;
    }

    /**
     * Returns the Material's Renderer class.
     *
     * @return the Material's Renderer class
     */
    @NotNull
    public Class getRenderer() {
        return renderer;
    }

    /**
     * Returns the Material's specified slot. If you use your own Renderer, you
     * can use any String key you want. However if you use the built-in
     * Renderers, you should use these Renderers' specified keys. You can reach
     * these keys as this Material class's static final String values. Note that
     * not all Rendeers support all types of slots, but that isn't a problem if
     * you add unsupported type of slot to a Material, the Renderer will ignore
     * it. If you don't add a slot which is requred by a built-in Renderer, it
     * will use some kind of a default value.
     *
     * @param key slot's key
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
     * own Renderer, you can use any String key you want. However if you use the
     * built-in Renderers, you should use these Renderers' specified keys. You
     * can reach these keys as this Material class's static final String values.
     * Note that not all Rendeers support all types of slots, but that isn't a
     * problem if you add unsupported type of slot to a Material, the Renderer
     * will ignore it. If you don't add a slot which is requred by a built-in
     * Renderer, it will use some kind of a default value.
     *
     * @param key slot's key
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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.renderer);
        hash = 29 * hash + Objects.hashCode(this.slots);
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
        return true;
    }

    @Override
    public String toString() {
        return "Material{" + "renderer=" + renderer + ", slots=" + slots + '}';
    }

}
