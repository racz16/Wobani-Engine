package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 Object oriented wrapper above the vertex attrib pointer.
 */
public class VertexAttribPointer{
    /**
     Index.
     */
    private int index;
    /**
     Size.
     */
    private int size = 4;
    /**
     Type.
     */
    private VertexAttribPointerType type = VertexAttribPointerType.FLOAT;
    /**
     Normalized.
     */
    private boolean normalized = false;
    /**
     Stride.
     */
    private int stride = 0;
    /**
     Pointer.
     */
    private int pointer = 0;

    /**
     Vertex attrib pointer type.
     */
    public enum VertexAttribPointerType{
        BYTE(GL11.GL_BYTE), UNSIGNED_BYTE(GL11.GL_UNSIGNED_BYTE), SHORT(GL11.GL_SHORT), UNSIGNED_SHORT(GL11.GL_UNSIGNED_SHORT), INT(GL11.GL_INT), UNSIGNED_INT(GL11.GL_UNSIGNED_INT), HALF_FLOAT(GL30.GL_HALF_FLOAT), FLOAT(GL11.GL_FLOAT), DOUBLE(GL11.GL_DOUBLE), UNSIGNED_INT_2_10_10_10_REV(GL12.GL_UNSIGNED_INT_2_10_10_10_REV), INT_2_10_10_10_REV(GL33.GL_INT_2_10_10_10_REV), FIXED(GL41.GL_FIXED);

        /**
         Vertex attrib pointer type's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new VertexAttribPointerType to the given value.

         @param code vertex attrib pointer type's OpenGL code
         */
        VertexAttribPointerType(int code){
            this.code = code;
        }

        /**
         Returns the VertexAttribPointerType of the given OpenGL code.

         @param code OpenGL vertex attrib pointer type

         @return the VertexAttribPointerType of the given OpenGL code

         @throws IllegalArgumentException the given parameter is not a vertex attrib pointer type
         */
        @NotNull
        public static VertexAttribPointerType valueOf(int code){
            for(VertexAttribPointerType mode : VertexAttribPointerType.values()){
                if(mode.getCode() == code){
                    return mode;
                }
            }
            throw new IllegalArgumentException("The given parameter is not a vertex attrib pointer type");
        }

        /**
         Returns the vertex attrib pointer type's OpenGL code.

         @return the vertex attrib pointer type's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }

    /**
     Initializes a new VertexAttribPointer to the given value.

     @param index index, must be lower than the maximum vertex attribs

     @see OpenGlConstants#MAX_VERTEX_ATTRIBS
     */
    public VertexAttribPointer(int index){
        setIndex(index);
    }

    /**
     Initializes a new VertexAttribPointer to the given values.

     @param index index, must be lower than the maximum vertex attribs
     @param size  size, must be in the (1;4) interval

     @see OpenGlConstants#MAX_VERTEX_ATTRIBS
     */
    public VertexAttribPointer(int index, int size){
        setIndex(index);
        setSize(size);
    }

    /**
     Initializes a new VertexAttribPointer to the given values.

     @param index      index, must be lower than the maximum vertex attribs
     @param size       size, must be in the (1;4) interval
     @param type       type
     @param normalized normalized
     @param stride     stride, can't be negative
     @param pointer    pointer, can't be negative

     @see OpenGlConstants#MAX_VERTEX_ATTRIBS
     */
    public VertexAttribPointer(int index, int size, @NotNull VertexAttribPointerType type, boolean normalized, int stride, int pointer){
        setIndex(index);
        setSize(size);
        setType(type);
        setNormalized(normalized);
        setStride(stride);
        setPointer(pointer);
    }

    /**
     Returns the index.

     @return index
     */
    public int getIndex(){
        return index;
    }

    /**
     Sets the index to the given value.

     @param index index, must be lower than the maximum vertex attribs

     @throws IllegalArgumentException if index is not lower than the maximum vertex attribs
     @see OpenGlConstants#MAX_VERTEX_ATTRIBS
     */
    private void setIndex(int index){
        if(index < 0 || index >= OpenGlConstants.MAX_VERTEX_ATTRIBS){
            throw new IllegalArgumentException("Index is not lower than the maximum vertex attribs");
        }
        this.index = index;
    }

    /**
     Returns the size

     @return size
     */
    public int getSize(){
        return size;
    }

    /**
     Sets the size to the given value.

     @param size size, must be in the (1;4) interval

     @throws IllegalArgumentException if size isn't in the (1;4) interval
     */
    private void setSize(int size){
        if(size < 1 || size > 4){
            throw new IllegalArgumentException("Size isn't in the (1;4) interval");
        }
        this.size = size;
    }

    /**
     Returns the type.

     @return type
     */
    @NotNull
    public VertexAttribPointerType getType(){
        return type;
    }

    /**
     Sets the type to the given value.

     @param type type

     @throws NullPointerException if the parameter is null
     */
    private void setType(@NotNull VertexAttribPointerType type){
        if(type == null){
            throw new NullPointerException();
        }
        this.type = type;
    }

    /**
     Determines whether the vertex attrib pointer is normalized.

     @return true if the vertex attrib pointer is normalized, false otherwise
     */
    public boolean isNormalized(){
        return normalized;
    }

    /**
     Sets whether or not the vertex attrib array is normalized.

     @param normalized true if this vertex attrib array should be normalized, false otherwise
     */
    private void setNormalized(boolean normalized){
        this.normalized = normalized;
    }

    /**
     Returns the stride.

     @return the stride
     */
    public int getStride(){
        return stride;
    }

    /**
     Sets stride to the given value.

     @param stride stride, can't be negative

     @throws IllegalArgumentException if stride is negative
     */
    private void setStride(int stride){
        if(stride < 0){
            throw new IllegalArgumentException("Stride is negative");
        }
        this.stride = stride;
    }

    /**
     Returns the pointer.

     @return the pointer
     */
    public int getPointer(){
        return pointer;
    }

    /**
     Sets pointer to the given value.

     @param pointer pointer, can't be negative

     @throws IllegalArgumentException if pointer is negative
     */
    private void setPointer(int pointer){
        if(pointer < 0){
            throw new IllegalArgumentException("Pointer is negative");
        }
        this.pointer = pointer;
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }

        VertexAttribPointer that = (VertexAttribPointer) o;

        if(index != that.index){
            return false;
        }
        if(size != that.size){
            return false;
        }
        if(normalized != that.normalized){
            return false;
        }
        if(stride != that.stride){
            return false;
        }
        if(pointer != that.pointer){
            return false;
        }
        return type == that.type;
    }

    @Override
    public int hashCode(){
        int result = index;
        result = 31 * result + size;
        result = 31 * result + type.hashCode();
        result = 31 * result + (normalized ? 1 : 0);
        result = 31 * result + stride;
        result = 31 * result + pointer;
        return result;
    }

    @Override
    public String toString(){
        return VertexAttribPointer.class
                .getSimpleName() + "(" + "index: " + index + ", " + "size: " + size + ", " + "type: " + type + ", " + "normalized: " + normalized + ", " + "stride: " + stride + ", " + "pointer: " + pointer + ")";
    }


}
