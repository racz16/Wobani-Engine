package resources.textures;

import java.nio.*;
import org.lwjgl.opengl.*;
import resources.*;
import toolbox.annotations.*;

public class CubeMapTexture extends AbstractTexture {

    /**
     * The texture data size (in bytes).
     */
    private int dataSize;
    /**
     * Texture's filtering mode.
     */
    private EasyFiltering.TextureFiltering filtering;
    /**
     * Texture's pixel data.
     */
    private ByteBuffer data;
    /**
     * Stores meta data about this texture.
     */
    private final LoadableResourceMetaData meta = new LoadableResourceMetaData();
//    /**
//     * The texture's default color space.
//     */
//    private final boolean basesRgb;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;

    public CubeMapTexture() {
        resourceId = new ResourceId();
    }

    @Override
    protected int getTextureType() {
        return GL13.GL_TEXTURE_CUBE_MAP;
    }

    @Override
    public void bind() {
        glBind();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void unbind() {
        glUnbind();
    }

    @Override
    public void bindToTextureUnit(int textureUnit) {
        glActivate(textureUnit);
        glBind();
    }

    @Override
    public boolean issRgb() {
        return sRgb;
    }

    @Override
    public int getDataSizeInRam() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDataSizeInAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void release() {
        glRelease();
        dataSize = 0;
    }

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public boolean isUsable() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
