package wobani.resource.opengl.buffer;

import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL44;
import org.lwjgl.opengl.GL45;
import wobani.toolbox.Utility;
import wobani.toolbox.annotation.Internal;
import wobani.toolbox.annotation.NotNull;
import wobani.toolbox.annotation.Nullable;

/**
 * Object oriented wrapper class above the native Vertex Buffer Object.
 */
public class Vbo extends BufferObject {

    /**
     * The connected vertex attrib array.
     */
    private VertexAttribArray vertexAttribArray;

    /**
     * Initializes a new VBO.
     */
    public Vbo() {
        super(GL15.GL_ARRAY_BUFFER);
    }

    /**
     * Initializes a new VBO to the given value.
     *
     * @param label label
     */
    public Vbo(@NotNull String label) {
        this();
        setLabel(label);
    }

    /**
     * Sets the connected vertex attrib array to the given value.
     *
     * @param vaa vertex attrib array
     */
    @Internal
    void setVertexAttribArray(@Nullable VertexAttribArray vaa) {
        checkConnection(vaa);
        vertexAttribArray = vaa;
    }

    @NotNull
    @Override
    protected String getTypeName() {
        return "VBO";
    }

    /**
     * Allocates memory for the Buffer Object and fills it with the given data.
     *
     * @param data  data to store
     * @param usage data usage
     */
    public void allocateAndStore(@NotNull AIVector3D.Buffer data, @NotNull BufferObjectUsage usage) {
        int size = AIVector3D.SIZEOF * data.remaining();
        allocationGeneral(size, usage);
        GL45.nglNamedBufferData(getId(), size, data.address(), usage.getCode());
    }

    /**
     * Allocates memory for the Buffer Object and fills it with the given data. After calling this method you can't
     * reallocate the buffer. However if allowDataModification is true, you can modify the stored data.
     *
     * @param data                  data to store
     * @param allowDataModification true if you want to later modify the Buffer Object's data, false otherwise
     */
    public void allocateAndStoreImmutable(@NotNull AIVector3D.Buffer data, boolean allowDataModification) {
        int size = AIVector3D.SIZEOF * data.remaining();
        allocationGeneral(size, null);
        setImmutable(true);
        setAllowDataModification(allowDataModification);
        GL45.nglNamedBufferStorage(getId(), size, data
                .address(), allowDataModification ? GL44.GL_DYNAMIC_STORAGE_BIT : GL11.GL_NONE);
    }

    /**
     * Stores the given data on the specified position. You should only call this method if the Buffer Object is not
     * immutable or if it allows data modification.
     *
     * @param data data to store
     */
    public void store(@NotNull AIVector3D.Buffer data) {
        store(data, 0);
    }

    /**
     * Stores the given data on the specified position. You should only call this method if the Buffer Object is not
     * immutable or if it allows data modification.
     *
     * @param data   data to store
     * @param offset data's offset (in bytes)
     */
    public void store(@NotNull AIVector3D.Buffer data, long offset) {
        storeGeneral(offset, data.limit() * Utility.FLOAT_SIZE);
        GL45.nglNamedBufferSubData(getId(), offset, AIVector3D.SIZEOF * data.remaining(), data.address());
    }

    /**
     * If the VBO is already connected to a VAO, it throws an IllegalArgumentException.
     *
     * @throws IllegalArgumentException if the VBO is already connected to a VAO
     */
    private void checkConnection(@Nullable VertexAttribArray vaa) {
        if (vertexAttribArray != null && vaa != null && (vertexAttribArray.getVao() != vaa.getVao() || vaa.getPointer()
                .getIndex() != vertexAttribArray.getPointer().getIndex())) {
            throw new IllegalArgumentException("The VBO is already connected to a VAO");
        }
    }

    @Override
    public void release() {
        super.release();
        if (vertexAttribArray != null) {
            vertexAttribArray.getVao().removeVertexAttribArray(vertexAttribArray.getPointer().getIndex());
            vertexAttribArray = null;
        }
    }

    @Override
    public String toString() {
        return super.toString() + "\n" +
                Vbo.class.getSimpleName() + "(" +
                "vertexAttribArray: " + vertexAttribArray + ")";
    }
}
