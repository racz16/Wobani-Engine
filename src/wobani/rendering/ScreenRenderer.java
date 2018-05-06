package wobani.rendering;

import wobani.resources.textures.texture2d.Texture2D;
import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resources.meshes.*;
import wobani.resources.shaders.*;
import wobani.toolbox.*;
import wobani.toolbox.annotations.*;
import wobani.window.*;

/**
 * Renders the rendering pipeline's final frame to a fullscreen quad.
 */
public class ScreenRenderer extends Renderer {

    /**
     * Textured quad shader.
     */
    private TexturedQuadShader shader;
    /**
     * The fullscreen quad.
     */
    private QuadMesh quad;
    /**
     * The only ScreenRenderer instance.
     */
    private static ScreenRenderer instance;

    /**
     * Initializes a new ScreenRenderer.
     */
    private ScreenRenderer() {
        shader = TexturedQuadShader.getInstance();
        quad = QuadMesh.getInstance();
    }

    /**
     * Returns the ScreenRenderer instance.
     *
     * @return the ScreenRenderer instance
     */
    @NotNull
    public static ScreenRenderer getInstance() {
        if (instance == null) {
            instance = new ScreenRenderer();
        }
        return instance;
    }

    @Override
    public void render() {
        beforeShader();
        shader.start();
        beforeDrawQuad();
        quad.draw();
        afterDrawQuad();
        shader.stop();
    }

    /**
     * Prepares the shader to the rendering.
     */
    private void beforeShader() {
        if (shader == null || !shader.isUsable()) {
            shader = TexturedQuadShader.getInstance();
        }
        if (quad == null || !quad.isUsable()) {
            quad = QuadMesh.getInstance();
        }
        OpenGl.setWireframe(false);
        OpenGl.bindDefaultFrameBuffer();
        OpenGl.setViewport(Window.getClientAreaSize(), new Vector2i());
    }

    /**
     * Prepares the quad to the rendering.
     */
    private void beforeDrawQuad() {
        quad.beforeDraw();
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        Texture2D image = RenderingPipeline.getParameters().getValue(RenderingPipeline.WORK);
        image.bindToTextureUnit(0);
    }

    /**
     * Unbinds the quad's VAO after rendering.
     */
    private void afterDrawQuad() {
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
    public boolean isUsable() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nScreenRenderer{" + "shader=" + shader
                + ", quad=" + quad + '}';
    }

}
