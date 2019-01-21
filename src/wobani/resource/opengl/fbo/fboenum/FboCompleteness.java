package wobani.resource.opengl.fbo.fboenum;

import org.lwjgl.opengl.*;
import wobani.toolbox.annotation.*;

public enum FboCompleteness{

    COMPLETE(GL30.GL_FRAMEBUFFER_COMPLETE),
    INCOMPLETE_ATTACHMENT(GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT),
    INCOMPLETE_MISSING_ATTACHMENT(GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT),
    INCOMPLETE_DRAW_BUFFER(GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER),
    INCOMPLETE_READ_BUFFER(GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER),
    UNSUPPORTED(GL30.GL_FRAMEBUFFER_UNSUPPORTED),
    INCOMPLETE_MULTISAMPLE(GL30.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE),
    UNDEFINED(GL30.GL_FRAMEBUFFER_UNDEFINED);

    private final int code;

    FboCompleteness(int code){
        this.code = code;
    }

    public int getCode(){
        return code;
    }

    @NotNull
    public static FboCompleteness valueOf(int code){
        for(FboCompleteness completeness : FboCompleteness.values()){
            if(completeness.getCode() == code){
                return completeness;
            }
        }
        throw new IllegalArgumentException("The given parameter is not an FBO completeness code");
    }
}
