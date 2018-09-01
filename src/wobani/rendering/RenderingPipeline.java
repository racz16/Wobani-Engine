package wobani.rendering;

import org.joml.*;
import wobani.component.camera.*;
import wobani.component.light.blinnphong.*;
import wobani.core.*;
import wobani.rendering.geometry.*;
import wobani.rendering.postprocessing.*;
import wobani.rendering.prepare.*;
import wobani.rendering.stage.*;
import wobani.resource.opengl.fbo.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.exceptions.*;
import wobani.toolbox.parameter.*;
import wobani.window.*;

import java.util.*;

import static wobani.toolbox.EngineInfo.Library.*;

/**
 Renders the scene with the specified renderers. In the Geometry Stage you can render the scene in different ways like
 Blinn-Phong shading, toon shading, shadow mapping etc. In the Screen Space Stage you can perform post processing effects
 or draw UI elements.
 */
public class RenderingPipeline{

    //TODO: should i release manually parameters like textures?
    /**
     Shadowmap key.
     */
    public static final ParameterKey<Texture2D> SHADOWMAP = new ParameterKey<>(Texture2D.class, "SHADOWMAP");
    /**
     Working frame's key. After the scene stage you can reach here the actual work in progress version of the current
     frame (and the final, displayed texture at the end of the rendering).
     */
    public static final ParameterKey<Texture2D> WORK = new ParameterKey<>(Texture2D.class, "WORK");
    public static final ParameterKey<Matrix4f> SHADOW_PROJECTION_VIEW_MATRIX = new ParameterKey<>(Matrix4f.class, "SHADOW_PROJECTION_VIEW_MATRIX");
    public static final ParameterKey<Float> GAMMA = new ParameterKey<>(Float.class, "GAMMA");
    public static final ParameterKey<Boolean> WIREFRAME_MODE = new ParameterKey<>(Boolean.class, "WIREFRAME_MODE");
    public static final ParameterKey<Integer> MSAA_LEVEL = new ParameterKey<>(Integer.class, "MSAA_LEVEL");
    private static final ParameterContainer parameters = new ParameterContainer();
    private static final List<GeometryRenderingStage> geometry = new ArrayList<>();
    /**
     Stores all the RenderableContainer groupped by Renderers and Renderables.
     */
    private static final RenderableContainer RENDERABLE_COMPONENTS = new RenderableContainer();
    /**
     The pipeline's rendering scale. It scales up or down the size of the window's client area.
     */
    private static float renderingScale = 1;
    /**
     The pipeline's main FBO. The scene rendering performed here.
     */
    private static Fbo fbo;
    /**
     Renders the final texture to the screen.
     */
    private static ScreenRenderer screenRenderer;
    private static SkyBoxRenderer skyboxRenderer;
    private static PrepareRenderingStage prepare = new PrepareRenderingStage();
    private static PostProcessingRenderingStage post = new PostProcessingRenderingStage();

    /**
     To can't create RenderingPipeline instance.
     */
    private RenderingPipeline(){
    }

    /**
     Returns the RenderableContainer.

     @return the RenderableContainer
     */
    @NotNull
    public static RenderableContainer getRenderableComponents(){
        return RENDERABLE_COMPONENTS;
    }

    @NotNull
    public static ParameterContainer getParameters(){
        return parameters;
    }

    /**
     Initializes the RenderingPipeline. You should call this method before using it. However the GameLoop's initialize
     methods call it.
     */
    public static void initialize(){
        addDefaultParameters();
        useBlinnPhongPipeline();
        refresh();
    }

    private static void addDefaultParameters(){
        getParameters().set(WIREFRAME_MODE, new Parameter<>(false));
        getParameters().set(GAMMA, new Parameter<Float>(2.2f){
            @Override
            public void setValue(@NotNull Float value){
                if(value < 1){
                    throw new IllegalArgumentException("Gamma can't be lower than 1");
                }
                super.setValue(value);
                //ResourceManager.changeTextureColorSpace();
            }

            @Override
            protected void removedFromParameters(@Nullable Parameter<Float> added){
                //ResourceManager.changeTextureColorSpace();
            }

            @Override
            protected void addedToParameters(@Nullable Parameter<Float> removed){
                //ResourceManager.changeTextureColorSpace();
            }

        });
        getParameters().set(MSAA_LEVEL, new Parameter<Integer>(2){
            @Override
            public void setValue(@NotNull Integer value){
                if(value < 1){
                    throw new IllegalArgumentException("MSAA can't be lower than 1");
                }
                super.setValue(value);
            }
        });
    }

    /**
     Refreshes the FBOs if the MSAA level or the rendering size changed and the screen renderer if released.
     */
    private static void refresh(){
        int msaaLevel = getParameters().getValueOrDefault(MSAA_LEVEL, 2);
        if(fbo == null || !fbo.isUsable() || msaaLevel != fbo.getNumberOfSamples() || !getRenderingSize()
                .equals(fbo.getSize())){
            //geometry FBO
            if(fbo != null){
                fbo.release();
            }
            fbo = new Fbo(getRenderingSize(), msaaLevel != 1, msaaLevel, true);
            fbo.bind();
            fbo.addAttachment(Fbo.FboAttachmentSlotWrong.COLOR, Fbo.FboAttachmentType.TEXTURE, 0);
            fbo.addAttachment(Fbo.FboAttachmentSlotWrong.DEPTH, Fbo.FboAttachmentType.RBO, 0);
            if(!fbo.isComplete()){
                Utility.logError(fbo.getStatus().name());
                throw new NativeException(OPENGL, "Incomplete FBO");
            }
            fbo.unbind();
        }
        if(screenRenderer == null || !screenRenderer.isUsable()){
            screenRenderer = ScreenRenderer.getInstance();
        }
        if(skyboxRenderer == null || !skyboxRenderer.isUsable()){
            skyboxRenderer = SkyBoxRenderer.getInstance();
        }
    }

    /**
     Returns the rendering scale. It scales up or down the size of the window's client area.

     @return rendering scale
     */
    public static float getRenderingScale(){
        return renderingScale;
    }

    /**
     Sets the rendering scale to the given value. It scales up or down the size of the window's client area.

     @param renderingScale rendering scale

     @throws IllegalArgumentException rendering scale must be higher than 0
     */
    public static void setRenderingScale(float renderingScale){
        if(renderingScale <= 0){
            throw new IllegalArgumentException("Rendering scale must be higher than 0");
        }
        RenderingPipeline.renderingScale = renderingScale;
        refresh();
    }

    /**
     Returns the pipeline's rendering size. It depends on the window's client area and the rendering scale.

     @return the pipeline's rendering size
     */
    @NotNull
    @ReadOnly
    public static Vector2i getRenderingSize(){
        Vector2i renderingSize = new Vector2i();
        renderingSize.x = (int) (Window.getClientAreaSize().x * renderingScale);
        renderingSize.y = (int) (Window.getClientAreaSize().y * renderingScale);
        return renderingSize;
    }

    /**
     Binds the pipelin's FBO for rendering.
     */
    public static void bindFbo(){
        fbo.bind();
    }

    public static void addRenderingStage(int index){
        geometry.add(index, new GeometryRenderingStage());
    }

    public static void addRenderingStageToTheEnd(){
        geometry.add(new GeometryRenderingStage());
    }

    public static GeometryRenderingStage getRenderingStage(int index){
        return geometry.get(index);
    }

    public static void removeRenderingStage(int index){
        geometry.remove(index).release();
    }

    public static int getRenderingStageCount(){
        return geometry.size();
    }

    public static PostProcessingRenderingStage getPostProcessingRenderingStage(){
        return post;
    }

    //
    //rendering-----------------------------------------------------------------
    //

    /**
     Renders the scene.
     */
    public static void render(){
        beforeRender();
        OpenGl.setDepthTest(true);
        prepare.render();
        for(GeometryRenderingStage stage : geometry){
            stage.render();
        }
        skyboxRenderer.render();
        post.beforeRender(fbo);
        post.render();
        afterRender();
    }

    /**
     Preperes for the rendering.
     */
    private static void beforeRender(){
        OpenGl.bindDefaultFrameBuffer();
        OpenGl.clear(true, true, false);
        refresh();
        bindFbo();
        OpenGl.clear(true, true, false);
        //is there camera and dir light?
        Parameter<Camera> mainCamera = Scene.getParameters().get(Scene.MAIN_CAMERA);
        Parameter<BlinnPhongDirectionalLightComponent> dirLight = Scene.getParameters()
                .get(BlinnPhongRenderer.MAIN_DIRECTIONAL_LIGHT);
        if(mainCamera == null || !mainCamera.getValue().isActive() || dirLight == null || !dirLight.getValue()
                .isActive()){
            throw new IllegalStateException("There is no active main directiona light or camera");
        }
    }

    /**
     Renders the final image to the screen.
     */
    private static void afterRender(){
        screenRenderer.render();
        getParameters().set(WORK, null);
    }

    //
    //misc----------------------------------------------------------------------
    //

    /**
     Releases the rendering pipeline.
     */
    public static void release(){
        for(GeometryRenderingStage stage : geometry){
            stage.release();
        }
        post.release();
        screenRenderer.release();
        if(fbo != null){
            fbo.release();
        }
    }

    /**
     Sets the rendering pipeline to use the Blinn-Phong shading for rendering meshes, draw the splines with a single
     color, create shadow map and some post processing effects.
     */
    @NotNull
    public static void useBlinnPhongPipeline(){
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

        renderer = GrayscaleRenderer.getInstance();
        renderer.setActive(false);
        post.addRendererToTheEnd(renderer);

        renderer = FxaaRenderer.getInstance();
        renderer.setActive(false);
        post.addRendererToTheEnd(renderer);

        renderer = ReinhardToneMappingRenderer.getInstance();
        renderer.setActive(false);
        post.addRendererToTheEnd(renderer);

        post.addRendererToTheEnd(GammaCorrectionRenderer.getInstance());
    }

}
