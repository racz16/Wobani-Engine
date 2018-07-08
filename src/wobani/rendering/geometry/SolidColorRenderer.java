package wobani.rendering.geometry;

import org.joml.*;
import wobani.component.camera.*;
import wobani.component.renderable.*;
import wobani.core.*;
import wobani.material.*;
import wobani.rendering.*;
import wobani.resources.*;
import wobani.resources.shader.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 * It can render RenderableContainer by drawing them with one color. Shadows and
 * lighting not affect the final color. It only uses the material's diffuse
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
	RenderableContainer renderables = RenderingPipeline.getRenderableComponents();
	for (Renderable renderable : renderables.getRenderables(renderer)) {
	    beforeDrawRenderable(renderable);
	    RenderableComponent renderableComponent;
	    for (int i = 0; i < renderables.getRenderableComponentCount(renderer, renderable); i++) {
		renderableComponent = renderables.getRenderableComponent(renderer, renderable, i);
		if (renderableComponent.isActive() && renderableComponent.isRenderableActive() && Utility.isInsideMainCameraFrustumAabb(renderableComponent)) {
		    beforeDrawRenderableInstance(renderableComponent.getMaterial(), renderableComponent.getGameObject().getTransform().getModelMatrix());
		    renderableComponent.draw();
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
	CameraComponent.refreshMatricesUbo();
	RenderingPipeline.bindFbo();
	boolean wirefreame = RenderingPipeline.getParameters().getValueOrDefault(RenderingPipeline.WIREFRAME_MODE, false);
	OpenGl.setWireframe(wirefreame);
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
//	GL20.glEnableVertexAttribArray(0);
    }

    /**
     * Unbinds the renderable's VAO after rendering.
     *
     * @param renderable Renderable
     */
    private void afterDrawRenderable(@NotNull Renderable renderable) {
//	GL20.glDisableVertexAttribArray(0);
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
