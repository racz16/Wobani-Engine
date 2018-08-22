package wobani.resource.opengl.texture.cubemaptexture;

import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.texture.*;

/**
 Interface for the cube map textures.
 */
public abstract class CubeMapTexture extends TextureBase{

    public CubeMapTexture(ResourceId resourceId){
        super(resourceId);
    }

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
    //
    //
    //

    /**
     For creating CubeMap Textures efficiently.
     */
    private static final CubeMapTexturePool CUBE_MAP_TEXTURE_POOL = new CubeMapTexturePool();

    /**
     Returns the CubeMap Texture Pool's maximum size. When you create a new CubeMap Texture, the system first tries to
     get one from the CubeMap Texture Pool. If it's empty it fills the pool with max pool size number of CubeMap
     Textures.
     */
    public static int getMaxPoolSize(){
        return CUBE_MAP_TEXTURE_POOL.getMaxPoolSize();
    }

    /**
     Sets the CubeMap Texture Pool's maximum size. When you create a new Buffer Object, the system first tries to get one
     from the CubeMap Texture Pool. If it's empty it fills the pool with max pool size number of CubeMap Textures.

     @param size CubeMap Texture Pool's maximum size
     */
    public static void setMaxPoolSize(int size){
        CUBE_MAP_TEXTURE_POOL.setMaxPoolSize(size);
    }

    /**
     For creating CubeMap Textures efficiently.
     */
    private static class CubeMapTexturePool extends ResourcePool{

        @Override
        protected void createResources(int[] resources){
            GL45.glCreateTextures(GL13.GL_TEXTURE_CUBE_MAP, resources);
        }

        @Override
        public String toString(){
            return super.toString() + "\n" +
                    CubeMapTexturePool.class.getSimpleName() + "(" + ")";
        }
    }

    @Override
    protected int createTextureId(){
        return CUBE_MAP_TEXTURE_POOL.getResource();
    }
}
