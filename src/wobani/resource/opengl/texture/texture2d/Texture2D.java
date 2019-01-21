package wobani.resource.opengl.texture.texture2d;

import org.joml.*;
import org.lwjgl.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.texture.*;
import wobani.resource.opengl.texture.cubemaptexture.*;
import wobani.toolbox.annotation.*;

import java.nio.*;

import static wobani.resource.ExceptionHelper.*;

/**
 Abstract class for 2D textures.
 */
public abstract class Texture2D extends TextureBase{
    /**
     Texture 2D pool.
     */
    private static final Texture2DPool TEXTURE_2D_POOL = new Texture2DPool();
    /**
     Multisampled texture 2D pool.
     */
    private static final Texture2DMultisampledPool TEXTURE_2D_MULTISAMPLED_POOL = new Texture2DMultisampledPool();

    /**
     Initializes a new Texture2D to the given values.

     @param resourceId   texture's unique id
     @param multisampled true if this texture should be multisampled, false otherwise
     */
    public Texture2D(ResourceId resourceId, boolean multisampled){
        super(resourceId, multisampled);
    }

    //
    //store-------------------------------------------------------------------------------------------------------------
    //

    /**
     Stores the given data in the texture.

     @param format the given data's format
     @param data   data to store
     */
    protected void store(@NotNull TextureFormat format, @NotNull ByteBuffer data){
        store(new Vector2i(0), getSize(), format, data);
    }

    /**
     Stores the given data in the texture.

     @param offset data's offset
     @param size   data's width and height
     @param format the given data's format
     @param data   data to store
     */
    protected void store(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull TextureFormat format, @NotNull ByteBuffer data){
        exceptionIfNotAvailable(this);
        exceptionIfNotAllocated(this);
        exceptionIfNull(offset, size, format, data);
        exceptionIfAreaExceedsFromSize(size, offset, getSize());
        exceptionIfFormatAndInternalFormatNotCompatible(getInternalFormat(), format);
        storeUnsafe(offset, format, data);
    }

    /**
     Stores the given data in the texture.

     @param offset data's offset
     @param format the given data's format
     @param data   data to store
     */
    private void storeUnsafe(@NotNull Vector2i offset, @NotNull TextureFormat format, @NotNull ByteBuffer data){
        GL45.glTextureSubImage2D(getId(), 0, offset.x, offset.y, getSize().x, getSize().y, format.getCode(), TextureDataType.UNSIGNED_BYTE.getCode(), data);
    }

    //
    //copy--------------------------------------------------------------------------------------------------------------
    //

    /**
     Copies data from this texture to the given texture.

     @param destination copy data to this texture
     @param size        copied data's width and height
     */
    public void copyTo(@NotNull DynamicTexture2D destination, @NotNull Vector2i size){
        copyTo(destination, new Vector2i(0), new Vector2i(0), size);
    }

    /**
     Copies data from this texture to the given texture.

     @param destination       copy data to this texture
     @param destinationOffset offset in the destination texture
     @param sourceOffset      offset in the source (this) texture
     @param size              copied data's width and height
     */
    public void copyTo(@NotNull DynamicTexture2D destination, @NotNull Vector2i destinationOffset, @NotNull Vector2i sourceOffset, @NotNull Vector2i size){
        exceptionIfNull(destination, sourceOffset, destinationOffset, size);
        exceptionIfNotAvailable(this);
        exceptionIfNotAvailable(destination);
        exceptionIfNotAllocated(this);
        exceptionIfNotAllocated(destination);
        exceptionIfAreaExceedsFromSize(size, destinationOffset, getSize());
        exceptionIfAreaExceedsFromSize(size, sourceOffset, destination.getSize());
        copyToUnsafe(destination, destinationOffset, sourceOffset, size);
    }

    /**
     Copies data from this texture to the given texture.

     @param destination       copy data to this texture
     @param destinationOffset offset in the destination texture
     @param sourceOffset      offset in the source (this) texture
     @param size              copied data's width and height
     */
    private void copyToUnsafe(@NotNull DynamicTexture2D destination, @NotNull Vector2i destinationOffset, @NotNull Vector2i sourceOffset, @NotNull Vector2i size){
        int sourceTarget = isMultisampled() ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D;
        int destinationTarget = destination.isMultisampled() ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D;
        GL43.glCopyImageSubData(getId(), sourceTarget, 0, sourceOffset.x, sourceOffset.y, 0,
                destination.getId(), destinationTarget, 0, destinationOffset.x, destinationOffset.y, 0,
                size.x, size.y, 1);
    }

    /**
     Copies data from this texture to the given texture's specified side.

     @param destination copy data to this texture's side
     @param side        destination texture's side
     @param size        copied data's width and height
     */
    public void copyTo(@NotNull DynamicCubeMapTexture destination, @NotNull CubeMapTexture.CubeMapSide side, @NotNull Vector2i size){
        copyTo(destination, new Vector2i(0), side, new Vector2i(0), size);
    }

    /**
     Copies data from this texture to the given texture's specified side.

     @param destination       copy data to this texture's side
     @param destinationOffset offset in the destination texture's side
     @param side              destination texture's side
     @param sourceOffset      offset in the source (this) texture
     @param size              copied data's width and height
     */
    public void copyTo(@NotNull DynamicCubeMapTexture destination, @NotNull Vector2i destinationOffset, @NotNull CubeMapTexture.CubeMapSide side, @NotNull Vector2i sourceOffset, @NotNull Vector2i size){
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
     Copies data from this texture to the given texture's specified side.

     @param destination       copy data to this texture's side
     @param destinationOffset offset in the destination texture's side
     @param side              destination texture's side
     @param sourceOffset      offset in the source (this) texture
     @param size              copied data's width and height
     */
    private void copyToUnsafe(@NotNull DynamicCubeMapTexture destination, @NotNull Vector2i destinationOffset, @NotNull CubeMapTexture.CubeMapSide side, @NotNull Vector2i sourceOffset, @NotNull Vector2i size){
        int sourceTarget = isMultisampled() ? GL32.GL_TEXTURE_2D_MULTISAMPLE : GL11.GL_TEXTURE_2D;
        GL43.glCopyImageSubData(getId(), sourceTarget, 0, sourceOffset.x, sourceOffset.y, 0,
                destination.getId(), GL13.GL_TEXTURE_CUBE_MAP, 0, destinationOffset.x, destinationOffset.y, side.getIndex(),
                size.x, size.y, 1);
    }

    @NotNull
    public ByteBuffer getByteSubImage(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull TextureDataType type){
        checkGetSubImage(offset, size, type, ByteBuffer.class);
        ByteBuffer data = BufferUtils.createByteBuffer(size.x * size.y * getInternalFormat().getColorChannelCount());
        GL45.glGetTextureSubImage(getId(), 0, offset.x, offset.y, 0, size.x, size.y, 1, getInternalFormat().convert().getCode(), TextureDataType.UNSIGNED_BYTE.getCode(), data);
        return data;
    }

    @NotNull
    public ShortBuffer getShortSubImage(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull TextureDataType type){
        checkGetSubImage(offset, size, type, ShortBuffer.class);
        ShortBuffer data = BufferUtils.createShortBuffer(size.x * size.y * getInternalFormat().getColorChannelCount());
        GL45.glGetTextureSubImage(getId(), 0, offset.x, offset.y, 0, size.x, size.y, 1, getInternalFormat().convert().getCode(), TextureDataType.UNSIGNED_BYTE.getCode(), data);
        return data;
    }

    @NotNull
    public IntBuffer getIntSubImage(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull TextureDataType type){
        checkGetSubImage(offset, size, type, IntBuffer.class);
        IntBuffer data = BufferUtils.createIntBuffer(size.x * size.y * getInternalFormat().getColorChannelCount());
        GL45.glGetTextureSubImage(getId(), 0, offset.x, offset.y, 0, size.x, size.y, 1, getInternalFormat().convert().getCode(), TextureDataType.UNSIGNED_BYTE.getCode(), data);
        return data;
    }

    @NotNull
    public FloatBuffer getFloatSubImage(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull TextureDataType type){
        checkGetSubImage(offset, size, type, FloatBuffer.class);
        FloatBuffer data = BufferUtils.createFloatBuffer(size.x * size.y * getInternalFormat().getColorChannelCount());
        GL45.glGetTextureSubImage(getId(), 0, offset.x, offset.y, 0, size.x, size.y, 1, getInternalFormat().convert().getCode(), TextureDataType.UNSIGNED_BYTE.getCode(), data);
        return data;
    }

    private void checkGetSubImage(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull TextureDataType type, @NotNull Class<? extends Buffer> returnType){
        ExceptionHelper.exceptionIfNotUsable(this);
        ExceptionHelper.exceptionIfNull(offset, size);
        ExceptionHelper.exceptionIfAnyLowerThan(offset, 0);
        ExceptionHelper.exceptionIfAnyLowerThan(size, 0);
        exceptionIfAreaExceedsFromSize(size, offset, getSize());
        if(!type.getJavaType().equals(returnType)){
            throw new IllegalArgumentException("TextureDataType and return type don't match");
        }
    }

    //
    //resource pool-----------------------------------------------------------------------------------------------------
    //

    /**
     Returns the texture pool's maximum size. When you create a new texture, the system first tries to get one from the
     texture pool. If it's empty it fills the pool with max pool size number of textures.

     @return the texture pool's maximum size
     */
    public static int getMaxPoolSize(){
        return TEXTURE_2D_POOL.getMaxPoolSize();
    }

    /**
     Sets the texture pool's maximum size to the given value. When you create a new texture, the system first tries to
     get one from the texture pool. If it's empty it fills the pool with max pool size number of textures.

     @param size texture pool's maximum size
     */
    public static void setMaxPoolSize(int size){
        TEXTURE_2D_POOL.setMaxPoolSize(size);
    }

    /**
     Returns the multisampled texture pool's maximum size. When you create a new texture, the system first tries to get
     one from the multisampled texture pool. If it's empty it fills the pool with max pool size number of textures.

     @return the multisampled texture pool's maximum size
     */
    public static int getMaxMultisampledPoolSize(){
        return TEXTURE_2D_MULTISAMPLED_POOL.getMaxPoolSize();
    }

    /**
     Sets the multisampled texture pool's maximum size to the given value. When you create a new multisampled texture,
     the system first tries to get one from the multisampled texture pool. If it's empty it fills the pool with max pool
     size number of multisampled textures.

     @param size multisampled texture pool's maximum size
     */
    public static void setMaxMultisampledPoolSize(int size){
        TEXTURE_2D_MULTISAMPLED_POOL.setMaxPoolSize(size);
    }

    /**
     Returns the texture 2D pool.

     @return the texture 2D pool
     */
    @NotNull
    protected static Texture2DPool getTexture2DPool(){
        return TEXTURE_2D_POOL;
    }

    /**
     Returns the multisampled texture 2D pool.

     @return the multisampled texture 2D pool
     */
    @NotNull
    protected static Texture2DMultisampledPool getTexture2DMultisampledPool(){
        return TEXTURE_2D_MULTISAMPLED_POOL;
    }

    //
    //misc--------------------------------------------------------------------------------------------------------------
    //

    @Override
    protected void initializeAfterAllocation(){
        setFilter(getDefaultFilter());
    }

    @Override
    public String toString(){
        return super.toString() + "\n" +
                Texture2D.class.getSimpleName() + "(" + ")";
    }

    /**
     For creating 2D textures efficiently.
     */
    static class Texture2DPool extends ResourcePool{

        @Override
        protected void createResources(int[] resources){
            GL45.glCreateTextures(GL11.GL_TEXTURE_2D, resources);
        }

        @Override
        public String toString(){
            return super.toString() + "\n" +
                    Texture2DPool.class.getSimpleName() + "(" + ")";
        }
    }

    /**
     For creating 2D multisampled textures efficiently.
     */
    static class Texture2DMultisampledPool extends ResourcePool{

        @Override
        protected void createResources(int[] resources){
            GL45.glCreateTextures(GL32.GL_TEXTURE_2D_MULTISAMPLE, resources);
        }

        @Override
        public String toString(){
            return super.toString() + "\n" +
                    Texture2DMultisampledPool.class.getSimpleName() + "(" + ")";
        }
    }

}
