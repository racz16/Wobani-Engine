package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.exceptions.*;

import java.util.*;

/**
 Object oriented wrapper class above the native Vertex Array Object.
 */
public class Vao extends OpenGlObject{
    /**
     The connected vertex attrib array.
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
     Initializes a new VAO to the given value.

     @param label label
     */
    public Vao(@NotNull String label){
        super();
        //FIXME: currently not working
        //setLabel(label);
    }

    @Override
    protected int createId(){
        return VAO_POOL.getResource();
    }

    @NotNull
    @Override
    protected ResourceId createResourceId(){
        return new ResourceId();
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
     Adds the given vertex attrib pointer to the VAO.

     @param vap vertex attrib pointer
     */
    @Bind
    public void setVertexAttribArray(@NotNull VertexAttribPointer vap){
        checkRelease();
        checkBind();
        Vbo vbo = Vbo.getBoundVbo();
        removeVertexAttribArray(vap.getIndex());
        if(vbo != null){
            setVertexAttribArrayUnsafe(vap, vbo);
        }
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
            vaa.getVbo().removeVertexAttribArray(vaa);
        }
    }

    /**
     Adds a vertex attrib array to the VAO with the given vertex attrib pointer and VBO.

     @param vap vertex attrib pointer
     @param vbo bound VBO
     */
    private void setVertexAttribArrayUnsafe(@NotNull VertexAttribPointer vap, @NotNull Vbo vbo){
        VertexAttribArray vaa = new VertexAttribArray(this, vbo, vap);
        vertexAttribArrays.put(vap.getIndex(), vaa);
        vbo.addVertexAttribArray(vaa);
        GL20.glVertexAttribPointer(vap.getIndex(), vap.getSize(), vap.getType().getCode(), vap.isNormalized(), vap
                .getStride(), vap.getPointer());
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

    /**
     If the VAO is not bound it throws a NotBoundException.

     @throws NotBoundException if the VAO is not bound
     */
    protected void checkBind(){
        if(!isBound()){
            throw new NotBoundException(this);
        }
    }

    //
    //EBO-----------------------------------------------------------------------
    //

    /**
     Sets the EBO to the given value.

     @param ebo ebo
     */
    @Internal
    void setEbo(@Nullable Ebo ebo){
        this.ebo = ebo;
    }

    /**
     Returns the VAO's EBO

     @return the VAO's EBO
     */
    @Nullable
    public Ebo getEbo(){
        return ebo;
    }

    //
    //misc----------------------------------------------------------------------
    //
    @Override
    protected int getType(){
        return GL11.GL_VERTEX_ARRAY;

    }

    /**
     Binds the VAO.
     */
    public void bind(){
        checkRelease();
        GL30.glBindVertexArray(getId());
        boundVao = this;
    }

    /**
     Unbinds the VAO.
     */
    public void unbind(){
        checkRelease();
        checkBind();
        GL30.glBindVertexArray(0);
        boundVao = null;
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
        return getId() != -1;
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
            vaa.getVbo().removeVertexAttribArray(vaa);
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
        setId(-1);
        if(isBound()){
            boundVao = null;
        }
    }

    @Override
    public int getCachedDataSize(){
        return 0;
    }

    @Override
    public int getActiveDataSize(){
        return 0;
    }

    @Override
    public void update(){

    }

    /**
     For creating VAOs efficiently.
     */
    private static class VaoPool extends ResourcePool{
        @Override
        protected void createResources(int[] resources){
            GL30.glGenVertexArrays(resources);
        }

        @Override
        public String toString(){
            return super.toString() + "\n" + VaoPool.class.getSimpleName() + "(" + ")";
        }
    }

    @Override
    public String toString(){
        return super.toString() + "\n" + Vao.class
                .getSimpleName() + "(" + "vertexAttribArrays: " + vertexAttribArrays + ", " + "ebo: " + ebo + ")";
    }
}
