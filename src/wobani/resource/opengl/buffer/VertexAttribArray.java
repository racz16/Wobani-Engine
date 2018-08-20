package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.exceptions.*;

/**
 Object oriented wrapper above the native vertex attrib array.
 */
public class VertexAttribArray{
    /**
     VAO.
     */
    private Vao vao;
    /**
     VBO.
     */
    private Vbo vbo;
    /**
     Vertex attrib pointer.
     */
    private VertexAttribPointer pointer;
    /**
     Determines whether the vertex attrib array is enabled.
     */
    private boolean enabled;

    /**
     Initializes a new VertexAttribArray to the given values.

     @param vao     vao
     @param vbo     vbo
     @param pointer vertex attrib pointer
     */
    public VertexAttribArray(@NotNull Vao vao, @NotNull Vbo vbo, @NotNull VertexAttribPointer pointer){
        setVao(vao);
        setVbo(vbo);
        setPointer(pointer);
    }

    /**
     Returns the VAO.

     @return the VAO
     */
    @NotNull
    public Vao getVao(){
        return vao;
    }

    /**
     Sets the VAO to the given value.

     @param vao vao

     @throws NullPointerException if the parameter is null
     */
    private void setVao(@NotNull Vao vao){
        if(vao == null){
            throw new NullPointerException();
        }
        this.vao = vao;
    }

    /**
     Returns the VBO.

     @return the VBO
     */
    @NotNull
    public Vbo getVbo(){
        return vbo;
    }

    /**
     Sets the VBO to the given value.

     @param vbo vbo

     @throws NullPointerException if the parameter is null
     */
    private void setVbo(@NotNull Vbo vbo){
        if(vbo == null){
            throw new NullPointerException();
        }
        this.vbo = vbo;
    }

    /**
     Returns the vertex attrib pointer.

     @return the vertex attrib pointer
     */
    @NotNull
    public VertexAttribPointer getPointer(){
        return pointer;
    }

    /**
     Sets the vertex attrib pointer to the given value.

     @param pointer vertex attrib pointer

     @throws NullPointerException if the parameter is null
     */
    private void setPointer(VertexAttribPointer pointer){
        if(pointer == null){
            throw new NullPointerException();
        }
        this.pointer = pointer;
    }

    /**
     Enables the vertex attrib array.
     */
    public void enable(){
        checkRelease();
        GL45.glEnableVertexArrayAttrib(vao.getId(), pointer.getIndex());
        enabled = true;
    }

    /**
     Disables the vertex attrib array.
     */
    public void disable(){
        checkRelease();
        GL45.glDisableVertexArrayAttrib(vao.getId(), pointer.getIndex());
        enabled = false;
    }

    /**
     Determines whether the vertex attrib array is enabled.

     @return true if the vertex attrib array is enabled, false otherwise
     */
    public boolean isEnabled(){
        return enabled;
    }

    /**
     If the VAO or the VBO is released it throws a ReleasedException.

     @throws ReleasedException if the VAO or the VBO is released
     */
    private void checkRelease(){
        if(!Utility.isUsable(vao)){
            throw new ReleasedException(vao);
        }else if(!Utility.isUsable(vbo)){
            throw new ReleasedException(vbo);
        }
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }

        VertexAttribArray that = (VertexAttribArray) o;

        if(enabled != that.enabled){
            return false;
        }
        if(!vao.equals(that.vao)){
            return false;
        }
        if(!vbo.equals(that.vbo)){
            return false;
        }
        return pointer.equals(that.pointer);
    }

    @Override
    public int hashCode(){
        int result = vao.hashCode();
        result = 31 * result + vbo.hashCode();
        result = 31 * result + pointer.hashCode();
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }

    @Override
    public String
    toString(){
        return VertexAttribArray.class.getSimpleName() + "(" +
                "vao: " + vao + ", " +
                "vbo: " + vbo + ", " +
                "pointer: " + pointer + ", " +
                "enabled: " + enabled + ")";
    }

}
