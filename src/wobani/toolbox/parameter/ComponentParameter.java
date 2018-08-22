package wobani.toolbox.parameter;

import wobani.component.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.exceptions.*;

/**
 Stores a Component as a Parameter.

 @param <T> stored data's type must extends the ComponentBase interface
 */
public class ComponentParameter<T extends ComponentBase> extends Parameter<T>{

    /**
     Initializes a new ComponentParameter to the given value.

     @param value data you want to store2D
     */
    public ComponentParameter(T value){
        super(value);
    }

    /**
     Returns the stored data.

     @return the stored data

     @throws ComponentAttachmentException component not attached to any GameObject
     */
    @Override
    public T getValue(){
        T ret = super.getValue();
        if(ret.getGameObject() == null){
            throw new ComponentAttachmentException(ret);
        }
        return ret;
    }

    @Internal
    @Override
    protected void addedToParameters(Parameter<T> removed){
        if(getValue().getGameObject() == null){
            throw new NullPointerException();
        }
        getValue().invalidate();
    }

    @Internal
    @Override
    protected void refresh(@NotNull T old){
        getValue().invalidate();
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(super.toString()).append("\n")
                .append(ComponentParameter.class.getSimpleName()).append("(").append(")");
        return res.toString();
    }
}
