package wobani.resource.opengl.texture.texture2d;

import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.texture.*;
import wobani.toolbox.annotation.*;

public abstract class Texture2D extends TextureBase{

    private static final Texture2DPool TEXTURE_2D_POOL = new Texture2DPool();
    private static final Texture2DMultisampledPool TEXTURE_2D_MULTISAMPLED_POOL = new Texture2DMultisampledPool();

    public Texture2D(ResourceId resourceId){
        super(resourceId);
    }

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

    @NotNull
    protected static Texture2DPool getTexture2DPool(){
        return TEXTURE_2D_POOL;
    }

    @NotNull
    protected static Texture2DMultisampledPool getTexture2DMultisampledPool(){
        return TEXTURE_2D_MULTISAMPLED_POOL;
    }

}
