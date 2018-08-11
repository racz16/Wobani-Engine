package wobani.resource.opengl.texture.cubemaptexture;

import org.lwjgl.opengl.*;
import wobani.resource.opengl.texture.*;

/**
 Interface for the cubemap textures.
 */
public interface CubeMapTexture extends Texture{

    public enum CubeMapSide{
        RIGHT(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X), LEFT(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X), UP(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y), DOWN(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y), FRONT(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z), BACK(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);

        private final int code;

        CubeMapSide(int code){
            this.code = code;
        }

        public int getCode(){
            return code;
        }
    }
}
