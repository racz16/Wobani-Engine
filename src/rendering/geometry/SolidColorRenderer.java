package rendering.geometry;

import components.renderables.*;
import core.*;
import materials.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import rendering.*;
import resources.*;
import resources.shaders.*;
import toolbox.*;
import toolbox.annotations.*;
import toolbox.parameters.*;

/**
 * It can render RenderableComponents by drawing them with one color. Shadows
 * and lighting not affect the final color. It only uses the material's diffuse
 * color, if there is no diffuse color in the material, it uses as default the
 * mid-gray color.
 */
public class SolidColorRenderer extends GeometryRenderer {

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
        Class<SolidColorRenderer> renderer = SolidColorRenderer.class;
        RenderableComponents renderables = Scene.getRenderableComponents();
        for (Renderable renderable : renderables.getRenderables(renderer)) {
            beforeDrawRenderable(renderable);
            RenderableComponent renderableComponent;
            for (int i = 0; i < renderables.getRenderableComponentCount(renderer, renderable); i++) {
                renderableComponent = renderables.getRenderableComponent(renderer, renderable, i);
                if (renderableComponent.isActive() && renderableComponent.isRenderableActive() && Utility.isInsideFrustum(renderableComponent)) {
                    beforeDrawRenderableInstance(renderableComponent.getMaterial(), renderableComponent.getGameObject().getTransform().getModelMatrix());
                    renderable.draw();
                    numberOfRenderedElements++;
                    numberOfRenderedFaces += renderableComponent.getFaceCount();
                }
            }
            afterDrawRenderable(renderable);
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
        Parameter<Boolean> wirefreame = RenderingPipeline.getParameters().getBooleanParameter(RenderingPipeline.BOOLEAN_WIREFRAME_MODE);
        OpenGl.setWireframe(Parameter.getValueOrDefault(wirefreame, false));
        OpenGl.setViewport(RenderingPipeline.getRenderingSize(), new Vector2i());
        numberOfRenderedElements = 0;
        numberOfRenderedFaces = 0;
    }

    /**
     * Prepares the given Renderable to the rendering.
     *
     * @param material    material
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
    public String toString() {
        return super.toString() + "\nSolidColorRenderer{" + "shader=" + shader + '}';
    }

}
