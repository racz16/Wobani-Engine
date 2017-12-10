package rendering;

import components.camera.*;
import components.light.lightTypes.*;
import core.*;
import java.util.*;
import org.joml.*;
import rendering.geometry.*;
import rendering.postProcessing.*;
import rendering.prepare.*;
import rendering.stages.*;
import resources.*;
import resources.textures.texture2D.*;
import toolbox.*;
import toolbox.annotations.*;
import window.*;

/**
 * Renders the scene with the specified renderers. In the Geometry Stage you can
 * render the scene in different ways like Blinn-Phong shading, toon shading,
 * shadow mapping etc. In the Screen Space Stage you can perform post processing
 * effects or draw UI elements.
 */
public class RenderingPipeline {

    /**
     * The pipeline's rendering scale. It scales up or down the size of the
     * window's client area.
     */
    private static float renderingScale = 1;
    /**
     * The pipeline's main FBO. The scene rendering performed here.
     */
    private static Fbo fbo;
    /**
     * Renders the final texture to the screen.
     */
    private static ScreenRenderer screenRenderer;

    private static SkyBoxRenderer skyboxRenderer;
    /**
     * Map of texture parameters.
     */
    private static final Map<String, Texture2D> textureParameters = new HashMap<>();
    /**
     * Map of matrix parameters.
     */
    private static final Map<String, Matrix4f> matrixParameters = new HashMap<>();
    /**
     * Map of float parameters.
     */
    private static final Map<String, Float> floatParameters = new HashMap<>();

    private static PrepareRenderingStage prepare = new PrepareRenderingStage();
    private static final List<GeometryRenderingStage> geometry = new ArrayList<>();
    private static PostProcessingRenderingStage post = new PostProcessingRenderingStage();

    /**
     * Shadowmap key.
     */
    public static final String TEXTURE_SHADOWMAP = "TEXTURE_SHADOWMAP";
    /**
     * Working frame's key. After the scene stage you can reach here the actual
     * work in progress version of the current frame (and the final, displayed
     * texture at the end of the rendering).
     */
    public static final String TEXTURE_WORK = "TEXTURE_WORK";

    /**
     * To can't create RenderingPipeline instance.
     */
    private RenderingPipeline() {
    }

    /**
     * Initializes the RenderingPipeline. You should call this method before
     * using it. However the GameLoop's initialize methods call it.
     */
    public static void initialize() {
        useBlinnPhongPipeline();
        refresh();
    }

    /**
     * Refreshes the FBOs if the MSAA level or the rendering size changed and
     * the screen renderer if released.
     */
    private static void refresh() {
        if (fbo == null || !fbo.isUsable() || Settings.getMsaaLevel() != fbo.getNumberOfSamples() || !getRenderingSize().equals(fbo.getSize())) {
            //geometry FBO
            if (fbo != null) {
                fbo.release();
            }
            fbo = new Fbo(getRenderingSize(), Settings.getMsaaLevel() != 1, Settings.getMsaaLevel(), true);
            fbo.bind();
            fbo.addAttachment(Fbo.FboAttachmentSlot.COLOR, Fbo.FboAttachmentType.TEXTURE, 0);
            fbo.addAttachment(Fbo.FboAttachmentSlot.DEPTH, Fbo.FboAttachmentType.RBO, 0);
            if (!fbo.isComplete()) {
                Utility.logError(fbo.getStatus().name());
                throw new RuntimeException("Incomplete FBO");
            }
            fbo.unbind();
        }
        if (screenRenderer == null || !screenRenderer.isUsable()) {
            screenRenderer = ScreenRenderer.getInstance();
        }
        if (skyboxRenderer == null || !skyboxRenderer.isUsable()) {
            skyboxRenderer = SkyBoxRenderer.getInstance();
        }
    }

    /**
     * Returns the rendering scale. It scales up or down the size of the
     * window's client area.
     *
     * @return rendering scale
     */
    public static float getRenderingScale() {
        return renderingScale;
    }

    /**
     * Sets the rendering scale to the given value. It scales up or down the
     * size of the window's client area.
     *
     * @param renderingScale rendering scale
     *
     * @throws IllegalArgumentException rendering scale must be higher than 0
     */
    public static void setRenderingScale(float renderingScale) {
        if (renderingScale <= 0) {
            throw new IllegalArgumentException("Rendering scale must be higher than 0");
        }
        RenderingPipeline.renderingScale = renderingScale;
        refresh();
    }

    /**
     * Returns the pipeline's rendering size. It depends on the window's client
     * area and the rendering scale.
     *
     * @return the pipeline's rendering size
     */
    @NotNull @ReadOnly
    public static Vector2i getRenderingSize() {
        Vector2i renderingSize = new Vector2i();
        renderingSize.x = (int) (Window.getClientAreaSize().x * renderingScale);
        renderingSize.y = (int) (Window.getClientAreaSize().y * renderingScale);
        return renderingSize;
    }

    /**
     * Binds the pipelin's FBO for rendering.
     */
    public static void bindFbo() {
        fbo.bind();
    }

    public static void addRenderingStage(int index) {
        geometry.add(index, new GeometryRenderingStage());
    }

    public static void addRenderingStageToTheEnd() {
        geometry.add(new GeometryRenderingStage());
    }

    public static GeometryRenderingStage getRenderingStage(int index) {
        return geometry.get(index);
    }

    public static void removeRenderingStage(int index) {
        geometry.remove(index).release();
    }

    public static int getRenderingStageCount() {
        return geometry.size();
    }

    public static PostProcessingRenderingStage getPostProcessingRenderingStage() {
        return post;
    }

    //
    //parameters----------------------------------------------------------------
    //
    /**
     * Returns the specified texture parameter. The general rule is that you
     * shouldn't release texture parameters unless yout put it to the
     * parameters. Try to not destroy other renderers' work. On the other hand,
     * yout should take care of your own texture parameters' release. You should
     * always chechk whether the return value is null.
     *
     * @param key parameter's key
     *
     * @return texture parameter
     */
    @Nullable
    public static Texture2D getTextureParameter(@NotNull String key) {
        return textureParameters.get(key);
    }

    /**
     * Sets specified texture parameter to the given value. The general rule is
     * that you shouldn't override texture parameters unless yout put it to the
     * parameters. Try to not destroy other renderers' work. On the other hand,
     * yout should take care of your own texture parameters' release.
     *
     * @param key     parameter's key
     * @param texture parameter's value
     */
    public static void setTextureParameter(@NotNull String key, @Nullable Texture2D texture) {
        if (key == null) {
            throw new NullPointerException();
        }
        textureParameters.put(key, texture);
    }

    /**
     * Returns the specified matrix parameter. You should always chechk whether
     * the return value is null.
     *
     * @param key parameter's key
     *
     * @return matrix parameter
     */
    @Nullable
    public static Matrix4f getMatrixParameter(@NotNull String key) {
        return matrixParameters.get(key);
    }

    /**
     * Sets specified matrix parameter to the given value. The general rule is
     * that you shouldn't override matrix parameters unless yout put it to the
     * parameters. Try to not destroy other renderers' work.
     *
     * @param key    parameter's key
     * @param matrix parameter's value
     */
    public static void setMatrixParameter(@NotNull String key, @Nullable Matrix4f matrix) {
        if (key == null) {
            throw new NullPointerException();
        }
        matrixParameters.put(key, matrix);
    }

    /**
     * Returns the specified matrix parameter. You should always chechk whether
     * the return value is null.
     *
     * @param key parameter's key
     *
     * @return matrix parameter
     */
    @Nullable
    public static Float getFloatParameter(@NotNull String key) {
        return floatParameters.get(key);
    }

    /**
     * Sets specified float parameter to the given value. The general rule is
     * that you shouldn't override float parameters unless yout put it to the
     * parameters. Try to not destroy other renderers' work.
     *
     * @param key   parameter's key
     * @param value parameter's value
     */
    public static void setFloatParameter(@NotNull String key, @Nullable Float value) {
        if (key == null) {
            throw new NullPointerException();
        }
        floatParameters.put(key, value);
    }

    //
    //rendering-----------------------------------------------------------------
    //
    /**
     * Renders the scene.
     */
    public static void render() {
        beforeRender();
        OpenGl.setDepthTest(true);
        prepare.render();
        for (GeometryRenderingStage stage : geometry) {
            stage.render();
        }
        skyboxRenderer.render();
        post.beforeRender(fbo);
        post.render();
        afterRender();
    }

    /**
     * Preperes for the rendering.
     */
    private static void beforeRender() {
        OpenGl.bindDefaultFrameBuffer();
        OpenGl.clear(true, true, false);
        refresh();
        bindFbo();
        OpenGl.clear(true, true, false);
        //is there camera and dir light?
        Camera camera = Scene.getCamera();
        DirectionalLight light = Scene.getDirectionalLight();
        if (camera == null || !camera.isActive() || light == null || !light.isActive()) {
            throw new IllegalStateException("There is no active main directiona light or camera");
        }
    }

    /**
     * Renders the final image to the screen.
     */
    private static void afterRender() {
        screenRenderer.render();
        setTextureParameter(TEXTURE_WORK, null);
    }

    //
    //misc----------------------------------------------------------------------
    //
    /**
     * Releases the rendering pipeline.
     */
    public static void release() {
        for (GeometryRenderingStage stage : geometry) {
            stage.release();
        }
        post.release();
        screenRenderer.release();
        if (fbo != null) {
            fbo.release();
        }
        for (Texture2D texture : textureParameters.values()) {
            if (texture != null && texture.isUsable()) {
                texture.release();
            }
        }
    }

    /**
     * Sets the rendering pipeline to use the Blinn-Phong shading for rendering
     * meshes, draw the splines with a single color, create shadow map and some
     * post processing effects.
     */
    @NotNull
    public static void useBlinnPhongPipeline() {
        //remove all
        prepare.addRendererToTheEnd(ShadowRenderer.getInstance());
        prepare.addRendererToTheEnd(EnvironmentMapRenderer.getInstance());

        addRenderingStageToTheEnd();
        GeometryRenderingStage main = getRenderingStage(getRenderingStageCount() - 1);
        main.addRendererToTheEnd(BlinnPhongRenderer.getInstance());
        main.addRendererToTheEnd(SolidColorRenderer.getInstance());

        PostProcessingRenderer renderer = InvertRenderer.getInstance();
        renderer.setActive(false);
        post.addRendererToTheEnd(renderer);
//
        renderer = GrayscaleRenderer.getInstance();
        renderer.setActive(false);
        post.addRendererToTheEnd(renderer);
//
        renderer = FxaaRenderer.getInstance();
        renderer.setActive(false);
        post.addRendererToTheEnd(renderer);

        renderer = ReinhardToneMappingRenderer.getInstance();
        renderer.setActive(false);
        post.addRendererToTheEnd(renderer);

        post.addRendererToTheEnd(GammaCorrectionRenderer.getInstance());
    }

}
