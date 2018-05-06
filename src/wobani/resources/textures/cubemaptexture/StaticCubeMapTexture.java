package wobani.resources.textures.cubemaptexture;

import java.io.*;
import java.nio.*;
import java.util.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.*;
import wobani.resources.*;
import wobani.resources.textures.*;
import wobani.toolbox.*;
import wobani.toolbox.annotations.*;

/**
 * Stores data about a loaded texture. You can load a texture only once, if you
 * try to load it twice, you get reference to the already loaded one.
 *
 * @see #loadTexture(List paths, boolean sRgb)
 */
public class StaticCubeMapTexture extends StaticTexture implements CubeMapTexture {

    /**
     * Texture's pixel data.
     */
    private final ByteBuffer[] data = new ByteBuffer[6];
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     * Initializes a new StaticCubeMapTexture to the given parameters.
     *
     * @param paths textures' relative path (with extension like
     *              "res/textures/myTexture.png")
     * @param sRgb  determines whether the texture is in sRgb color space
     */
    public StaticCubeMapTexture(@NotNull List<File> paths, boolean sRgb) {
        basesRgb = sRgb;
        this.sRgb = sRgb;
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
        resourceId = new ResourceId(paths);
        ResourceManager.addTexture(this);
    }

    //
    //loading/saving------------------------------------------------------------
    //
    /**
     * Loads a texture from the given path. You can load a texture only once, if
     * you try to load it twice, you get reference to the already loaded one.
     *
     * @param paths texture's relative path (with extension like
     *              "res/textures/myTexture.png")
     * @param sRgb  determines whether the texture is in sRGB color space
     *
     * @return texture
     */
    @NotNull
    public static StaticCubeMapTexture loadTexture(@NotNull List<File> paths, boolean sRgb) {
        StaticCubeMapTexture tex = (StaticCubeMapTexture) ResourceManager.getTexture(new ResourceId(paths));
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
                size.set(image.getSize());
            } else {
                if (!size.equals(image.getSize())) {
                    throw new IllegalStateException("Each image have to be the same size");
                }
            }
            data[i] = image.getData();
        }

        meta.setState(ResourceManager.ResourceState.RAM);
    }

    @Override
    protected void ramToVram() {
        glGenerateTextureId();
        bind();

        for (int i = 0; i < 6; i++) {
            if (sRgb) {
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL21.GL_SRGB, size.x, size.y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data[i]);
            } else {
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGB, size.x, size.y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data[i]);
            }
        }

        setTextureWrap(TextureWrapDirection.WRAP_U, TextureWrap.CLAMP_TO_EDGE);
        setTextureWrap(TextureWrapDirection.WRAP_V, TextureWrap.CLAMP_TO_EDGE);
        setTextureWrap(TextureWrapDirection.WRAP_W, TextureWrap.CLAMP_TO_EDGE);
        setBorderColor(borderColor);
        changeFiltering();

        meta.setState(ResourceManager.ResourceState.ACTION);
    }

    @Override
    protected void vramToRam() {
        glRelease();

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
    @Override
    protected int getTextureType() {
        return GL13.GL_TEXTURE_CUBE_MAP;
    }

    /**
     * Returns the texture's specified path.
     *
     * @param index the method returns the indexth path, it must be in the (0;6)
     *              interval
     *
     * @return the texture's specified path
     */
    @NotNull
    public File getPath(int index) {
        return meta.getPaths().get(index);
    }

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public String toString() {
        return super.toString() + "\nStaticCubeMapTexture{" + "data=" + data
                + ", resourceId=" + resourceId + '}';
    }

}
