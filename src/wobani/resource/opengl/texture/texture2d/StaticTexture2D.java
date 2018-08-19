package wobani.resource.opengl.texture.texture2d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21;
import wobani.resource.ResourceId;
import wobani.resource.ResourceManager;
import wobani.resource.ResourceManager.ResourceState;
import wobani.resource.opengl.texture.StaticTexture;
import wobani.toolbox.Image;
import wobani.toolbox.Utility;
import wobani.toolbox.annotation.Bind;
import wobani.toolbox.annotation.NotNull;

import java.io.File;

/**
 * Stores data about a loaded texture. You can load a texture only once, if you try to load it twice, you get reference to
 * the already loaded one.
 *
 * @see #loadTexture(File path, boolean sRgb)
 */
public class StaticTexture2D extends StaticTexture implements Texture2D {

    /**
     * Texture's pixel data.
     */
    private Image image;

    /**
     * Initializes a new StaticTexture to the given parameters.
     *
     * @param path texture's relative path (with extension like "res/textures/myTexture.png")
     * @param sRgb determines whether the texture is in sRgb color space
     */
    private StaticTexture2D(@NotNull File path, boolean sRgb) {
        super(new ResourceId(path));
        basesRgb = sRgb;
        getTexture().setsRgb(sRgb);
        meta.setPaths(Utility.wrapObjectByList(path));
        meta.setLastActiveToNow();
        meta.setDataStorePolicy(ResourceState.ACTION);
        filtering = ResourceManager.getTextureFiltering();

        hddToRam();
        ramToVram();

        meta.setDataSize(image.getData().capacity());
    }

    //
    //loading/saving------------------------------------------------------------
    //

    /**
     * Loads a texture from the given path. You can load a texture only once, if you try to load it twice, you get
     * reference to the already loaded one.
     *
     * @param path texture's relative path (with extension like "res/textures/myTexture.png")
     * @param sRgb determines whether the texture is in sRGB color space
     * @return texture
     */
    @NotNull
    public static StaticTexture2D loadTexture(@NotNull File path, boolean sRgb) {
        StaticTexture2D tex = ResourceManager.getResource(new ResourceId(path), StaticTexture2D.class);
        //StaticTexture2D tex = (StaticTexture2D) ResourceManager.getTexture(new ResourceId(path));
        if (tex != null) {
            return tex;
        }
        return new StaticTexture2D(path, sRgb);
    }

    @Override
    protected void hddToRam() {
        image = new Image(meta.getPaths().get(0), true);
        getTexture().setSize(image.getSize());

        meta.setState(ResourceState.RAM);
    }

    @Override
    protected void ramToVram() {
        getTexture().createTexture(getTarget());

        bind();

        if (getTexture().isSRgb()) {
            getTexture().texImage(GL21.GL_SRGB8_ALPHA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image.getData());
        } else {
            getTexture().texImage(GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image.getData());
        }

        setTextureWrap(TextureWrapDirection.WRAP_U, getTexture().getWrap(TextureWrapDirection.WRAP_U));
        setTextureWrap(TextureWrapDirection.WRAP_V, getTexture().getWrap(TextureWrapDirection.WRAP_V));
        setBorderColor(getTexture().getBorderColor());
        changeFiltering();

        meta.setState(ResourceState.ACTION);
    }

    @Override
    protected void vramToRam() {
        getTexture().release();

        meta.setState(ResourceState.RAM);
    }

    @Override
    protected void ramToHdd() {
        image.release();
        image = null;

        meta.setState(ResourceState.HDD);
    }

    //
    //texture wrapping----------------------------------------------------------
    //

    /**
     * Returns the texture's specified wrap mode.
     *
     * @param type texture wrap direction
     * @return the texture's specified wrap mode
     * @throws IllegalArgumentException w direction can't apply to a 2D texture
     */
    @NotNull
    @Override
    public TextureWrap getTextureWrap(@NotNull TextureWrapDirection type) {
        if (type == TextureWrapDirection.WRAP_W) {
            throw new IllegalArgumentException("W direction can't apply to a 2D texture");
        }
        return getTexture().getWrap(type);
    }

    /**
     * Sets the texture's specified wrap mode to the given value.
     *
     * @param type texture wrap direction
     * @param tw   texture wrap
     * @throws IllegalArgumentException w direction can't apply to a 2D texture
     */
    @Bind
    @Override
    public void setTextureWrap(@NotNull TextureWrapDirection type, @NotNull TextureWrap tw) {
        if (type == TextureWrapDirection.WRAP_W) {
            throw new IllegalArgumentException("W direction can't apply to a 2D texture");
        }
        getTexture().setWrap(type, tw);
    }

    //
    //misc----------------------------------------------------------------------
    //

    /**
     * Returns the texture's path.
     *
     * @return the texture's path
     */
    @NotNull
    public File getPath() {
        return meta.getPaths().get(0);
    }

    private int getTarget() {
        return GL11.GL_TEXTURE_2D;
    }

    @Override
    protected String getTypeName() {
        return "Static Texture2D";
    }

}
