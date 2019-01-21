package wobani.resource.opengl.texture.cubemaptexture;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.texture.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.annotation.*;

import java.nio.*;

import static wobani.resource.ExceptionHelper.*;

/**
 Abstract class for the cube map textures.
 */
public abstract class CubeMapTexture extends TextureBase{

    /**
     Number of sides in a cube map texture.
     */
    public static int SIDE_COUNT = 6;

    /**
     One side of a cube map texture.
     */
    public enum CubeMapSide{
        /**
         Right side.
         */
        RIGHT(0, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X),
        /**
         Left side.
         */
        LEFT(1, GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X),
        /**
         Up side.
         */
        UP(2, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y),
        /**
         Down side.
         */
        DOWN(3, GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y),
        /**
         Front side.
         */
        FRONT(4, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z),
        /**
         Back side.
         */
        BACK(5, GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);

        /**
         The side's index.
         */
        private final int index;
        private final int code;

        /**
         Initializes a new CubeMapSide to the given value.

         @param index side's index
         */
        CubeMapSide(int index, int code){
            this.index = index;
            this.code = code;
        }

        /**
         Returns the side's index.

         @return the side's index
         */
        public int getIndex(){
            return index;
        }

        public int getCode(){
            return code;
        }
    }

    /**
     For creating CubeMap Textures efficiently.
     */
    private static final CubeMapTexturePool CUBE_MAP_TEXTURE_POOL = new CubeMapTexturePool();

    /**
     Initializes a new CubeMapTexture to the given values.

     @param resourceId   texture's unique id
     @param multisampled true if this texture should be multisampled, false otherwise
     */
    public CubeMapTexture(ResourceId resourceId, boolean multisampled){
        super(resourceId, multisampled);
    }

    @Override
    protected void initializeAfterAllocation(){
        setWrapU(TextureWrap.MIRROR_CLAMP_TO_EDGE);
        setWrapV(TextureWrap.MIRROR_CLAMP_TO_EDGE);
        setWrapW(TextureWrap.MIRROR_CLAMP_TO_EDGE);
        setFilter(getDefaultFilter());
    }

    //
    //copy--------------------------------------------------------------------------------------------------------------
    //

    /**
     Copies data from this texture's specified side to the given texture.

     @param destination copy data to this texture
     @param side        copy data from this side of the texture
     @param size        copied data's width and height
     */
    public void copyTo(@NotNull DynamicTexture2D destination, @NotNull CubeMapSide side, @NotNull Vector2i size){
        copyTo(destination, new Vector2i(0), side, new Vector2i(0), size);
    }

    /**
     Copies data from this texture's specified side to the given texture.

     @param destination       copy data to this texture
     @param destinationOffset offset in the destination texture
     @param side              copy data from this side of the texture
     @param sourceOffset      offset in the source texture's side
     @param size              copied data's width and height
     */
    public void copyTo(@NotNull DynamicTexture2D destination, @NotNull Vector2i destinationOffset, @NotNull CubeMapSide side, @NotNull Vector2i sourceOffset, @NotNull Vector2i size){
        exceptionIfNull(destination, sourceOffset, side, destinationOffset, size);
        exceptionIfNotAvailable(this);
        exceptionIfNotAvailable(destination);
        exceptionIfNotAllocated(this);
        exceptionIfNotAllocated(destination);
        exceptionIfAreaExceedsFromSize(size, destinationOffset, getSize());
        exceptionIfAreaExceedsFromSize(size, sourceOffset, destination.getSize());
        copyToUnsafe(destination, destinationOffset, side, sourceOffset, size);
    }

    /**
     Copies data from this texture's specified side to the given texture.

     @param destination       copy data to this texture
     @param destinationOffset offset in the destination texture
     @param side              copy data from this side of the texture
     @param sourceOffset      offset in the source texture's side
     @param size              copied data's width and height
     */
    private void copyToUnsafe(@NotNull DynamicTexture2D destination, @NotNull Vector2i destinationOffset, @NotNull CubeMapSide side, @NotNull Vector2i sourceOffset, @NotNull Vector2i size){
        int destinationTarget = destination.isMultisampled() ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D;
        GL43.glCopyImageSubData(getId(), GL13.GL_TEXTURE_CUBE_MAP, 0, sourceOffset.x, sourceOffset.y, side.getIndex(),
                destination.getId(), destinationTarget, 0, destinationOffset.x, destinationOffset.y, 0,
                size.x, size.y, 1);
    }

    /**
     Copies data from this texture's side to the given texture's side.

     @param destination     copy data to this texture'side
     @param destinationSide destination texture's side
     @param sourceSide      source texture's side
     @param size            copied data's width and height
     */
    public void copyTo(@NotNull CubeMapTexture destination, @NotNull CubeMapSide destinationSide, @NotNull CubeMapSide sourceSide, @NotNull Vector2i size){
        copyTo(destination, new Vector2i(0), sourceSide, new Vector2i(0), destinationSide, size);
    }

    /**
     Copies data from this texture's side to the given texture's side.

     @param destination       copy data to this texture'side
     @param destinationOffset offset in the destination texture's side
     @param destinationSide   destination texture's side
     @param sourceOffset      offset in the source texture's side
     @param sourceSide        source texture's side
     @param size              copied data's width and height
     */
    public void copyTo(@NotNull CubeMapTexture destination, @NotNull Vector2i destinationOffset, @NotNull CubeMapSide destinationSide, @NotNull Vector2i sourceOffset, @NotNull CubeMapSide sourceSide, @NotNull Vector2i size){
        exceptionIfNull(destination, sourceOffset, sourceSide, destinationOffset, destinationSide, size);
        exceptionIfNotAvailable(this);
        exceptionIfNotAvailable(destination);
        exceptionIfNotAllocated(this);
        exceptionIfNotAllocated(destination);
        exceptionIfAreaExceedsFromSize(size, destinationOffset, getSize());
        exceptionIfAreaExceedsFromSize(size, sourceOffset, destination.getSize());
        GL43.glCopyImageSubData(getId(), GL13.GL_TEXTURE_CUBE_MAP, 0, sourceOffset.x, sourceOffset.y, sourceSide.getIndex(),
                destination.getId(), GL13.GL_TEXTURE_CUBE_MAP, 0, destinationOffset.x, destinationOffset.y, destinationSide.getIndex(),
                size.x, size.y, 1);
    }

    //
    //wrap--------------------------------------------------------------------------------------------------------------
    //

    @NotNull
    @Override
    public TextureWrap getWrapW(){
        return super.getWrapW();
    }

    @Override
    public void setWrapW(@NotNull TextureWrap wrap){
        super.setWrapW(wrap);
    }

    //
    //store-------------------------------------------------------------------------------------------------------------
    //

    /**
     Stores the given data in the specified side of the texture.

     @param offset data's offset
     @param side   cube map texture's side
     @param size   data's width and height
     @param format the given data's format
     @param data   data to store
     */
    protected void storeCubeMapSide(@NotNull Vector2i offset, @NotNull CubeMapTexture.CubeMapSide side, @NotNull Vector2i size, @NotNull TextureFormat format, @NotNull ByteBuffer data){
        exceptionIfNotAvailable(this);
        exceptionIfNotAllocated(this);
        exceptionIfNull(offset, side, size, format, data);
        exceptionIfAreaExceedsFromSize(size, offset, getSize());
        exceptionIfFormatAndInternalFormatNotCompatible(getInternalFormat(), format);
        GL45.glTextureSubImage3D(getId(), 0, offset.x, offset.y, side.getIndex(), getSize().x, getSize().y, 1, format.getCode(), TextureDataType.UNSIGNED_BYTE.getCode(), data);
    }

    //
    //resource pool-----------------------------------------------------------------------------------------------------
    //

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
        if(!isAvailable()){
            setId(CUBE_MAP_TEXTURE_POOL.getResource());
        }
    }

    @Override
    public int getActiveDataSize(){
        return super.getActiveDataSize() * SIDE_COUNT;
    }

    @Override
    public String toString(){
        return super.toString() + "\n" +
                CubeMapTexture.class.getSimpleName() + "(" +
                ")";
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
