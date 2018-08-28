package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.exceptions.*;

import java.util.*;

import static wobani.resource.opengl.OpenGlHelper.*;

/**
 Object oriented wrapper class above the native Vertex Array Object.
 */
public class Vao extends OpenGlObject{
    /**
     The connected vertex attrib arrays.
     */
    private final Map<Integer, VertexAttribArray> vertexAttribArrays = new HashMap<>();
    /**
     The connected EBO.
     */
    private Ebo ebo;
    /**
     The currently bound VAO.
     */
    private static Vao boundVao;
    /**
     For creating VAOs efficiently.
     */
    private static final VaoPool VAO_POOL = new VaoPool();

    /**
     Initializes a new Vao.
     */
    public Vao(){
        super(new ResourceId());
        setId(createId());
    }

    /**
     Initializes a new Vao to the given value.

     @param label label
     */
    public Vao(@NotNull String label){
        this();
        setLabel(label);
    }

    protected int createId(){
        return VAO_POOL.getResource();
    }

    /**
     Returns the VAO Pool's maximum size. When you create a new VAO the system first tries to get one from the VAO Pool.
     If it's empty it fills the pool with max pool size number of VAOs.
     */
    public static int getMaxPoolSize(){
        return VAO_POOL.getMaxPoolSize();
    }

    /**
     Sets the VAO Pool's maximum size. When you create a new VAO the system first tries to get one from the VAO Pool. If
     it's empty it fills the pool with max pool size number of VAOs.

     @param size Buffer Object Pool's maximum size
     */
    public static void setMaxPoolSize(int size){
        VAO_POOL.setMaxPoolSize(size);
    }

    //
    //VBO-----------------------------------------------------------------------
    //

    /**
     Connects the given VBO with the vertex attrib pointer to this VAO.

     @param vbo VBO
     @param vap vertex attrib pointer
     */
    public void connectVbo(@NotNull Vbo vbo, @NotNull VertexAttribPointer vap){
        //FIXME: interleaved data may not work, try it with QuadMesh
        exceptionIfNotAvailable(this);
        exceptionIfNotAvailable(vbo);
        removeVertexAttribArray(vap.getIndex());
        VertexAttribArray vaa = new VertexAttribArray(this, vbo, vap);
        vertexAttribArrays.put(vap.getIndex(), vaa);
        vbo.setVertexAttribArray(vaa);
        connectVboUnsafe(vaa, vap);
    }

    /**
     Connects the given vertex attrib array with the vertex attrib pointer to this VAO.

     @param vaa vertex attrib array
     @param vap vertex attrib pointer
     */
    private void connectVboUnsafe(@NotNull VertexAttribArray vaa, @NotNull VertexAttribPointer vap){
        vaa.enable();
        GL45.glVertexArrayVertexBuffer(getId(), vap.getIndex(), vaa.getVbo().getId(), vap.getOffset(), vap
                .getSize() * 4);
        GL45.glVertexArrayAttribFormat(getId(), vap.getIndex(), vap.getSize(), vap.getType().getCode(), vap
                .isNormalized(), vap.getRelativeOffset());
        GL45.glVertexArrayAttribBinding(getId(), vap.getIndex(), vap.getIndex());
    }

    /**
     Removes the specified vertex attrib array from the VAO.

     @param index vertex attrib array's index
     */
    @Internal
    void removeVertexAttribArray(int index){
        VertexAttribArray vaa = vertexAttribArrays.get(index);
        if(vaa != null){
            vertexAttribArrays.remove(index);
            vaa.getVbo().setVertexAttribArray(null);
        }
    }

    /**
     Returns the specified vertex attrib array.

     @param index vertex attrib array's index

     @return the specified vertex attrib array
     */
    @NotNull
    public VertexAttribArray getVertexAttribArray(int index){
        return vertexAttribArrays.get(index);
    }

    /**
     Returns the collection of the vertex attrib array indices of the VAO.

     @return the collection of the vertex attrib array indices of the VAO
     */
    @NotNull
    @ReadOnly
    public Collection<Integer> getVertexAttribArrayIndices(){
        return Collections.unmodifiableCollection(vertexAttribArrays.keySet());
    }

    //
    //EBO---------------------------------------------------------------------------------------------------------------
    //

    /**
     Connects the given EBO to this VAO.

     @param ebo EBO
     */
    public void connectEbo(@NotNull Ebo ebo){
        exceptionIfNotAvailable(this);
        exceptionIfNotAvailable(ebo);
        if(Utility.isUsable(this.ebo)){
            this.ebo.setVao(null);
        }
        ebo.setVao(this);
        this.ebo = ebo;
        GL45.glVertexArrayElementBuffer(getId(), ebo.getId());
    }

    /**
     Returns the VAO's EBO

     @return the VAO's EBO
     */
    @Nullable
    public Ebo getEbo(){
        return ebo;
    }

    /**
     Sets the EBO to the given value.

     @param ebo ebo
     */
    @Internal
    void setEbo(@Nullable Ebo ebo){
        this.ebo = ebo;
    }

    //
    //misc--------------------------------------------------------------------------------------------------------------
    //
    @Override
    protected int getType(){
        return GL11.GL_VERTEX_ARRAY;
    }

    /**
     Binds the VAO.
     */
    public void bind(){
        exceptionIfNotAvailable(this);
        GL30.glBindVertexArray(getId());
        boundVao = this;
    }

    /**
     Unbinds the VAO.
     */
    public void unbind(){
        exceptionIfNotAvailable(this);
        checkBind();
        GL30.glBindVertexArray(0);
        boundVao = null;
    }

    /**
     If the VAO is not bound it throws a NotBoundException.

     @throws NotBoundException if the VAO is not bound
     */
    protected void checkBind(){
        if(!isBound()){
            throw new NotBoundException(this);
        }
    }

    /**
     Returns the currently bound VAO.

     @return the currently bound VAO
     */
    @Nullable
    public static Vao getBoundVao(){
        return boundVao;
    }

    /**
     Determines whether this VAO is bound.

     @return true if this VAO is bound, false otherwise
     */
    public boolean isBound(){
        return this == getBoundVao();
    }

    @NotNull
    @Override
    protected String getTypeName(){
        return "VAO";
    }

    /**
     Determines whether this VAO is usable.

     @return true if usable, false otherwise
     */
    @Override
    public boolean isUsable(){
        return isAvailable();
    }

    @Override
    public int getCacheDataSize(){
        return 0;
    }

    @Override
    public void release(){
        removeVertexAttribArrays();
        releaseEbo();
        releaseVao();
    }

    /**
     Removes the VAO's vertex attrib arrays.
     */
    private void removeVertexAttribArrays(){
        for(VertexAttribArray vaa : vertexAttribArrays.values()){
            vaa.getVbo().setVertexAttribArray(null);
        }
        vertexAttribArrays.clear();
    }

    /**
     Releases the EBO.
     */
    private void releaseEbo(){
        if(Utility.isUsable(ebo)){
            ebo.release();
            ebo = null;
        }
    }

    /**
     Releases the VAO.
     */
    private void releaseVao(){
        GL30.glDeleteVertexArrays(getId());
        setIdToInvalid();
        if(isBound()){
            boundVao = null;
        }
    }

    @Override
    public String toString(){
        //FIXME: stackoverflow
        return super.toString() + "\n" +
                Vao.class.getSimpleName() + "(" +
                "vertexAttribArrays: " + vertexAttribArrays + ", " +
                "ebo: " + ebo + ")";
    }

    /**
     For creating VAOs efficiently.
     */
    private static class VaoPool extends ResourcePool{
        @Override
        protected void createResources(int[] resources){
            GL45.glCreateVertexArrays(resources);
        }

        @Override
        public String toString(){
            return super.toString() + "\n" +
                    VaoPool.class.getSimpleName() + "(" + ")";
        }
    }

}
