package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 Object oriented wrapper class above the native Shader Storage Buffer Object.
 */
public class Ssbo extends IndexBindableBufferObject{

    /**
     Initializes a new SSBO.
     */
    public Ssbo(){
        super(GL43.GL_SHADER_STORAGE_BUFFER);
    }

    /**
     Initializes a new SSBO to the given value.

     @param label label
     */
    public Ssbo(@NotNull String label){
        this();
        setLabel(label);
    }

    @NotNull
    @Override
    protected String getTypeName(){
        return "SSBO";
    }

    @Override
    public int getMaxDataSize(){
        return OpenGlConstants.MAX_SHADER_STORAGE_BLOCK_SIZE;
    }

    public int getMaxDataSizeSafe(){
        return OpenGlConstants.MAX_SHADER_STORAGE_BLOCK_SIZE_SAFE;
    }

    @Override
    protected int getHighestValidBindingPoint(){
        return getMaxBindingPointCount() - 1;
    }

    /**
     Returns the number of the valid binding points to the SSBOs.

     @return the number of the valid binding points to the SSBOs
     */
    public static int getMaxBindingPointCount(){
        return OpenGlConstants.MAX_SHADER_STORAGE_BUFFER_BINDINGS;
    }

    public static int getMaxBindingPointCountSafe(){
        return OpenGlConstants.MAX_SHADER_STORAGE_BUFFER_BINDINGS_SAFE;
    }

    @Override
    public String toString(){
        return super.toString() + "\n" +
                Ssbo.class.getSimpleName() + "(" + ")";
    }
}
