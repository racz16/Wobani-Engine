package wobani.component.renderable;

import wobani.toolbox.annotation.NotNull;
import wobani.material.Material;
import java.util.*;
import wobani.core.*;
import wobani.rendering.*;
import wobani.rendering.geometry.*;
import wobani.resources.*;

/**
 * Contains a Renderable what you can render.
 *
 * @param <T> type
 */
public abstract class RenderableComponent<T extends Renderable> extends Component {

    /**
     * Renderable to render.
     */
    private T renderable;
    /**
     * The Renderable's Material.
     */
    private Material material;
    /**
     * Determines whether the Renderable is active.
     */
    private boolean renderableActive = true;
    /**
     * Determines whether the Material is active.
     */
    private boolean materialActive = true;
    /**
     * Determines whether the Renderable casts shadow.
     */
    private boolean castShadow = true;
    /**
     * Determines whether the Renderable receives shadows.
     */
    private boolean receiveShadow = true;
    /**
     * Determines whether the Component is reflectable.
     */
    private boolean reflectable;
    /**
     * The Renderable's bounding shapes.
     */
    private final RenderableBoundingShape boundingShape;

    /**
     * Initializes a new RenderableComponent to the given value.
     *
     * @param renderable Renderable
     */
    public RenderableComponent(@NotNull T renderable) {
	setRenderable(renderable);
	setMaterial(new Material(BlinnPhongRenderer.class));
	boundingShape = new RenderableBoundingShape(this);
	addInvalidatable(boundingShape);
    }

    /**
     * Initializes a new RenderableComponent to the given values.
     *
     * @param renderable Renderable
     * @param material   Renderable's material
     */
    public RenderableComponent(@NotNull T renderable, @NotNull Material material) {
	setRenderable(renderable);
	setMaterial(material);
	boundingShape = new RenderableBoundingShape(this);
	addInvalidatable(boundingShape);
    }

    /**
     * Returns the Renderable.
     *
     * @return Renderable
     */
    @NotNull
    public T getRenderable() {
	return renderable;
    }

    /**
     * Sets the Renderable to the given value.
     *
     * @param renderable Renderable
     *
     * @throws NullPointerException parameter can't be null
     */
    public void setRenderable(@NotNull T renderable) {
	if (renderable == null) {
	    throw new NullPointerException();
	}
	Renderable old = this.renderable;
	this.renderable = renderable;
	RenderingPipeline.getRenderableComponents().refreshRenderableChange(this, old);
	invalidate();
    }

    /**
     * Returns the Renderable's Material.
     *
     * @return Material
     */
    @NotNull
    public Material getMaterial() {
	return material;
    }

    /**
     * Sets the Material to the givan value.
     *
     * @param material Material
     *
     * @throws NullPointerException parameter can't be null
     */
    public void setMaterial(@NotNull Material material) {
	if (material == null) {
	    throw new NullPointerException();
	}
	Material old = this.material;
	this.material = material;
	RenderingPipeline.getRenderableComponents().refreshMaterialChange(this, old);
    }

    /**
     * Returns the Renderable's bounding shapes.
     *
     * @return the Renderable's bounding shapes
     */
    @NotNull
    public RenderableBoundingShape getBoundingShape() {
	return boundingShape;
    }

    /**
     * Determines whether the Renderable is reflectable.
     *
     * @return true if the Renderable is reflectable, false otherwise
     */
    public boolean isReflectable() {
	return reflectable;
    }

    /**
     * Sets whether or not the Renderable is reflectable.
     *
     * @param reflectable true if the Renderable is reflectable, false otherwise
     */
    public void setReflectable(boolean reflectable) {
	this.reflectable = reflectable;
    }

    /**
     * Determines whether the Renderable casts shadow.
     *
     * @return true if the Renderable casts shadow, false otherwise
     */
    public boolean isCastShadow() {
	return castShadow;
    }

    /**
     * Sets whether or not the Renderable casts shadow.
     *
     * @param castShadow true if the Renderable should cast shadows, false
     *                   otherwise
     */
    public void setCastShadow(boolean castShadow) {
	this.castShadow = castShadow;
    }

    /**
     * Determines whether the Renderable receives shadows.
     *
     * @return true if the Renderable receives shadows, false otherwise
     */
    public boolean isReceiveShadows() {
	return receiveShadow;
    }

    /**
     * Sets whether or not the Renderable receives shadows.
     *
     * @param receiveShadows true if the Renderable should receive shadows,
     *                       false otherwise
     */
    public void setReceiveShadows(boolean receiveShadows) {
	this.receiveShadow = receiveShadows;
    }

    /**
     * Determines whether the Renderable is active.
     *
     * @return true if the Renderable is active, false otherwise
     */
    public boolean isRenderableActive() {
	return renderableActive;
    }

    /**
     * Sets whether or not the Renderable is active.
     *
     * @param renderableActive true if the Renderable is active, false otherwise
     */
    public void setRenderableActive(boolean renderableActive) {
	this.renderableActive = renderableActive;
    }

    /**
     * Determines whether the Material is active.
     *
     * @return true if the Material is active, false otherwise
     */
    public boolean isMaterialActive() {
	return materialActive;
    }

    /**
     * Sets whether or not the Material is active.
     *
     * @param materialActive true if the Material is active, false otherwise
     */
    public void setMaterialActive(boolean materialActive) {
	this.materialActive = materialActive;
    }

    /**
     * Return the number of consisted faces.
     *
     * @return the number of consisted faces
     */
    public abstract int getFaceCount();

    /**
     * Draws the Renderable.
     */
    public void draw() {
	renderable.draw();
    }

    @Override
    protected void detachFromGameObject() {
	getGameObject().getTransform().removeInvalidatable(this);
	super.detachFromGameObject();
	RenderingPipeline.getRenderableComponents().remove(this);
	invalidate();
    }

    @Override
    protected void attachToGameObject(@NotNull GameObject object) {
	super.attachToGameObject(object);
	getGameObject().getTransform().addInvalidatable(this);
	RenderingPipeline.getRenderableComponents().add(this);
	invalidate();
    }

    @Override
    public int hashCode() {
	int hash = 7 + super.hashCode();
	hash = 47 * hash + Objects.hashCode(this.renderable);
	hash = 47 * hash + Objects.hashCode(this.material);
	hash = 47 * hash + (this.renderableActive ? 1 : 0);
	hash = 47 * hash + (this.materialActive ? 1 : 0);
	hash = 47 * hash + (this.castShadow ? 1 : 0);
	hash = 47 * hash + (this.receiveShadow ? 1 : 0);
	hash = 47 * hash + (this.reflectable ? 1 : 0);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (!super.equals(obj)) {
	    return false;
	}
	final RenderableComponent<?> other = (RenderableComponent<?>) obj;
	if (this.renderableActive != other.renderableActive) {
	    return false;
	}
	if (this.materialActive != other.materialActive) {
	    return false;
	}
	if (this.castShadow != other.castShadow) {
	    return false;
	}
	if (this.receiveShadow != other.receiveShadow) {
	    return false;
	}
	if (this.reflectable != other.reflectable) {
	    return false;
	}
	if (!Objects.equals(this.renderable, other.renderable)) {
	    return false;
	}
	if (!Objects.equals(this.material, other.material)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	StringBuilder res = new StringBuilder()
		.append(super.toString()).append("\n")
		.append("RenderableComponent(")
		.append(" faces: ").append(getFaceCount())
		.append(")");
	return res.toString();
    }

}
