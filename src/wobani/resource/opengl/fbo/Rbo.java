package wobani.resource.opengl.fbo;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.*;
import wobani.resource.opengl.texture.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import static wobani.resource.ExceptionHelper.*;

public class Rbo extends OpenGlObject implements FboAttachment{

    private final Vector2i size = new Vector2i();
    private Texture.TextureInternalFormat internalFormat;
    private int sampleCount = 1;
    private boolean allocated;

    private static final RboPool RBO_POOL = new RboPool();

    public Rbo(@NotNull Vector2i size, @NotNull Texture.TextureInternalFormat internalFormat, int samples){
        super(new ResourceId());
        setId(RBO_POOL.getResource());
        allocateImmutable(size, internalFormat, samples);
    }

    private void allocateImmutable(@NotNull Vector2i size, @NotNull Texture.TextureInternalFormat internalFormat, int sampleCount){
        exceptionIfNotAvailable(this);
        exceptionIfAllocated(this);
        exceptionIfNull(internalFormat, size);
        setInternalFormat(internalFormat);
        setSize(size);
        setSampleCount(sampleCount);
        setActiveDataSize(computeActiveDataSize());
        allocated = true;
        allocateImmutableUnsafe();
    }

    private void allocateImmutableUnsafe(){
        if(isMultisampled()){
            GL45.glNamedRenderbufferStorageMultisample(getId(), sampleCount, internalFormat.getCode(), size.x, size.y);
        }else{
            GL45.glNamedRenderbufferStorage(getId(), internalFormat.getCode(), size.x, size.y);
        }
    }

    public boolean isAllocated(){
        return allocated;
    }

    private int computeActiveDataSize(){
        int pixelSizeInBits = getInternalFormat().getBitDepth() * sampleCount;
        int numberOfPixels = size.x * size.y;
        double dataSizeInBits = pixelSizeInBits * numberOfPixels;
        double dataSizeInBytes = dataSizeInBits / 8;
        return (int) (dataSizeInBytes);
    }

    @Nullable
    public Texture.TextureInternalFormat getInternalFormat(){
        return internalFormat;
    }

    private void setInternalFormat(@NotNull Texture.TextureInternalFormat internalFormat){
        exceptionIfNull(internalFormat);
        this.internalFormat = internalFormat;
    }

    @ReadOnly
    @NotNull
    public Vector2i getSize(){
        return new Vector2i(size);
    }

    private void setSize(@NotNull Vector2i size){
        exceptionIfNull(size);
        exceptionIfNotInsideClosedInterval(1, getMaxSize(), size);
        this.size.set(size);
    }

    public static int getMaxSize(){
        return OpenGlConstants.MAX_RENDERBUFFER_SIZE;
    }

    public static int getMaxSizeSafe(){
        return OpenGlConstants.MAX_RENDERBUFFER_SIZE_SAFE;
    }

    public boolean isMultisampled(){
        return sampleCount > 1;
    }

    public int getSampleCount(){
        return sampleCount;
    }

    private void setSampleCount(int sampleCount){
        exceptionIfNotInsideClosedInterval(1, getMaxSampleCount(), sampleCount);
        this.sampleCount = sampleCount;
    }

    public static int getMaxSampleCount(){
        return OpenGlConstants.MAX_SAMPLES;
    }

    public static int getMaxSampleCountSafe(){
        return OpenGlConstants.MAX_SAMPLES_SAFE;
    }

    public static int getMaxPoolSize(){
        return RBO_POOL.getMaxPoolSize();
    }

    public static void setMaxPoolSize(int size){
        RBO_POOL.setMaxPoolSize(size);
    }

    @Override
    protected int getType(){
        return GL30.GL_RENDERBUFFER;
    }

    @Override
    protected String getTypeName(){
        return "RBO";
    }

    @Override
    public int getCacheDataSize(){
        return 0;
    }

    @Override
    public void release(){
        GL30.glDeleteRenderbuffers(getId());
        setIdToInvalid();
        setActiveDataSize(0);
        allocated = false;
    }

    @Override
    public boolean isUsable(){
        return isAvailable();
    }

    private static class RboPool extends ResourcePool{

        @Override
        protected void createResources(int[] resources){
            GL45.glCreateRenderbuffers(resources);
        }

        @Override
        public String toString(){
            return super.toString() + "\n" +
                    RboPool.class.getSimpleName() + "(" + ")";
        }
    }
}
