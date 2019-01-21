package wobani.resource.opengl.shader;

import org.lwjgl.opengl.*;

public enum ShaderStage{
    VERTEX_SHADER(GL20.GL_VERTEX_SHADER),
    TESS_CONTROL_SHADER(GL40.GL_TESS_CONTROL_SHADER),
    TESS_EVALUATION_SHADER(GL40.GL_TESS_EVALUATION_SHADER),
    GEOMETRY_SHADER(GL32.GL_GEOMETRY_SHADER),
    FRAGMENT_SHADER(GL20.GL_FRAGMENT_SHADER);

    private final int code;

    ShaderStage(int code){
        this.code = code;
    }

    public int getCode(){
        return code;
    }
}