package rendering;

import core.*;
import java.util.*;
import org.joml.*;
import rendering.geometry.*;
import rendering.postProcessing.*;
import rendering.prepare.*;
import rendering.stages.*;
import resources.*;
import toolbox.*;
import toolbox.annotations.*;
import toolbox.parameters.*;
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

    private static final Parameters parameters = new Parameters();

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

    public static final String MATRIX_SHADOW_PROJECTION_VIEW = "MATRIX_SHADOW_PROJECTION_VIEW";
    public static final String FLOAT_GAMMA = "FLOAT_GAMMA";
    public static final String BOOLEAN_WIREFRAME_MODE = "BOOLEAN_WIREFRAME_MODE";
    public static final String INT_MSAA_LEVEL = "INT_MSAA_LEVEL";

    @NotNull
    public static Parameters getParameters() {
        return parameters;
    }

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
        addDefaultParameters();
        useBlinnPhongPipeline();
        refresh();
    }

    private static void addDefaultParameters() {
        getParameters().setBooleanParameter(BOOLEAN_WIREFRAME_MODE, new Parameter<>(false));
        getParameters().setFloatParameter(FLOAT_GAMMA, new Parameter<Float>(2.2f) {
            @Override
            public void setValue(@NotNull Float value) {
                if (value < 1) {
                    throw new IllegalArgumentException("Gamma can't be lower than 1");
                }
                super.setValue(value);
                ResourceManager.changeTextureColorSpace();
            }

            @Override
            protected void removedFromParameters(@Nullable Parameter<Float> added) {
                ResourceManager.changeTextureColorSpace();
            }

            @Override
            protected void addedToParameters(@Nullable Parameter<Float> removed) {
                ResourceManager.changeTextureColorSpace();
            }

        });
        getParameters().setIntParameter(INT_MSAA_LEVEL, new Parameter<Integer>(2) {
            @Override
            public void setValue(@NotNull Integer value) {
                if (value < 1) {
                    throw new IllegalArgumentException("MSAA can't be lower than 1");
                }
                super.setValue(value);
            }
        });
    }

    /**
     * Refreshes the FBOs if the MSAA level or the rendering size changed and
     * the screen renderer if released.
     */
    private static void refresh() {
        Parameter<Integer> msaaParameter = getParameters().getIntParameter(INT_MSAA_LEVEL);
        int msaaLevel = Parameter.getValueOrDefault(msaaParameter, 2);
        if (fbo == null || !fbo.isUsable() || msaaLevel != fbo.getNumberOfSamples() || !getRenderingSize().equals(fbo.getSize())) {
            //geometry FBO
            if (fbo != null) {
                fbo.release();
            }
            fbo = new Fbo(getRenderingSize(), msaaLevel != 1, msaaLevel, true);
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
        MainCamera mainCamera = Scene.getParameters().getParameter(MainCamera.class);
        MainDirectionalLight dirLight = Scene.getParameters().getParameter(MainDirectionalLight.class);
        if (mainCamera == null || !mainCamera.getValue().isActive() || dirLight == null || !dirLight.getValue().isActive()) {
            throw new IllegalStateException("There is no active main directiona light or camera");
        }
    }

    /**
     * Renders the final image to the screen.
     */
    private static void afterRender() {
        screenRenderer.render();
        getParameters().setTextureParameter(TEXTURE_WORK, null);
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
        getParameters().release();
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
