package resources.textures.cubeMapTexture;

import org.lwjgl.opengl.*;
import resources.textures.*;

/**
 * Interface for the cubemap textures.
 */
public interface CubeMapTexture extends Texture {

    public enum CubeMapSide {
        RIGHT(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X),
        LEFT(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X),
        UP(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y),
        DOWN(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y),
        FRONT(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z),
        BACK(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);

        private final int code;

        private CubeMapSide(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
