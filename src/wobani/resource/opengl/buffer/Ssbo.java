package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 Object oriented wrapper class above the native Shader Storage Buffer Object.
 */
public class Ssbo extends IndexBindableBufferObject{

    /**
     The currently bound SSBO.
     */
    private static Ssbo boundSsbo;

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

    /**
     Returns the currently bound SSBO.

     @return the currently bound SSBO
     */
    @Nullable
    public static Ssbo getBoundSsbo(){
        return boundSsbo;
    }

    @Override
    public boolean isBound(){
        return this == getBoundSsbo();
    }

    @Override
    protected int getMaxDataSize(){
        return OpenGlConstants.MAX_SHADER_STORAGE_BLOCK_SIZE;
    }

    @Override
    public void bind(){
        super.bind();
        boundSsbo = this;
    }

    @Override
    public void unbind(){
        super.unbind();
        boundSsbo = null;
    }

    @Override
    protected int getHighestValidBindingPoint(){
        return getAvailableBindingPointCount() - 1;
    }

    /**
     Returns the number of the valid binding points to the SSBOs.

     @return the number of the valid binding points to the SSBOs
     */
    public static int getAvailableBindingPointCount(){
        return OpenGlConstants.MAX_SHADER_STORAGE_BUFFER_BINDINGS;
    }

    @Override
    public void release(){
        super.release();
        if(isBound()){
            boundSsbo = null;
        }
    }

    @Override
    public String toString(){
        return super.toString() + "\n" + Ssbo.class.getSimpleName() + "(" + ")";
    }
}
