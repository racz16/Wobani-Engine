package wobani.resource.opengl.fbo;

import org.joml.*;
import org.lwjgl.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import wobani.resource.*;
import wobani.resource.opengl.*;
import wobani.resource.opengl.fbo.fboenum.*;
import wobani.resource.opengl.texture.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.nio.*;
import java.util.*;

import static org.lwjgl.system.MemoryStack.*;
import static wobani.resource.ExceptionHelper.*;
import static wobani.resource.opengl.fbo.fboenum.FboAttachmentSlot.*;

public class Fbo extends OpenGlObject{

    private final FboAttachmentContainer[] color = new FboAttachmentContainer[getMaxColorAttachments()];
    private final FboAttachmentContainer depth = new FboAttachmentContainer(this, DEPTH, 0);
    private final FboAttachmentContainer stencil = new FboAttachmentContainer(this, STENCIL, 0);
    private final FboAttachmentContainer depthStencil = new FboAttachmentContainer(this, DEPTH_STENCIL, 0);
    private int readBuffer;

    private static Fbo readBound;
    private static Fbo drawBound;
    private static final FboPool FBO_POOL = new FboPool();

    public Fbo(){
        super(new ResourceId());
        setId(FBO_POOL.getResource());
        for(int i = 0; i < color.length; i++){
            color[i] = new FboAttachmentContainer(this, COLOR, i);
        }
    }

    @Override
    protected int getType(){
        return GL30.GL_FRAMEBUFFER;
    }

    @Override
    protected String getTypeName(){
        return "FBO";
    }

    //attachments-------------------------------------------------------------------------------------------------------
    @Nullable
    public FboAttachmentContainer getAttachmentContainer(FboAttachmentSlot slot, int index){
        ExceptionHelper.exceptionIfNull(slot);
        switch(slot){
            case COLOR:
                return color[index];
            case DEPTH:
                return depth;
            case STENCIL:
                return stencil;
            case DEPTH_STENCIL:
                return depthStencil;
        }
        return null;
    }

    public static int getMaxColorAttachments(){
        return OpenGlConstants.MAX_COLOR_ATTACHMENTS;
    }

    public static int getMaxColorAttachmentsSafe(){
        return OpenGlConstants.MAX_COLOR_ATTACHMENTS_SAFE;
    }

    //read buffer-------------------------------------------------------------------------------------------------------
    public int getReadBuffer(){
        return readBuffer;
    }

    public void setReadBuffer(int index){
        ExceptionHelper.exceptionIfNotUsable(this);
        ExceptionHelper.exceptionIfLowerOrEquals(index, color.length);
        setReadBufferUnsafe(index);
    }

    private void setReadBufferUnsafe(int index){
        if(index < 0){
            readBuffer = -1;
            GL45.glNamedFramebufferReadBuffer(getId(), GL11.GL_NONE);
        }else{
            readBuffer = index;
            GL45.glNamedFramebufferReadBuffer(getId(), GL30.GL_COLOR_ATTACHMENT0 + index);
        }
    }

    //draw buffers------------------------------------------------------------------------------------------------------
    @ReadOnly
    @NotNull
    public Set<Integer> getDrawBuffers(){
        Set<Integer> result = new HashSet<>();
        for(FboAttachmentContainer fac : color){
            if(fac.isDrawBuffer()){
                result.add(fac.getIndex());
            }
        }
        return result;
    }

    public void setDrawBuffers(@NotNull Set<Integer> indices){
        Integer[] result = new Integer[indices.size()];
        indices.toArray(result);
        setDrawBuffers(result);
    }

    public void setDrawBuffers(@NotNull Integer... indices){
        ExceptionHelper.exceptionIfNotUsable(this);
        ExceptionHelper.exceptionIfLower(indices.length, getMaxDrawBuffers());
        setDrawBuffersUnsafe(indices);
    }

    private void setDrawBuffersUnsafe(@NotNull Integer[] indices){
        List<Integer> indicesList = Arrays.asList(indices);
        try(MemoryStack stack = stackPush()){
            IntBuffer result = stack.mallocInt(color.length);
            setDrawBufferIndices(result, indicesList);
            result.flip();
            GL45.glNamedFramebufferDrawBuffers(getId(), result);
        }
    }

    private void setDrawBufferIndices(IntBuffer result, List<Integer> indices){
        setDrawBufferReadIndices(result, indices);
        if(result.position() == 0){
            result.put(GL11.GL_NONE);
        }
    }

    private void setDrawBufferReadIndices(IntBuffer result, List<Integer> indices){
        for(FboAttachmentContainer fac : color){
            if(indices.contains(fac.getIndex())){
                result.put(GL30.GL_COLOR_ATTACHMENT0 + fac.getIndex());
                fac.setDrawBuffer(true);
            }else{
                fac.setDrawBuffer(false);
            }
        }
    }

    public static int getMaxDrawBuffers(){
        return OpenGlConstants.MAX_DRAW_BUFFERS;
    }

    public static int getMaxDrawBuffersSafe(){
        return OpenGlConstants.MAX_DRAW_BUFFERS_SAFE;
    }

    //status------------------------------------------------------------------------------------------------------------
    public boolean isReadComplete(){
        return getReadStatus() == FboCompleteness.COMPLETE;
    }

    public boolean isDrawComplete(){
        return getDrawStatus() == FboCompleteness.COMPLETE;
    }

    @NotNull
    public FboCompleteness getReadStatus(){
        return getStatusUnsafe(GL30.GL_READ_FRAMEBUFFER);
    }

    @NotNull
    public FboCompleteness getDrawStatus(){
        return getStatusUnsafe(GL30.GL_DRAW_FRAMEBUFFER);
    }

    @NotNull
    private FboCompleteness getStatusUnsafe(int target){
        ExceptionHelper.exceptionIfNotUsable(this);
        int code = GL45.glCheckNamedFramebufferStatus(getId(), target);
        return FboCompleteness.valueOf(code);
    }

    //bind--------------------------------------------------------------------------------------------------------------
    public void bind(){
        ExceptionHelper.exceptionIfNotUsable(this);
        GL30.glBindFramebuffer(getType(), getId());
        readBound = this;
        drawBound = this;
    }

    public void bindRead(){
        ExceptionHelper.exceptionIfNotUsable(this);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, getId());
        readBound = this;
    }

    public void bindDraw(){
        ExceptionHelper.exceptionIfNotUsable(this);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, getId());
        drawBound = this;
    }

    public void unbind(){
        GL30.glBindFramebuffer(getType(), 0);
        readBound = null;
        drawBound = null;
    }

    @Nullable
    public static Fbo getReadBound(){
        return readBound;
    }

    @Nullable
    public static Fbo getDrawBound(){
        return drawBound;
    }

    //blit--------------------------------------------------------------------------------------------------------------
    public void blitTo(@NotNull Fbo destination, @NotNull Vector2i fromOffset, @NotNull Vector2i fromSize, @NotNull Vector2i toOffset, @NotNull Vector2i toSize, @NotNull FboAttachmentSlot slot){
        ExceptionHelper.exceptionIfNotUsable(this);
        ExceptionHelper.exceptionIfNotUsable(destination);
        ExceptionHelper.exceptionIfNull(fromOffset, fromSize, toOffset, toSize, slot);
        GL45.glBlitNamedFramebuffer(getId(), destination.getId(), fromOffset.x, fromOffset.y, fromSize.x, fromSize.y, toOffset.x, toOffset.y, toSize.x, toSize.y, slot.getBitMask(), GL11.GL_NEAREST);
    }

    //misc--------------------------------------------------------------------------------------------------------------

    @NotNull
    public ByteBuffer readBytePixels(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull FboAttachmentSlot slot, @NotNull Texture.TextureDataType type){
        checkReadPixelsExceptions(offset, size, slot, type, ByteBuffer.class);
        FboAttachment fa = getAttachmentContainer(slot, readBuffer).getAttachment();
        exceptionIfAreaExceedsFromSize(size, offset, fa.getSize());
        return readBytePixelsUnsafe(offset, size, fa.getInternalFormat(), type);
    }

    @NotNull
    private ByteBuffer readBytePixelsUnsafe(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull Texture.TextureInternalFormat tif, @NotNull Texture.TextureDataType type){
        ByteBuffer data = BufferUtils.createByteBuffer(size.x * size.y * tif.getColorChannelCount());
        GL11.glReadPixels(offset.x, offset.y, size.x, size.y, tif.convert().getCode(), type.getCode(), data);
        return data;
    }

    @NotNull
    public ShortBuffer readShortPixels(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull FboAttachmentSlot slot, @NotNull Texture.TextureDataType type){
        checkReadPixelsExceptions(offset, size, slot, type, ShortBuffer.class);
        FboAttachment fa = getAttachmentContainer(slot, readBuffer).getAttachment();
        exceptionIfAreaExceedsFromSize(size, offset, fa.getSize());
        return readShortPixelsUnsafe(offset, size, fa.getInternalFormat(), type);
    }

    @NotNull
    private ShortBuffer readShortPixelsUnsafe(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull Texture.TextureInternalFormat tif, @NotNull Texture.TextureDataType type){
        ShortBuffer data = BufferUtils.createShortBuffer(size.x * size.y * tif.getColorChannelCount());
        GL11.glReadPixels(offset.x, offset.y, size.x, size.y, tif.convert().getCode(), type.getCode(), data);
        return data;
    }

    @NotNull
    public IntBuffer readIntPixels(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull FboAttachmentSlot slot, @NotNull Texture.TextureDataType type){
        checkReadPixelsExceptions(offset, size, slot, type, IntBuffer.class);
        FboAttachment fa = getAttachmentContainer(slot, readBuffer).getAttachment();
        exceptionIfAreaExceedsFromSize(size, offset, fa.getSize());
        return readIntPixelsUnsafe(offset, size, fa.getInternalFormat(), type);
    }

    @NotNull
    private IntBuffer readIntPixelsUnsafe(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull Texture.TextureInternalFormat tif, @NotNull Texture.TextureDataType type){
        IntBuffer data = BufferUtils.createIntBuffer(size.x * size.y * tif.getColorChannelCount());
        GL11.glReadPixels(offset.x, offset.y, size.x, size.y, tif.convert().getCode(), type.getCode(), data);
        return data;
    }

    @NotNull
    public FloatBuffer readFloatPixels(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull FboAttachmentSlot slot, @NotNull Texture.TextureDataType type){
        checkReadPixelsExceptions(offset, size, slot, type, FloatBuffer.class);
        FboAttachment fa = getAttachmentContainer(slot, readBuffer).getAttachment();
        exceptionIfAreaExceedsFromSize(size, offset, fa.getSize());
        return readFloatPixelsUnsafe(offset, size, fa.getInternalFormat(), type);
    }

    @NotNull
    private FloatBuffer readFloatPixelsUnsafe(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull Texture.TextureInternalFormat tif, @NotNull Texture.TextureDataType type){
        FloatBuffer data = BufferUtils.createFloatBuffer(size.x * size.y * tif.getColorChannelCount());
        GL11.glReadPixels(offset.x, offset.y, size.x, size.y, tif.convert().getCode(), type.getCode(), data);
        return data;
    }

    private void checkReadPixelsExceptions(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull FboAttachmentSlot slot, @NotNull Texture.TextureDataType type, @NotNull Class<? extends Buffer> returnType){
        ExceptionHelper.exceptionIfNotUsable(this);
        ExceptionHelper.exceptionIfNull(offset, size, slot, type);
        ExceptionHelper.exceptionIfAnyLowerThan(offset, 0);
        ExceptionHelper.exceptionIfAnyLowerThan(size, 0);
        if(!getAttachmentContainer(slot, readBuffer).isThereAttachment() || !type.getJavaType().equals(returnType)){
            throw new IllegalStateException();
        }
    }

    //drawBufferIndex isn't a color attachment index but a draw buffer index
    public void clearColor(int drawBufferIndex, @NotNull Vector4f clearColor){
        ExceptionHelper.exceptionIfNotUsable(this);
        ExceptionHelper.exceptionIfNull(clearColor);
        ExceptionHelper.exceptionIfNotInsideClosedInterval(0, getMaxDrawBuffers() - 1, drawBufferIndex);
        GL45.glClearNamedFramebufferfv(getId(), COLOR.getAttachmentSlotCode(), drawBufferIndex, Utility.convert(clearColor));
    }

    public void clearDepth(float depthClear){
        ExceptionHelper.exceptionIfNotUsable(this);
        GL45.glClearNamedFramebufferfv(getId(), DEPTH.getAttachmentSlotCode(), 0, Utility.wrapValueByArray(depthClear));
    }

    public void clearStencil(int stencilClear){
        ExceptionHelper.exceptionIfNotUsable(this);
        GL45.glClearNamedFramebufferiv(getId(), STENCIL.getAttachmentSlotCode(), 0, Utility.wrapValueByArray(stencilClear));
    }

    public void clearDepthStencil(float depthClear, int stencilClear){
        ExceptionHelper.exceptionIfNotUsable(this);
        GL45.glClearNamedFramebufferfi(getId(), DEPTH_STENCIL.getAttachmentSlotCode(), 0, depthClear, stencilClear);
    }

    public static int getMaxPoolSize(){
        return FBO_POOL.getMaxPoolSize();
    }

    public static void setMaxPoolSize(int size){
        FBO_POOL.setMaxPoolSize(size);
    }

    @Override
    public int getCacheDataSize(){
        return 0;
    }

    @Override
    public void update(){

    }

    @Override
    public void release(){
        unbindIfBound();
        detachAllAttachments();
        GL30.glDeleteFramebuffers(getId());
        setIdToInvalid();
    }

    private void unbindIfBound(){
        if(getDrawBound() == this){
            drawBound = null;
        }
        if(getReadBound() == this){
            readBound = null;
        }
    }

    private void detachAllAttachments(){
        for(int i = 0; i < color.length; i++){
            color[i].detach();
        }
        depth.detach();
        stencil.detach();
        depthStencil.detach();
    }

    @Override
    public boolean isUsable(){
        return isAvailable();
    }

    @Override
    public String toString(){
        return super.toString() + "\n" +
                Fbo.class.getSimpleName() + "(" +
                "color: " + Utility.toString(color) + ", " +
                "depth: " + depth + ", " +
                "stencil: " + stencil + ", " +
                "depthStencil: " + depthStencil + ", " +
                "readBuffer: " + readBuffer + ")";
    }

    private static class FboPool extends ResourcePool{
        @Override
        protected void createResources(int[] resources){
            GL45.glCreateFramebuffers(resources);
        }
    }

}
