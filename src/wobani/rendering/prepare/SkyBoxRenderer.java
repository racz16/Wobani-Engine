package wobani.rendering.prepare;

import org.joml.*;
import wobani.core.*;
import wobani.rendering.*;
import wobani.resources.*;
import wobani.resources.mesh.*;
import wobani.resources.shader.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 * This renderer can draw a skybox. In theory it can render any number of meshes
 * and splines, but in practice it's adivsed to only use one cube. To render the
 * cube, it's material must contain a CubeMapTexture in the diffuse slot. If
 * there is no CubeMapTexture in the diffuse slot, the entire cube will be
 * filled with mid-grey color.
 */
public class SkyBoxRenderer extends Renderer {

    /**
     * SkyBox shader.
     */
    private SkyBoxShader shader;
    /**
     * The only SkyBoxRenderer instance.
     */
    private static SkyBoxRenderer instance;

    private Mesh box;

    /**
     * Initializes a new SkyBoxRenderer.
     */
    private SkyBoxRenderer() {
        shader = SkyBoxShader.getInstance();
        box = CubeMesh.getInstance();
    }

    /**
     * Returns the SkyBoxRenderer instance.
     *
     * @return the SkyBoxRenderer instance
     */
    @NotNull
    public static SkyBoxRenderer getInstance() {
        if (instance == null) {
            instance = new SkyBoxRenderer();
        }
        return instance;
    }

    /**
     * Renders the scene.
     */
    @Override
    public void render() {
        if (Scene.getParameters().get(Scene.MAIN_SKYBOX) == null) {
            return;
        }
        beforeDrawShader();

        beforeDrawRenderable(box);
        beforeDrawInstance();
        box.draw();
        afterDrawRenderable(box);

        shader.stop();
        OpenGl.setDepthTestMode(OpenGl.DepthTestMode.LESS);
    }

    /**
     * Prepares the shader to the rendering.
     */
    private void beforeDrawShader() {
        if (shader == null || !shader.isUsable()) {
            shader = SkyBoxShader.getInstance();
        }
        shader.start();
        RenderingPipeline.bindFbo();
        OpenGl.setDepthTestMode(OpenGl.DepthTestMode.LESS_OR_EQUAL);
        OpenGl.setViewport(RenderingPipeline.getRenderingSize(), new Vector2i());
        boolean wirefreame = RenderingPipeline.getParameters().getValueOrDefault(RenderingPipeline.WIREFRAME_MODE, false);
        OpenGl.setWireframe(wirefreame);
    }

    /**
     * Prepares the given Renderable to the rendering.
     *
     * @param renderable Renderable
     */
    private void beforeDrawRenderable(@NotNull Renderable renderable) {
        renderable.beforeDraw();
//        GL20.glEnableVertexAttribArray(0);
    }

    /**
     * Unbinds the Renderable's VAO and the vertex attrib arrays after
     * rendering.
     *
     * @param renderable Renderable
     */
    private void afterDrawRenderable(@NotNull Renderable renderable) {
//        GL20.glDisableVertexAttribArray(0);
        renderable.afterDraw();
    }

    /**
     * Prepares the MeshComponent to the rendering.
     *
     * @param rc MeshComponent
     */
    private void beforeDrawInstance() {
        shader.loadUniforms();
    }

    /**
     * Removes the shader program from the GPU's memory. After this method call
     * you can't use this shader.
     */
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
        return super.toString() + "\nSkyBoxRenderer{" + "shader=" + shader + '}';
    }

}
