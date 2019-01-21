package wobani.resource.opengl.fbo.fboenum;

import org.lwjgl.opengl.*;

public enum FboAttachmentSlot{
    COLOR(GL30.GL_COLOR_ATTACHMENT0, GL11.GL_COLOR, GL11.GL_COLOR_BUFFER_BIT),
    DEPTH(GL30.GL_DEPTH_ATTACHMENT, GL11.GL_DEPTH, GL11.GL_DEPTH_BUFFER_BIT),
    STENCIL(GL30.GL_STENCIL_ATTACHMENT, GL11.GL_STENCIL, GL11.GL_STENCIL_BUFFER_BIT),
    DEPTH_STENCIL(GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_DEPTH_STENCIL, GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

    private final int attachmentPointCode;
    private final int attachmentSlotCode;
    private final int bitMask;

    FboAttachmentSlot(int attachmentPointCode, int attachmentSlotCode, int bitMask){
        this.attachmentPointCode = attachmentPointCode;
        this.attachmentSlotCode = attachmentSlotCode;
        this.bitMask = bitMask;
    }

    public int getAttachmentPointCode(int index){
        int newIndex = this == COLOR ? index : 0;
        return attachmentPointCode + newIndex;
    }

    public int getAttachmentSlotCode(){
        return attachmentSlotCode;
    }

    public int getBitMask(){
        return bitMask;
    }

}