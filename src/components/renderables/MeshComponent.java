package components.renderables;

import materials.*;
import resources.meshes.*;
import toolbox.*;
import toolbox.annotations.*;

public class MeshComponent extends RenderableComponent<Mesh> {

    /**
     * Determines whether the Mesh rendered two sided.
     */
    private boolean twoSided;

    public MeshComponent(@NotNull Mesh mesh) {
        super(mesh);
    }

    public MeshComponent(@NotNull Mesh mesh, @NotNull Material material) {
        super(mesh, material);
    }

    /**
     * Determines whether the Mesh rendered two sided.
     *
     * @return true if the Mesh rendered two sided, false otherwise
     */
    @Override
    public boolean isTwoSided() {
        return twoSided;
    }

    /**
     * Sets whether or not the Mesh rendered two sided.
     *
     * @param twoSided true if the Mesh should be rendered two sided, false
     *                 otherwise
     */
    public void setTwoSided(boolean twoSided) {
        this.twoSided = twoSided;
    }

    @Override
    public int getFaceCount() {
        return getRenderable().getFaceCount();
    }

    @Override
    public void draw() {
        if (!isTwoSided()) {
            OpenGl.setFaceCulling(true);
        } else {
            OpenGl.setFaceCulling(false);
        }
        super.draw();
    }

}
