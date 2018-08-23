package wobani.resource.opengl.texture.cubemaptexture;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.texture.*;
import wobani.toolbox.annotation.*;

import java.nio.*;

/**
 Abstract class for the cube map textures.
 */
public abstract class CubeMapTexture extends TextureBase{

    /**
     One side of a cube map texture.
     */
    public enum CubeMapSide{
        /**
         Right side.
         */
        RIGHT(0),
        /**
         Left side.
         */
        LEFT(1),
        /**
         Up side.
         */
        UP(2),
        /**
         Down side.
         */
        DOWN(3),
        /**
         Front side.
         */
        FRONT(4),
        /**
         Back side.
         */
        BACK(5);

        /**
         The side's index.
         */
        private final int index;

        /**
         Initializes a new CubeMapSide to the given value.

         @param index side's index
         */
        CubeMapSide(int index){
            this.index = index;
        }

        /**
         Returns the side's index.

         @return the side's index
         */
        public int getIndex(){
            return index;
        }
    }

    /**
     For creating CubeMap Textures efficiently.
     */
    private static final CubeMapTexturePool CUBE_MAP_TEXTURE_POOL = new CubeMapTexturePool();

    /**
     Initializes a new CubeMapTexture to the given value.

     @param resourceId   texture's unique id
     @param multisampled true if this texture should be multisampled, false otherwise
     */
    public CubeMapTexture(ResourceId resourceId, boolean multisampled){
        super(resourceId, multisampled);
    }

    @Override
    protected void initializeAfterAllocation(){
        setWrap(TextureWrapDirection.WRAP_U, TextureWrap.CLAMP_TO_EDGE);
        setWrap(TextureWrapDirection.WRAP_V, TextureWrap.CLAMP_TO_EDGE);
        setFilter(getDefaultFilter());
    }

    /**
     Stores the given data in the specified side of the texture and generates the mipmaps.

     @param offset data's offset
     @param side   cube map texture's side
     @param size   data's width and height
     @param format the given data's format
     @param data   data to store
     */
    protected void storeCubeMapSide(@NotNull Vector2i offset, @NotNull CubeMapTexture.CubeMapSide side, @NotNull Vector2i size, @NotNull TextureFormat format, @NotNull ByteBuffer data){
        checkCreation();
        checkAllocation();
        checkSubImage(new Vector2i(offset.x, offset.y), size);
        setFormat(format);
        storeCubeMapSideUnsafe(offset, side, data);
    }

    /**
     Stores the given data in the specified side of the texture and generates the mipmaps.

     @param offset data's offset
     @param side   cube map texture's side
     @param data   data to store
     */
    private void storeCubeMapSideUnsafe(@NotNull Vector2i offset, @NotNull CubeMapTexture.CubeMapSide side, @NotNull ByteBuffer data){
        GL45.glTextureSubImage3D(getId(), 0, offset.x, offset.y, side.getIndex(), getSize().x, getSize().y, 1, getFormat().getCode(), TextureDataType.UNSIGNED_BYTE.getCode(), data);
        GL45.glGenerateTextureMipmap(getId());
    }

    /**
     Returns the CubeMap Texture Pool's maximum size. When you create a new CubeMap Texture, the system first tries to
     get one from the CubeMap Texture Pool. If it's empty it fills the pool with max pool size number of CubeMap
     Textures.

     @return the CubeMap Texture Pool's maximum size
     */
    public static int getMaxPoolSize(){
        return CUBE_MAP_TEXTURE_POOL.getMaxPoolSize();
    }

    /**
     Sets the CubeMap Texture Pool's maximum size to the given value. When you create a new CubeMap Texture, the system
     first tries to get one from the CubeMap Texture Pool. If it's empty it fills the pool with max pool size number of
     CubeMap Textures.

     @param size CubeMap Texture Pool's maximum size
     */
    public static void setMaxPoolSize(int size){
        CUBE_MAP_TEXTURE_POOL.setMaxPoolSize(size);
    }

    @Override
    protected void createTextureId(){
        if(!isIdValid()){
            setId(CUBE_MAP_TEXTURE_POOL.getResource());
        }
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
}
