package wobani.resource.opengl.texture.texture2d;

import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.texture.*;
import wobani.toolbox.annotation.*;

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
     Initializes a new Texture2D to the given value.

     @param resourceId   texture's unique id
     @param multisampled true if this texture should be multisampled, false otherwise
     */
    public Texture2D(ResourceId resourceId, boolean multisampled){
        super(resourceId, multisampled);
    }

    @Override
    protected void initializeAfterAllocation(){
        setFilter(getDefaultFilter());
    }

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
