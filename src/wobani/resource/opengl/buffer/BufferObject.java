package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import wobani.resource.*;
import wobani.resource.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.nio.*;

import static org.lwjgl.system.MemoryStack.*;

/**
 * Object oriented wrapper class above the native OpenGL Buffer Object.
 */
public abstract class BufferObject extends OpenGlObject {
    /**
     * Native, OpenGL id.
     */
    private int id;
    /**
     * The buffer's target.
     */
    private final int target;
    /**
     * The allocated memory size (in bytes).
     */
    private int dataSize;
    /**
     * Determines whether the Buffer Object is immutable. If it is, you cannot reallocate the data and if {@link
     * #allowDataModification} is false you cannot even modify the data.
     */
    private boolean immutable;
    /**
     * Determines whether the immutable data is modifiable. It cannot be false if the Buffer Object isn't immutable.
     */
    private boolean allowDataModification = true;
    /**
     * The Buffer Object's usage hint.0
     */
    private BufferObjectUsage usage;
    /**
     * For creating Buffer Objects efficiently.
     */
    private static final BufferObjectPool BUFFER_OBJECT_POOL = new BufferObjectPool();

    /**
     * Initializes a new Buffer Object.
     *
     * @param target buffer's target
     */
    public BufferObject(int target) {
        super(new ResourceId());
        checkTarget(target);
        this.target = target;
        id = createId();
    }

    @Override
    protected int getId() {
        return id;
    }

    @Override
    protected void checkRelease() {
        super.checkRelease();
    }

    /**
     * Returns a new native OpenGL Buffer Object id.
     *
     * @return a new native OpenGL Buffer Object id
     */
    private int createId() {
        return BUFFER_OBJECT_POOL.getResource();
    }

    /**
     * Determines whether the given parameter is a valid OpenGL Buffer Object target.
     *
     * @param target buffer's type
     * @throws IllegalArgumentException if the given target is not valid
     */
    private void checkTarget(int target) {
        if (target != GL15.GL_ARRAY_BUFFER && target != GL42.GL_ATOMIC_COUNTER_BUFFER && target != GL31.GL_COPY_READ_BUFFER && target != GL31.GL_COPY_WRITE_BUFFER && target != GL43.GL_DISPATCH_INDIRECT_BUFFER && target != GL40.GL_DRAW_INDIRECT_BUFFER && target != GL15.GL_ELEMENT_ARRAY_BUFFER && target != GL21.GL_PIXEL_PACK_BUFFER && target != GL21.GL_PIXEL_UNPACK_BUFFER && target != GL44.GL_QUERY_BUFFER && target != GL43.GL_SHADER_STORAGE_BUFFER && target != GL31.GL_TEXTURE_BUFFER && target != GL30.GL_TRANSFORM_FEEDBACK_BUFFER && target != GL31.GL_UNIFORM_BUFFER) {
            throw new IllegalArgumentException("The given target is not valid");
        }
    }

    /**
     * Returns the Buffer Object's OpenGL target.
     *
     * @return the Buffer Object's OpenGL target
     */
    protected int getTarget() {
        return target;
    }

    /**
     * Returns the Buffer Object Pool's maximum size. When you create a new Buffer Object the system first tries to get one
     * from the Buffer Object Pool. If it's empty it fills the pool with max pool size number of Buffer Objects.
     */
    public static int getMaxPoolSize() {
        return BUFFER_OBJECT_POOL.getMaxPoolSize();
    }

    /**
     * Sets the Buffer Object Pool's maximum size. When you create a new Buffer Object the system first tries to get one
     * from the Buffer Object Pool. If it's empty it fills the pool with max pool size number of Buffer Objects.
     *
     * @param size Buffer Object Pool's maximum size
     */
    public static void setMaxPoolSize(int size) {
        BUFFER_OBJECT_POOL.setMaxPoolSize(size);
    }

    //
    //data allocation---------------------------------------------------------------------------------------------------
    //

    /**
     * Checks whether the allocation is possible and stores the given parameters.
     *
     * @param size  memory size to allocate (in bytes)
     * @param usage usage hint
     * @throws IllegalArgumentException if size is negative or higher than the maximum
     */
    protected void allocationGeneral(int size, @Nullable BufferObjectUsage usage) {
        checkRelease();
        checkReallocation();
        if (size < 0 || size > getMaxDataSize()) {
            throw new IllegalArgumentException("Size is negative or higher than the maximum");
        }
        dataSize = size;
        this.usage = usage;
    }

    /**
     * Returns the maximum capacity of the Buffer Object.
     *
     * @return the maximum capacity of the Buffer Object
     */
    protected int getMaxDataSize() {
        return Integer.MAX_VALUE;
    }

    /**
     * If the Buffer Object is immutable it throws an UnsupportedOperationException.
     *
     * @throws UnsupportedOperationException if the Buffer Object is immutable
     */
    protected void checkReallocation() {
        if (isImmutable()) {
            throw new UnsupportedOperationException("You cannot reallocate immutable data");
        }
    }

    /**
     * Allocates memory for the Buffer Object.
     *
     * @param size  memory size to allocate (in bytes)
     * @param usage data usage
     */
    public void allocate(int size, @NotNull BufferObjectUsage usage) {
        allocationGeneral(size, usage);
        this.usage = usage;
        GL45.glNamedBufferData(getId(), size, usage.getCode());
    }

    /**
     * Allocates memory for the Buffer Object. It'll be immutable which means in this case that you can't reallocate it,
     * however you can modify the stored data.
     *
     * @param size memory size to allocate (in bytes)
     */
    public void allocateImmutable(int size) {
        allocationGeneral(size, null);
        immutable = true;
        GL45.glNamedBufferStorage(getId(), size, GL44.GL_DYNAMIC_STORAGE_BIT);
    }

    /**
     * Allocates memory for the Buffer Object and fills it with the given data.
     *
     * @param data  data to store
     * @param usage data usage
     */
    public void allocateAndStore(@NotNull float[] data, @NotNull BufferObjectUsage usage) {
        try (MemoryStack stack = stackPush()) {
            allocateAndStore(Utility.storeInBuffer(stack, data), usage);
        }
    }

    /**
     * Allocates memory for the Buffer Object and fills it with the given data.
     *
     * @param data  data to store
     * @param usage data usage
     */
    public void allocateAndStore(@NotNull FloatBuffer data, @NotNull BufferObjectUsage usage) {
        allocationGeneral(data.limit() * Utility.FLOAT_SIZE, usage);
        GL45.glNamedBufferData(getId(), data, usage.getCode());
    }

    /**
     * Allocates memory for the Buffer Object and fills it with the given data.
     *
     * @param data  data to store
     * @param usage data usage
     */
    public void allocateAndStore(@NotNull int[] data, @NotNull BufferObjectUsage usage) {
        try (MemoryStack stack = stackPush()) {
            allocateAndStore(Utility.storeInBuffer(stack, data), usage);
        }
    }

    /**
     * Allocates memory for the Buffer Object and fills it with the given data.
     *
     * @param data  data to store
     * @param usage data usage
     */
    public void allocateAndStore(@NotNull IntBuffer data, @NotNull BufferObjectUsage usage) {
        allocationGeneral(data.limit() * Utility.INT_SIZE, usage);
        GL45.glNamedBufferData(getId(), data, usage.getCode());
    }

    /**
     * Allocates memory for the Buffer Object and fills it with the given data. After calling this method you can't
     * reallocate the buffer. However if allowDataModification is true, you can modify the stored data.
     *
     * @param data                  data to store
     * @param allowDataModification true if you want to later modify the Buffer Object's data, false otherwise
     */
    public void allocateAndStoreImmutable(@NotNull float[] data, boolean allowDataModification) {
        try (MemoryStack stack = stackPush()) {
            allocateAndStoreImmutable(Utility.storeInBuffer(stack, data), allowDataModification);
        }
    }

    /**
     * Allocates memory for the Buffer Object and fills it with the given data. After calling this method you can't
     * reallocate the buffer. However if allowDataModification is true, you can modify the stored data.
     *
     * @param data                  data to store
     * @param allowDataModification true if you want to later modify the Buffer Object's data, false otherwise
     */
    public void allocateAndStoreImmutable(@NotNull FloatBuffer data, boolean allowDataModification) {
        allocationGeneral(data.limit() * Utility.FLOAT_SIZE, null);
        immutable = true;
        this.allowDataModification = allowDataModification;
        GL45.glNamedBufferStorage(getId(), data, allowDataModification ? GL44.GL_DYNAMIC_STORAGE_BIT : GL11.GL_NONE);
    }

    /**
     * Allocates memory for the Buffer Object and fills it with the given data. After calling this method you can't
     * reallocate the buffer. However if allowDataModification is true, you can modify the stored data.
     *
     * @param data                  data to store
     * @param allowDataModification true if you want to later modify the Buffer Object's data, false otherwise
     */
    public void allocateAndStoreImmutable(@NotNull int[] data, boolean allowDataModification) {
        try (MemoryStack stack = stackPush()) {
            allocateAndStoreImmutable(Utility.storeInBuffer(stack, data), allowDataModification);
        }
    }

    /**
     * Allocates memory for the Buffer Object and fills it with the given data. After calling this method you can't
     * reallocate the buffer. However if allowDataModification is true, you can modify the stored data.
     *
     * @param data                  data to store
     * @param allowDataModification true if you want to later modify the Buffer Object's data, false otherwise
     */
    public void allocateAndStoreImmutable(@NotNull IntBuffer data, boolean allowDataModification) {
        allocationGeneral(data.limit() * Utility.INT_SIZE, null);
        immutable = true;
        this.allowDataModification = allowDataModification;
        GL45.glNamedBufferStorage(getId(), data, allowDataModification ? GL44.GL_DYNAMIC_STORAGE_BIT : GL11.GL_NONE);
    }

    //
    //data store--------------------------------------------------------------------------------------------------------
    //

    /**
     * Checks whether the data modifications is possible with the given parameters.
     *
     * @param offset data's offset (in bytes)
     * @param size   data's size (in bytes)
     * @throws IllegalArgumentException if offset is negative or if the data exceeds from the Buffer Object (because of
     *                                  it's size or the offset)
     */
    protected void storeGeneral(long offset, int size) {
        checkRelease();
        checkDataModification();
        //FIXME: error if memory not allocated
        if (offset < 0) {
            throw new IllegalArgumentException("Offset can't be negative");
        }
        if (getActiveDataSize() - offset < size) {
            throw new IllegalStateException("The data exceeds from the Buffer Object, data size or offset is too high");
        }
    }

    /**
     * If the Buffer Object not allows data modification it throws an UnsupportedOperationException.
     *
     * @throws UnsupportedOperationException if the Buffer Object not allows data modification
     */
    protected void checkDataModification() {
        if (!isAllowDataModification()) {
            throw new UnsupportedOperationException("You cannot modify immutable data");
        }
    }

    /**
     * Stores the given data in the Buffer Object. You should only call this method if the Buffer Object is not immutable
     * or if it allows data modification.
     *
     * @param data data to store
     */
    public void store(@NotNull float[] data) {
        store(data, 0);
    }

    /**
     * Stores the given data on the specified position. You should only call this method if the Buffer Object is not
     * immutable or if it allows data modification.
     *
     * @param data   data to store
     * @param offset data's offset (in bytes)
     */
    public void store(@NotNull float[] data, long offset) {
        try (MemoryStack stack = stackPush()) {
            store(Utility.storeInBuffer(stack, data), offset);
        }
    }

    /**
     * Stores the given data on the specified position. You should only call this method if the Buffer Object is not
     * immutable or if it allows data modification.
     *
     * @param data   data to store
     * @param offset data's offset (in bytes)
     */
    public void store(@NotNull FloatBuffer data, long offset) {
        storeGeneral(offset, data.limit() * Utility.FLOAT_SIZE);
        GL45.glNamedBufferSubData(getId(), offset, data);
    }

    /**
     * Stores the given data in the Buffer Object. You should only call this method if the Buffer Object is not immutable
     * or if it allows data modification.
     *
     * @param data data to store
     */
    public void store(@NotNull int[] data) {
        store(data, 0);
    }

    /**
     * Stores the given data on the specified position. You should only call this method if the Buffer Object is not
     * immutable or if it allows data modification.
     *
     * @param data   data to store
     * @param offset data's offset (in bytes)
     */
    public void store(@NotNull int[] data, long offset) {
        try (MemoryStack stack = stackPush()) {
            store(Utility.storeInBuffer(stack, data), offset);
        }
    }

    /**
     * Stores the given data on the specified position. You should only call this method if the Buffer Object is not
     * immutable or if it allows data modification.
     *
     * @param data   data to store
     * @param offset data's offset (in bytes)
     */
    public void store(@NotNull IntBuffer data, long offset) {
        storeGeneral(offset, data.limit() * Utility.INT_SIZE);
        GL45.glNamedBufferSubData(getId(), offset, data);
    }

    //
    //data copy---------------------------------------------------------------------------------------------------------
    //

    /**
     * Copies the defined data from this Buffer Object to the given.
     *
     * @param writeTarget data's destination
     * @param readOffset  read offset (in bytes)
     * @param writeOffset write offset (in bytes)
     * @param size        size of the data (in bytes)
     */
    public void copyDataTo(@NotNull BufferObject writeTarget, int readOffset, int writeOffset, int size) {
        checkRelease();
        writeTarget.checkRelease();
        checkDataModification();
        writeTarget.checkDataModification();
        checkOffsetAndSize(readOffset, writeOffset, size);
        checkDataExceed(writeTarget, readOffset, writeOffset, size);
        checkRangeOverlapItself(writeTarget, readOffset, writeOffset, size);
        GL45.glCopyNamedBufferSubData(getId(), writeTarget.getId(), readOffset, writeOffset, size);
    }

    /**
     * If one of the offsets are negative or the size is not positive it throws an IllegalArgumentException.
     *
     * @throws IllegalArgumentException if one of the offsets are negative or the size is not positive
     */
    private void checkOffsetAndSize(int readOffset, int writeOffset, int size) {
        if (readOffset < 0 || writeOffset < 0 || size <= 0) {
            throw new IllegalArgumentException("One of the offsets are negative or the size is not positive");
        }
    }

    /**
     * If the data exceeds from the write Buffer Object, it throws an IllegalArgumentException.
     *
     * @throws IllegalArgumentException if the data exceeds from the write Buffer Object (because of it's size or the
     *                                  offset)
     */
    private void checkDataExceed(@NotNull BufferObject writeTarget, int readOffset, int writeOffset, int size) {
        if (readOffset + size > dataSize || writeOffset + size > writeTarget.dataSize) {
            throw new IllegalArgumentException("The data exceeds from the write Buffer Object");
        }
    }

    /**
     * If the read and write Buffer Objects are the same and the ranges are overlapping, it throws an
     * IllegalArgumentException.
     *
     * @throws IllegalArgumentException if the read and write Buffer Objects are the same and the ranges are overlapping
     */
    private void checkRangeOverlapItself(@NotNull BufferObject writeTarget, int readOffset, int writeOffset, int size) {
        if (writeTarget == this && (readOffset <= writeOffset && readOffset + size >= writeOffset || readOffset <= writeOffset + size && readOffset + size >= writeOffset + size)) {
            throw new IllegalArgumentException("The read and write Buffer Objects are the same and the ranges are overlapping");
        }
    }

    //
    //misc--------------------------------------------------------------------------------------------------------------
    //

    /**
     * Returns the Buffer Object's usage hint.
     *
     * @return the Buffer Object's usage hint
     */
    @Nullable
    public BufferObjectUsage getUsage() {
        return usage;
    }

    /**
     * Determines whether the Buffer Object is immutable. If it is, you cannot reallocate the data and if {@link
     * #isAllowDataModification()} is false you cannot even modify the data.
     *
     * @return true if the Buffer Object is immutable, false otherwise
     */
    public boolean isImmutable() {
        return immutable;
    }

    /**
     * Sets whether or not the Buffer Object is immutable.
     *
     * @param immutable true if this Buffer Object should be immutable, false otherwise
     */
    protected void setImmutable(boolean immutable) {
        this.immutable = immutable;
    }

    /**
     * Determines whether the immutable data is modifiable. It cannot be false if the Buffer Object isn't immutable.
     *
     * @return true if the Buffer Object's data is modifiable, false otherwise
     */
    public boolean isAllowDataModification() {
        return allowDataModification;
    }

    /**
     * Sets whether or not the Buffer Object is allow data modification.
     *
     * @param allowDataModification true if this Buffer Object should allow data modification, false otherwise
     */
    protected void setAllowDataModification(boolean allowDataModification) {
        this.allowDataModification = allowDataModification;
    }

    @Override
    protected int getType() {
        return GL43.GL_BUFFER;
    }

    /**
     * Sets the data size to the given value.
     *
     * @param size size
     */
    protected void setDataSize(int size) {
        this.dataSize = size;
    }

    @Override
    public int getCachedDataSize() {
        return 0;
    }

    @Override
    public int getActiveDataSize() {
        return dataSize;
    }

    @Override
    public boolean isUsable() {
        return getId() != -1;
    }

    @Override
    public void release() {
        GL15.glDeleteBuffers(getId());
        id = -1;
        dataSize = 0;
    }

    @Override
    public String toString() {
        return super.toString() + "\n" +
                BufferObject.class.getSimpleName() + "(" +
                "id: " + id + ", " +
                "target: " + target + ", " +
                "dataSize: " + dataSize + ", " +
                "immutable: " + immutable + ", " +
                "allowDataModification: " + allowDataModification + ", " +
                "usage: " + usage + ")";
    }

    /**
     * Buffer Object usage.
     */
    public enum BufferObjectUsage {
        /**
         * Stream draw.
         */
        STREAM_DRAW(GL15.GL_STREAM_DRAW),
        /**
         * Stream read.
         */
        STREAM_READ(GL15.GL_STREAM_READ),
        /**
         * Stream copy.
         */
        STREAM_COPY(GL15.GL_STREAM_COPY),
        /**
         * Static draw.
         */
        STATIC_DRAW(GL15.GL_STATIC_DRAW),
        /**
         * Static read.
         */
        STATIC_READ(GL15.GL_STATIC_READ),
        /**
         * Static copy.
         */
        STATIC_COPY(GL15.GL_STATIC_COPY),
        /**
         * Dynamic draw.
         */
        DYNAMIC_DRAW(GL15.GL_DYNAMIC_DRAW),
        /**
         * Dynamic read.
         */
        DYNAMIC_READ(GL15.GL_DYNAMIC_READ),
        /**
         * Dynamic copy.
         */
        DYNAMIC_COPY(GL15.GL_DYNAMIC_COPY);

        /**
         * Buffer Object usage's OpenGL code.
         */
        private final int code;

        /**
         * Initializes a new BufferObjectUsage to the given value.
         *
         * @param code Buffer Object usage's OpenGL code
         */
        BufferObjectUsage(int code) {
            this.code = code;
        }

        /**
         * Returns the BufferObjectUsage of the given OpenGL code.
         *
         * @param code OpenGL Buffer Object usage
         * @return the BufferObjectUsage of the given OpenGL code
         * @throws IllegalArgumentException if the given parameter is not a Buffer Object usage
         */
        @NotNull
        public static BufferObjectUsage valueOf(int code) {
            for (BufferObjectUsage mode : BufferObjectUsage.values()) {
                if (mode.getCode() == code) {
                    return mode;
                }
            }
            throw new IllegalArgumentException("The given parameter is not a Buffer Object usage");
        }

        /**
         * Returns the Buffer Object usage's OpenGL code.
         *
         * @return the Buffer Object usage's OpenGL code
         */
        public int getCode() {
            return code;
        }
    }

    /**
     * For creating Buffer Objects efficiently.
     */
    private static class BufferObjectPool extends ResourcePool {

        @Override
        protected void createResources(int[] resources) {
            GL45.glCreateBuffers(resources);
        }

        @Override
        public String toString() {
            return super.toString() + "\n" +
                    BufferObjectPool.class.getSimpleName() + "(" + ")";
        }
    }

}
