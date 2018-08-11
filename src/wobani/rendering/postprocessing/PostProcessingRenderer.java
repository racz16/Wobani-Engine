package wobani.rendering.postprocessing;

import org.joml.*;
import wobani.rendering.*;
import wobani.resource.mesh.*;
import wobani.resource.opengl.shader.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.*;

/**
 Abstract class for post processing renderers.
 */
public abstract class PostProcessingRenderer extends Renderer{

    /**
     The fullscreen quad.
     */
    private final QuadMesh quad = QuadMesh.getInstance();
    /**
     GeometryRenderer's shader.
     */
    protected Shader shader;

    @Override
    public void render(){
        beforeShader();
        shader.start();
        beforeDrawQuad();
        quad.draw();
        afterDrawQuad();
        shader.stop();
    }

    /**
     Refreshes the shader if it's not usable.
     */
    protected abstract void refreshShader();

    /**
     Prepares the shader to the rendering.
     */
    private void beforeShader(){
        OpenGl.setWireframe(false);
        RenderingPipeline.getPostProcessingRenderingStage().bindFbo();
        OpenGl.setViewport(RenderingPipeline.getRenderingSize(), new Vector2i());
        refreshShader();
    }

    /**
     Prepares the quad and the texture to the rendering.
     */
    protected void beforeDrawQuad(){
        quad.beforeDraw();
        //        GL20.glEnableVertexAttribArray(0);
        //        GL20.glEnableVertexAttribArray(1);
        Texture2D image = RenderingPipeline.getParameters().get(RenderingPipeline.WORK).getValue();
        image.bindToTextureUnit(0);
    }

    /**
     Unbinds the quad's VAO after rendering.
     */
    protected void afterDrawQuad(){
        //        GL20.glDisableVertexAttribArray(0);
        //        GL20.glDisableVertexAttribArray(1);
        quad.afterDraw();
    }

    @Override
    public void release(){
        shader.release();
    }

    @Override
    public void removeFromRenderingPipeline(){

    }

    @Override
    public String toString(){
        return "PostProcessingBase{" + "shader=" + shader + ", quad=" + quad + '}';
    }

}
