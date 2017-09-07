package renderers;

import components.camera.*;
import components.renderables.*;
import core.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import resources.*;
import resources.materials.*;
import resources.meshes.*;
import resources.splines.*;
import shaders.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * It can render Renderables by drawing them with one color. Shadows and
 * lighting not affect the final color. It only uses the material's diffuse
 * color, if there is no diffuse color in the material, it uses as default the
 * mid-gray color.
 */
public class SolidColorRenderer extends Renderer {

    /**
     * Solid color shader.
     */
    private SolidColorShader shader;
    /**
     * The only SolidColorRenderer instance.
     */
    private static SolidColorRenderer instance;

    /**
     * Initializes a new SolidColorRenderer.
     */
    private SolidColorRenderer() {
        shader = SolidColorShader.getInstance();
    }

    /**
     * Returns the SolidColorRenderer instance.
     *
     * @return the SolidColorRenderer instance
     */
    @NotNull
    public static SolidColorRenderer getInstance() {
        if (instance == null) {
            instance = new SolidColorRenderer();
        }
        return instance;
    }

    /**
     * Renders the scene.
     */
    @Override
    public void render() {
        beforeShader();
        shader.start();
        Camera camera = Scene.getCamera();
        Class renderer = SolidColorRenderer.class;
        //meshes
        for (Mesh mesh : Scene.getMeshes(renderer)) {
            beforeDrawRenderable(mesh);
            MeshComponent meshComponent;
            for (int i = 0; i < Scene.getNumberOfMeshComponents(renderer, mesh); i++) {
                meshComponent = Scene.getMeshComponent(renderer, mesh, i);
                if (meshComponent.isActive() && meshComponent.isMeshActive() && camera.isInsideFrustum(meshComponent.getRealAabbMin(), meshComponent.getRealAabbMax())) {
                    beforeDrawRenderableInstance(meshComponent.getMaterial(), meshComponent.getGameObject().getTransform().getModelMatrix());
                    mesh.draw();
                    numberOfRenderedElements++;
                    numberOfRenderedFaces += mesh.getFaceCount();
                }
            }
            afterDrawRenderable(mesh);
        }
        //splines
        for (Spline spline : Scene.getSplines(renderer)) {
            beforeDrawRenderable(spline);
            SplineComponent splineComponent;
            for (int i = 0; i < Scene.getNumberOfSplineComponents(renderer, spline); i++) {
                splineComponent = Scene.getSplineComponent(renderer, spline, i);
                if (splineComponent.isActive() && splineComponent.isSplineActive() && camera.isInsideFrustum(splineComponent.getRealAabbMin(), splineComponent.getRealAabbMax())) {
                    beforeDrawRenderableInstance(splineComponent.getMaterial(), splineComponent.getGameObject().getTransform().getModelMatrix());
                    spline.draw();
                    numberOfRenderedElements++;
                }
            }
            afterDrawRenderable(spline);
        }
        shader.stop();
    }

    /**
     * Prepares the shader to the rendering.
     */
    private void beforeShader() {
        if (shader == null || !shader.isUsable()) {
            shader = SolidColorShader.getInstance();
        }
        RenderingPipeline.bindFbo();
        OpenGl.setWireframe(Settings.isWireframeMode());
        OpenGl.setViewport(RenderingPipeline.getRenderingSize(), new Vector2i());
        numberOfRenderedElements = 0;
        numberOfRenderedFaces = 0;
    }

    /**
     * Prepares the given Renderable to the rendering.
     *
     * @param material material
     * @param modelMatrix model matrix
     */
    private void beforeDrawRenderableInstance(@NotNull Material material, @NotNull Matrix4f modelMatrix) {
        MaterialSlot slot = material.getSlot(Material.DIFFUSE);
        Vector4f color = slot != null && slot.isActive() && slot.getColor() != null ? slot.getColor() : new Vector4f(0.5f);
        shader.loadUniforms(modelMatrix, new Vector3f(color.x, color.y, color.z));
    }

    /**
     * Prepares the given Renderable to the rendering.
     *
     * @param renderable Renderable
     */
    private void beforeDrawRenderable(@NotNull Renderable renderable) {
        renderable.beforeDraw();
        GL20.glEnableVertexAttribArray(0);
    }

    /**
     * Unbinds the renderable's VAO after rendering.
     *
     * @param renderable Renderable
     */
    private void afterDrawRenderable(@NotNull Renderable renderable) {
        GL20.glDisableVertexAttribArray(0);
        renderable.afterDraw();
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
    public boolean isGeometryRenderer() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nSolidColorRenderer{" + "shader=" + shader + '}';
    }

}
