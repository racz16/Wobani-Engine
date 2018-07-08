package wobani.rendering.stage;

import wobani.resources.buffers.Fbo;
import java.util.*;
import wobani.rendering.*;
import wobani.rendering.postprocessing.*;
import wobani.resources.*;
import wobani.resources.texture.*;
import wobani.resources.texture.texture2d.*;
import static wobani.toolbox.EngineInfo.Library.OPENGL;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.exceptions.*;
import wobani.toolbox.parameter.*;

public class PostProcessingRenderingStage {

    /**
     * FBO for resolving the multisampled textures and performing ping-pong
     * rendering in the screen space stage.
     */
    private Fbo postProcessingFbo;
    /**
     * List of screenspace renderers. You may add to this list renderers like
     * tone mapping, FXAA or UI renderers.
     */
    private final List<PostProcessingRenderer> postProcessingRenderers = new ArrayList<>();
    /**
     * The index of the post processing FBO's color attachment where you
     * shouldn't render to.
     */
    private int notDraw = 0;
    /**
     * The index of the post processing FBO's color attachment where you should
     * render to.
     */
    private int draw = 1;

    /**
     * Refreshes the FBOs if the MSAA level or the rendering size changed and
     * the screen renderer if released.
     */
    private void refresh() {
	if (postProcessingFbo == null || !postProcessingFbo.isUsable() || !RenderingPipeline.getRenderingSize().equals(postProcessingFbo.getSize())) {
	    //screen space FBO
	    if (postProcessingFbo != null) {
		postProcessingFbo.release();
	    }
	    postProcessingFbo = new Fbo(RenderingPipeline.getRenderingSize(), false, 1, true);
	    postProcessingFbo.bind();
	    postProcessingFbo.addAttachment(Fbo.FboAttachmentSlot.COLOR, Fbo.FboAttachmentType.TEXTURE, 0);
	    postProcessingFbo.addAttachment(Fbo.FboAttachmentSlot.COLOR, Fbo.FboAttachmentType.TEXTURE, 1);
	    DynamicTexture2D texture = (DynamicTexture2D) postProcessingFbo.getTextureAttachment(Fbo.FboAttachmentSlot.COLOR, 0);
	    texture.setFilter(Texture.TextureFilterType.MINIFICATION, Texture.TextureFilter.LINEAR);
	    texture.setFilter(Texture.TextureFilterType.MAGNIFICATION, Texture.TextureFilter.LINEAR);
	    texture = (DynamicTexture2D) postProcessingFbo.getTextureAttachment(Fbo.FboAttachmentSlot.COLOR, 1);
	    texture.setFilter(Texture.TextureFilterType.MINIFICATION, Texture.TextureFilter.LINEAR);
	    texture.setFilter(Texture.TextureFilterType.MAGNIFICATION, Texture.TextureFilter.LINEAR);
	    if (!postProcessingFbo.isComplete()) {
		Utility.logError(postProcessingFbo.getStatus().name());
		throw new NativeException(OPENGL, "Incomplete FBO");
	    }
	}
    }

    /**
     * Binds the pipelin's FBO for rendering.
     */
    public void bindFbo() {
	postProcessingFbo.bind();
	postProcessingFbo.setActiveDraw(false, notDraw);
	postProcessingFbo.setActiveDraw(true, draw);
    }

    /**
     * Swaps the FBO's color attachments' index where render what you want in
     * the Screen Space Stage. It's similar to double buffering.
     */
    private void swapFboAttachments() {
	int temp = draw;
	draw = notDraw;
	notDraw = temp;
	RenderingPipeline.getParameters().set(RenderingPipeline.WORK, new Parameter<>(postProcessingFbo.getTextureAttachment(Fbo.FboAttachmentSlot.COLOR, notDraw)));
    }

    public boolean addRendererToTheEnd(@NotNull PostProcessingRenderer renderer) {
	return addRenderer(getNumberOfRenderers(), renderer);
    }

    public boolean addRenderer(int index, @NotNull PostProcessingRenderer renderer) {
	for (PostProcessingRenderer ren : postProcessingRenderers) {
	    if (ren.getClass() == renderer.getClass()) {
		return false;
	    }
	}
	postProcessingRenderers.add(index, renderer);
	return true;
    }

    /**
     * Returns the indexth renderer of the specified stage.
     *
     * @param geometry specifies the list of Renderers
     * @param index    index
     *
     * @return the indexth renderer of the specified stage
     */
    @NotNull
    public PostProcessingRenderer getRenderer(int index) {
	return postProcessingRenderers.get(index);
    }

    /**
     * Returns the number of the renderers in the specified stage.
     *
     * @param geometry specifies the list of Renderers
     *
     * @return the number of the renderers in the specified stage
     */
    public int getNumberOfRenderers() {
	return postProcessingRenderers.size();
    }

    /**
     * Removes the specified stage's indexth renderer.
     *
     * @param geometry specifies the list of Renderers
     * @param index    index
     *
     * @throws NullPointerException stage can't be null
     */
    public void removeRenderer(int index) {//FIXME why public??
	postProcessingRenderers.remove(index).removeFromRenderingPipeline();
    }

    private void removePipeline(@NotNull List<? extends Renderer> list) {
	while (!list.isEmpty()) {
	    removeRenderer(0);
	}
    }

    public void release() {
	removePipeline(postProcessingRenderers);
	if (postProcessingFbo != null) {
	    postProcessingFbo.release();
	}
    }

    /**
     * Renders the scene.
     */
    public void render() {
	renderStage();
	RenderingPipeline.getParameters().set(RenderingPipeline.WORK, new Parameter<>(postProcessingFbo.getTextureAttachment(Fbo.FboAttachmentSlot.COLOR, notDraw)));
    }

    /**
     * Preperes for the rendering.
     */
    public void beforeRender(Fbo geometryFbo) {
	refresh();
	geometryFbo.resolveFbo(postProcessingFbo, Fbo.FboAttachmentSlot.COLOR, Fbo.FboAttachmentType.TEXTURE, 0, 0);
	RenderingPipeline.getParameters().set(RenderingPipeline.WORK, new Parameter<>(postProcessingFbo.getTextureAttachment(Fbo.FboAttachmentSlot.COLOR, 0)));
	notDraw = 0;
	draw = 1;
	bindFbo();
	OpenGl.clear(true, true, false);
	OpenGl.setDepthTest(false);
    }

    /**
     * Renders with the list of given renderers.
     *
     * @param stage list of renderers
     */
    private void renderStage() {
	for (PostProcessingRenderer renderer : postProcessingRenderers) {
	    if (renderer.isActive()) {
		renderer.render();
		swapFboAttachments();
	    }
	}
    }
}
