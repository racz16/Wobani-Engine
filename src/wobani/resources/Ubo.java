package wobani.resources;

import java.nio.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import wobani.toolbox.annotations.*;

/**
 * Object oriented wrapper class above the native Uniform Buffer Objec.t
 */
public class Ubo implements Resource {

    /**
     * UBO's id.
     */
    private int id = -1;
    /**
     * The allocated memory size (in bytes).
     */
    private int dataSize;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     * Initializes a new UBO.
     */
    public Ubo() {
        id = GL15.glGenBuffers();
        resourceId = new ResourceId();
        ResourceManager.addUbo(this);
    }

    /**
     * Allocates memory for the UBO.
     *
     * @param size    memory size to allocate (in bytes)
     * @param dynamic true if the data should be dynamic, false otherwise
     *
     * @throws IllegalArgumentException size must be positive
     */
    @Bind
    public void allocateMemory(int size, boolean dynamic) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        dataSize = size;
        GL15.glBufferData(GL31.GL_UNIFORM_BUFFER, size, dynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW);
    }

    /**
     * Stores the given data on the specified position.
     *
     * @param data   data to store
     * @param offset data's offset (in bytes)
     */
    @Bind
    public void storeData(@NotNull float[] data, long offset) {
        try (MemoryStack stack = stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(data.length);
            buffer.put(data);
            buffer.flip();
            storeData(buffer, offset);
        }
    }

    /**
     * Stores the given data on the specified position.
     *
     * @param data   data to store
     * @param offset data's offset (in bytes)
     *
     * @throws IllegalArgumentException offset can't be lower than 0
     * @throws IllegalStateException    didn't allocated enough memory for the
     *                                  data
     */
    @Bind
    public void storeData(@NotNull FloatBuffer data, long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset can't be lower than 0");
        }
        if (getDataSizeInAction() < data.capacity()) {
            throw new IllegalStateException("Didn't allocated enough memory for the data");
        }
        GL15.glBufferSubData(GL31.GL_UNIFORM_BUFFER, offset, data);
    }

    /**
     * Stores the given data on the specified position.
     *
     * @param data   data to store
     * @param offset data's offset (in bytes)
     */
    @Bind
    public void storeData(@NotNull int[] data, long offset) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer buffer = stack.mallocInt(data.length);
            buffer.put(data);
            buffer.flip();
            storeData(buffer, offset);
        }
    }

    /**
     * Stores the given data on the specified position.
     *
     * @param data   data to store
     * @param offset data's offset (in bytes)
     *
     * @throws IllegalArgumentException offset can't be lower than 0
     * @throws IllegalStateException    didn't allocated enough memory for the
     *                                  data
     */
    @Bind
    public void storeData(@NotNull IntBuffer data, long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset can't be lower than 0");
        }
        if (getDataSizeInAction() < data.capacity()) {
            throw new IllegalStateException("Didn't allocated enough memory for the data");
        }
        GL15.glBufferSubData(GL31.GL_UNIFORM_BUFFER, offset, data);
    }

    /**
     * Binds the UBO to the given binding point. You don't have to bind the UBO
     * before calling this method (but it's not a problem if youe do). Nothe
     * that this method doesn't bind the UBO, it binds to a binding point. If
     * you want to bind the UBO, you should call the bind method.
     *
     * @param bindingPoint binding point
     *
     * @throws IllegalArgumentException binding point can't be lower than 0
     * @see #bind()
     */
    public void bindToBindingPoint(int bindingPoint) {
        if (bindingPoint < 0) {
            throw new IllegalArgumentException("Binding point can't be lower than 0");
        }
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, bindingPoint, id);
    }

    /**
     * Binds the UBO.
     */
    public void bind() {
        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, id);
    }

    /**
     * Unbinds the UBO.
     */
    public void unbind() {
        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);
    }

    @Override
    public int getDataSizeInRam() {
        return 0;
    }

    @Override
    public int getDataSizeInAction() {
        return dataSize;
    }

    @Override
    public void update() {

    }

    /**
     * Determines wheter this UBO is usable. If it returns false, you can't use
     * it for anything.
     *
     * @return true if usable, false otherwise
     */
    @Override
    public boolean isUsable() {
        return id != -1;
    }

    /**
     * Removes the UBO from the VRAM. After you released the UBO, you can't use
     * it for anything.
     */
    @Override
    public void release() {
        GL15.glDeleteBuffers(id);
        id = -1;
        dataSize = 0;
    }

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public String toString() {
        return "Ubo{" + "id=" + id + ", dataSize=" + dataSize + ", resourceId="
                + resourceId + '}';
    }

}
