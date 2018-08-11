package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.*;
import wobani.toolbox.annotation.*;

/**
 Object oriented wrapper class above the native Element Buffer Object.
 */
public class Ebo extends BufferObject{

    /**
     The currently bound EBO.
     */
    private static Ebo boundEbo;
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
     Binds the EBO and connects to the currently bound VAO.
     */
    @Override
    public void bind(){
        super.bind();
        boundEbo = this;
        removeFromPreviousVao();
        addToBoundVao();
    }

    /**
     Removes this EBO from it's VAO.
     */
    private void removeFromPreviousVao(){
        if(vao != null){
            vao.setEbo(null);
            this.vao = null;
        }
    }

    /**
     Adds this EBO to the currently bound VAO.
     */
    private void addToBoundVao(){
        Vao vao = Vao.getBoundVao();
        if(vao != null){
            vao.setEbo(this);
            this.vao = vao;
        }
    }

    @Override
    public void unbind(){
        super.unbind();
        boundEbo = null;
    }

    @Override
    public boolean isBound(){
        return this == getBoundEbo();
    }

    /**
     Returns the currently bound EBO.

     @return the currently bound EBO
     */
    @Nullable
    public static Ebo getBoundEbo(){
        return boundEbo;
    }

    @Override
    public void release(){
        super.release();
        removeFromPreviousVao();
        if(isBound()){
            boundEbo = null;
        }
    }

    @Override
    public String toString(){
        return super.toString() + "\n" + Ebo.class.getSimpleName() + "(" + ")";
    }
}
