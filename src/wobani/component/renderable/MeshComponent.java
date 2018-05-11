package wobani.component.renderable;

import wobani.material.*;
import wobani.resources.mesh.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 * Contains a Mesh what you can render.
 */
public class MeshComponent extends RenderableComponent<Mesh> {

    /**
     * Determines whether the Mesh rendered two sided.
     */
    private boolean twoSided;

    /**
     * Initializes a new MeshComponent to the given value.
     *
     * @param mesh Mesh
     */
    public MeshComponent(@NotNull Mesh mesh) {
	super(mesh);
    }

    /**
     * Initializes a new MeshComponent to the given values.
     *
     * @param mesh     Mesh
     * @param material Material
     */
    public MeshComponent(@NotNull Mesh mesh, @NotNull Material material) {
	super(mesh, material);
    }

    /**
     * Determines whether the Mesh rendered two sided.
     *
     * @return true if the Mesh rendered two sided, false otherwise
     */
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

    @Override
    public int hashCode() {
	int hash = 5 + super.hashCode();
	hash = 97 * hash + (this.twoSided ? 1 : 0);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (!super.equals(obj)) {
	    return false;
	}
	final MeshComponent other = (MeshComponent) obj;
	if (this.twoSided != other.twoSided) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	StringBuilder res = new StringBuilder()
		.append(super.toString()).append("\n")
		.append(MeshComponent.class.getSimpleName()).append("(")
		.append(")");
	return res.toString();
    }

}
