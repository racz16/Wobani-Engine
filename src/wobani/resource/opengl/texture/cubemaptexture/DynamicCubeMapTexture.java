package wobani.resource.opengl.texture.cubemaptexture;

import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import wobani.resource.ResourceId;
import wobani.resource.opengl.texture.DynamicTexture;
import wobani.resource.opengl.texture.texture2d.DynamicTexture2D;
import wobani.toolbox.annotation.NotNull;

public class DynamicCubeMapTexture extends DynamicTexture implements CubeMapTexture {

    public DynamicCubeMapTexture(@NotNull Vector2i size) {
        super(new ResourceId());
        if (size.x <= 0 || size.y <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        getTexture().createTexture(getTarget());
        getTexture().setSize(size);
        dataSize = size.x * size.y * 4 * 4 * 6;

        bind();

        setFilter(TextureFilterType.MINIFICATION, getTexture().getFilter(TextureFilterType.MINIFICATION));
        setFilter(TextureFilterType.MAGNIFICATION, getTexture().getFilter(TextureFilterType.MAGNIFICATION));
        setTextureWrap(TextureWrapDirection.WRAP_U, TextureWrap.CLAMP_TO_EDGE);
        setTextureWrap(TextureWrapDirection.WRAP_V, TextureWrap.CLAMP_TO_EDGE);
        setTextureWrap(TextureWrapDirection.WRAP_W, TextureWrap.CLAMP_TO_EDGE);
        setBorderColor(getTexture().getBorderColor());

        for (int i = 0; i < 6; i++) {
            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGB, size.x, size.y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (float[]) null);
        }
    }

    public void setSide(@NotNull CubeMapSide side, @NotNull DynamicTexture2D texture) {
        //        GL11.glTexImage2D(side.getCode(), 0, GL11.GL_RGB, size.x, size.y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data[i]);
    }

    private int getTarget() {
        return GL13.GL_TEXTURE_CUBE_MAP;
    }

    @Override
    protected String getTypeName() {
        return "Dynamic CubeMap Texture";
    }

}
