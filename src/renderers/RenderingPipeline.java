package renderers;

import resources.textures.texture2D.DynamicTexture2D;
import resources.textures.texture2D.Texture2D;
import components.camera.*;
import components.light.lightTypes.*;
import core.*;
import java.util.*;
import org.joml.*;
import renderers.postProcessing.*;
import resources.*;
import resources.textures.*;
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
    private static Fbo geometryFbo;
    /**
     * FBO for resolving the multisampled textures and performing ping-pong
     * rendering in the screen space stage.
     */
    private static Fbo screenSpaceFbo;
    /**
     * List of geometry renderers. You may add to this list renderers like
     * Blinn-Phong, shadow renderer or toon renderer to actually draw the
     * scene's geometry.
     */
    private static final List<Renderer> geometryRenderers = new ArrayList<>();
    /**
     * List of screenspace renderers. You may add to this list renderers like
     * tone mapping, FXAA or UI renderers.
     */
    private static final List<Renderer> screenSpaceRenderers = new ArrayList<>();
    /**
     * Renders the final texture to the screen.
     */
    private static ScreenRenderer screenRenderer;
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
    /**
     * The index of the post processing FBO's color attachment where you
     * shouldn't render to.
     */
    private static int notDraw = 0;
    /**
     * The index of the post processing FBO's color attachment where you should
     * render to.
     */
    private static int draw = 1;
    /**
     * Determines whether the rendering pipeline is in the screen space stage.
     */
    private static boolean screenSpaceStage;

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
        if (geometryFbo == null || !geometryFbo.isUsable() || Settings.getMsaaLevel() != geometryFbo.getNumberOfSamples() || !getRenderingSize().equals(geometryFbo.getSize())) {
            //geometry FBO
            if (geometryFbo != null) {
                geometryFbo.release();
            }
            geometryFbo = new Fbo(getRenderingSize(), Settings.getMsaaLevel() != 1, Settings.getMsaaLevel(), true);
            geometryFbo.bind();
            geometryFbo.addAttachment(Fbo.FboAttachmentSlot.COLOR, Fbo.FboAttachmentType.TEXTURE, 0);
            geometryFbo.addAttachment(Fbo.FboAttachmentSlot.DEPTH, Fbo.FboAttachmentType.RBO, 0);
            if (!geometryFbo.isComplete()) {
                Utility.logError(geometryFbo.getStatus().name());
                throw new RuntimeException("Incomplete FBO");
            }
            geometryFbo.unbind();
            //screen space FBO
            if (screenSpaceFbo != null) {
                screenSpaceFbo.release();
            }
            screenSpaceFbo = new Fbo(getRenderingSize(), false, 1, true);
            screenSpaceFbo.bind();
            screenSpaceFbo.addAttachment(Fbo.FboAttachmentSlot.COLOR, Fbo.FboAttachmentType.TEXTURE, 0);
            screenSpaceFbo.addAttachment(Fbo.FboAttachmentSlot.COLOR, Fbo.FboAttachmentType.TEXTURE, 1);
            DynamicTexture2D texture = (DynamicTexture2D) screenSpaceFbo.getTextureAttachment(Fbo.FboAttachmentSlot.COLOR, 0);
            texture.setFilter(Texture.TextureFilterType.MINIFICATION, Texture.TextureFilter.LINEAR);
            texture.setFilter(Texture.TextureFilterType.MAGNIFICATION, Texture.TextureFilter.LINEAR);
            texture = (DynamicTexture2D) screenSpaceFbo.getTextureAttachment(Fbo.FboAttachmentSlot.COLOR, 1);
            texture.setFilter(Texture.TextureFilterType.MINIFICATION, Texture.TextureFilter.LINEAR);
            texture.setFilter(Texture.TextureFilterType.MAGNIFICATION, Texture.TextureFilter.LINEAR);
            if (!screenSpaceFbo.isComplete()) {
                Utility.logError(screenSpaceFbo.getStatus().name());
                throw new RuntimeException("Incomplete FBO");
            }
        }
        if (screenRenderer == null || !screenRenderer.isUsable()) {
            screenRenderer = ScreenRenderer.getInstance();
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
        if (!screenSpaceStage) {
            geometryFbo.bind();
        } else {
            screenSpaceFbo.bind();
            screenSpaceFbo.setActiveDraw(false, notDraw);
            screenSpaceFbo.setActiveDraw(true, draw);
        }
    }

    /**
     * Swaps the FBO's color attachments' index where render what you want in
     * the Screen Space Stage. It's similar to double buffering.
     */
    public static void swapFboAttachments() {
        if (screenSpaceStage) {
            int temp = draw;
            draw = notDraw;
            notDraw = temp;
            setTextureParameter(TEXTURE_WORK, RenderingPipeline.screenSpaceFbo.getTextureAttachment(Fbo.FboAttachmentSlot.COLOR, notDraw));
        }
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
     * @param key parameter's key
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
     * @param key parameter's key
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
     * @param key parameter's key
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
        renderStage(geometryRenderers);
        OpenGl.setDepthTest(false);
        resolve();
        renderStage(screenSpaceRenderers);
        afterRender();
    }

    /**
     * Preperes for the rendering.
     */
    private static void beforeRender() {
        screenSpaceStage = false;
        //clearing
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
     * Resolves the scene FBO's content to the screen space FBO.
     */
    private static void resolve() {
        screenSpaceStage = true;
        geometryFbo.resolveFbo(screenSpaceFbo, Fbo.FboAttachmentSlot.COLOR, Fbo.FboAttachmentType.TEXTURE, 0, 0);
        setTextureParameter(TEXTURE_WORK, screenSpaceFbo.getTextureAttachment(Fbo.FboAttachmentSlot.COLOR, 0));
        notDraw = 0;
        draw = 1;
    }

    /**
     * Renders the final image to the screen.
     */
    private static void afterRender() {
        setTextureParameter(TEXTURE_WORK, screenSpaceFbo.getTextureAttachment(Fbo.FboAttachmentSlot.COLOR, notDraw));
        screenRenderer.render();
        setTextureParameter(TEXTURE_WORK, null);
    }

    /**
     * Renders with the list of given renderers.
     *
     * @param stage list of renderers
     */
    private static void renderStage(@NotNull List<Renderer> stage) {
        for (Renderer renderer : stage) {
            if (renderer.isActive()) {
                renderer.render();
            }
        }
    }

    //
    //renderers-----------------------------------------------------------------
    //
    /**
     * Adds the given renderer to the end of the appropirate stage.
     *
     * @param renderer renderer
     *
     * @return true if the renderer added successfully, false otherwise
     */
    public static boolean addRendererToTheEnd(@NotNull Renderer renderer) {
        return addRenderer(getNumberOfRenderers(renderer.isGeometryRenderer()), renderer);
    }

    /**
     * Adds the given renderer to the indexth place of the appropirate stage.
     *
     * @param index index
     * @param renderer renderer
     *
     * @return true if the renderer added successfully, false otherwise
     */
    public static boolean addRenderer(int index, @NotNull Renderer renderer) {
        List<Renderer> list;
        if (renderer.isGeometryRenderer()) {
            list = geometryRenderers;
        } else {
            list = screenSpaceRenderers;
        }
        for (Renderer ren : list) {
            if (ren.getClass() == renderer.getClass()) {
                return false;
            }
        }
        list.add(index, renderer);
        return true;
    }

    /**
     * Returns the indexth renderer of the specified stage.
     *
     * @param geometry specifies the list of Renderers
     * @param index index
     *
     * @return the indexth renderer of the specified stage
     */
    @NotNull
    public static Renderer getRenderer(boolean geometry, int index) {
        if (geometry) {
            return geometryRenderers.get(index);
        } else {
            return screenSpaceRenderers.get(index);
        }
    }

    /**
     * Returns the number of the renderers in the specified stage.
     *
     * @param geometry specifies the list of Renderers
     *
     * @return the number of the renderers in the specified stage
     */
    public static int getNumberOfRenderers(boolean geometry) {
        if (geometry) {
            return geometryRenderers.size();
        } else {
            return screenSpaceRenderers.size();
        }
    }

    /**
     * Sets the appropirate rendering stage's indexth renderer to the given
     * value.
     *
     * @param index index
     * @param renderer renderer
     *
     * @return true if the renderer set successfully, false otherwise
     */
    public static boolean setRenderer(int index, @NotNull Renderer renderer) {
        List<Renderer> list;
        if (renderer.isGeometryRenderer()) {
            list = geometryRenderers;
        } else {
            list = screenSpaceRenderers;
        }

        Renderer ren;
        for (int i = 0; i < list.size(); i++) {
            ren = list.get(index);
            if (ren.getClass() == renderer.getClass() && index != i) {
                return false;
            }
        }
        list.get(index).removeFromRenderingPipeline();
        list.set(index, renderer);
        return true;
    }

    /**
     * Removes the specified stage's indexth renderer.
     *
     * @param geometry specifies the list of Renderers
     * @param index index
     *
     * @throws NullPointerException stage can't be null
     */
    public static void removeRenderer(boolean geometry, int index) {
        if (geometry) {
            geometryRenderers.remove(index).removeFromRenderingPipeline();
        } else {
            screenSpaceRenderers.remove(index).removeFromRenderingPipeline();
        }
    }

    //
    //misc----------------------------------------------------------------------
    //
    /**
     * Releases the rendering pipeline.
     */
    public static void release() {
        removePipeline(geometryRenderers);
        removePipeline(screenSpaceRenderers);
        screenRenderer.release();
        if (geometryFbo != null) {
            geometryFbo.release();
        }
        if (screenSpaceFbo != null) {
            screenSpaceFbo.release();
        }
        for (Texture2D texture : textureParameters.values()) {
            if (texture != null && texture.isUsable()) {
                texture.release();
            }
        }
    }

    /**
     * Removes all the renderer's from the list and the renderers' parameters.
     *
     * @param list list of renderers
     */
    private static void removePipeline(@NotNull List<Renderer> list) {
        while (!list.isEmpty()) {
            list.remove(0).removeFromRenderingPipeline();
        }
    }

    /**
     * Sets the rendering pipeline to use the Blinn-Phong shading for rendering
     * meshes, draw the splines with a single color, create shadow map and some
     * post processing effects.
     */
    @NotNull
    public static void useBlinnPhongPipeline() {
        removePipeline(geometryRenderers);
        removePipeline(screenSpaceRenderers);
        addRendererToTheEnd(ShadowRenderer.getInstance());
        addRendererToTheEnd(BlinnPhongRenderer.getInstance());
        addRendererToTheEnd(SolidColorRenderer.getInstance());
        addRendererToTheEnd(SkyBoxRenderer.getInstance());
        //invert
        Renderer renderer = InvertRenderer.getInstance();
        renderer.setActive(false);
        addRendererToTheEnd(renderer);
        //grayscale
        renderer = GrayscaleRenderer.getInstance();
        renderer.setActive(false);
        addRendererToTheEnd(renderer);
        //fxaa
        renderer = FxaaRenderer.getInstance();
        renderer.setActive(false);
        addRendererToTheEnd(renderer);
        //tone mapping
        renderer = ReinhardToneMappingRenderer.getInstance();
        renderer.setActive(false);
        addRendererToTheEnd(renderer);
        //gamma correction
        addRendererToTheEnd(GammaCorrectionRenderer.getInstance());
    }

}
