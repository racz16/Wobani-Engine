package materials;

import java.util.*;
import org.joml.*;
import resources.textures.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * The Material's slot. It can store a color, parameters, a texture, the
 * texture's tiling and offset. All of them are optional but in some cases you
 * should fill some values. For example if you use the MaterialSlot as a normal
 * map, you should set a texture, shaders can't do anything with a single color
 * as a normal map.
 */
//TODO add cubemaptexture
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
     * The slot's texture.
     */
    private Texture2D texture;
    /**
     * Texture's tile factor along U and V directions.
     */
    private final Vector2f textureTile = new Vector2f(1);
    /**
     * Texture coordinates' offset along U and V directions.
     */
    private final Vector2f textureOffset = new Vector2f(0);
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
    public static final String GLOSSINESS_USE_FLOAT = "GLOSSINESS_USE_FLOAT";
    /**
     * Determines whether the shader use the normal map's alpha chanel as a
     * parallax map.
     */
    public static final String POM_USE_FLOAT = "POM_USE_FLOAT";
    /**
     * Parallax occlussion map's scale factor.
     */
    public static final String POM_SCALE_FLOAT = "POM_SCALE_FLOAT";
    /**
     * Parallax occlussion map's minimum layers.
     */
    public static final String POM_MIN_LAYERS_FLOAT = "POM_MIN_LAYERS_FLOAT";
    /**
     * Parallax occlussion map's maximum layers.
     */
    public static final String POM_MAX_LAYERS_FLOAT = "POM_MAX_LAYERS_FLOAT";

    /**
     * Initializes a new MaterialSlot.
     */
    public MaterialSlot() {
    }

    /**
     * Initializes a new MaterialSlot to the given value.
     *
     * @param texture texture
     */
    public MaterialSlot(@Nullable Texture2D texture) {
        setTexture(texture);
    }

    /**
     * Initializes a new MaterialSlot to the given values.
     *
     * @param path texture's relative path (with extension like
     * "res/textures/myTexture.png")
     * @param sRgb determines whether the texture is in sRGB color space
     *
     */
    public MaterialSlot(@NotNull String path, boolean sRgb) {
        setTexture(StaticTexture.loadTexture(path, sRgb));
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
    @Nullable @ReadOnly
    public Vector4f getColor() {
        return color == null ? null : new Vector4f(color);
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

    //
    //parameters----------------------------------------------------------------
    //
    /**
     * Returns the specified key's Float value. Float, String and vector
     * parameters use distinct keys and values.
     *
     * @param key key
     * @return the specified key's Float value
     *
     * @throws NullPointerException key can't be null
     * @see #POM_SCALE_FLOAT
     * @see #POM_MIN_LAYERS_FLOAT
     * @see #POM_MAX_LAYERS_FLOAT
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
     * the MaterialSlot doesn't check whether or not given value is valid so try
     * to give sensable values. Float, String and vector parameters use distinct
     * keys and values.
     *
     * @param key key
     * @param value value
     *
     * @throws NullPointerException key can't be null
     *
     * @see #POM_SCALE_FLOAT
     * @see #POM_MIN_LAYERS_FLOAT
     * @see #POM_MAX_LAYERS_FLOAT
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
     * the MaterialSlot doesn't check whether or not given value is valid so try
     * to give sensable values. Float, String and vector parameters use distinct
     * keys and values.
     *
     * @param key key
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
     * the MaterialSlot doesn't check whether or not given value is valid so try
     * to give sensable values. Float, String and vector parameters use distinct
     * keys and values.
     *
     * @param key key
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

    //
    //misc----------------------------------------------------------------------
    //
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.active ? 1 : 0);
        hash = 37 * hash + Objects.hashCode(this.color);
        hash = 37 * hash + Objects.hashCode(this.texture);
        hash = 37 * hash + Objects.hashCode(this.textureTile);
        hash = 37 * hash + Objects.hashCode(this.textureOffset);
        hash = 37 * hash + Objects.hashCode(this.floatParameters);
        hash = 37 * hash + Objects.hashCode(this.vectorParameters);
        hash = 37 * hash + Objects.hashCode(this.stringParameters);
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
        if (!Objects.equals(this.textureTile, other.textureTile)) {
            return false;
        }
        if (!Objects.equals(this.textureOffset, other.textureOffset)) {
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
        return "MaterialSlot{" + "active=" + active + ", color=" + color
                + ", texture=" + texture + ", textureTile=" + textureTile
                + ", textureOffset=" + textureOffset + ", floatParameters=" + floatParameters
                + ", vectorParameters=" + vectorParameters + ", stringParameters=" + stringParameters + '}';
    }

}
