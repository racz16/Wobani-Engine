package wobani.toolbox.exceptions;

import wobani.resource.*;
import wobani.toolbox.annotation.*;

/**
 Signs that you didn't bind a resource before you tried to execute an operation which needs the resource to be bound.
 */
public class NotBoundException extends RuntimeException{

    /**
     The resource.
     */
    private final Resource resource;

    /**
     Initializes a new NotBoundException to the given parameter.

     @param resource the not bound resource
     */
    public NotBoundException(@Nullable Resource resource){
        this.resource = resource;
    }

    /**
     Returns the not bound resource.

     @return the not bound resource
     */
    public Resource getNotBoundResource(){
        return resource;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(NotBoundException.class.getSimpleName()).append("(")
                .append(" resource: ").append(resource).append(")");
        return res.toString();
    }

}
