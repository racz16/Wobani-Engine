package wobani.materials;

import wobani.resources.textures.texture2d.Texture2D;
import wobani.resources.environmentprobes.EnvironmentProbe;
import java.util.*;
import org.joml.*;
import wobani.toolbox.*;
import wobani.toolbox.annotations.*;
import wobani.toolbox.parameters.*;

/**
 * The Material's slot. It can store a color, parameters, a 2D and a
 * CubeMapTexture, the texture's tiling and offset. All of them are optional but
 * in some cases you should fill some values. For example if you use the
 * MaterialSlot as a normal map, you should set a texture, shaders can't do
 * anything with a single color as a normal map.
 */
public class MaterialSlot {

    /**
     * Determines whether the MaterialSlot is active.
     */
    private boolean active = true;
    /**
     * The slot's color.
     */
    private Vector4f color;
    /**
     * The slot's 2D texture.
     */
    private Texture2D texture;
    /**
     * The slot's environment probe.
     */
    private EnvironmentProbe environmentProbe;
    /**
     * Texture's tile factor along U and V directions.
     */
    private final Vector2f textureTile = new Vector2f(1);
    /**
     * Texture coordinates' offset along U and V directions.
     */
    private final Vector2f textureOffset = new Vector2f(0);
    /**
     * The MaterialSlot's parameters.
     */
    private final ParameterContainer parameters = new ParameterContainer();

    /**
     * Determines whether the shader use the specular map's or the specular
     * color's alpha channel as a glossiness value.
     */
    public static final ParameterKey<Float> USE_GLOSSINESS = new ParameterKey<>(Float.class, "PARAM_USE_GLOSSINESS");
    /**
     * Determines whether the shader use the normal map's alpha channel as a
     * parallax map.
     */
    public static final ParameterKey<Float> USE_POM = new ParameterKey<>(Float.class, "PARAM_USE_POM");
    /**
     * Parallax occlussion map's scale factor.
     */
    public static final ParameterKey<Float> POM_SCALE = new ParameterKey<>(Float.class, "PARAM_POM_SCALE");
    /**
     * Parallax occlussion map's minimum layers.
     */
    public static final ParameterKey<Float> POM_MIN_LAYERS = new ParameterKey<>(Float.class, "PARAM_POM_MIN_LAYERS");
    /**
     * Parallax occlussion map's maximum layers.
     */
    public static final ParameterKey<Float> POM_MAX_LAYERS = new ParameterKey<>(Float.class, "PARAM_POM_MAX_LAYERS");

    /**
     * Initializes a new MaterialSlot.
     */
    public MaterialSlot() {
    }

    /**
     * Initializes a new MaterialSlot to the given value.
     *
     * @param texture 2D texture
     */
    public MaterialSlot(@Nullable Texture2D texture) {
        setTexture(texture);
    }

    /**
     * Initializes a new MaterialSlot to the given value.
     *
     * @param environmentProbe environment probe
     */
    public MaterialSlot(@Nullable EnvironmentProbe environmentProbe) {
        setEnvironmentProbe(environmentProbe);
    }

    /**
     * Initializes a new MaterialSlot to the given value.
     *
     * @param color color
     */
    public MaterialSlot(@Nullable Vector4f color) {
        setColor(color);
    }

    /**
     * Determines whether the MaterialSlot is active.
     *
     * @return true if the MaterialSlot is active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether or not the MaterialSlot is active.
     *
     * @param active true if this MaterialSlot should be active, false otherwise
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the MaterialSlot's color
     *
     * @return MaterialSlot's color
     */
    @Nullable
    public Vector4f getColor() {
        return color;
    }

    /**
     * Sets the color to the given value.
     *
     * @param color color
     *
     * @throws IllegalArgumentException color can't be lower than 0
     */
    public void setColor(@Nullable Vector4f color) {
        if (color != null && !Utility.isHdrColor(new Vector3f(color.x, color.y, color.z))) {
            throw new IllegalArgumentException("Color can't be lower than 0");
        }
        this.color = color;
    }

    /**
     * Returns the MaterialSlot's texture.
     *
     * @return the MaterialSlot's texture
     */
    @Nullable
    public Texture2D getTexture() {
        return texture;
    }

    /**
     * Sets the texture to the given value.
     *
     * @param texture texture
     */
    public void setTexture(@Nullable Texture2D texture) {
        this.texture = texture;
    }

    /**
     * Returns the MaterialSlot's environment probe.
     *
     * @return the MaterialSlot's environment probe
     */
    @Nullable
    public EnvironmentProbe getEnvironmentProbe() {
        return environmentProbe;
    }

    /**
     * Sets the environment probe to the given value.
     *
     * @param environmentProbe environment probe
     */
    public void setEnvironmentProbe(@Nullable EnvironmentProbe environmentProbe) {
        this.environmentProbe = environmentProbe;
    }

    /**
     * Returns the texture's tile factor. The x coordinate is the tiling along
     * the U direction ad the y coordinate is the tiling along the V direction.
     *
     * @return the texture's tile factor
     */
    @NotNull
    public Vector2f getTextureTile() {
        return textureTile;
    }

    /**
     * Sets the texture's tile factor to the given value. The x coordinate is
     * the tiling along the U direction ad the y coordinate is the tiling along
     * the V direction.
     *
     * @param textureTile the texture's tile factor
     */
    public void setTextureTile(@NotNull Vector2f textureTile) {
        this.textureTile.set(textureTile);
    }

    /**
     * Returns the texture coordinates' offset.
     *
     * @return the texture coordinates' offset
     */
    @NotNull
    public Vector2f getTextureOffset() {
        return textureOffset;
    }

    /**
     * Sets the texture coordinates' offset to the given value.
     *
     * @param textureOffset the texture coordinates' offset
     */
    public void setTextureOffset(@NotNull Vector2f textureOffset) {
        this.textureOffset.set(textureOffset);
    }

    /**
     * Returns the MaterialSlot's parameters.
     *
     * @return the MaterialSlot's parameters
     */
    public ParameterContainer getParameters() {
        return parameters;
    }

    //
    //misc----------------------------------------------------------------------
    //
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.active ? 1 : 0);
        hash = 29 * hash + Objects.hashCode(this.color);
        hash = 29 * hash + Objects.hashCode(this.texture);
        hash = 29 * hash + Objects.hashCode(this.environmentProbe);
        hash = 29 * hash + Objects.hashCode(this.textureTile);
        hash = 29 * hash + Objects.hashCode(this.textureOffset);
        hash = 29 * hash + Objects.hashCode(this.parameters);
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
        final MaterialSlot other = (MaterialSlot) obj;
        if (this.active != other.active) {
            return false;
        }
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        if (!Objects.equals(this.texture, other.texture)) {
            return false;
        }
        if (!Objects.equals(this.environmentProbe, other.environmentProbe)) {
            return false;
        }
        if (!Objects.equals(this.textureTile, other.textureTile)) {
            return false;
        }
        if (!Objects.equals(this.textureOffset, other.textureOffset)) {
            return false;
        }
        if (!Objects.equals(this.parameters, other.parameters)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append("MaterialSlot(")
                .append(" active: ").append(active)
                .append(", color: ").append(color)
                .append(", texture: ").append(texture)
                .append(", environment probe: ").append(environmentProbe)
                .append(", texture tile: ").append(textureTile)
                .append(", texture offset: ").append(textureOffset)
                .append(", parameters: ").append(parameters)
                .append(")");
        return res.toString();
    }

}
