package wobani.material;

import wobani.rendering.geometry.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.parameter.*;

import java.util.*;

/**
 Material for Splines and Meshes. It can work together with any kind of Renderers, you should only store2D the required
 slots in it. Note that not all Renderers support all types of slots.
 */
public class Material{

    /**
     Refractive material's refraction index.
     */
    public static final ParameterKey<Float> REFRACTION_INDEX = new ParameterKey<>(Float.class, "REFRACTION_INDEX");
    /**
     Diffuse slot's key.
     */
    public static final ParameterKey<MaterialSlot> DIFFUSE = new ParameterKey<>(MaterialSlot.class, "DIFFUSE");
    /**
     Specular slot's key.
     */
    public static final ParameterKey<MaterialSlot> SPECULAR = new ParameterKey<>(MaterialSlot.class, "SPECULAR");
    /**
     Normal slot's key.
     */
    public static final ParameterKey<MaterialSlot> NORMAL = new ParameterKey<>(MaterialSlot.class, "NORMAL");
    /**
     Reflection slot's key.
     */
    public static final ParameterKey<MaterialSlot> REFLECTION = new ParameterKey<>(MaterialSlot.class, "REFLECTION");
    /**
     Refraction slot's key.
     */
    public static final ParameterKey<MaterialSlot> REFRACTION = new ParameterKey<>(MaterialSlot.class, "REFRACTION");
    /**
     Environment intensity slot's key. The r channel means the object's color, the g channel means the reflection
     intensity and the b channel means the refraction intensity.
     */
    public static final ParameterKey<MaterialSlot> ENVIRONMENT_INTENSITY = new ParameterKey<>(MaterialSlot.class, "ENVIRONMENT_INTENSITY");
    /**
     The Material's slots.
     */
    private final ParameterContainer slots = new ParameterContainer();
    /**
     The Material's parameters.
     */
    private final ParameterContainer parameters = new ParameterContainer();
    /**
     The Material's GeometryRenderer class.
     */
    private Class<? extends GeometryRenderer> renderer;

    /**
     Initializes a new Material to the given value.

     @param renderer Material's GeometryRenderer class

     @throws NullPointerException GeometryRenderer class can't be null
     */
    public Material(@NotNull Class<? extends GeometryRenderer> renderer){
        if(renderer == null){
            throw new NullPointerException();
        }
        this.renderer = renderer;
    }

    /**
     Returns the Material's GeometryRenderer class.

     @return the Material's GeometryRenderer class
     */
    @NotNull
    public Class<? extends GeometryRenderer> getRenderer(){
        return renderer;
    }

    /**
     Returns the Material's specified slot. You can reach the built-in slots' keys as this Material class's public static
     variables. Note that not all Renderers support all types of slots.

     @param key slot's key

     @return the Material's specified slot

     @throws NullPointerException the key can't be null
     */
    @Nullable
    public MaterialSlot getSlot(@NotNull ParameterKey<MaterialSlot> key){
        if(key == null){
            throw new NullPointerException();
        }
        return slots.getValue(key);
    }

    /**
     Sets the Material's specified slot to the given value. You can reach the built-in slots' keys as this Material
     class's public static variables. Note that not all Renderers support all types of slots.

     @param key  slot's key
     @param slot Material's slot

     @throws NullPointerException the key can't be null
     */
    public void setSlot(@NotNull ParameterKey<MaterialSlot> key, @Nullable MaterialSlot slot){
        if(key == null){
            throw new NullPointerException();
        }
        slots.set(key, new Parameter<>(slot));
    }

    /**
     Returns the Material's parameters.

     @return the Material's parameters
     */
    public ParameterContainer getParameters(){
        return parameters;
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.renderer);
        hash = 29 * hash + Objects.hashCode(this.slots);
        hash = 29 * hash + Objects.hashCode(this.parameters);
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }
        final Material other = (Material) obj;
        if(!Objects.equals(this.renderer, other.renderer)){
            return false;
        }
        if(!Objects.equals(this.slots, other.slots)){
            return false;
        }
        if(!Objects.equals(this.parameters, other.parameters)){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(Material.class.getSimpleName()).append("(").append(" renderer: ")
                .append(renderer).append(", slots: ").append(slots).append(", parameters: ").append(parameters)
                .append(")");
        return res.toString();
    }
}
