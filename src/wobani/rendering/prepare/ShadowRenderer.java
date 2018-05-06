package wobani.rendering.prepare;

import java.util.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.components.renderables.*;
import wobani.core.*;
import wobani.rendering.*;
import wobani.rendering.geometry.*;
import wobani.rendering.stages.*;
import wobani.resources.*;
import wobani.resources.shaders.*;
import wobani.resources.textures.texture2d.*;
import wobani.toolbox.*;
import wobani.toolbox.annotations.*;
import wobani.toolbox.parameters.*;

/**
 * Performs shadow map rendering.
 */
public class ShadowRenderer extends PrepareRenderer {

    /**
     * Shadow shader.
     */
    private ShadowShader shader;
    /**
     * Shadow map's framebuffer object.
     */
    private Fbo fbo;
    /**
     * The only ShadowRenderer instance.
     */
    private static ShadowRenderer instance;

    private int resolution = 2048;
    /**
     * The directional light's shadow camera's distance from the user's camera's
     * center.
     */
    private float distance = 400;
    /**
     * The directional light's shadow camera's near plane's distance.
     */
    private float nearDistance = 0.1f;
    /**
     * The directional light's shadow camera's far plane's distance.
     */
    private float farDistance = 10000;
    /**
     * Light's projection view matrix.
     */
    private final Matrix4f projectionViewMatrix = new Matrix4f();
    /**
     * Performs the frustum intersection tests for shadow mapping.
     */
    private final FrustumIntersection frustum = new FrustumIntersection();

    /**
     * Determines whether the frustum culling is enabled.
     */
    private boolean frustumCulling = true;

    /**
     * Determines whether frustum culling is enabled.
     *
     * @return true if frustum culling is enabled, false otherwise
     */
    public boolean isFrustumCulling() {
	return frustumCulling;
    }

    /**
     * Sets whether or not frustum culling is enabled.
     *
     * @param frustumCulling true if frustum culling should be enabled, false
     *                       otherwise
     */
    public void setFrustumCulling(boolean frustumCulling) {
	this.frustumCulling = frustumCulling;
    }

    /**
     * Creates a new ShadowRenderer.
     * <p>
     */
    private ShadowRenderer() {
	shader = ShadowShader.getInstance();
	//refresh();
    }

    /**
     * Returns the ShadowRenderer instance.
     *
     * @return the ShadowRenderer instance
     */
    @NotNull
    public static ShadowRenderer getInstance() {
	if (instance == null) {
	    instance = new ShadowRenderer();
	}
	return instance;
    }

    /**
     * Returns true if the sphere (determined by the given parameters) is
     * inside, or intersects the frustum and returns false if it is fully
     * outside. Note that if frustum culling is disabled, or this Component
     * isn't connected to a GameObject this method always returns true.
     *
     * @param position position
     * @param radius   radius
     *
     * @return false if the sphere is fully outside the frustum, true otherwise
     *
     * @throws NullPointerException     position can't be null
     * @throws IllegalArgumentException radius can't be negative
     * @see Settings#isFrustumCulling()
     */
    public boolean isInsideFrustum(@NotNull Vector3f position, float radius) {
	if (position == null) {
	    throw new NullPointerException();
	}
	if (radius < 0) {
	    throw new IllegalArgumentException("Radius can't be negative");
	}
	if (isFrustumCulling()) {
	    refresh();
	    return frustum.testSphere(position, radius);
	} else {
	    return true;
	}
    }

    /**
     * Returns true if the axis alligned bounding box (determined by the given
     * parameters) is inside, or intersects the frustum and returns false if it
     * is fully outside. Note that if frustum culling is disabled, or this
     * Component isn't connected to a GameObject this method always returns
     * true.
     *
     * @param aabbMin the axis alligned bounding box's minimum x, y and z values
     * @param aabbMax the axis alligned bounding box's maximum x, y and z values
     *
     * @return false if the bounding box is fully outside the frustum, true
     *         otherwise
     *
     * @throws NullPointerException the parameters can't be null
     * @see Settings#isFrustumCulling()
     */
    public boolean isInsideFrustum(@NotNull Vector3f aabbMin, @NotNull Vector3f aabbMax) {
	if (aabbMin == null || aabbMax == null) {
	    throw new NullPointerException();
	}
	if (isFrustumCulling()) {
	    refresh();
	    return frustum.testAab(aabbMin, aabbMax);
	} else {
	    return true;
	}
    }

    public int getResolution() {
	return resolution;
    }

    public void setResolution(int resolution) {
	if (resolution <= 0) {
	    throw new IllegalArgumentException("");
	}
	this.resolution = resolution;
    }

    /**
     * Returns the shadow camera's distance from the user's camera's center.
     *
     * @return the shadow camera's distance from the user's camera's center
     */
    public float getShadowCameraDistance() {
	return distance;
    }

    /**
     * Sets the shadow camera's distance from the user's camera's center to the
     * given value.
     *
     * @param shadowCameraDistance the shadow camera's distance from the user's
     *                             camera's center
     *
     * @throws IllegalArgumentException the parameter have to be higher than
     *                                  zero
     */
    public void setShadowCameraDistance(float shadowCameraDistance) {
	if (shadowCameraDistance <= 0) {
	    throw new IllegalArgumentException("the parameter have to be higher than zero");
	}
	this.distance = shadowCameraDistance;
    }

    /**
     * Returns the shadow camera's near plane's distance.
     *
     * @return the shadow camera's near plane's distance
     */
    public float getShadowCameraNearDistance() {
	return nearDistance;
    }

    /**
     * Sets the shadow camera's near plane's distance to the given value.
     *
     * @param shadowCameraNearDistance the shadow camera's near plane's distance
     *
     * @throws IllegalArgumentException near plane's distance must be higher
     *                                  than 0 and it can't be higher than the
     *                                  far plane's distance
     */
    public void setShadowCameraNearDistance(float shadowCameraNearDistance) {
	if (shadowCameraNearDistance <= 0) {
	    throw new IllegalArgumentException("Near plane's distance must be higher than 0");
	}
	if (shadowCameraNearDistance > farDistance) {
	    throw new IllegalArgumentException("Near plane's distance can't be higher than the far plane's distance");
	}
	this.nearDistance = shadowCameraNearDistance;
    }

    /**
     * Returns the shadow camera's far plane's distance.
     *
     * @return the shadow camera's far plane's distance
     */
    public float getShadowCameraFarDistance() {
	return farDistance;
    }

    /**
     * Sets the shadow camera's far plane's distance to the given value.
     *
     * @param shadowCameraFarDistance the shadow camera's far plane's distance
     *
     * @throws IllegalArgumentException far plane's distance must be higher than
     *                                  the near plane's distance
     */
    public void setShadowCameraFarDistance(float shadowCameraFarDistance) {
	if (nearDistance > shadowCameraFarDistance) {
	    throw new IllegalArgumentException("Far plane's distance must be higher than the near plane's distance");
	}
	this.farDistance = shadowCameraFarDistance;
    }

    /**
     * Refreshes the FBO.
     *
     * @see Settings#getShadowMapResolution()
     */
    private void refresh() {
	if (isActive()) {
	    projectionViewMatrix.set(Utility.computeShadowMapProjectionViewMatrix(getShadowCameraDistance(), getShadowCameraNearDistance(), getShadowCameraFarDistance()));
	    frustum.set(projectionViewMatrix);
	    RenderingPipeline.getParameters().set(RenderingPipeline.SHADOW_PROJECTION_VIEW_MATRIX, new Parameter<>(new Matrix4f(projectionViewMatrix)));
	    if (fbo == null || !fbo.isUsable() || getResolution() != fbo.getSize().x) {
		releaseFbo();
		generateFbo();
	    }
	} else {
	    numberOfRenderedFaces = 0;
	    numberOfRenderedElements = 0;
	    releaseFbo();
	}

    }

    /**
     * Renders the shadow map.
     */
    @Override
    public void render() {
	refresh();

	beforeShader();
	shader.start();

	List<Class<? extends GeometryRenderer>> renderers = new ArrayList<>();
	for (int j = 0; j < RenderingPipeline.getRenderingStageCount(); j++) {
	    GeometryRenderingStage stage = RenderingPipeline.getRenderingStage(j);
	    for (int i = 0; i < stage.getRendererCount(); i++) {
		Class renderer = stage.getRenderer(i).getClass();
		if (renderer != getClass()) {
		    renderers.add(renderer);
		}
	    }
	}
	RenderableContainer renderables = RenderingPipeline.getRenderableComponents();
	for (Class<? extends GeometryRenderer> renderer : renderers) {
	    for (Renderable renderable : renderables.getRenderables(renderer)) {
		beforeDrawRenderable(renderable);
		RenderableComponent renderableComponent;
		for (int i = 0; i < renderables.getRenderableComponentCount(renderer, renderable); i++) {
		    renderableComponent = renderables.getRenderableComponent(renderer, renderable, i);
		    if (renderableComponent.isActive() && renderableComponent.isRenderableActive() && renderableComponent.isCastShadow() && isInsideFrustum(renderableComponent)) {
			beforeDrawMeshInstance(projectionViewMatrix, renderableComponent.getGameObject().getTransform().getModelMatrix());
			renderableComponent.draw();
			numberOfRenderedElements++;
			numberOfRenderedFaces += renderableComponent.getFaceCount();
		    }
		}
		afterDrawRenderable(renderable);
	    }
	}
	shader.stop();
	afterShader();
	RenderingPipeline.getParameters().set(RenderingPipeline.SHADOWMAP, new Parameter<>(fbo.getTextureAttachment(Fbo.FboAttachmentSlot.DEPTH, 0)));
    }

    /**
     * Prepares the shader to the rendering.
     */
    private void beforeShader() {
	if (shader == null || !shader.isUsable()) {
	    shader = ShadowShader.getInstance();
	}
	OpenGl.setViewport(fbo.getSize(), new Vector2i());
	OpenGl.setFaceCullingMode(OpenGl.FaceCullingMode.FRONT);
	fbo.bind();
	OpenGl.clear(false, true, false);
	numberOfRenderedElements = 0;
	numberOfRenderedFaces = 0;
    }

    /**
     * Unbinds the FBO and prepares the pipeline to the next stage of the
     * rendering.
     */
    private void afterShader() {
	fbo.unbind();
	OpenGl.setViewport(RenderingPipeline.getRenderingSize(), new Vector2i());
	OpenGl.setFaceCullingMode(OpenGl.FaceCullingMode.BACK);
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
     * Unbinds the Renderable's VAO after rendering.
     *
     * @param renderable Renderable
     */
    private void afterDrawRenderable(@NotNull Renderable renderable) {
	GL20.glDisableVertexAttribArray(0);
	renderable.afterDraw();
    }

    /**
     * Prepares for rendering the Mesh.
     *
     * @param renderableComponent  OldMeshComponent
     * @param projectionViewMatrix projection view matrix
     * @param modelMatrix          model matrix
     */
    private void beforeDrawMeshInstance(@NotNull Matrix4f projectionViewMatrix, @NotNull Matrix4f modelMatrix) {
	Matrix4f projectionViewModelMatrix = new Matrix4f();
	projectionViewMatrix.mul(modelMatrix, projectionViewModelMatrix);
	shader.loadProjectionViewModelMatrix(projectionViewModelMatrix);
    }

    /**
     * Generates a new FBO for shadowmap rendering.
     */
    private void generateFbo() {
	if (fbo == null || !fbo.isUsable()) {
	    fbo = new Fbo(new Vector2i(getResolution()), false, 1, false);
	    fbo.bind();
	    fbo.addAttachment(Fbo.FboAttachmentSlot.DEPTH, Fbo.FboAttachmentType.TEXTURE, 0);
	    fbo.setActiveDraw(false, 0);
	    fbo.setActiveRead(false, 0);
	    if (!fbo.isComplete()) {
		throw new RuntimeException("Incomplete FBO");
	    }
	    fbo.unbind();
	}
    }

    /**
     * Determines whether the given mesh component is inside the directional
     * light's view frustum.
     *
     * @param renderableComponent mesh component
     *
     * @return true if the mesh component is inside the directional light's view
     *         frustum, false otherwise
     */
    private boolean isInsideFrustum(@NotNull RenderableComponent renderableComponent) {
	Transform transform = renderableComponent.getGameObject().getTransform();
//        if (transform.getBillboardingMode() == Transform.BillboardingMode.NO_BILLBOARDING) {
	return isInsideFrustum(renderableComponent.getBoundingShape().getRealAabbMin(), renderableComponent.getBoundingShape().getRealAabbMax());
//        } else {
//            return isInsideMainCameraFrustumAabb(transform.getAbsolutePosition(), renderableComponent.getRealRadius());
//        }
    }

    /**
     * Removes the FBO from the GPU's memory.
     */
    private void releaseFbo() {
	if (fbo != null) {
	    fbo.release();
	    fbo = null;
	}
    }

    /**
     * Removes the shader program and the FBO from the GPU's memory. After this
     * method call you can't use this GeometryRenderer.
     */
    @Override
    public void release() {
	releaseFbo();
	shader.release();
    }

    @Override
    public boolean isUsable() {
	return true;
    }

    @Override
    public void setActive(boolean active) {
	if (isActive() != active) {
	    super.setActive(active);
	    refresh();
	}
    }

    @Override
    public void removeFromRenderingPipeline() {
	Texture2D shadowMap = RenderingPipeline.getParameters().get(RenderingPipeline.SHADOWMAP).getValue();
	if (shadowMap != null) {
	    if (shadowMap.isUsable()) {
		shadowMap.release();
	    }
	    RenderingPipeline.getParameters().set(RenderingPipeline.SHADOWMAP, null);
	}
    }

    @Override
    public String toString() {
	return super.toString() + "\nShadowRenderer{" + "shader=" + shader
		+ ", fbo=" + fbo + '}';
    }

}
