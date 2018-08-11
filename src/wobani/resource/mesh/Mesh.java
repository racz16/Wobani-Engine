package wobani.resource.mesh;

import wobani.resource.*;

/**
 Interface for meshes.
 */
public interface Mesh extends Renderable{

    /**
     Returns the mseh's triangle count.

     @return triangle count
     */
    public int getFaceCount();

}
