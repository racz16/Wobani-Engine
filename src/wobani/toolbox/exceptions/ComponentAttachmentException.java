package wobani.toolbox.exceptions;

import wobani.component.*;
import wobani.toolbox.annotation.*;

/**
 This exception signs that a Component is attached to a GameObject or not attached to a GameObject and the function which
 throws this exception wants the opposite.
 */
public class ComponentAttachmentException extends RuntimeException{

    /**
     The Component caused the exception.
     */
    private final ComponentBase component;

    /**
     Initializes a new ComponentAttachmentException to the given value.

     @param component the Component caused the exception
     */
    public ComponentAttachmentException(@Nullable ComponentBase component){
        this.component = component;
    }

    /**
     Returns the Component caused the exception.

     @return the Component
     */
    @Nullable
    public ComponentBase getComponent(){
        return component;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(ComponentAttachmentException.class.getSimpleName()).append("(")
                .append(" component: ").append(component).append(")");
        return res.toString();
    }
}
