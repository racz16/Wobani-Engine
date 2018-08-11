package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import wobani.resource.*;
import wobani.resource.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.exceptions.*;

import java.nio.*;

import static org.lwjgl.system.MemoryStack.*;

/**
 Object oriented wrapper class above the native OpenGL Buffer Object.
 */
public abstract class BufferObject extends OpenGlObject{

    /**
     The buffer's target.
     */
    private final int target;
    /**
     The allocated memory size (in bytes).
     */
    private int dataSize;
    /**
     For creating Buffer Objects efficiently.
     */
    private static final BufferObjectPool BUFFER_OBJECT_POOL = new BufferObjectPool();

    /**
     Initializes a new Buffer Object.

     @param target buffer's target

     @throws IllegalArgumentException if the given target is not valid
     */
    public BufferObject(int target){
        super();
        if(!isTargetValid(target)){
            throw new IllegalArgumentException("The given target is not valid");
        }
        this.target = target;
    }

    @Override
    protected int createId(){
        return BUFFER_OBJECT_POOL.getResource();
    }

    @NotNull
    @Override
    protected ResourceId createResourceId(){
        return new ResourceId();
    }

    /**
     Determines whether the given parameter is a valid OpenGL Buffer Object target.

     @param target buffer's type

     @return true if the given parameter is a valid OpenGL Buffer Object target, false otherwise
     */
    private boolean isTargetValid(int target){
        return target == GL15.GL_ARRAY_BUFFER || target == GL42.GL_ATOMIC_COUNTER_BUFFER || target == GL31.GL_COPY_READ_BUFFER || target == GL31.GL_COPY_WRITE_BUFFER || target == GL43.GL_DISPATCH_INDIRECT_BUFFER || target == GL40.GL_DRAW_INDIRECT_BUFFER || target == GL15.GL_ELEMENT_ARRAY_BUFFER || target == GL21.GL_PIXEL_PACK_BUFFER || target == GL21.GL_PIXEL_UNPACK_BUFFER || target == GL44.GL_QUERY_BUFFER || target == GL43.GL_SHADER_STORAGE_BUFFER || target == GL31.GL_TEXTURE_BUFFER || target == GL30.GL_TRANSFORM_FEEDBACK_BUFFER || target == GL31.GL_UNIFORM_BUFFER;
    }

    /**
     Returns the Buffer Object's OpenGL target.

     @return the Buffer Object's OpenGL target
     */
    protected int getTarget(){
        return target;
    }

    /**
     Returns the Buffer Object Pool's maximum size. When you create a new Buffer Object the system first tries to get one
     from the Buffer Object Pool. If it's empty it fills the pool with max pool size number of Buffer Objects.
     */
    public static int getMaxPoolSize(){
        return BUFFER_OBJECT_POOL.getMaxPoolSize();
    }

    /**
     Sets the Buffer Object Pool's maximum size. When you create a new Buffer Object the system first tries to get one
     from the Buffer Object Pool. If it's empty it fills the pool with max pool size number of Buffer Objects.

     @param size Buffer Object Pool's maximum size
     */
    public static void setMaxPoolSize(int size){
        BUFFER_OBJECT_POOL.setMaxPoolSize(size);
    }

    /**
     Allocates memory for the Buffer Object.

     @param size  memory size to allocate (in bytes)
     @param usage data usage

     @throws IllegalArgumentException if size is negative or higher than the maximum
     */
    @Bind
    public void allocate(int size, @NotNull BufferObjectUsage usage){
        checkRelease();
        checkBind();
        if(size < 0 || size > getMaxDataSize()){
            throw new IllegalArgumentException("Size is negative or higher than the maximum");
        }
        dataSize = size;
        GL15.glBufferData(target, size, usage.getCode());
    }

    /**
     Allocates memory for the Buffer Object and fills it with the given data.

     @param data  data to store
     @param usage data usage
     */
    @Bind
    public void allocateAndStore(@NotNull float[] data, @NotNull BufferObjectUsage usage){
        try(MemoryStack stack = stackPush()){
            FloatBuffer buffer = stack.mallocFloat(data.length);
            buffer.put(data);
            buffer.flip();
            allocateAndStore(buffer, usage);
        }
    }

    /**
     Allocates memory for the Buffer Object and fills it with the given data.

     @param data  data to store
     @param usage data usage

     @throws IllegalArgumentException if size is bigger than the maximum
     */
    @Bind
    public void allocateAndStore(@NotNull FloatBuffer data, @NotNull BufferObjectUsage usage){
        checkRelease();
        checkBind();
        dataSize = data.limit() * Utility.FLOAT_SIZE;
        if(dataSize > getMaxDataSize()){
            throw new IllegalArgumentException("Size is bigger than the maximum");
        }
        GL15.glBufferData(target, data, usage.getCode());
    }

    /**
     Allocates memory for the Buffer Object and fills it with the given data.

     @param data  data to store
     @param usage data usage
     */
    @Bind
    public void allocateAndStore(@NotNull int[] data, @NotNull BufferObjectUsage usage){
        try(MemoryStack stack = stackPush()){
            IntBuffer buffer = stack.mallocInt(data.length);
            buffer.put(data);
            buffer.flip();
            allocateAndStore(buffer, usage);
        }
    }

    /**
     Allocates memory for the Buffer Object and fills it with the given data.

     @param data  data to store
     @param usage data usage

     @throws IllegalArgumentException if size is bigger than the maximum
     */
    @Bind
    public void allocateAndStore(@NotNull IntBuffer data, @NotNull BufferObjectUsage usage){
        checkRelease();
        checkBind();
        dataSize = data.limit() * Utility.INT_SIZE;
        if(dataSize > getMaxDataSize()){
            throw new IllegalArgumentException("Size is bigger than the maximum");
        }
        GL15.glBufferData(target, data, usage.getCode());
    }

    /**
     Returns the maximum capacity of the Buffer Object.

     @return the maximum capacity of the Buffer Object
     */
    protected int getMaxDataSize(){
        return Integer.MAX_VALUE;
    }

    /**
     Stores the given data in the Buffer Object.

     @param data data to store
     */
    @Bind
    public void store(@NotNull float[] data){
        store(data, 0);
    }

    /**
     Stores the given data on the specified position.

     @param data   data to store
     @param offset data's offset (in bytes)
     */
    @Bind
    public void store(@NotNull float[] data, long offset){
        try(MemoryStack stack = stackPush()){
            FloatBuffer buffer = stack.mallocFloat(data.length);
            buffer.put(data);
            buffer.flip();
            store(buffer, offset);
        }
    }

    /**
     Stores the given data on the specified position.

     @param data   data to store
     @param offset data's offset (in bytes)

     @throws IllegalArgumentException if offset is negative or if the data exceeds from the Buffer Object (because of
     it's size or the offset)
     */
    @Bind
    public void store(@NotNull FloatBuffer data, long offset){
        checkRelease();
        checkBind();
        if(offset < 0){
            throw new IllegalArgumentException("Offset can't be negative");
        }
        if(getActiveDataSize() - offset < data.limit() * Utility.FLOAT_SIZE){
            throw new IllegalStateException("The data exceeds from the Buffer Object, data size or offset is too high");
        }
        GL15.glBufferSubData(target, offset, data);
    }

    /**
     Stores the given data in the Buffer Object.

     @param data data to store
     */
    @Bind
    public void store(@NotNull int[] data){
        store(data, 0);
    }

    /**
     Stores the given data on the specified position.

     @param data   data to store
     @param offset data's offset (in bytes)
     */
    @Bind
    public void store(@NotNull int[] data, long offset){
        try(MemoryStack stack = stackPush()){
            IntBuffer buffer = stack.mallocInt(data.length);
            buffer.put(data);
            buffer.flip();
            store(buffer, offset);
        }
    }

    /**
     Stores the given data on the specified position.

     @param data   data to store
     @param offset data's offset (in bytes)

     @throws IllegalArgumentException if offset is negative or if the data exceeds from the Buffer Object (because of
     it's size or the offset)
     */
    @Bind
    public void store(@NotNull IntBuffer data, long offset){
        checkRelease();
        checkBind();
        if(offset < 0){
            throw new IllegalArgumentException("Offset can't be negative");
        }
        if(getActiveDataSize() - offset < data.limit() * Utility.INT_SIZE){
            throw new IllegalStateException("The data exceeds from the Buffer Object, data size or offset is too high");
        }
        GL15.glBufferSubData(target, offset, data);
    }

    /**
     Copies the defined data from this Buffer Object to the given.

     @param writeTarget data's destination
     @param readOffset  read offset (in bytes)
     @param writeOffset write offset (in bytes)
     @param size        size of the data (in bytes)

     @throws ReleasedException if this or the parameter Buffer Object is released
     */
    public void copyDataTo(@NotNull BufferObject writeTarget, int readOffset, int writeOffset, int size){
        checkRelease();
        if(!Utility.isUsable(writeTarget)){
            throw new ReleasedException(writeTarget);
        }
        checkOffsetAndSize(readOffset, writeOffset, size);
        checkDataExceed(writeTarget, readOffset, writeOffset, size);
        checkRangeOverlapItself(writeTarget, readOffset, writeOffset, size);
        copyDataToUnsafe(writeTarget, readOffset, writeOffset, size);
    }

    /**
     If one of the offsets are negative or the size is not positive it throws a NotBoundException.

     @throws NotBoundException if one of the offsets are negative or the size is not positive
     */
    private void checkOffsetAndSize(int readOffset, int writeOffset, int size){
        if(readOffset < 0 || writeOffset < 0 || size <= 0){
            throw new IllegalArgumentException("One of the offsets are negative or the size is not positive");
        }
    }

    /**
     If the data exceeds from the write Buffer Object it throws a NotBoundException.

     @throws NotBoundException if the data exceeds from the write Buffer Object (because of it's size or the offset)
     */
    private void checkDataExceed(@NotNull BufferObject writeTarget, int readOffset, int writeOffset, int size){
        if(readOffset + size > dataSize || writeOffset + size > writeTarget.dataSize){
            throw new IllegalArgumentException("The data exceeds from the write Buffer Object");
        }
    }

    /**
     If the read and write Buffer Objects are the same and the ranges are overlapping, it throws a NotBoundException.

     @throws NotBoundException if the read and write Buffer Objects are the same and the ranges are overlapping
     */
    private void checkRangeOverlapItself(@NotNull BufferObject writeTarget, int readOffset, int writeOffset, int size){
        if(writeTarget == this && readOffset <= writeOffset && readOffset + size >= writeOffset || readOffset <= writeOffset + size && readOffset + size >= writeOffset + size){
            throw new IllegalArgumentException("The read and write Buffer Objects are the same and the ranges are overlapping");
        }
    }

    /**
     Copies the defined data from this Buffer Object to the given Buffer Object.

     @param writeTarget data's destination
     @param readOffset  read offset (in bytes)
     @param writeOffset write offset (in bytes)
     @param size        size of the data (in bytes)
     */
    private void copyDataToUnsafe(@NotNull BufferObject writeTarget, int readOffset, int writeOffset, int size){
        GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, getId());
        GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, writeTarget.getId());
        GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, readOffset, writeOffset, size);
    }

    @Override
    protected int getType(){
        return GL43.GL_BUFFER;
    }

    /**
     Determines whether this Buffer Object is bound.

     @return true if this Buffer Object is bound, false otherwise
     */
    public abstract boolean isBound();

    /**
     Binds the Buffer Object.
     */
    public void bind(){
        checkRelease();
        GL15.glBindBuffer(target, getId());
    }

    /**
     Unbinds the Buffer Object. The Buffers Object must be bound.
     */
    @Bind
    public void unbind(){
        checkRelease();
        checkBind();
        GL15.glBindBuffer(target, 0);
    }

    /**
     If the Buffer Object is not bound it throws a NotBoundException.

     @throws NotBoundException if the Buffer Object is not bound
     */
    protected void checkBind(){
        if(!isBound()){
            throw new NotBoundException(this);
        }
    }

    /**
     Sets the data size to the given value.

     @param size size
     */
    protected void setDataSize(int size){
        this.dataSize = size;
    }

    @Override
    public int getCachedDataSize(){
        return 0;
    }

    @Override
    public int getActiveDataSize(){
        return dataSize;
    }

    @Override
    public void update(){

    }

    @Override
    public boolean isUsable(){
        return getId() != -1;
    }

    @Override
    public void release(){
        GL15.glDeleteBuffers(getId());
        setId(-1);
        dataSize = 0;
    }

    @Override
    public String toString(){
        return super.toString() + "\n" + BufferObject.class
                .getSimpleName() + "(" + "target: " + target + ", " + "dataSize: " + dataSize + ")";
    }

    /**
     Buffer Object usage.
     */
    public enum BufferObjectUsage{
        /**
         Stream draw.
         */
        STREAM_DRAW(GL15.GL_STREAM_DRAW),
        /**
         Stream read.
         */
        STREAM_READ(GL15.GL_STREAM_READ),
        /**
         Stream copy.
         */
        STREAM_COPY(GL15.GL_STREAM_COPY),
        /**
         Static draw.
         */
        STATIC_DRAW(GL15.GL_STATIC_DRAW),
        /**
         Static read.
         */
        STATIC_READ(GL15.GL_STATIC_READ),
        /**
         Static copy.
         */
        STATIC_COPY(GL15.GL_STATIC_COPY),
        /**
         Dynamic draw.
         */
        DYNAMIC_DRAW(GL15.GL_DYNAMIC_DRAW),
        /**
         Dynamic read.
         */
        DYNAMIC_READ(GL15.GL_DYNAMIC_READ),
        /**
         Dynamic copy.
         */
        DYNAMIC_COPY(GL15.GL_DYNAMIC_COPY);

        /**
         Buffer Object usage's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new BufferObjectUsage to the given value.

         @param code Buffer Object usage's OpenGL code
         */
        BufferObjectUsage(int code){
            this.code = code;
        }

        /**
         Returns the BufferObjectUsage of the given OpenGL code.

         @param code OpenGL Buffer Object usage

         @return the BufferObjectUsage of the given OpenGL code

         @throws IllegalArgumentException the given parameter is not a Buffer Object usage
         */
        @NotNull
        public static BufferObjectUsage valueOf(int code){
            for(BufferObjectUsage mode : BufferObjectUsage.values()){
                if(mode.getCode() == code){
                    return mode;
                }
            }
            throw new IllegalArgumentException("The given parameter is not a Buffer Object usage");
        }

        /**
         Returns the Buffer Object usage's OpenGL code.

         @return the Buffer Object usage's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }

    /**
     For creating Buffer Objects efficiently.
     */
    private static class BufferObjectPool extends ResourcePool{

        @Override
        protected void createResources(int[] resources){
            GL15.glGenBuffers(resources);
        }

        @Override
        public String toString(){
            return super.toString() + "\n" + BufferObjectPool.class.getSimpleName() + "(" + ")";
        }
    }

}
