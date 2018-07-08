package wobani.rendering.geometry;

import org.joml.*;
import wobani.component.camera.*;
import wobani.component.light.blinnphong.*;
import wobani.component.renderable.*;
import wobani.core.*;
import wobani.material.*;
import wobani.rendering.*;
import wobani.resources.*;
import wobani.resources.shader.*;
import wobani.resources.texture.texture2d.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.parameter.*;

/**
 * This GeometryRenderer can draw meshes and splines by using the Blinn-Phong
 * shading. You can fill the materials with diffuse color or diffuse map,
 * specular color or specular map and normal map. If you set the appropirate
 * parameters, the specular map's alpha channel used as the glossiness value and
 * the normal map's alpha channel as a parallax map. If you don't fill the
 * diffuse or specular slots, the shader uses default values (basically you can
 * even use this GeometryRenderer with an empty material).
 *
 * @see Material#PARAM_POM_MAX_LAYERS_F
 * @see Material#PARAM_POM_MIN_LAYERS_F
 * @see Material#PARAM_POM_SCALE_F
 * @see Material#PARAM_USE_POM_F
 * @see Material#PARAM_REFRACTION_INDEX_F
 * @see Material#PARAM_USE_GLOSSINESS_F
 */
public class BlinnPhongRenderer extends GeometryRenderer {

    /**
     * Blinn-Phong shader.
     */
    private BlinnPhongShader shader;
    /**
     * The only BlinnPhongRenderer instance.
     */
    private static BlinnPhongRenderer instance;

    /**
     * Key of the main BlinnPhongDirectionalLight Parameter.
     */
    public static final ParameterKey<BlinnPhongDirectionalLightComponent> MAIN_DIRECTIONAL_LIGHT = new ParameterKey<>(BlinnPhongDirectionalLightComponent.class, "MAIN_DIRECTIONAL_LIGHT");

    /**
     * Initializes a new BlinnPhongRenderer.
     */
    private BlinnPhongRenderer() {
	shader = new BlinnPhongShader();
    }

    /**
     * Returns the BlinnPhongRenderer instance.
     *
     * @return the BlinnPhongRenderer instance
     */
    @NotNull
    public static BlinnPhongRenderer getInstance() {
	if (instance == null) {
	    instance = new BlinnPhongRenderer();
	}
	return instance;
    }

    /**
     * Renders the scene.
     */
    @Override
    public void render() {
	beforeDrawShader();
	Class<BlinnPhongRenderer> renderer = BlinnPhongRenderer.class;
	RenderableContainer renderables = RenderingPipeline.getRenderableComponents();
	for (Renderable renderable : renderables.getRenderables(renderer)) {
	    beforeDrawRenderable(renderable);
	    RenderableComponent<?> renderableComponent;
	    for (int i = 0; i < renderables.getRenderableComponentCount(renderer, renderable); i++) {
		renderableComponent = renderables.getRenderableComponent(renderer, renderable, i);
		if (renderableComponent.isActive() && renderableComponent.isRenderableActive() && Utility.isInsideMainCameraFrustumAabb(renderableComponent)) {
		    beforeDrawInstance(renderableComponent);
		    renderableComponent.draw();
		}
	    }
	    afterDrawRenderable(renderable);
	}
	shader.stop();
	OpenGl.setFaceCulling(true);
    }

    /**
     * Prepares the shader to the rendering.
     */
    private void beforeDrawShader() {
	if (!Utility.isUsable(shader)) {
	    shader = new BlinnPhongShader();
	}
	CameraComponent.refreshMatricesUbo();
	BlinnPhongLightSources.refresh();
	shader.start();
	shader.loadGlobalUniforms();
	RenderingPipeline.bindFbo();
	OpenGl.setViewport(RenderingPipeline.getRenderingSize(), new Vector2i());
	boolean wirefreame = RenderingPipeline.getParameters().getValueOrDefault(RenderingPipeline.WIREFRAME_MODE, false);
	OpenGl.setWireframe(wirefreame);
	numberOfRenderedElements = 0;
	numberOfRenderedFaces = 0;
	//shadow map
	Parameter<Texture2D> shadowMap = RenderingPipeline.getParameters().get(RenderingPipeline.SHADOWMAP);
	if (shadowMap != null) {
	    shadowMap.getValue().bindToTextureUnit(0);
	}
    }

    /**
     * Prepares the given model to the rendering.
     *
     * @param renderable Renderable
     */
    private void beforeDrawRenderable(@NotNull Renderable renderable) {
	renderable.beforeDraw();
    }

    /**
     * Unbinds the model's VAO and the vertex attrib arrays after rendering.
     *
     * @param renderable Renderable
     */
    private void afterDrawRenderable(@NotNull Renderable renderable) {
	renderable.afterDraw();
    }

    private void beforeDrawInstance(@NotNull RenderableComponent rc) {
	numberOfRenderedElements++;
	numberOfRenderedFaces += rc.getFaceCount();
	Transform transform = rc.getGameObject().getTransform();
	BlinnPhongLightSources.bindClosestLightSources(transform.getAbsolutePosition());
	shader.loadObjectUniforms(transform.getModelMatrix(), new Matrix3f(transform.getInverseModelMatrix()), rc.isReceiveShadows());
	Material material = rc.getMaterial();
	shader.loadMaterial(material);
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
	return super.toString() + "\nBlinnPhongRenderer{" + "shader=" + shader + '}';
    }

}
