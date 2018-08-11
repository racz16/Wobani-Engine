package wobani.toolbox.exceptions;

import wobani.resource.*;
import wobani.toolbox.annotation.*;

/**
 Signs that the resource you wanted to use is already released and you can't use it.
 */
public class ReleasedException extends RuntimeException{
    /**
     The resource.
     */
    private final Resource resource;

    /**
     Initializes a new ReleasedException to the given parameter.

     @param resource the released resource
     */
    public ReleasedException(@Nullable Resource resource){
        this.resource = resource;
    }

    /**
     Returns the released resource.

     @return the released resource
     */
    public Resource getReleasedResource(){
        return resource;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(ReleasedException.class.getSimpleName()).append("(")
                .append(" released resource: ").append(resource).append(")");
        return res.toString();
    }
}
