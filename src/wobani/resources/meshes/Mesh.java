package wobani.resources.meshes;

import wobani.resources.*;

/**
 * Interface for meshes.
 */
public interface Mesh extends Renderable {

    /**
     * Returns the mseh's triangle count.
     *
     * @return triangle count
     */
    public int getFaceCount();

}
