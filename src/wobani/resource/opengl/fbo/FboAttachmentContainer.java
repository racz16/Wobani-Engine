package wobani.resource.opengl.fbo;

import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.fbo.fboenum.*;
import wobani.resource.opengl.texture.cubemaptexture.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import static wobani.resource.ExceptionHelper.*;

public class FboAttachmentContainer{

    private DynamicTexture2D texture;
    private CubeMapSideTexture cubeMapSideTexture;
    private Rbo rbo;
    private int index;
    private FboAttachmentSlot slot;
    private boolean draw;
    private Fbo fbo;

    FboAttachmentContainer(@NotNull Fbo fbo, @NotNull FboAttachmentSlot slot, int index){
        exceptionIfNull(fbo, slot);
        this.fbo = fbo;
        this.slot = slot;
        this.index = index;
        if(index == 0){
            draw = true;
        }
    }

    public int getIndex(){
        return index;
    }

    @NotNull
    public FboAttachmentSlot getSlot(){
        return slot;
    }

    //getter------------------------------------------------------------------------------------------------------------
    @Nullable
    public FboAttachment getAttachment(){
        return isThereRboAttachment() ? getRboAttachment() : (isThereTextureAttachment() ? getTextureAttachment() : getCubeMapSideTextureAttachment());
    }

    @Nullable
    public DynamicTexture2D getTextureAttachment(){
        return isThereTextureAttachment() ? texture : null;
    }

    @Nullable
    public CubeMapSideTexture getCubeMapSideTextureAttachment(){
        return isThereCubeMapSideTextureAttachment() ? cubeMapSideTexture : null;
    }

    @Nullable
    public Rbo getRboAttachment(){
        return isThereRboAttachment() ? rbo : null;
    }

    //attach------------------------------------------------------------------------------------------------------------
    public void attach(@NotNull DynamicTexture2D texture){
        ExceptionHelper.exceptionIfNotUsable(fbo);
        ExceptionHelper.exceptionIfNotUsable(texture);
        if(isThereAttachment()){
            throw new IllegalStateException();
        }
        attachUnsafe(texture);
    }

    private void attachUnsafe(@NotNull DynamicTexture2D texture){
        rbo = null;
        this.texture = texture;
        cubeMapSideTexture = null;
        GL45.glNamedFramebufferTexture(fbo.getId(), slot.getAttachmentPointCode(index), texture.getId(), 0);
    }

    public void attach(@NotNull CubeMapSideTexture texture){
        ExceptionHelper.exceptionIfNotUsable(fbo);
        if(texture == null || !texture.isUsable()){
            throw new IllegalArgumentException();
        }
        if(isThereAttachment()){
            throw new IllegalStateException();
        }
        attachUnsafe(texture);
    }

    private void attachUnsafe(@NotNull CubeMapSideTexture texture){
        rbo = null;
        this.texture = null;
        this.cubeMapSideTexture = texture;
        EXTDirectStateAccess.glNamedFramebufferTexture2DEXT(fbo.getId(), slot.getAttachmentPointCode(index), texture.getSide().getCode(), texture.getId(), 0);
        //GL45.glNamedFramebufferTextureLayer(fbo.getId(), slot.getAttachmentPointCode(index), texture.getId(), 0, texture.getSide().getIndex());
    }

    public void attach(@NotNull Rbo rbo){
        ExceptionHelper.exceptionIfNotUsable(fbo);
        ExceptionHelper.exceptionIfNotUsable(rbo);
        if(isThereAttachment()){
            throw new IllegalStateException();
        }
        attachUnsafe(rbo);
    }

    private void attachUnsafe(@NotNull Rbo rbo){
        this.rbo = rbo;
        texture = null;
        cubeMapSideTexture = null;
        GL45.glNamedFramebufferRenderbuffer(fbo.getId(), slot.getAttachmentPointCode(index), rbo.getType(), rbo.getId());
    }

    public void detach(){
        if(rbo != null || texture != null || cubeMapSideTexture != null){
            rbo = null;
            texture = null;
            cubeMapSideTexture = null;
            GL45.glNamedFramebufferTexture(fbo.getId(), slot.getAttachmentPointCode(index), 0, 0);
        }
    }

    //contains----------------------------------------------------------------------------------------------------------
    public boolean isThereAttachment(){
        return isThereTextureAttachment() || isThereRboAttachment() || isThereCubeMapSideTextureAttachment();
    }

    public boolean isThereTextureAttachment(){
        return Utility.isUsable(texture);
    }

    public boolean isThereCubeMapSideTextureAttachment(){
        return cubeMapSideTexture != null && cubeMapSideTexture.isUsable();
    }

    public boolean isThereRboAttachment(){
        return Utility.isUsable(rbo);
    }

    //draw--------------------------------------------------------------------------------------------------------------
    public boolean isDrawBuffer(){
        return draw;
    }

    void setDrawBuffer(boolean draw){
        this.draw = draw;
    }


}
