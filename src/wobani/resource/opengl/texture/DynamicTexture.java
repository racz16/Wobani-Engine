package wobani.resource.opengl.texture;

import org.joml.Vector4f;
import wobani.resource.ResourceId;
import wobani.toolbox.annotation.Bind;
import wobani.toolbox.annotation.NotNull;
import wobani.toolbox.annotation.ReadOnly;

public abstract class DynamicTexture extends AbstractTexture {

    /**
     * The texture data size (in bytes).
     */
    protected int dataSize;

    public DynamicTexture(ResourceId resourceId) {
        super(resourceId);
    }

    //
    //texture wrapping----------------------------------------------------------
    //

    /**
     * Returns the texture's specified wrap mode.
     *
     * @param type texture wrap direction
     * @return the texture's specified wrap mode
     */
    @NotNull
    public TextureWrap getTextureWrap(@NotNull TextureWrapDirection type) {
        return getTexture().getWrap(type);
    }

    /**
     * Sets the texture's specified wrap mode to the given value.
     *
     * @param type texture wrap direction
     * @param tw   texture wrap
     */
    @Bind
    public void setTextureWrap(@NotNull TextureWrapDirection type, @NotNull TextureWrap tw) {
        getTexture().setWrap(type, tw);
    }

    /**
     * Returns the texture's specified filter mode.
     *
     * @param type texture filter type
     * @return the texture's specified filter mode
     */
    @NotNull
    public TextureFilter getFilter(@NotNull TextureFilterType type) {
        return getTexture().getFilter(type);
    }

    /**
     * Sets the texture's specified filter to the given value.
     *
     * @param type  texture filter type
     * @param value texture filter
     */
    @Bind
    public void setFilter(@NotNull TextureFilterType type, @NotNull TextureFilter value) {
        getTexture().setFilter(type, value);
    }

    /**
     * Returns the texture's border color.
     *
     * @return the texture's border color
     */
    @NotNull
    @ReadOnly
    public Vector4f getBorderColor() {
        return new Vector4f(getTexture().getBorderColor());
    }

    /**
     * Sets the texture's border color to the given value.
     *
     * @param borderColor border color
     */
    @Bind
    public void setBorderColor(@NotNull Vector4f borderColor) {
        getTexture().setBorderColor(borderColor);
    }


    @Override
    public void bindToTextureUnit(int textureUnit) {
        getTexture().bindToTextureUnit(textureUnit);
    }

    @Override
    public boolean issRgb() {
        return false;
    }

    @Override
    public int getCachedDataSize() {
        return 0;
    }

    @Override
    public int getActiveDataSize() {
        return dataSize;
    }

    /**
     * Releases the texture's data. After calling this method, you can't use this texture for anything.
     */
    @Override
    public void release() {
        getTexture().release();
        dataSize = 0;
    }

    @Override
    public boolean isUsable() {
        return getId() != 0;
    }

}
