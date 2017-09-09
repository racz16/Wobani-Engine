package renderers;

import components.renderables.*;
import core.*;
import materials.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import resources.*;
import resources.meshes.*;
import resources.shaders.*;
import resources.splines.*;
import resources.textures.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * This Renderer can draw meshes and splines by using the Blinn-Phong shading.
 * You can fill the materials with diffuse color or diffuse map, specular color
 * or specular map and normal map. If you set the appropirate parameters, the
 * specular map's alpha channel used as the glossiness value and the normal
 * map's alpha channel as a parallax map. If you don't fill the diffuse or
 * specular slots, the shader uses default values (basically you can even use
 * this Renderer with an empty material).
 *
 */
public class SkyBoxRenderer extends Renderer {

    /**
     * SkyBox shader.
     */
    private SkyBoxShader shader;
    /**
     * The only SkyBoxRenderer instance.
     */
    private static SkyBoxRenderer instance;

    /**
     * Initializes a new SkyBoxRenderer.
     */
    private SkyBoxRenderer() {
        shader = SkyBoxShader.getInstance();
    }

    /**
     * Returns the SkyBoxRenderer instance.
     *
     * @return the SkyBoxRenderer instance
     */
    @NotNull
    public static SkyBoxRenderer getInstance() {
        if (instance == null) {
            instance = new SkyBoxRenderer();
        }
        return instance;
    }

    /**
     * Renders the scene.
     */
    @Override
    public void render() {
        beforeDrawShader();
        Class renderer = SkyBoxRenderer.class;
        //meshes
        for (Mesh mesh : Scene.getMeshes(renderer)) {
            beforeDrawRenderable(mesh);
            MeshComponent meshComponent;
            for (int i = 0; i < Scene.getNumberOfMeshComponents(renderer, mesh); i++) {
                meshComponent = Scene.getMeshComponent(renderer, mesh, i);
                if (meshComponent.isActive() && meshComponent.isMeshActive()) {
                    beforeDrawInstance(meshComponent);
                    mesh.draw();
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
                if (splineComponent.isActive() && splineComponent.isSplineActive()) {
                    beforeDrawInstance(splineComponent);
                    spline.draw();
                }
            }
            afterDrawRenderable(spline);
        }
        shader.stop();
        OpenGl.setDepthTestMode(OpenGl.DepthTestMode.LESS);
    }

    /**
     * Prepares the shader to the rendering.
     */
    private void beforeDrawShader() {
        if (shader == null || !shader.isUsable()) {
            shader = SkyBoxShader.getInstance();
        }
        shader.start();
        RenderingPipeline.bindFbo();
        OpenGl.setDepthTestMode(OpenGl.DepthTestMode.LESS_OR_EQUAL);
        OpenGl.setViewport(RenderingPipeline.getRenderingSize(), new Vector2i());
        OpenGl.setWireframe(Settings.isWireframeMode());
        numberOfRenderedElements = 0;
        numberOfRenderedFaces = 0;
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
     * Unbinds the Renderable's VAO and the vertex attrib arrays after
     * rendering.
     *
     * @param renderable Renderable
     */
    private void afterDrawRenderable(@NotNull Renderable renderable) {
        GL20.glDisableVertexAttribArray(0);
        renderable.afterDraw();
    }

    /**
     * Prepares the MeshComponent to the rendering.
     *
     * @param rc MeshComponent
     */
    private void beforeDrawInstance(@NotNull MeshComponent rc) {
        numberOfRenderedElements++;
        numberOfRenderedFaces += rc.getMesh().getFaceCount();
        Material material = rc.getMaterial();
        CubeMapTexture texture = material.getSlot(Material.DIFFUSE).getCubeMapTexture();
        texture.bindToTextureUnit(0);
        if (!rc.isTwoSided()) {
            OpenGl.setFaceCulling(true);
        } else {
            OpenGl.setFaceCulling(false);
        }
    }

    /**
     * Prepares the SplineComponent to the rendering.
     *
     * @param rc SplineComponent
     */
    private void beforeDrawInstance(@NotNull SplineComponent rc) {
        numberOfRenderedElements++;
        Material material = rc.getMaterial();
        CubeMapTexture texture = material.getSlot(Material.DIFFUSE).getCubeMapTexture();
        texture.bindToTextureUnit(0);
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
    public boolean isGeometryRenderer() {
        return true;
    }

    @Override
    public boolean isUsable() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nSkyBoxRenderer{" + "shader=" + shader + '}';
    }

}
