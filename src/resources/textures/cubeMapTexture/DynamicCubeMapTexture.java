package resources.textures.cubeMapTexture;

import org.joml.*;
import org.lwjgl.opengl.*;
import resources.*;
import resources.textures.*;
import resources.textures.texture2D.*;
import toolbox.annotations.*;

public class DynamicCubeMapTexture extends DynamicTexture implements CubeMapTexture {

    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;

    public DynamicCubeMapTexture(@NotNull Vector2i size) {
        if (size.x <= 0 || size.y <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        this.size.set(size);
        dataSize = size.x * size.y * 4 * 4 * 6;

        glGenerateTextureId();
        bind();

        setFilter(TextureFilterType.MINIFICATION, minification);
        setFilter(TextureFilterType.MAGNIFICATION, magnification);
        setTextureWrap(TextureWrapDirection.WRAP_U, TextureWrap.CLAMP_TO_EDGE);
        setTextureWrap(TextureWrapDirection.WRAP_V, TextureWrap.CLAMP_TO_EDGE);
        setTextureWrap(TextureWrapDirection.WRAP_W, TextureWrap.CLAMP_TO_EDGE);
        setBorderColor(borderColor);

        for (int i = 0; i < 6; i++) {
            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGB, size.x, size.y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (float[]) null);
        }

        resourceId = new ResourceId();
        ResourceManager.addTexture(this);
    }

    public void setSide(@NotNull CubeMapSide side, @NotNull DynamicTexture2D texture) {
//        GL11.glTexImage2D(side.getCode(), 0, GL11.GL_RGB, size.x, size.y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data[i]);
    }

    @Override
    protected int getTextureType() {
        return GL13.GL_TEXTURE_CUBE_MAP;
    }

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

}
