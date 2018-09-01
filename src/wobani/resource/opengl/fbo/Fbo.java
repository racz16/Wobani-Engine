package wobani.resource.opengl.fbo;

import org.joml.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import wobani.resource.*;
import wobani.resource.opengl.*;
import wobani.resource.opengl.texture.Texture.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.nio.*;
import java.util.*;

import static org.lwjgl.system.MemoryStack.*;

/**
 Object oriented wrapper class above the native FBO. Supports 8 color attachments, the depth, the stencil and the
 depth24-stencil8 attachment. You can add textures or Render Buffer Objects as attachments.
 */
public class Fbo extends OpenGlObject{

    /**
     FBO's color attachments.
     */
    private final List<AttachmentSlot> color = new ArrayList<>();
    /**
     FBO's depth attachment.
     */
    private final AttachmentSlot depth = new AttachmentSlot(FboAttachmentSlotWrong.DEPTH, 0);
    /**
     FBO's stencil attachment.
     */
    private final AttachmentSlot stencil = new AttachmentSlot(FboAttachmentSlotWrong.STENCIL, 0);
    /**
     FBO's depth-stencil attachment.
     */
    private final AttachmentSlot depthStencil = new AttachmentSlot(FboAttachmentSlotWrong.DEPTH_STENCIL, 0);
    /**
     The FBO's width and height.
     */
    private final Vector2i size = new Vector2i();
    /**
     The index of the attachment, which is active to read.
     */
    private int activeRead;
    /**
     Determines whether the FBO is multisampled.
     */
    private boolean multisampled;
    /**
     Number of the FBO's samples.
     */
    private int samples;
    /**
     Determines whether the color attachments stored as floating point values.
     */
    private boolean floatingPoint;

    private static final FboPool FBO_POOL = new FboPool();

    //TODO: glClear

    /**
     Initializes a new FBO to the given value.

     @param size          FBO's width and height
     @param multisampled  multisampled
     @param samples       number of samples, if the FBO isn't multisampled, it can be anything
     @param floatingPoint FBO store color attachments as floating point values or not

     @throws IllegalArgumentException width and height must be positive
     @throws IllegalArgumentException samples can't be lower than 1
     */
    public Fbo(@NotNull Vector2i size, boolean multisampled, int samples, boolean floatingPoint){
        super(new ResourceId());
        if(size.x <= 0 || size.y <= 0){
            throw new IllegalArgumentException("Width and height must be positive");
        }
        if(multisampled && samples < 1){
            throw new IllegalArgumentException("Samples can't be lower than 1");
        }
        for(int i = 0; i < 8; i++){
            color.add(new AttachmentSlot(FboAttachmentSlotWrong.COLOR, i));
        }
        this.size.set(size);
        this.multisampled = multisampled;
        this.floatingPoint = floatingPoint;
        if(multisampled){
            this.samples = samples;
        }else{
            this.samples = 1;
        }
        activeRead = 0;
        setId(FBO_POOL.getResource());
    }

    @Override
    protected int getType(){
        return GL30.GL_FRAMEBUFFER;
    }

    @Override
    protected String getTypeName(){
        return "FBO";
    }

    /**
     Adds the specified attachment to the FBO. If there is already an attachment in the given slot, this method don't do
     anything. If you want to add both depth and stencil attachments, you should add a depth-stencil attachment.

     @param slot  attachment's slot
     @param type  attachment's type
     @param index attachment's index (0;7), if slot isn't color attachment, it can be anything

     @return true if the attachment added successfully, false otherwise

     @throws NullPointerException     slot and type can't be null
     @throws IllegalArgumentException if the slot is color attachment, the index must be in the (0;7) interval
     */
    @Bind
    public boolean addAttachment(@NotNull FboAttachmentSlotWrong slot, @NotNull FboAttachmentType type, int index){
        if(slot == null || type == null){
            throw new NullPointerException();
        }
        switch(slot){
            case COLOR:
                if(index < 0 || index > 7){
                    throw new IllegalArgumentException("If the slot is color, the index must be in the (0;7) interval");
                }
                return color.get(index).addAttachment(type);
            case DEPTH:
                if(isThereAttachment(FboAttachmentSlotWrong.STENCIL, 0) || isThereAttachment(FboAttachmentSlotWrong.DEPTH_STENCIL, 0)){
                    return false;
                }else{
                    return depth.addAttachment(type);
                }
            case STENCIL:
                if(isThereAttachment(FboAttachmentSlotWrong.DEPTH, 0) || isThereAttachment(FboAttachmentSlotWrong.DEPTH_STENCIL, 0)){
                    return false;
                }else{
                    return stencil.addAttachment(type);
                }
            case DEPTH_STENCIL:
                if(isThereAttachment(FboAttachmentSlotWrong.DEPTH, 0) || isThereAttachment(FboAttachmentSlotWrong.STENCIL, 0)){
                    return false;
                }else{
                    return depthStencil.addAttachment(type);
                }
        }
        return false;
    }

    /**
     Return true if there is a texture or a RBO attachment in the given slot, false otherwise.

     @param slot  attachment's slot
     @param index attachment's index (0;7), if slot isn't color attachment, it can be anything

     @return true if there is a texture or a RBO attachment in the given slot, false otherwise
     */
    public boolean isThereAttachment(@NotNull FboAttachmentSlotWrong slot, int index){
        return isThereAttachment(slot, FboAttachmentType.RBO, index) || isThereAttachment(slot, FboAttachmentType.TEXTURE, index);
    }

    /**
     Returns true if there is attachment in the given slot and it's type is the same as the given parameter.

     @param slot  attachment's slot
     @param type  attachment's type
     @param index attachment's index (0;7), if slot isn't color attachment, it can be anything

     @return true if there is attachment in the given slot and it's type is the same as the given parameter, false
     otherwise

     @throws NullPointerException     slot and type can't be null
     @throws IllegalArgumentException if the slot is color attachment, the index must be in the (0;7) interval
     */
    public boolean isThereAttachment(@NotNull FboAttachmentSlotWrong slot, @NotNull FboAttachmentType type, int index){
        if(slot == null || type == null){
            throw new NullPointerException();
        }
        switch(slot){
            case COLOR:
                if(index < 0 || index > 7){
                    throw new IllegalArgumentException("If the slot is color, the index must be in the (0;7) interval");
                }
                return color.get(index).isThereAttachment(type);
            case DEPTH:
                return depth.isThereAttachment(type);
            case STENCIL:
                return stencil.isThereAttachment(type);
            case DEPTH_STENCIL:
                return depthStencil.isThereAttachment(type);
        }
        return false;
    }

    /**
     Returns the specified slot's texture attachment. Returns null if there is no texture attachment.

     @param slot  attachment's slot
     @param index attachment's index (0;7), if slot isn't color attachment, it can be anything

     @return the slot's texture attachment

     @throws NullPointerException     slot can't be null
     @throws IllegalArgumentException if the slot is color attachment, the index must be in the (0;7) interval
     */
    @Nullable
    public Texture2D getTextureAttachment(@NotNull FboAttachmentSlotWrong slot, int index){
        if(slot == null){
            throw new NullPointerException();
        }
        switch(slot){
            case COLOR:
                if(index < 0 || index > 7){
                    throw new IllegalArgumentException("If the slot is color, the index must be in the (0;7) interval");
                }
                return color.get(index).getTextureAttachment();
            case DEPTH:
                return depth.getTextureAttachment();
            case STENCIL:
                return stencil.getTextureAttachment();
            case DEPTH_STENCIL:
                return depthStencil.getTextureAttachment();
        }
        return null;
    }

    /**
     Detaches the specified slot's attachment and releases the corresponding RBO or texture.

     @param slot  attachment's slot
     @param index attachment's index (0;7), if slot isn't color attachment, it can be anything

     @throws NullPointerException     slot can't be null
     @throws IllegalArgumentException if the slot is attachment, the index must be in the (0;7) interval
     */
    public void removeAttachment(@NotNull FboAttachmentSlotWrong slot, int index){
        if(slot == null){
            throw new NullPointerException();
        }
        switch(slot){
            case COLOR:
                if(index < 0 || index > 7){
                    throw new IllegalArgumentException("If the slot is color, the index must be in the (0;7) interval");
                }
                color.get(index).removeAttachment();
                break;
            case DEPTH:
                depth.removeAttachment();
                break;
            case STENCIL:
                stencil.removeAttachment();
                break;
            case DEPTH_STENCIL:
                depthStencil.removeAttachment();
                break;
        }
    }

    /**
     Detaches the specified slot's texture attachment but it doesn't release the texture.

     @param slot  attachment's slot
     @param index attachment's index (0;7), if slot isn't color attachment, it can be anything

     @throws NullPointerException     slot can't be null
     @throws IllegalArgumentException if the slot is color attachment, the index must be in the (0;7) interval
     */
    public void detachTexture(@NotNull FboAttachmentSlotWrong slot, int index){
        if(slot == null){
            throw new NullPointerException();
        }
        switch(slot){
            case COLOR:
                if(index < 0 || index > 7){
                    throw new IllegalArgumentException("If the slot is color, the index must be in the (0;7) interval");
                }
                color.get(index).detachTexture();
                break;
            case DEPTH:
                depth.detachTexture();
                break;
            case STENCIL:
                stencil.detachTexture();
                break;
            case DEPTH_STENCIL:
                depthStencil.detachTexture();
                break;
        }
    }

    /**
     Determines whether the specified attachment is active to draw.

     @param index attachment's index (0;7)

     @return true if the specified attachment is active to draw, false otherwise

     @throws IllegalArgumentException the index must be in the (0;7) interval
     */
    public boolean isActiveDraw(int index){
        if(index < 0 || index > 7){
            throw new IllegalArgumentException("The index must be in the (0;7) interval");
        }
        if(color.get(index).isThereAttachment()){
            return color.get(index).isActiveDraw();
        }else{
            return false;
        }
    }

    /**
     Sets whether or not the specified attachment is active to draw.

     @param draw  true if the specified attachment should be active to draw, false otherwise
     @param index attachment's index (0;7)

     @throws IllegalArgumentException the index must be in the (0;7) interval
     */
    @Bind
    public void setActiveDraw(boolean draw, int index){
        if(index < 0 || index > 7){
            throw new IllegalArgumentException("The index must be in the (0;7) interval");
        }

        if(color.get(index).isThereAttachment()){
            color.get(index).setActiveDraw(draw);

            try(MemoryStack stack = stackPush()){
                IntBuffer result = stack.mallocInt(8);
                for(AttachmentSlot slot : color){
                    if(slot.isThereAttachment() && slot.isActiveDraw()){
                        result.put(GL30.GL_COLOR_ATTACHMENT0 + slot.getIndex());
                    }
                }
                result.flip();
                GL45.glNamedFramebufferDrawBuffers(getId(), result);
            }
        }
    }

    /**
     Determines whether the specified attachment is active to read. At the time only one attachment can be active to
     read.

     @param index attachment's index (0;7)

     @return true if the specified attachment is active to read, false otherwise

     @throws IllegalArgumentException the index must be in the (0;7) interval
     */
    public boolean isActiveRead(int index){
        if(index < 0 || index > 7){
            throw new IllegalArgumentException("If the slot is color, the index must be in the (0;7) interval");
        }
        return activeRead == index;
    }

    //
    //misc----------------------------------------------------------------------
    //

    /**
     Sets whether or not the specified attachment is active to read. At the time only one attachment can be active to
     read.

     @param read  true if the specified attachment should be active to read, false otherwise
     @param index attachment's index (0;7)

     @throws IllegalArgumentException the index must be in the (0;7) interval
     */
    @Bind
    public void setActiveRead(boolean read, int index){
        if(index < 0 || index > 7){
            throw new IllegalArgumentException("The index must be in the (0;7) interval");
        }
        if(color.get(index).isThereAttachment()){
            if(read){
                activeRead = index;
                GL45.glNamedFramebufferReadBuffer(getId(), GL30.GL_COLOR_ATTACHMENT0 + index);
            }else{
                if(activeRead == index){
                    GL45.glNamedFramebufferReadBuffer(getId(), GL11.GL_NONE);
                }
            }
        }
    }

    /**
     Determines whether the color attachments stored as floating point values.

     @return true if the color attachments stored as floating point values, false otherwise
     */
    public boolean isFloatingPoint(){
        return floatingPoint;
    }

    /**
     Returns true if the FBO is complete, false otherwise. If it returns false it's not a problem by itself, but you
     can't use the FBO until it returns true. The getStatus method may help you identifying what's wrong with your FBO.

     @return true if the FBO is complete, false otherwise
     */
    @Bind
    public boolean isComplete(){
        return getStatus() == FboCompleteness.COMPLETE;
    }

    /**
     Return the FBO's current status. It may help you to identify what is the problem with your FBO.

     @return the FBO's status
     */
    //@Bind
    @NotNull
    public FboCompleteness getStatus(){
        int code = GL45.glCheckNamedFramebufferStatus(getId(), GL30.GL_FRAMEBUFFER);
        for(FboCompleteness fbc : FboCompleteness.values()){
            if(code == fbc.getCode()){
                return fbc;
            }
        }

        return null;
    }

    /**
     Binds this FBO for both reading and drawing.
     */
    public void bind(){
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, getId());
    }

    /**
     Binds this FBO for reading.
     */
    public void bindRead(){
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, getId());
    }

    /**
     Binds this FBO for drawing.
     */
    public void bindDraw(){
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, getId());
    }

    /**
     Unbinds this FBO (binds the default framebuffer).
     */
    public void unbind(){
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    /**
     Resolves this FBO and all of it's attachments to another (not multisampled) FBO.

     @return resolved FBO
     */
    @NotNull
    public Fbo resolveFbo(){
        Fbo resolved = new Fbo(getSize(), false, 1, floatingPoint);
        for(AttachmentSlot slot : color){
            resolveFbo(resolved, FboAttachmentSlotWrong.COLOR, slot.getIndex(), slot.getIndex());
        }
        resolveFbo(resolved, FboAttachmentSlotWrong.DEPTH, 0, 0);
        resolveFbo(resolved, FboAttachmentSlotWrong.STENCIL, 0, 0);
        resolveFbo(resolved, FboAttachmentSlotWrong.DEPTH_STENCIL, 0, 0);
        return resolved;
    }

    /**
     Resolves this FBO's specified attachments to the given FBO's specified attachment slot. If the slot is color, this
     method resolves this FBO's fromIndexth color attachment to the given FBO's toIndexth attachment slot. If the slot
     isn't color, this method resolves this FBO's attachment to the given FBO's same slot.

     @param toResolve destonation FBO
     @param slot      attachment's slot
     @param fromIndex this FBO's attachment's index (0;7), if slot isn't color attachment, it can be anything
     @param toIndex   given FBO's attachment's index (0;7), if slot isn't color attachment, it can be anything
     */
    public void resolveFbo(@NotNull Fbo toResolve, @NotNull FboAttachmentSlotWrong slot, int fromIndex, int toIndex){
        resolveFbo(toResolve, slot, FboAttachmentType.TEXTURE, fromIndex, toIndex);
        resolveFbo(toResolve, slot, FboAttachmentType.RBO, fromIndex, toIndex);
    }

    /**
     Resolves this FBO's specified attachments to the given FBO's specified attachment slot. If the slot is color, this
     method resolves this FBO's fromIndexth color attachment to the given FBO's toIndexth attachment slot. If the slot
     isn't color, this method resolves this FBO's attachment to the given FBO's same slot.

     @param toResolve destonation FBO
     @param slot      attachment's slot
     @param fromType  specifies the type of this FBO to resolve
     @param fromIndex this FBO's attachment's index (0;7), if slot isn't color attachment, it can be anything
     @param toIndex   given FBO's attachment's index (0;7), if slot isn't color attachment, it can be anything

     @throws NullPointerException     slot can't be null
     @throws IllegalArgumentException both FBOs have to be usable, the same size and the specified attachment have to be
     exists
     */
    public void resolveFbo(@NotNull Fbo toResolve, @NotNull FboAttachmentSlotWrong slot, @NotNull FboAttachmentType fromType, int fromIndex, int toIndex){
        if(slot == null){
            throw new NullPointerException();
        }
        if(this == toResolve || !size
                .equals(toResolve.size) || !isThereAttachment(slot, fromIndex) || !isUsable() || !toResolve.isUsable()){
            throw new IllegalArgumentException("Both FBOs have to be usable, the same size and the specified attachment have to be exists");
        }

        toResolve.bindDraw();
        bindRead();
        if(!toResolve.isThereAttachment(slot, toIndex)){
            toResolve.addAttachment(slot, fromType, toIndex);
        }
        switch(slot){
            case COLOR:
                int read = activeRead;
                setActiveRead(true, fromIndex);
                boolean draw = toResolve.isActiveDraw(toIndex);
                toResolve.setActiveDraw(true, toIndex);
                GL45.glBlitNamedFramebuffer(getId(), toResolve.getId(), 0, 0, size.x, size.y, 0, 0, size.x, size.y, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
                toResolve.setActiveDraw(draw, toIndex);
                setActiveRead(true, read);
                break;
            case DEPTH:
                GL45.glBlitNamedFramebuffer(getId(), toResolve.getId(), 0, 0, size.x, size.y, 0, 0, size.x, size.y, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
                break;
            case STENCIL:
                GL45.glBlitNamedFramebuffer(getId(), toResolve.getId(), 0, 0, size.x, size.y, 0, 0, size.x, size.y, GL11.GL_STENCIL_BUFFER_BIT, GL11.GL_NEAREST);
                break;
            case DEPTH_STENCIL:
                GL45.glBlitNamedFramebuffer(getId(), toResolve.getId(), 0, 0, size.x, size.y, 0, 0, size.x, size.y, GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT, GL11.GL_NEAREST);
                break;
        }
    }

    /**
     Returns the FBO's width and height.

     @return the FBO's width and height
     */
    @ReadOnly
    @NotNull
    public Vector2i getSize(){
        return new Vector2i(size);
    }

    @Override
    public int getCacheDataSize(){
        return 0;
    }

    @Override
    public int getActiveDataSize(){
        int attachmentSize = size.x * size.y * 4 * 4 * samples;
        int size = 0;
        for(AttachmentSlot aColor : color){
            size += aColor.isThereAttachment() ? attachmentSize : 0;
        }
        size += depth.isThereAttachment() ? attachmentSize : 0;
        size += stencil.isThereAttachment() ? attachmentSize : 0;
        size += depthStencil.isThereAttachment() ? attachmentSize : 0;

        return size;
    }

    @Override
    public void update(){

    }

    /**
     Releases the FBO and the FBO's attachments. After calling this method, you can't use the FBO and it's attachments
     for anything.
     */
    @Override
    public void release(){
        for(int i = 0; i < color.size(); i++){
            color.get(i).removeAttachment();
        }
        depth.removeAttachment();
        stencil.removeAttachment();
        depthStencil.removeAttachment();
        GL30.glDeleteFramebuffers(getId());
        setIdToInvalid();
    }

    /**
     Releases the FBO and it's RBOs, detaches the textures, but keeps alive the textures. You must have reference to all
     the FBO's textures, otherwise you cause memory leak. After calling this method, you can't use the FBO for anything.
     */
    public void releaseFboRbo(){
        for(int i = 0; i < color.size(); i++){
            color.get(i).detachTexture();
            color.get(i).removeRbo();
        }
        depth.detachTexture();
        depth.removeRbo();
        stencil.detachTexture();
        stencil.removeRbo();
        depthStencil.detachTexture();
        depthStencil.removeRbo();
        GL30.glDeleteFramebuffers(getId());
        setIdToInvalid();
    }

    @Override
    public boolean isUsable(){
        return isAvailable();
    }

    /**
     Determines whether the FBO is multisampled.

     @return true if the FBO is multisampled, false otherwise
     */
    public boolean isMultisampled(){
        return multisampled;
    }

    /**
     Returns the number of the FBO's samples.

     @return the number of the FBO's samples
     */
    public int getNumberOfSamples(){
        return samples;
    }

    public enum FboAttachmentSlot{
        COLOR(GL30.GL_COLOR_ATTACHMENT0),
        DEPTH(GL30.GL_DEPTH_ATTACHMENT),
        STENCIL(GL30.GL_STENCIL_ATTACHMENT),
        DEPTH_STENCIL(GL30.GL_DEPTH_STENCIL_ATTACHMENT);

        private final int code;

        FboAttachmentSlot(int code){
            this.code = code;
        }

        public int getCode(){
            return code;
        }

        public int getCode(int index){
            return code + index;
        }
    }

    /**
     Attachment slot.
     */
    public enum FboAttachmentSlotWrong{
        /**
         Color attachment.
         */
        COLOR(GL30.GL_COLOR_ATTACHMENT0, TextureInternalFormat.RGBA8, TextureFormat.RGBA, TextureDataType.FLOAT),
        /**
         Depth attachment.
         */
        DEPTH(GL30.GL_DEPTH_ATTACHMENT, TextureInternalFormat.DEPTH32F, TextureFormat.DEPTH, TextureDataType.FLOAT),
        /**
         Stencil attachment.
         */
        STENCIL(GL30.GL_STENCIL_ATTACHMENT, TextureInternalFormat.STENCIL8, TextureFormat.STENCIL, TextureDataType.FLOAT),
        /**
         Mixed depth and stencil attachment. Depth part get 24 bits and stencil part get 8 bits.
         */
        DEPTH_STENCIL(GL30.GL_DEPTH_STENCIL_ATTACHMENT, TextureInternalFormat.DEPTH24_STENCIL8, TextureFormat.DEPTH_STENCIL, TextureDataType.UNSIGNED_INT);//FIXME: GL30.GL_UNSIGNED_INT_24_8

        /**
         Floating point attachments's internal format.
         */
        private static final int floatingPointInternalFormat = GL30.GL_RGBA16F;
        /**
         Attachment's OpenGL code.
         */
        private final int attachment;
        /**
         Texture's OpenGL internal format.
         */
        private final TextureInternalFormat internalFormat;
        /**
         Texture's OpenGL format.
         */
        private final TextureFormat format;
        /**
         Texture's OpenGL type.
         */
        private final TextureDataType type;

        /**
         Initializes a new FboAttachmentSlotWrong to the given values.

         @param attachment     attachment's OpenGL code
         @param internalFormat texture's OpenGL internal format
         @param format         texture's OpenGL format
         @param type           texture's OpenGL type
         */
        FboAttachmentSlotWrong(int attachment, TextureInternalFormat internalFormat, TextureFormat format, TextureDataType type){
            this.attachment = attachment;
            this.internalFormat = internalFormat;
            this.format = format;
            this.type = type;
        }

        /**
         Returns the attachment's OpenGL code.

         @return the attachment's OpenGL code
         */
        public int getAttachmet(){
            return attachment;
        }

        /**
         Returns the texture's OpenGL internal format.

         @param floatingPoint is this attachment stored as floating point values or not

         @return the texture's OpenGL internal format
         */
        public TextureInternalFormat getInternalFormat(boolean floatingPoint){
            return floatingPoint && attachment == GL30.GL_COLOR_ATTACHMENT0 ? TextureInternalFormat.RGBA16F : internalFormat;
        }

        /**
         Returns the texture's OpenGL format.

         @return the texture's OpenGL format
         */
        public TextureFormat getFormat(){
            return format;
        }

        /**
         Returns the texture's OpenGL type.

         @return the texture's OpenGL type
         */
        public TextureDataType getType(){
            return type;
        }
    }

    /**
     Attachment's type.
     */
    public enum FboAttachmentType{
        /**
         Texture.
         */
        TEXTURE,
        /**
         Render Buffer Object.
         */
        RBO
    }

    /**
     FBO's completeness.
     */
    public enum FboCompleteness{
        /**
         Complete.
         */
        COMPLETE(GL30.GL_FRAMEBUFFER_COMPLETE),
        /**
         Incomplete attachment.
         */
        INCOMPLETE_ATTACHMENT(GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT),
        /**
         Incomplete missing attachment.
         */
        INCOMPLETE_MISSING_ATTACHMENT(GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT),
        /**
         Incomplete draw buffer.
         */
        INCOMPLETE_DRAW_BUFFER(GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER),
        /**
         Incomplete read buffer.
         */
        INCOMPLETE_READ_BUFFER(GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER),
        /**
         Unsupported.
         */
        UNSUPPORTED(GL30.GL_FRAMEBUFFER_UNSUPPORTED),
        /**
         Incomplete multisample.
         */
        INCOMPLETE_MULTISAMPLE(GL30.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE),
        /**
         Undefined.
         */
        UNDEFINED(GL30.GL_FRAMEBUFFER_UNDEFINED);

        /**
         Completeness's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new FboCompleteness to the given value.

         @param code completeness's OpenGL code
         */
        FboCompleteness(int code){
            this.code = code;
        }

        /**
         Returns the completeness's OpenGL code.

         @return the completeness's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }

    /**
     Represents an attachment slot of a FBO. It can store a texture or a RBO.
     */
    private class AttachmentSlot{

        /**
         Attachment slot's texture attachment.
         */
        private Texture2D texture;
        /**
         Render Buffer Object's id.
         */
        private Rbo rbo;
        /**
         Color attachment's index, if it isn't a color attachment, it's value is 0.
         */
        private int index;
        /**
         Attachment's slot.
         */
        private FboAttachmentSlotWrong slot;
        /**
         Determines whether this attachment is active to draw.
         */
        private boolean draw;

        /**
         Initializes a new AttachmentSlot to the given values.

         @param slot  attachment's slot
         @param index color attachment's index, if it isn't a color attachment, it can be anything

         @throws NullPointerException     slot can't be null
         @throws IllegalArgumentException if the slot is color attachment, index have to be in the (0;7) interval
         */
        public AttachmentSlot(@NotNull FboAttachmentSlotWrong slot, int index){
            if(slot == null){
                throw new NullPointerException();
            }
            if(slot == FboAttachmentSlotWrong.COLOR){
                if(index < 0 || index > 7){
                    throw new IllegalArgumentException("If the slot is color, the index must be in the (0;7) interval");
                }
                if(index == 0){
                    draw = true;
                }
            }else{
                index = 0;
            }
            this.slot = slot;
            this.index = index;
        }

        /**
         Returns the attachment's index. If the attachment is a attachment attachment it's in the (0;7) interval, 0
         otherwise.

         @return the attachment's index
         */
        public int getIndex(){
            return index;
        }

        /**
         Returns the attachment's slot.

         @return the attachment's slot
         */
        @NotNull
        public FboAttachmentSlotWrong getSlot(){
            return slot;
        }

        /**
         Adds the given type of attachment to the slot if there is no attachment currently in the slot.

         @param type attachment's type

         @return true if the attachment added successfully, false otherwise
         */
        @Bind
        public boolean addAttachment(@NotNull FboAttachmentType type){
            if(isThereAttachment()){
                return false;
            }
            //int msaa = RenderingPipeline.getParameters().getValueOrDefault(RenderingPipeline.MSAA_LEVEL, 2);
            if(type == FboAttachmentType.TEXTURE){
                //TODO: mipmaps always false?
                texture = new DynamicTexture2D(size, slot.getInternalFormat(floatingPoint), samples);
                GL45.glNamedFramebufferTexture(getId(), slot.getAttachmet() + index, texture.getId(), 0);
            }else{
                rbo = new Rbo(size, slot.getInternalFormat(floatingPoint), samples);
                GL45.glNamedFramebufferRenderbuffer(getId(), slot.getAttachmet() + index, GL30.GL_RENDERBUFFER, rbo.getId());
            }
            return true;
        }

        /**
         Returns the slot's texture attachment. Returns null if there is no texture attachment.

         @return the slot's texture attachment
         */
        @Nullable
        public Texture2D getTextureAttachment(){
            return texture;
        }

        /**
         Returns true if there is a texture or a RBO attachment, false otherwise.

         @return true if there is a texture or a RBO attachment, false otherwise
         */
        public boolean isThereAttachment(){
            return isThereAttachment(FboAttachmentType.TEXTURE) || isThereAttachment(FboAttachmentType.RBO);
        }

        /**
         Returns true if there is attachment in this slot and it's type is the same as the given parameter.

         @param type attachment's type

         @return true if there is attachment in this slot and it's type is the same as the given parameter, false
         otherwise
         */
        public boolean isThereAttachment(@NotNull FboAttachmentType type){
            if(type == FboAttachmentType.TEXTURE){
                return texture != null && texture.isUsable();
            }else{
                return Utility.isUsable(rbo);
            }
        }

        /**
         Removes the slot's attachment.
         */
        public void removeAttachment(){
            removeTexture();
            removeRbo();
        }

        /**
         If this attachment is a texture, this method releases it.
         */
        public void removeTexture(){
            if(isThereAttachment(FboAttachmentType.TEXTURE)){
                texture.release();
                texture = null;
            }
        }

        /**
         If this attachment is a RBO, this method releases it.
         */
        public void removeRbo(){
            if(isThereAttachment(FboAttachmentType.RBO)){
                rbo.release();
                rbo = null;
            }
        }

        /**
         If this attachment is a texture, this method detaches it but doesn't release.
         */
        public void detachTexture(){
            if(isThereAttachment(FboAttachmentType.TEXTURE)){
                texture = null;
            }
        }

        /**
         Determines whether this attachment is active to draw.

         @return true if this attachment is active to draw, false otherwise
         */
        public boolean isActiveDraw(){
            return draw;
        }

        /**
         Sets whether or not this attachment is active to draw.

         @param draw true if this attachment should be active to draw, false otherwise
         */
        public void setActiveDraw(boolean draw){
            this.draw = draw;
        }

    }

    private static class FboPool extends ResourcePool{
        @Override
        protected void createResources(int[] resources){
            GL45.glCreateFramebuffers(resources);
        }
    }

}
