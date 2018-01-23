package wobani.rendering.prepare;

import java.util.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.components.environmentProbes.*;
import wobani.components.renderables.*;
import wobani.core.*;
import wobani.materials.*;
import wobani.rendering.*;
import wobani.rendering.geometry.*;
import wobani.rendering.stages.*;
import wobani.resources.*;
import wobani.resources.environmentProbes.*;
import wobani.resources.shaders.*;
import wobani.toolbox.*;
import wobani.toolbox.annotations.*;

public class EnvironmentMapRenderer extends PrepareRenderer {

    private EnvironmentShader shader;
    /**
     * The only EnvironmentMapRenderer instance.
     */
    private static EnvironmentMapRenderer instance;

    private static SkyBoxRenderer skyboxRenderer;

    private EnvironmentMapRenderer() {
        shader = EnvironmentShader.getInstance();
    }

    /**
     * Returns the EnvironmentMapRenderer instance.
     *
     * @return the EnvironmentMapRenderer instance
     */
    @NotNull
    public static EnvironmentMapRenderer getInstance() {
        if (instance == null) {
            instance = new EnvironmentMapRenderer();
        }
        return instance;
    }

    @Override
    public void render() {
        beforeDrawShader();

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

        for (int probeIndex = 0; probeIndex < Scene.getComponentLists().getComponentCount(DynamicEnvironmentProbeComponent.class); probeIndex++) {
            DynamicEnvironmentProbe probe = Scene.getComponentLists().getComponent(DynamicEnvironmentProbeComponent.class, probeIndex).getProbe();
            if (!probe.shouldRenderNow()) {
                continue;
            }
            probe.refresh();
            OpenGl.setViewport(probe.getSize(), new Vector2i());

            probe.bindCubeMap();
            probe.bindFbo();
            for (int faceIndex = 0; faceIndex < 6; faceIndex++) {
                shader.loadViewMatrix(probe.getViewMatrix(faceIndex));
                probe.FboTexture(faceIndex);
                OpenGl.setClearColor(new Vector4f(0, 1, 0, 1));
//                if (faceIndex == 0) {
//                    OpenGl.setClearColor(new Vector4f(1, 0, 0, 1));
//                } else if (faceIndex == 1) {
//                    OpenGl.setClearColor(new Vector4f(0, 1, 0, 1));
//                } else if (faceIndex == 2) {
//                    OpenGl.setClearColor(new Vector4f(0, 0, 1, 1));
//                } else if (faceIndex == 3) {
//                    OpenGl.setClearColor(new Vector4f(1, 1, 0, 1));
//                } else if (faceIndex == 4) {
//                    OpenGl.setClearColor(new Vector4f(1, 0, 1, 1));
//                } else if (faceIndex == 5) {
//                    OpenGl.setClearColor(new Vector4f(0, 1, 1, 1));
//                }
                OpenGl.clear(true, true, false);
                RenderableContainer renderables = Scene.getRenderableComponents();
                for (Class<? extends GeometryRenderer> renderer : renderers) {
                    for (Renderable renderable : renderables.getRenderables(renderer)) {
                        beforeDrawRenderable(renderable);
                        RenderableComponent renderableComponent;
                        for (int i = 0; i < renderables.getRenderableComponentCount(renderer, renderable); i++) {
                            renderableComponent = renderables.getRenderableComponent(renderer, renderable, i);
                            if (renderableComponent.isActive() && renderableComponent.isRenderableActive() && renderableComponent.getBoundingShape().getRealRadius() >= probe.getMinSize() && renderableComponent.getGameObject().getTransform().getAbsolutePosition().distance(probe.getPosition()) <= probe.getMaxDistance()) {
                                beforeDrawInstance(renderableComponent);
                                renderableComponent.draw();
                                numberOfRenderedElements++;
                                numberOfRenderedFaces += renderableComponent.getFaceCount();
                            }
                        }
                        afterDrawRenderable(renderable);
                    }
                }

//                for (int meshIndex = 0; meshIndex < Scene.getReflectablesCount(); meshIndex++) {
//                    OldMeshComponent meshComponent = Scene.getReflectable(meshIndex);
//                    beforeDrawRenderable(meshComponent.getMesh());
//                    if (meshComponent.isActive() && meshComponent.isMeshActive() && meshComponent.getRealFurthestVertexDistance() >= probe.getMinSize() && meshComponent.getGameObject().getTransform().getAbsolutePosition().distance(probe.getPosition()) <= probe.getMaxDistance()) {
//                        beforeDrawInstance(meshComponent);
//                        meshComponent.getMesh().draw();
//                    }
//                    afterDrawRenderable(meshComponent.getMesh());
//                }
//                skyboxRenderer.render();
            }
            probe.unbindFbo();
            probe.unbindCubeMap();
        }
        shader.stop();
        OpenGl.setFaceCulling(true);
    }

    /**
     * Prepares the shader to the rendering.
     */
    private void beforeDrawShader() {
        if (shader == null || !shader.isUsable()) {
            shader = EnvironmentShader.getInstance();
        }
        if (skyboxRenderer == null || !skyboxRenderer.isUsable()) {
            skyboxRenderer = SkyBoxRenderer.getInstance();
        }
        shader.start();
        shader.loadGlobalUniforms(DynamicEnvironmentProbe.getProjectionMatrix());
        OpenGl.setFaceCulling(true);

//        numberOfRenderedElements = 0;
//        numberOfRenderedFaces = 0;
    }

    /**
     * Prepares the given model to the rendering.
     *
     * @param renderable Renderable
     */
    private void beforeDrawRenderable(@NotNull Renderable renderable) {
        renderable.beforeDraw();
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
    }

    /**
     * Unbinds the model's VAO and the vertex attrib arrays after rendering.
     *
     * @param renderable Renderable
     */
    private void afterDrawRenderable(@NotNull Renderable renderable) {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        renderable.afterDraw();
    }

    /**
     * Prepares the OldMeshComponent to the rendering.
     *
     * @param rc OldMeshComponent
     */
    private void beforeDrawInstance(@NotNull RenderableComponent rc) {
        numberOfRenderedElements++;
        numberOfRenderedFaces += rc.getFaceCount();
        Transform transform = rc.getGameObject().getTransform();
        shader.loadObjectUniforms(transform.getModelMatrix(), new Matrix3f(transform.getInverseModelMatrix()));
        Material material = rc.getMaterial();
        shader.loadMaterial(material);
    }

    @Override
    public void release() {
        shader.release();
    }

    @Override
    public boolean isUsable() {
        return true;
    }

    @Override
    public void removeFromRenderingPipeline() {

    }

}
