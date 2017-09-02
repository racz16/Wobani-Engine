package renderers.postProcessing;

import org.joml.*;
import org.lwjgl.opengl.*;
import renderers.*;
import resources.meshes.*;
import resources.textures.*;
import shaders.*;
import toolbox.*;
import window.*;

/**
 * Abstract class for post processing renderers.
 */
public abstract class PostProcessingBase extends Renderer {

    /**
     * Renderer's shader.
     */
    protected Shader shader;
    /**
     * The fullscreen quad.
     */
    private final QuadMesh quad = QuadMesh.getInstance();

    @Override
    public void render() {
        beforeShader();
        shader.start();
        beforeDrawQuad();
        quad.draw();
        afterDrawQuad();
        shader.stop();
        RenderingPipeline.swapFboAttachments();
    }

    /**
     * Refreshes the shader if it's not usable.
     */
    protected abstract void refreshShader();

    /**
     * Prepares the shader to the rendering.
     */
    private void beforeShader() {
        OpenGl.setWireframe(false);
        RenderingPipeline.bindFbo();
        OpenGl.setViewport(Window.getClientAreaSize(), new Vector2i());
        refreshShader();
    }

    /**
     * Prepares the quad and the texture to the rendering.
     */
    protected void beforeDrawQuad() {
        quad.beforeDraw();
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        Texture2D image = RenderingPipeline.getTextureParameter(RenderingPipeline.TEXTURE_WORK);
        image.bindToTextureUnit(0);
    }

    /**
     * Unbinds the quad's VAO after rendering.
     */
    protected void afterDrawQuad() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        quad.afterDraw();
    }

    @Override
    public void release() {
        shader.release();
    }

    @Override
    public void removeFromRenderingPipeline() {

    }

    @Override
    public boolean isGeometryRenderer() {
        return false;
    }

    @Override
    public String toString() {
        return "PostProcessingBase{" + "shader=" + shader + ", quad=" + quad + '}';
    }

}
