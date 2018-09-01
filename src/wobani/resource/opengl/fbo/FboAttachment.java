package wobani.resource.opengl.fbo;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resource.opengl.texture.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

public class FboAttachment{

    /**
     Attachment slot's texture attachment.
     */
    private DynamicTexture2D texture;
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
    private Fbo.FboAttachmentSlot slot;
    /**
     Determines whether this attachment is active to draw.
     */
    private boolean draw;

    private Fbo fbo;

    /**
     Initializes a new AttachmentSlot to the given values.

     @param slot  attachment's slot
     @param index color attachment's index, if it isn't a color attachment, it can be anything

     @throws NullPointerException     slot can't be null
     @throws IllegalArgumentException if the slot is color attachment, index have to be in the (0;7) interval
     */
    public FboAttachment(@NotNull Fbo fbo, @NotNull Fbo.FboAttachmentSlot slot, int index){
        if(slot == null){
            throw new NullPointerException();
        }
        this.fbo = fbo;
        if(slot == Fbo.FboAttachmentSlot.COLOR){
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
    public Fbo.FboAttachmentSlot getSlot(){
        return slot;
    }

    /**
     Adds the given type of attachment to the slot if there is no attachment currently in the slot.

     @param type attachment's type

     @return true if the attachment added successfully, false otherwise
     */
    @Bind
    public boolean addAttachment(@NotNull Fbo.FboAttachmentType type, Vector2i size, Texture.TextureInternalFormat internalFormat){
        if(isThereAttachment()){
            return false;
        }
        //int msaa = RenderingPipeline.getParameters().getValueOrDefault(RenderingPipeline.MSAA_LEVEL, 2);
        if(type == Fbo.FboAttachmentType.TEXTURE){
            //TODO: mipmaps always false?
            texture = new DynamicTexture2D(size, internalFormat, fbo.getNumberOfSamples());
            GL45.glNamedFramebufferTexture(fbo.getId(), slot.getCode(index), texture.getId(), 0);
        }else{
            rbo = new Rbo(size, internalFormat, fbo.getNumberOfSamples());
            GL45.glNamedFramebufferRenderbuffer(fbo.getId(), slot.getCode(index), GL30.GL_RENDERBUFFER, rbo.getId());
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
        return isThereAttachment(Fbo.FboAttachmentType.TEXTURE) || isThereAttachment(Fbo.FboAttachmentType.RBO);
    }

    /**
     Returns true if there is attachment in this slot and it's type is the same as the given parameter.

     @param type attachment's type

     @return true if there is attachment in this slot and it's type is the same as the given parameter, false otherwise
     */
    public boolean isThereAttachment(@NotNull Fbo.FboAttachmentType type){
        if(type == Fbo.FboAttachmentType.TEXTURE){
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
        if(isThereAttachment(Fbo.FboAttachmentType.TEXTURE)){
            texture.release();
            texture = null;
        }
    }

    /**
     If this attachment is a RBO, this method releases it.
     */
    public void removeRbo(){
        if(isThereAttachment(Fbo.FboAttachmentType.RBO)){
            rbo.release();
            rbo = null;
        }
    }

    /**
     If this attachment is a texture, this method detaches it but doesn't release.
     */
    public void detachTexture(){
        if(isThereAttachment(Fbo.FboAttachmentType.TEXTURE)){
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
