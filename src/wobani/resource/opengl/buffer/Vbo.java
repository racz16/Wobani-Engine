package wobani.resource.opengl.buffer;

import org.lwjgl.assimp.*;
import org.lwjgl.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.util.*;

/**
 Object oriented wrapper class above the native Vertex Buffer Object.
 */
public class Vbo extends BufferObject{

    /**
     The currently bound VBO.
     */
    private static Vbo boundVbo;
    /**
     The connected vertex attrib arrays.
     */
    private final Set<VertexAttribArray> vertexAttribArrays = new HashSet<>();

    /**
     Initializes a new VBO.
     */
    public Vbo(){
        super(GL15.GL_ARRAY_BUFFER);
    }

    /**
     Initializes a new VBO to the given value.

     @param label label
     */
    public Vbo(@NotNull String label){
        this();
        setLabel(label);
    }

    /**
     Adds the given vertex attrib array to the connected vertex attrib arrays.

     @param vaa vertex attrib array
     */
    @Internal
    void addVertexAttribArray(@Nullable VertexAttribArray vaa){
        vertexAttribArrays.add(vaa);
    }

    /**
     Removes the given vertex attrib array from the connected vertex attrib arrays.

     @param vaa vertex attrib array
     */
    @Internal
    void removeVertexAttribArray(@Nullable VertexAttribArray vaa){
        vertexAttribArrays.remove(vaa);
    }

    @NotNull
    @Override
    protected String getTypeName(){
        return "VBO";
    }

    /**
     Allocates memory for the Buffer Object and fills it with the given data.

     @param data  data to store
     @param usage data usage
     */
    @Bind
    public void allocateAndStore(@NotNull AIVector3D.Buffer data, @NotNull BufferObjectUsage usage){
        checkRelease();
        checkBind();
        int size = AIVector3D.SIZEOF * data.remaining();
        setDataSize(size);
        GL15.nglBufferData(getTarget(), size, data.address(), usage.getCode());
    }

    /**
     Stores the given data in the VBO.

     @param data data to store
     */
    @Bind
    public void store(@NotNull AIVector3D.Buffer data){
        store(data, 0);
    }

    /**
     Stores the given data on the specified position.

     @param data   data to store
     @param offset data's offset (in bytes)

     @throws IllegalArgumentException if offset is negative or if the data exceeds from the VBO (because of it's size or
     the offset)
     */
    @Bind
    public void store(@NotNull AIVector3D.Buffer data, long offset){
        checkRelease();
        checkBind();
        if(offset < 0){
            throw new IllegalArgumentException("Offset can't be negative");
        }
        if(getActiveDataSize() - offset < data.limit() * Utility.FLOAT_SIZE){
            throw new IllegalStateException("The data exceeds from the VBO, data size or offset is too high");
        }
        GL15.nglBufferSubData(getTarget(), offset, AIVector3D.SIZEOF * data.remaining(), data.address());
    }

    @Override
    public void bind(){
        super.bind();
        boundVbo = this;
    }

    @Override
    public void unbind(){
        super.unbind();
        boundVbo = null;
    }

    @Override
    public boolean isBound(){
        return this == getBoundVbo();
    }

    /**
     Returns the currently bound VBO.

     @return the currently bound VBO
     */
    @Nullable
    public static Vbo getBoundVbo(){
        return boundVbo;
    }

    @Override
    public void release(){
        super.release();
        for(VertexAttribArray vaa : vertexAttribArrays){
            vaa.getVao().removeVertexAttribArray(vaa.getPointer().getIndex());
        }
        vertexAttribArrays.clear();
        if(isBound()){
            boundVbo = null;
        }
    }

    @Override
    public String toString(){
        return super.toString() + "\n" + Vbo.class.getSimpleName() + "(" + "vertexAttribArrays: " + Utility
                .toString(vertexAttribArrays) + ")";
    }
}
