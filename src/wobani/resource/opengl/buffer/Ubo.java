package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 Object oriented wrapper class above the native Uniform Buffer Object.
 */
public class Ubo extends IndexBindableBufferObject{

    /**
     The currently bound UBO.
     */
    private static Ubo boundUbo;

    /**
     Initializes a new UBO.
     */
    public Ubo(){
        super(GL31.GL_UNIFORM_BUFFER);
    }

    /**
     Initializes a new UBO to the given value.

     @param label label
     */
    public Ubo(@NotNull String label){
        this();
        setLabel(label);
    }

    @NotNull
    @Override
    protected String getTypeName(){
        return "UBO";
    }

    /**
     Returns the currently bound UBO.

     @return the currently bound UBO
     */
    @Nullable
    public static Ubo getBoundUbo(){
        return boundUbo;
    }

    @Override
    public boolean isBound(){
        return this == getBoundUbo();
    }

    @Override
    protected int getMaxDataSize(){
        return OpenGlConstants.MAX_UNIFORM_BLOCK_SIZE;
    }

    @Override
    public void bind(){
        super.bind();
        boundUbo = this;
    }

    @Override
    public void unbind(){
        super.unbind();
        boundUbo = null;
    }

    @Override
    protected int getHighestValidBindingPoint(){
        return getAvailableBindingPointCount() - 1;
    }

    /**
     Returns the number of the valid binding points to the UBOs.

     @return the number of the valid binding points to the UBOs
     */
    public static int getAvailableBindingPointCount(){
        return OpenGlConstants.MAX_UNIFORM_BUFFER_BINDINGS;
    }

    @Override
    public void release(){
        super.release();
        if(isBound()){
            boundUbo = null;
        }
    }

    @Override
    public String toString(){
        return super.toString() + "\n" + Ubo.class.getSimpleName() + "(" + ")";
    }
}
