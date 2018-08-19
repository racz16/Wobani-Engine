package wobani.resource.opengl.texture;

import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;
import wobani.resource.ResourceId;
import wobani.resource.opengl.OpenGlObject;
import wobani.toolbox.annotation.NotNull;
import wobani.toolbox.annotation.Nullable;

/**
 * Basic data and methods for implementing a texture.
 */
public abstract class AbstractTexture extends OpenGlObject implements Texture {

    private NativeTexture texture;

    @Nullable
    protected NativeTexture getTexture() {
        return texture;
    }

    public AbstractTexture(@NotNull ResourceId resourceId) {
        super(resourceId);
        texture = new NativeTexture();
    }

    @Override
    protected int getType() {
        return GL11.GL_TEXTURE;
    }

    @Override
    public void bind() {
        texture.bind();
    }

    @Override
    public void unbind() {
        texture.unbind();
    }

    @Override
    public Vector2i getSize() {
        return texture.getSize();
    }

    @Override
    public boolean issRgb() {
        return texture.isSRgb();
    }

    @Override
    public int getId() {
        //FIXME: to protected
        return texture.getId();
    }

}
