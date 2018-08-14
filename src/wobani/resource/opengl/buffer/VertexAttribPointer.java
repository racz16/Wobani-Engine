package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 Object oriented wrapper above the vertex attrib relativeOffset.
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
     Offset.
     */
    private int offset = 0;
    /**
     Relative offset.
     */
    private int relativeOffset = 0;

    /**
     Vertex attrib relativeOffset type.
     */
    public enum VertexAttribPointerType{
        /**
         Byte.
         */
        BYTE(GL11.GL_BYTE),
        /**
         Unsigned byte.
         */
        UNSIGNED_BYTE(GL11.GL_UNSIGNED_BYTE),
        /**
         Short.
         */
        SHORT(GL11.GL_SHORT),
        /**
         Unsigned short.
         */
        UNSIGNED_SHORT(GL11.GL_UNSIGNED_SHORT),
        /**
         Integer.
         */
        INT(GL11.GL_INT),
        /**
         Unsigned integer.
         */
        UNSIGNED_INT(GL11.GL_UNSIGNED_INT),
        /**
         Half float.
         */
        HALF_FLOAT(GL30.GL_HALF_FLOAT),
        /**
         Float.
         */
        FLOAT(GL11.GL_FLOAT),
        /**
         Double.
         */
        DOUBLE(GL11.GL_DOUBLE),
        /**
         Unsigned integer 2-10-10-10.
         */
        UNSIGNED_INT_2_10_10_10_REV(GL12.GL_UNSIGNED_INT_2_10_10_10_REV),
        /**
         Integer 2-10-10-10.
         */
        INT_2_10_10_10_REV(GL33.GL_INT_2_10_10_10_REV),
        /**
         Fixed.
         */
        FIXED(GL41.GL_FIXED);

        /**
         Vertex attrib array type's OpenGL code.
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
         Returns the vertex attrib array pointer's OpenGL code.

         @return the vertex attrib array pointer's OpenGL code
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

     @param index          index, must be lower than the maximum vertex attribs
     @param size           size, must be in the (1;4) interval
     @param type           type
     @param normalized     normalized
     @param offset         offset, can't be negative
     @param relativeOffset relative offset, can't be negative

     @see OpenGlConstants#MAX_VERTEX_ATTRIBS
     */
    public VertexAttribPointer(int index, int size, @NotNull VertexAttribPointerType type, boolean normalized, int offset, int relativeOffset){
        setIndex(index);
        setSize(size);
        setType(type);
        setNormalized(normalized);
        setOffset(offset);
        setRelativeOffset(relativeOffset);
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
     Sets whether or not the vertex attrib pointer is normalized.

     @param normalized true if this vertex attrib pointer should be normalized, false otherwise
     */
    private void setNormalized(boolean normalized){
        this.normalized = normalized;
    }

    /**
     Returns the offset.

     @return the offset
     */
    public int getOffset(){
        return offset;
    }

    /**
     Sets offset to the given value.

     @param offset offset, can't be negative

     @throws IllegalArgumentException if offset is negative
     */
    private void setOffset(int offset){
        if(offset < 0){
            throw new IllegalArgumentException("Offset is negative");
        }
        this.offset = offset;
    }

    /**
     Returns the relative offset.

     @return the relative offset
     */
    public int getRelativeOffset(){
        return relativeOffset;
    }

    /**
     Sets relative offset to the given value.

     @param relativeOffset relative offset, can't be negative

     @throws IllegalArgumentException if relative offset is negative
     */
    private void setRelativeOffset(int relativeOffset){
        if(relativeOffset < 0){
            throw new IllegalArgumentException("Relative offset is negative");
        }
        this.relativeOffset = relativeOffset;
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
        if(offset != that.offset){
            return false;
        }
        if(relativeOffset != that.relativeOffset){
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
        result = 31 * result + offset;
        result = 31 * result + relativeOffset;
        return result;
    }

    @Override
    public String toString(){
        return VertexAttribPointer.class
                .getSimpleName() + "(" + "index: " + index + ", " + "size: " + size + ", " + "type: " + type + ", " + "normalized: " + normalized + ", " + "offset: " + offset + ", " + "relativeOffset: " + relativeOffset + ")";
    }


}
