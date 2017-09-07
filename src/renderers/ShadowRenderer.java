package renderers;

import components.light.*;
import components.renderables.*;
import core.*;
import java.util.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import resources.*;
import resources.meshes.*;
import resources.splines.*;
import resources.textures.*;
import shaders.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * Performs shadow map rendering.
 */
public class ShadowRenderer extends Renderer {

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

    /**
     * Creates a new ShadowRenderer.
     *
     */
    private ShadowRenderer() {
        shader = ShadowShader.getInstance();
        refresh();
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
     * Refreshes the FBO.
     *
     * @see Settings#getShadowMapResolution()
     */
    private void refresh() {
        if (Settings.isShadowMapping() && isActive()) {
            if (fbo == null || !fbo.isUsable() || Settings.getShadowMapResolution() != fbo.getSize().x) {
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
        if (!Settings.isShadowMapping()) {
            return;
        }

        beforeShader();
        shader.start();

        DirectionalLightComponent light = (DirectionalLightComponent) Scene.getDirectionalLight();
        Matrix4f projectionViewMatrix = light.getProjectionViewMatrix();

        List<Class> renderers = new ArrayList<>();
        for (int i = 0; i < RenderingPipeline.getNumberOfRenderers(true); i++) {
            Class renderer = RenderingPipeline.getRenderer(true, i).getClass();
            if (renderer != getClass()) {
                renderers.add(renderer);
            }
        }
        for (Class renderer : renderers) {
            //meshes
            for (Mesh mesh : Scene.getMeshes(renderer)) {
                beforeDrawRenderable(mesh);
                MeshComponent meshComponent;
                for (int i = 0; i < Scene.getNumberOfMeshComponents(renderer, mesh); i++) {
                    meshComponent = Scene.getMeshComponent(renderer, mesh, i);
                    if (meshComponent.isActive() && meshComponent.isMeshActive() && meshComponent.isCastShadow() && light.isInsideFrustum(meshComponent.getRealAabbMin(), meshComponent.getRealAabbMax())) {
                        beforeDrawMeshInstance(meshComponent, projectionViewMatrix, meshComponent.getGameObject().getTransform().getModelMatrix());
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
                    if (splineComponent.isActive() && splineComponent.isSplineActive() && splineComponent.isCastShadow() && light.isInsideFrustum(splineComponent.getRealAabbMin(), splineComponent.getRealAabbMax())) {
                        beforeDrawSplineInstance(projectionViewMatrix, splineComponent.getGameObject().getTransform().getModelMatrix());
                        spline.draw();
                        numberOfRenderedElements++;
                    }
                }
                afterDrawRenderable(spline);
            }
        }
        shader.stop();
        afterShader();
        RenderingPipeline.setTextureParameter(RenderingPipeline.TEXTURE_SHADOWMAP, fbo.getTextureAttachment(Fbo.FboAttachmentSlot.DEPTH, 0));
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
     * @param meshComponent MeshComponent
     * @param projectionViewMatrix projection view matrix
     * @param modelMatrix model matrix
     */
    private void beforeDrawMeshInstance(MeshComponent meshComponent, @NotNull Matrix4f projectionViewMatrix, @NotNull Matrix4f modelMatrix) {
        loadProjectionViewModelMatrix(projectionViewMatrix, modelMatrix);
        if (!meshComponent.isTwoSided()) {
            OpenGl.setFaceCulling(true);
            GL11.glEnable(GL11.GL_CULL_FACE);
        } else {
            OpenGl.setFaceCulling(true);
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
    }

    /**
     * Prepares for rendering the Spline.
     *
     * @param projectionViewMatrix projection view matrix
     * @param modelMatrix model matrix
     */
    private void beforeDrawSplineInstance(@NotNull Matrix4f projectionViewMatrix, @NotNull Matrix4f modelMatrix) {
        loadProjectionViewModelMatrix(projectionViewMatrix, modelMatrix);
    }

    /**
     * Loads the projection view model matrix to the shader as uniform variable.
     *
     * @param projectionViewMatrix projection view matrix
     * @param modelMatrix model matrix
     */
    private void loadProjectionViewModelMatrix(@NotNull Matrix4f projectionViewMatrix, @NotNull Matrix4f modelMatrix) {
        Matrix4f projectionViewModelMatrix = new Matrix4f();
        projectionViewMatrix.mul(modelMatrix, projectionViewModelMatrix);
        shader.loadProjectionViewModelMatrix(projectionViewModelMatrix);
    }

    /**
     * Generates a new FBO for shadowmap rendering.
     */
    private void generateFbo() {
        if (fbo == null || !fbo.isUsable()) {
            int size = Settings.getShadowMapResolution();
            fbo = new Fbo(new Vector2i(size), false, 1, false);
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
     * method call you can't use this Renderer.
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
        Texture2D shadowMap = RenderingPipeline.getTextureParameter(RenderingPipeline.TEXTURE_SHADOWMAP);
        if (shadowMap != null) {
            if (shadowMap.isUsable()) {
                shadowMap.release();
            }
            RenderingPipeline.setTextureParameter(RenderingPipeline.TEXTURE_SHADOWMAP, null);
        }
    }

    @Override
    public boolean isGeometryRenderer() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nShadowRenderer{" + "shader=" + shader
                + ", fbo=" + fbo + '}';
    }

}
