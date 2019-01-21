package wobani.rendering.stage;

import org.joml.*;
import wobani.rendering.*;
import wobani.rendering.postprocessing.*;
import wobani.resource.opengl.fbo.*;
import wobani.resource.opengl.fbo.fboenum.*;
import wobani.resource.opengl.texture.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.exceptions.*;
import wobani.toolbox.parameter.*;

import java.util.*;

import static wobani.toolbox.EngineInfo.Library.*;

public class PostProcessingRenderingStage{

    /**
     List of screenspace renderers. You may add to this list renderers like tone mapping, FXAA or UI renderers.
     */
    private final List<PostProcessingRenderer> postProcessingRenderers = new ArrayList<>();
    /**
     FBO for resolving the multisampled textures and performing ping-pong rendering in the screen space stage.
     */
    private Fbo postProcessingFbo;
    /**
     The index of the post processing FBO's color attachment where you shouldn't render to.
     */
    private int notDraw = 0;
    /**
     The index of the post processing FBO's color attachment where you should render to.
     */
    private int draw = 1;

    /**
     Refreshes the FBOs if the MSAA level or the rendering size changed and the screen renderer if released.
     */
    private void refresh(){
        if(postProcessingFbo == null || !postProcessingFbo.isUsable() || !RenderingPipeline.getRenderingSize()
                .equals(postProcessingFbo.getAttachmentContainer(FboAttachmentSlot.COLOR, 1).getAttachment().getSize())){
            //screen space FBO
            if(postProcessingFbo != null){
                postProcessingFbo.release();
            }
            postProcessingFbo = new Fbo();
            postProcessingFbo.bind();
            postProcessingFbo.getAttachmentContainer(FboAttachmentSlot.COLOR, 0).attach(new DynamicTexture2D(RenderingPipeline.getRenderingSize(), Texture.TextureInternalFormat.RGBA16F, false));
            postProcessingFbo.getAttachmentContainer(FboAttachmentSlot.COLOR, 1).attach(new DynamicTexture2D(RenderingPipeline.getRenderingSize(), Texture.TextureInternalFormat.RGBA16F, false));
            DynamicTexture2D texture = postProcessingFbo.getAttachmentContainer(FboAttachmentSlot.COLOR, 0).getTextureAttachment();
            texture.setFilter(Texture.TextureFilter.BILINEAR);
            //texture.setFilter(Texture.TextureFilterType.MINIFICATION, Texture.TextureFilter.LINEAR);
            //texture.setFilter(Texture.TextureFilterType.MAGNIFICATION, Texture.TextureFilter.LINEAR);
            texture = postProcessingFbo.getAttachmentContainer(FboAttachmentSlot.COLOR, 1).getTextureAttachment();
            texture.setFilter(Texture.TextureFilter.BILINEAR);
            //texture.setFilter(Texture.TextureFilterType.MINIFICATION, Texture.TextureFilter.LINEAR);
            //texture.setFilter(Texture.TextureFilterType.MAGNIFICATION, Texture.TextureFilter.LINEAR);
            if(!postProcessingFbo.isDrawComplete()){
                Utility.logError(postProcessingFbo.getDrawStatus().name());
                throw new NativeException(OPENGL, "Incomplete FBO");
            }
        }
    }

    /**
     Binds the pipelin's FBO for rendering.
     */
    public void bindFbo(){
        postProcessingFbo.bind();
        postProcessingFbo.setDrawBuffers(draw);
    }

    /**
     Swaps the FBO's color attachments' index where render what you want in the Screen Space Stage. It's similar to
     double buffering.
     */
    private void swapFboAttachments(){
        int temp = draw;
        draw = notDraw;
        notDraw = temp;
        RenderingPipeline.getParameters().set(RenderingPipeline.WORK, new Parameter<>(postProcessingFbo.getAttachmentContainer(FboAttachmentSlot.COLOR, notDraw).getTextureAttachment()));
    }

    public boolean addRendererToTheEnd(@NotNull PostProcessingRenderer renderer){
        return addRenderer(getNumberOfRenderers(), renderer);
    }

    public boolean addRenderer(int index, @NotNull PostProcessingRenderer renderer){
        for(PostProcessingRenderer ren : postProcessingRenderers){
            if(ren.getClass() == renderer.getClass()){
                return false;
            }
        }
        postProcessingRenderers.add(index, renderer);
        return true;
    }

    /**
     Returns the indexth renderer of the specified stage.

     @param index index

     @return the indexth renderer of the specified stage
     */
    @NotNull
    public PostProcessingRenderer getRenderer(int index){
        return postProcessingRenderers.get(index);
    }

    /**
     Returns the number of the renderers in the specified stage.

     @return the number of the renderers in the specified stage
     */
    public int getNumberOfRenderers(){
        return postProcessingRenderers.size();
    }

    /**
     Removes the specified stage's indexth renderer.

     @param index index

     @throws NullPointerException stage can't be null
     */
    public void removeRenderer(int index){//FIXME why public??
        postProcessingRenderers.remove(index).removeFromRenderingPipeline();
    }

    private void removePipeline(@NotNull List<? extends Renderer> list){
        while(!list.isEmpty()){
            removeRenderer(0);
        }
    }

    public void release(){
        removePipeline(postProcessingRenderers);
        if(postProcessingFbo != null){
            postProcessingFbo.release();
        }
    }

    /**
     Renders the scene.
     */
    public void render(){
        renderStage();
        RenderingPipeline.getParameters().set(RenderingPipeline.WORK, new Parameter<>(postProcessingFbo.getAttachmentContainer(FboAttachmentSlot.COLOR, notDraw).getTextureAttachment()));
    }

    /**
     Preperes for the rendering.
     */
    public void beforeRender(Fbo geometryFbo){
        refresh();
        postProcessingFbo.setDrawBuffers(0);
        FboAttachmentContainer from = geometryFbo.getAttachmentContainer(FboAttachmentSlot.COLOR, 0);
        FboAttachmentContainer to = postProcessingFbo.getAttachmentContainer(FboAttachmentSlot.COLOR, 0);
        geometryFbo.blitTo(postProcessingFbo, new Vector2i(0), from.getAttachment().getSize(), new Vector2i(0), to.getAttachment().getSize(), FboAttachmentSlot.COLOR);

        /*if(!rendered){
            DynamicTexture2D texture = postProcessingFbo.getAttachmentContainer(FboAttachmentSlot.COLOR, 0).getTextureAttachment();
            ByteBuffer data = texture.getByteSubImage(new Vector2i(0), texture.getSize(), Texture.TextureDataType.UNSIGNED_BYTE);
            STBImageWrite.stbi_flip_vertically_on_write(true);
            STBImageWrite.stbi_write_bmp("att0.bmp", texture.getSize().x, texture.getSize().y, 4, data);

            texture = postProcessingFbo.getAttachmentContainer(FboAttachmentSlot.COLOR, 1).getTextureAttachment();
            data = texture.getByteSubImage(new Vector2i(0), texture.getSize(), Texture.TextureDataType.UNSIGNED_BYTE);
            STBImageWrite.stbi_flip_vertically_on_write(true);
            STBImageWrite.stbi_write_bmp("att1.bmp", texture.getSize().x, texture.getSize().y, 4, data);

            rendered = true;
        }*/


        RenderingPipeline.getParameters().set(RenderingPipeline.WORK, new Parameter<>(postProcessingFbo.getAttachmentContainer(FboAttachmentSlot.COLOR, 0).getTextureAttachment()));
        notDraw = 0;
        draw = 1;
        bindFbo();
        OpenGl.clear(true, true, false);
        OpenGl.setDepthTest(false);
    }

    private static boolean rendered;

    /**
     Renders with the list of given renderers.
     */
    private void renderStage(){
        for(PostProcessingRenderer renderer : postProcessingRenderers){
            if(renderer.isActive()){
                renderer.render();
                swapFboAttachments();
            }
        }

        /*
        if(!rendered && Time.getTime() > 5){
            rendered = true;
            DynamicTexture2D texture = postProcessingFbo.getAttachmentContainer(FboAttachmentSlot.COLOR, notDraw).getTextureAttachment();
            ByteBuffer data = texture.getByteSubImage(new Vector2i(0), texture.getSize(), Texture.TextureDataType.UNSIGNED_BYTE);
            STBImageWrite.stbi_flip_vertically_on_write(true);
            STBImageWrite.stbi_write_bmp("save.bmp", texture.getSize().x, texture.getSize().y, 4, data);
        }*/
    }
}
