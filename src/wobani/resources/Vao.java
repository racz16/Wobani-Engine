package wobani.resources;

import java.nio.*;
import java.util.*;
import org.lwjgl.assimp.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import wobani.toolbox.*;
import wobani.toolbox.annotations.*;

/**
 * Object oriented wrapper class above the native Vertex Array Object. It can
 * store an EBO and VBOs.
 */
public class Vao implements Resource {

    /**
     * Vertex Array Object's id.
     */
    private int vao = 0;
    /**
     * Element Buffer Object's id.
     */
    private int ebo = 0;
    /**
     * EBO's size.
     */
    private int eboSize;
    /**
     * The Vertex Buffer Objects' ids.
     */
    private final HashMap<String, Integer> vbos = new HashMap<>();
    /**
     * VBOs' size.
     */
    private final HashMap<String, Integer> vboSize = new HashMap<>();
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     * Initializes a new VAO.
     */
    public Vao() {
        vao = GL30.glGenVertexArrays();
        resourceId = new ResourceId();
        ResourceManager.addVao(this);
    }

    /**
     * Binds this VAO.
     */
    public void bindVao() {
        GL30.glBindVertexArray(vao);
    }

    /**
     * Unbinds the VAO.
     */
    public void unbindVao() {
        GL30.glBindVertexArray(0);
    }

    /**
     * Returns the VAO's id.
     *
     * @return VAO's id
     */
    public int getVao() {
        return vao;
    }

    /**
     * Removes the VAO.
     */
    private void removeVao() {
        GL30.glDeleteVertexArrays(vao);
        vao = 0;
    }

    //
    //VBO-----------------------------------------------------------------------
    //
    /**
     * Creates a new VBO. Each VBO's name must be unique.
     *
     * @param name VBO's name
     *
     * @return true if the VBO successfully created, false otherwise
     *
     * @throws NullPointerException name can't be null
     */
    @Bind
    public boolean createVbo(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (!vbos.containsKey(name)) {
            vbos.put(name, GL15.glGenBuffers());
            vboSize.put(name, 0);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Binds the specified VBO and stores the given data in it.
     *
     * @param vboName         vbo's name
     * @param attributeNumber shader's attribute number
     * @param coordinateSize  number of a vector's coordinates
     * @param data            data
     * @param dynamic         true if the data should be dynamic, false
     *                        otherwise
     */
    public void bindAndAddData(@NotNull String vboName, int attributeNumber, int coordinateSize, @NotNull float[] data, boolean dynamic) {
        try (MemoryStack stack = stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(data.length);
            buffer.put(data);
            buffer.flip();
            bindAndAddData(vboName, attributeNumber, coordinateSize, Utility.storeDataInFloatBuffer(data), dynamic);
        }
    }

    /**
     * Binds the specified VBO and stores the given data in it.
     *
     * @param vboName         vbo's name
     * @param attributeNumber shader's attribute number
     * @param coordinateSize  number of a vector's coordinates
     * @param data            data
     * @param dynamic         true if the data should be dynamic, false
     *                        otherwise
     *
     * @throws IllegalArgumentException attribute number can't be lower than 0
     *                                  and coordinate size must be in the (1;4)
     *                                  interval
     * @throws NullPointerException     arguments can't be null
     */
    public void bindAndAddData(@NotNull String vboName, int attributeNumber, int coordinateSize, @NotNull FloatBuffer data, boolean dynamic) {
        if (data == null) {
            throw new NullPointerException();
        }
        if (attributeNumber < 0 || coordinateSize < 1 || coordinateSize > 4) {
            throw new IllegalArgumentException("Attribute number can't be lower than 0 and coordinate size must be in the (1;4) interval");
        }
        bindVbo(vboName);
        vboSize.put(vboName, data.capacity());
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, dynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
    }

    /**
     * Binds the specified VBO and stores the given data in it.
     *
     * @param vboName         vbo's name
     * @param attributeNumber shader's attribute number
     * @param coordinateSize  number of a vector's coordinates
     * @param data            data
     * @param dynamic         true if the data should be dynamic, false
     *                        otherwise
     *
     * @throws IllegalArgumentException attribute number can't be lower than 0
     *                                  and coordinate size must be in the (1;4)
     *                                  interval
     */
    public void bindAndAddData(@NotNull String vboName, int attributeNumber, int coordinateSize, @NotNull AIVector3D.Buffer data, boolean dynamic) {
        if (attributeNumber < 0 || coordinateSize < 1 || coordinateSize > 4) {
            throw new IllegalArgumentException("Attribute number can't be lower than 0 and coordinate size must be in the (1;4) interval");
        }
        bindVbo(vboName);
        vboSize.put(vboName, data.capacity());
        GL15.nglBufferData(GL15.GL_ARRAY_BUFFER, AIVector3D.SIZEOF * data.remaining(), data.address(), dynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
    }

    /**
     * Binds the specified VBO.
     *
     * @param name VBO's name
     */
    public void bindVbo(@NotNull String name) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, getVbo(name));
    }

    /**
     * Unbinds VBO.
     */
    public void unbindVbo() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    /**
     * Returns the specified VBO's id.
     *
     * @param name VBO's name
     *
     * @return VBO's id
     *
     * @throws NullPointerException     name can't be null
     * @throws IllegalArgumentException there is no such a VBO
     */
    public int getVbo(@NotNull String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (!vbos.containsKey(name)) {
            throw new IllegalArgumentException("There is no such a VBO");
        }
        return vbos.get(name);
    }

    /**
     * Removes the specified VBO.
     *
     * @param name VBO's name
     *
     * @throws NullPointerException     name can't be null
     * @throws IllegalArgumentException there is no such a VBO
     */
    public void removeVbo(@NotNull String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (!vbos.containsKey(name)) {
            throw new IllegalArgumentException("There is no such a VBO");
        }
        vboSize.remove(name);
        GL15.glDeleteBuffers(vbos.remove(name));
    }

    /**
     * Returns all the VBOs' names.
     *
     * @return the VBOs' names
     */
    @NotNull @ReadOnly
    public String[] getVboNames() {
        String[] names = new String[vbos.keySet().size()];
        vbos.keySet().toArray(names);
        return names;
    }

    /**
     * Returns the number of the VBOs in this VAO.
     *
     * @return number of the VBOs
     */
    public int getNumberOfVbos() {
        return vbos.size();
    }

    //
    //EBO-----------------------------------------------------------------------
    //
    /**
     * Creates the EBO, if it isn't exists already.
     *
     * @return true if the EBO successfully created, false otherwise
     */
    @Bind
    public boolean createEbo() {
        if (ebo == 0) {
            ebo = GL15.glGenBuffers();
            return true;
        }
        return false;
    }

    /**
     * Adds indices to the EBO.
     *
     * @param indices indices
     * @param dynamic true if the data should be dynamic, false otherwise
     */
    @Bind
    public void addIndices(@NotNull int[] indices, boolean dynamic) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer buffer = stack.mallocInt(indices.length);
            buffer.put(indices);
            buffer.flip();
            addIndices(buffer, dynamic);
        }
    }

    /**
     * Adds indices to the EBO.
     *
     * @param indices indices
     * @param dynamic true if the data should be dynamic, false otherwise
     *
     * @throws NullPointerException indices can't be null
     */
    @Bind
    public void addIndices(@NotNull IntBuffer indices, boolean dynamic) {
        if (indices == null) {
            throw new NullPointerException();
        }
        eboSize = indices.capacity();
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, dynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW);
    }

    /**
     * Binds the EBO.
     */
    public void bindEbo() {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
    }

    /**
     * Unbinds the EBO.
     */
    public void unbindEbo() {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Returns the EBO's id. Returns 0 if there is no EBO in this VAO.
     *
     * @return EBO's id
     */
    public int getEbo() {
        return ebo;
    }

    /**
     * Removes the EBO.
     */
    public void removeEbo() {
        GL15.glDeleteBuffers(ebo);
        ebo = 0;
        eboSize = 0;
    }

    //
    //misc----------------------------------------------------------------------
    //
    /**
     * Determines wheter this VAO is usable. If it returns false, you can't use
     * it for anything.
     *
     * @return true if usable, false otherwise
     */
    @Override
    public boolean isUsable() {
        return vao != 0;
    }

    /**
     * Removes the VBOs, the EBO and the VAO from the VRAM. After you released
     * the VAO, you can't use it for anything.
     */
    @Override
    public void release() {
        String[] names = new String[vbos.keySet().size()];
        vbos.keySet().toArray(names);
        for (String id : names) {
            removeVbo(id);
        }
        removeEbo();
        removeVao();
    }

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public int getDataSizeInRam() {
        return 0;
    }

    @Override
    public int getDataSizeInAction() {
        int size = 0;
        for (Integer value : vboSize.values()) {
            size += value;
        }
        size += eboSize;
        return size;
    }

    @Override
    public void update() {

    }

    @Override
    public String toString() {
        return "Vao{" + "vao=" + vao + ", ebo=" + ebo + ", eboSize=" + eboSize
                + ", vbos=" + vbos + ", vboSize=" + vboSize + ", resourceId="
                + resourceId + '}';
    }

}
