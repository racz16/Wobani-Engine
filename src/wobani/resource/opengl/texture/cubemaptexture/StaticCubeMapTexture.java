package wobani.resource.opengl.texture.cubemaptexture;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL21;
import org.lwjgl.stb.STBImage;
import wobani.resource.ResourceId;
import wobani.resource.ResourceManager;
import wobani.resource.opengl.texture.StaticTexture;
import wobani.toolbox.Image;
import wobani.toolbox.annotation.NotNull;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Stores data about a loaded texture. You can load a texture only once, if you try to load it twice, you get reference to
 * the already loaded one.
 *
 * @see #loadTexture(List paths, boolean sRgb)
 */
public class StaticCubeMapTexture extends StaticTexture implements CubeMapTexture {

    /**
     * Texture's pixel data.
     */
    private final ByteBuffer[] data = new ByteBuffer[6];

    /**
     * Initializes a new StaticCubeMapTexture to the given parameters.
     *
     * @param paths textures' relative path (with extension like "res/textures/myTexture.png")
     * @param sRgb  determines whether the texture is in sRgb color space
     */
    public StaticCubeMapTexture(@NotNull List<File> paths, boolean sRgb) {
        super(new ResourceId(paths));
        basesRgb = sRgb;
        getTexture().setsRgb(sRgb);
        meta.setPaths(paths);
        meta.setLastActiveToNow();
        meta.setDataStorePolicy(ResourceManager.ResourceState.ACTION);
        filtering = ResourceManager.getTextureFiltering();

        hddToRam();
        ramToVram();

        int size = 0;
        for (int i = 0; i < 6; i++) {
            size += data[i].capacity();
        }
        meta.setDataSize(size);
    }

    //
    //loading/saving------------------------------------------------------------
    //

    /**
     * Loads a texture from the given path. You can load a texture only once, if you try to load it twice, you get
     * reference to the already loaded one.
     *
     * @param paths texture's relative path (with extension like "res/textures/myTexture.png")
     * @param sRgb  determines whether the texture is in sRGB color space
     * @return texture
     */
    @NotNull
    public static StaticCubeMapTexture loadTexture(@NotNull List<File> paths, boolean sRgb) {
        StaticCubeMapTexture tex = ResourceManager.getResource(new ResourceId(paths), StaticCubeMapTexture.class);
        //StaticCubeMapTexture tex = (StaticCubeMapTexture) ResourceManager.getTexture(new ResourceId(paths));
        if (tex != null) {
            return tex;
        }
        return new StaticCubeMapTexture(paths, sRgb);
    }

    /**
     * Loads the texture's data from file to the RAM.
     *
     * @throws IllegalStateException each image have to be the same size
     */
    @Override
    protected void hddToRam() {
        for (int i = 0; i < 6; i++) {
            Image image = new Image(getPath(i), false);
            if (i == 0) {
                getTexture().setSize(image.getSize());
            } else {
                if (!getTexture().getSize().equals(image.getSize())) {
                    throw new IllegalStateException("Each image have to be the same size");
                }
            }
            data[i] = image.getData();
        }

        meta.setState(ResourceManager.ResourceState.RAM);
    }

    @Override
    protected void ramToVram() {
        getTexture().createTexture(getTarget());
        bind();

        for (int i = 0; i < 6; i++) {
            if (getTexture().isSRgb()) {
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL21.GL_SRGB, getTexture().getSize().x, getTexture().getSize().y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data[i]);
            } else {
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGB, getTexture().getSize().x, getTexture().getSize().y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data[i]);
            }
        }

        setTextureWrap(TextureWrapDirection.WRAP_U, TextureWrap.CLAMP_TO_EDGE);
        setTextureWrap(TextureWrapDirection.WRAP_V, TextureWrap.CLAMP_TO_EDGE);
        setTextureWrap(TextureWrapDirection.WRAP_W, TextureWrap.CLAMP_TO_EDGE);
        setBorderColor(getTexture().getBorderColor());
        changeFiltering();

        meta.setState(ResourceManager.ResourceState.ACTION);
    }

    @Override
    protected void vramToRam() {
        getTexture().release();

        meta.setState(ResourceManager.ResourceState.RAM);
    }

    @Override
    protected void ramToHdd() {
        for (int i = 0; i < 6; i++) {
            STBImage.stbi_image_free(data[i]);
            data[i] = null;
        }

        meta.setState(ResourceManager.ResourceState.HDD);
    }

    //
    //misc----------------------------------------------------------------------
    //

    /**
     * Returns the texture's specified path.
     *
     * @param index the method returns the indexth path, it must be in the (0;6) interval
     * @return the texture's specified path
     */
    @NotNull
    public File getPath(int index) {
        return meta.getPaths().get(index);
    }

    private int getTarget() {
        return GL13.GL_TEXTURE_CUBE_MAP;
    }

    @Override
    protected String getTypeName() {
        return "Static CubeMap Texture";
    }
}
