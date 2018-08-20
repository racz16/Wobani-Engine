package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.*;
import wobani.toolbox.annotation.*;

/**
 Object oriented wrapper class above the native Element Buffer Object.
 */
public class Ebo extends BufferObject{

    /**
     The EBO's VAO.
     */
    private Vao vao;

    /**
     Initializes a new EBO.
     */
    public Ebo(){
        super(GL15.GL_ELEMENT_ARRAY_BUFFER);
    }

    /**
     Initializes a new EBO to the given value.

     @param label label
     */
    public Ebo(@NotNull String label){
        this();
        setLabel(label);
    }

    @NotNull
    @Override
    protected String getTypeName(){
        return "EBO";
    }

    /**
     Returns this EBO's VAO.

     @return this EBO's VAO
     */
    @Nullable
    public Vao getVao(){
        return vao;
    }

    /**
     Sets the VAO to the given value.

     @param vao connected VAO
     */
    protected void setVao(@Nullable Vao vao){
        checkConnection(vao);
        this.vao = vao;
    }

    /**
     If the EBO is already connected to a VAO, it throws an IllegalArgumentException.

     @throws IllegalArgumentException if the EBO is already connected to a VAO
     */
    private void checkConnection(@Nullable Vao vao){
        if(this.vao != null && vao != null && this.vao != vao){
            throw new IllegalArgumentException("The EBO is already connected to a VAO");
        }
    }

    @Override
    public void release(){
        super.release();
        if(vao != null){
            vao.setEbo(null);
            vao = null;
        }
    }

    @Override
    public String toString(){
        return super.toString() + "\n" +
                Ebo.class.getSimpleName() + "(" +
                "vao: " + vao + ")";
    }
}
