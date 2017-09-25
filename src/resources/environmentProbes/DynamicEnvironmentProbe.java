package resources.environmentProbes;

import components.renderables.*;
import core.*;
import java.util.*;
import materials.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import renderers.*;
import resources.*;
import resources.meshes.*;
import resources.shaders.*;
import resources.textures.cubeMapTexture.*;
import toolbox.*;
import toolbox.annotations.*;

public class DynamicEnvironmentProbe implements EnvironmentProbe {

    private CubeMapTexture cubeMap;
    private float maxDistance = 500;
    private float minSize = 0;
    private int resolution = 512;
    private int renderingFrequency = 1;
    private final Vector3f position;
    private EnvironmentShader shader;
    private static Matrix4f projectionMatrix;
    private final Matrix4f[] viewMatrices;
    private Fbo fbo;

    //render now and than static
    public DynamicEnvironmentProbe() {
        refresh();
        position = new Vector3f();
        shader = EnvironmentShader.getInstance();
        projectionMatrix = new Matrix4f().setPerspective(Utility.toRadians(90), 1, 0.001f, 1000);
        viewMatrices = new Matrix4f[6];
        refresshViewMatrices();
    }

    public void refresh() {
        refreshCubeMap();
        refreshFbo();
    }

    private void refreshCubeMap() {
        if (cubeMap == null || !cubeMap.isUsable()) {
            cubeMap = new DynamicCubeMapTexture(new Vector2i(resolution));
        } else {
            if (resolution != cubeMap.getSize().x) {
                releaseCubeMap();
                cubeMap = new DynamicCubeMapTexture(new Vector2i(resolution));
            }
        }
    }

    private void refreshFbo() {
        if (fbo == null || !fbo.isUsable()) {
            fbo = new Fbo(new Vector2i(resolution), false, 1, false);
            fbo.bind();
//            fbo.addAttachment(Fbo.FboAttachmentSlot.COLOR, Fbo.FboAttachmentType.TEXTURE, 0);
            fbo.addAttachment(Fbo.FboAttachmentSlot.DEPTH, Fbo.FboAttachmentType.RBO, 0);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X, cubeMap.getId(), 0);
            GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
            if (!fbo.isComplete()) {
                System.out.println("ERROR");
            }
            fbo.unbind();
        } else {
            if (resolution != fbo.getSize().x) {
                releaseFbo();
                fbo = new Fbo(new Vector2i(resolution), false, 1, false);
                fbo.bind();
//                fbo.addAttachment(Fbo.FboAttachmentSlot.COLOR, Fbo.FboAttachmentType.TEXTURE, 0);
                fbo.addAttachment(Fbo.FboAttachmentSlot.DEPTH, Fbo.FboAttachmentType.RBO, 0);
                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X, cubeMap.getId(), 0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                if (!fbo.isComplete()) {
                    System.out.println("ERROR");
                }
                fbo.unbind();
            }
        }
    }

    private void refresshViewMatrices() {
        viewMatrices[0] = new Matrix4f().lookAt(position, new Vector3f(position.x + 100, position.y, position.z), new Vector3f(0, -1, 0));
        viewMatrices[1] = new Matrix4f().lookAt(position, new Vector3f(position.x - 100, position.y, position.z), new Vector3f(0, -1, 0));
        viewMatrices[2] = new Matrix4f().lookAt(position, new Vector3f(position.x, position.y + 100, position.z), new Vector3f(0, 0, 1));
        viewMatrices[3] = new Matrix4f().lookAt(position, new Vector3f(position.x, position.y - 100, position.z), new Vector3f(0, 0, 1));
        viewMatrices[4] = new Matrix4f().lookAt(position, new Vector3f(position.x, position.y, position.z + 100), new Vector3f(0, -1, 0));
        viewMatrices[5] = new Matrix4f().lookAt(position, new Vector3f(position.x, position.y, position.z - 100), new Vector3f(0, -1, 0));
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(float maxDistance) {
        if (maxDistance < 0) {
            throw new IllegalArgumentException("Max distance can't be lower than 0");
        }
        this.maxDistance = maxDistance;
    }

    public float getMinSize() {
        return minSize;
    }

    public void setMinSize(float minSize) {
        if (minSize < 0) {
            throw new IllegalArgumentException("Size can't be lower than 0");
        }
        this.minSize = minSize;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        if (resolution <= 0) {
            throw new IllegalArgumentException("Resolution must be higher than 0");
        }
        this.resolution = resolution;
        refresh();
    }

    public int getRenderingFrequency() {
        return renderingFrequency;
    }

    public void setRenderingFrequency(int renderingFrequency) {
        if (renderingFrequency < 0) {
            throw new IllegalArgumentException("Rendering frequency can't be lower than 0");
        }
        this.renderingFrequency = renderingFrequency;
    }

    @NotNull
    public Vector3f getPosition() {
        return position;
    }

    @Override
    public void setPosition(@NotNull Vector3f position) {
        this.position.set(position);
        refresshViewMatrices();
    }

    @Override
    public void update() {
        if (renderingFrequency == 0) {
            render();
        } else if (GameLoop.getFrameCount() % renderingFrequency == 0) {
            render();
        }
    }

    public void render() {
        OpenGl.setFaceCulling(true);
        beforeDrawShader();

        List<Class> renderers = new ArrayList<>();
        for (int i = 0; i < RenderingPipeline.getNumberOfRenderers(true); i++) {
            Class renderer = RenderingPipeline.getRenderer(true, i).getClass();
            if (renderer != getClass()) {
                renderers.add(renderer);
            }
        }
//        refresh();
        cubeMap.bind();
        fbo.bind();

        for (int i = 0; i < 6; i++) {
            shader.loadViewMatrix(viewMatrices[i]);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, cubeMap.getId(), 0);
            OpenGl.setClearColor(new Vector4f(0, 1, 0, 1));
//            if (i == 0) {
//                OpenGl.setClearColor(new Vector4f(1, 0, 0, 1));
//            } else if (i == 1) {
//                OpenGl.setClearColor(new Vector4f(0, 1, 0, 1));
//            } else if (i == 2) {
//                OpenGl.setClearColor(new Vector4f(0, 0, 1, 1));
//            } else if (i == 3) {
//                OpenGl.setClearColor(new Vector4f(1, 1, 0, 1));
//            } else if (i == 4) {
//                OpenGl.setClearColor(new Vector4f(1, 0, 1, 1));
//            } else if (i == 5) {
//                OpenGl.setClearColor(new Vector4f(0, 1, 1, 1));
//            }
            OpenGl.clear(true, true, false);
            for (Class renderer : renderers) {
                for (Mesh mesh : Scene.getMeshes(renderer)) {
                    beforeDrawRenderable(mesh);
                    MeshComponent meshComponent;
                    for (int j = 0; j < Scene.getNumberOfMeshComponents(renderer, mesh); j++) {
                        meshComponent = Scene.getMeshComponent(renderer, mesh, j);
//                        if (meshComponent.isActive() && meshComponent.isMeshActive() && camera.isInsideFrustum(meshComponent.getRealAabbMin(), meshComponent.getRealAabbMax())) {
                        beforeDrawInstance(meshComponent);
                        mesh.draw();
//                        }
                    }
                    afterDrawRenderable(mesh);
                }
            }
        }
        shader.stop();
        fbo.unbind();
        cubeMap.unbind();
        OpenGl.setFaceCulling(true);
    }

    /**
     * Prepares the shader to the rendering.
     */
    private void beforeDrawShader() {
        if (shader == null || !shader.isUsable()) {
            shader = EnvironmentShader.getInstance();
        }
        shader.start();
        shader.loadGlobalUniforms(projectionMatrix);
        OpenGl.setViewport(new Vector2i(resolution), new Vector2i());
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
     * Prepares the MeshComponent to the rendering.
     *
     * @param rc MeshComponent
     */
    private void beforeDrawInstance(@NotNull MeshComponent rc) {
//        numberOfRenderedElements++;
//        numberOfRenderedFaces += rc.getMesh().getFaceCount();
        Transform transform = rc.getGameObject().getTransform();
        shader.loadObjectUniforms(transform.getModelMatrix(), new Matrix3f(transform.getInverseModelMatrix()));
        Material material = rc.getMaterial();
        shader.loadMaterial(material);
        if (!rc.isTwoSided()) {
            OpenGl.setFaceCulling(true);
        } else {
            OpenGl.setFaceCulling(false);
        }
    }

    public void releaseCubeMap() {
        cubeMap.release();
        cubeMap = null;
    }

    public void releaseFbo() {
        fbo.release();
        fbo = null;
    }

    public void release() {
        releaseCubeMap();
        releaseFbo();
        shader.release();
    }

    @Override
    public void bindToTextureUnit(int textureUnit) {
        cubeMap.bindToTextureUnit(textureUnit);
    }

}
