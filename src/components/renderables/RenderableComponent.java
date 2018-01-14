package components.renderables;

import core.*;
import materials.*;
import org.joml.*;
import rendering.geometry.*;
import resources.*;
import toolbox.annotations.*;

public abstract class RenderableComponent<T extends Renderable> extends Component {

    /**
     * Mesh to render.
     */
    private T renderable;
    /**
     * The Mesh's Material.
     */
    private Material material;
    /**
     * Determines whether the Mesh is active.
     */
    private boolean renderableActive = true;
    /**
     * Determines whether the Material is active.
     */
    private boolean materialActive = true;
    /**
     * Determines whether the Mesh casts shadow.
     *
     * @see Settings#isShadowMapping()
     */
    private boolean castShadow = true;
    /**
     * Determines whether the Mesh receives shadows.
     *
     * @see Settings#isShadowMapping()
     */
    private boolean receiveShadow = true;
    /**
     * The renderable's original axis alligned bouning box's minimum values.
     */
    private final Vector3f originalAabbMin = new Vector3f();
    /**
     * The renderable's calculated axis alligned bouning box's minimum values.
     */
    private final Vector3f aabbMin = new Vector3f();
    /**
     * The renderable's original axis alligned bouning box's maximum values.
     */
    private final Vector3f originalAabbMax = new Vector3f();
    /**
     * The renderable's calculated axis alligned bouning box's maximum values.
     */
    private final Vector3f aabbMax = new Vector3f();
    /**
     * The renderable's original furthest vertex distance value.
     */
    private float originalRadius;
    /**
     * The renderable's calculated furthest vertex distance value.
     */
    private float radius;
    /**
     * Determines whether the Component is valid.
     */
    private boolean valid;

    private boolean reflectable;

    public boolean isReflectable() {
        return reflectable;
    }

    public void setReflectable(boolean ref) {
        reflectable = ref;
    }

    /**
     * Initializes a new MeshComponent to the given value.
     *
     * @param renderable renderable
     */
    public RenderableComponent(@NotNull T renderable) {
        setRenderable(renderable);
        setMaterial(new Material(BlinnPhongRenderer.class));
    }

    /**
     * Initializes a new MeshComponent to the given values.
     *
     * @param renderable renderable
     * @param material   renderable's material
     */
    public RenderableComponent(@NotNull T renderable, @NotNull Material material) {
        setRenderable(renderable);
        setMaterial(material);
    }

    /**
     * Returns the Mesh.
     *
     * @return renderable
     */
    @NotNull
    public T getRenderable() {
        return renderable;
    }

    /**
     * Sets the Mesh to the given value.
     *
     * @param renderable renderable
     *
     * @throws NullPointerException renderable can't be null
     */
    public void setRenderable(@NotNull T renderable) {
        if (renderable == null) {
            throw new NullPointerException();
        }
        Renderable old = this.renderable;
        this.renderable = renderable;
        Scene.getRenderableComponents().refreshRenderableChange(this, old);
        invalidate();
    }

    /**
     * Returns the Mesh's Material.
     *
     * @return material
     */
    @NotNull
    public Material getMaterial() {
        return material;
    }

    /**
     * Sets the Material to the givan value.
     *
     * @param material material
     *
     * @throws NullPointerException material can't be null
     */
    public void setMaterial(@NotNull Material material) {
        if (material == null) {
            throw new NullPointerException();
        }
        Material old = this.material;
        this.material = material;
        Scene.getRenderableComponents().refreshMaterialChange(this, old);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        valid = false;
    }

    /**
     * Refreshes the Component's data.
     */
    private void refresh() {
        if (!valid || !originalAabbMin.equals(renderable.getAabbMin()) || !originalAabbMax.equals(renderable.getAabbMax()) || originalRadius != renderable.getRadius()) {
            originalAabbMin.set(renderable.getAabbMin());
            originalAabbMax.set(renderable.getAabbMax());
            originalRadius = renderable.getRadius();

            Matrix4f modelMatrix = getGameObject().getTransform().getModelMatrix();
            Vector4f[] cornerPoints = new Vector4f[8];
            cornerPoints[0] = new Vector4f(originalAabbMax.x, originalAabbMax.y, originalAabbMax.z, 1);//right-top-front
            cornerPoints[1] = new Vector4f(originalAabbMax.x, originalAabbMin.y, originalAabbMax.z, 1);//right-bottom-front
            cornerPoints[2] = new Vector4f(originalAabbMax.x, originalAabbMax.y, originalAabbMin.z, 1);//right-top-back
            cornerPoints[3] = new Vector4f(originalAabbMax.x, originalAabbMin.y, originalAabbMin.z, 1);//right-bottom-back
            cornerPoints[4] = new Vector4f(originalAabbMin.x, originalAabbMax.y, originalAabbMax.z, 1);//left-top-front
            cornerPoints[5] = new Vector4f(originalAabbMin.x, originalAabbMin.y, originalAabbMax.z, 1);//left-bottom-front
            cornerPoints[6] = new Vector4f(originalAabbMin.x, originalAabbMax.y, originalAabbMin.z, 1);//left-top-back
            cornerPoints[7] = new Vector4f(originalAabbMin.x, originalAabbMin.y, originalAabbMin.z, 1);//left-bottom-back

            Vector3f min = new Vector3f();
            Vector3f max = new Vector3f();
            cornerPoints[0].mul(modelMatrix);
            min.set(cornerPoints[0].x, cornerPoints[0].y, cornerPoints[0].z);
            max.set(cornerPoints[0].x, cornerPoints[0].y, cornerPoints[0].z);

            for (int i = 1; i < cornerPoints.length; i++) {
                cornerPoints[i].mul(modelMatrix);
                for (int j = 0; j < 3; j++) {
                    if (cornerPoints[i].get(j) < min.get(j)) {
                        min.setComponent(j, cornerPoints[i].get(j));
                    }
                    if (cornerPoints[i].get(j) > max.get(j)) {
                        max.setComponent(j, cornerPoints[i].get(j));
                    }
                }
            }
            aabbMin.set(min);
            aabbMax.set(max);
            radius = originalRadius * getGameObject().getTransform().getAbsoluteScale().get(getGameObject().getTransform().getAbsoluteScale().maxComponent());
            valid = true;
        }
    }

    /**
     * Returns the distance between the origin and the Mesh's furthest vertex.
     * This value is not depends on the GameObject's scale (object space), so if
     * you scale the GameObject, this method gives you wrong value. If you want
     * to get the scaled value of the furthest vertex distance (what is depends
     * on the GameObject's scale), use the getRealRadius method.
     *
     * @return furthest vertex distance
     *
     * @see #getRealRadius()
     * @see Transform#getAbsoluteScale()
     */
    public float getOriginalRadius() {
        return renderable.getRadius();
    }

    /**
     * Returns the distance between the origin and the Mesh's furthest vertex.
     * This value depends on the GameObject's scale (world space), so even if
     * you scale the GameObject, this method gives you the right value. If you
     * want to get the original value of the furthest vertex distance (what is
     * not depends on the GameObject's scale), use the getOriginalRadius method.
     * <br>
     * Note that if this Component doesn't assigned to a GameObject (if
     * getGameObject returns null), this method returns the original furthest
     * vertex distance.
     *
     * @return furthest vertex distance
     *
     * @see #getOriginalRadius()
     * @see Transform#getAbsoluteScale()
     * @see #getGameObject()
     */
    public float getRealRadius() {
        if (getGameObject() == null) {
            return getOriginalRadius();
        } else {
            refresh();
            return radius;
        }
    }

    /**
     * Returns the axis alligned bounding box's minimum x, y and z values. This
     * value is not depends on the GameObject's position, rotation and scale
     * (object space), so if you move, rotate or scale the GameObject, this
     * method gives you wrong value. If you want to get the moved, rotated and
     * scaled value of the AABB min, use the getRealAabbMin method.
     *
     * @return the axis alligned bounding box's minimum x, y and z values
     *
     * @see #getRealAabbMin()
     * @see Transform#getAbsoluteScale()
     * @see Transform#getAbsolutePosition()
     */
    @NotNull @ReadOnly
    public Vector3f getOriginalAabbMin() {
        return renderable.getAabbMin();
    }

    /**
     * Returns the axis alligned bounding box's minimum x, y and z values. This
     * value depends on the GameObject's position, rotation and scale (world
     * space), so even if you move, rotate or scale the GameObject, this method
     * gives you the right values. If you want to get the original value of the
     * AABB min (what is not depends on the GameObject's position, rotation and
     * scale), use the getOriginalAabbMin method.
     * <br>
     * Note that if this Component doesn't assigned to a GameObject (if
     * getGameObject returns null), this method returns the original AABB min.
     *
     * @return the axis alligned bounding box's minimum x, y and z values
     *
     * @see #getOriginalAabbMin()
     * @see Transform#getAbsoluteScale()
     * @see Transform#getAbsolutePosition()
     * @see #getGameObject()
     */
    @NotNull @ReadOnly
    public Vector3f getRealAabbMin() {
        if (getGameObject() == null) {
            return getOriginalAabbMin();
        } else {
            refresh();
            return new Vector3f(aabbMin);
        }
    }

    /**
     * Returns the axis alligned bounding box's maximum x, y and z values. This
     * value is not depends on the GameObject's position, rotation and scale
     * (object space), so if you move, rotate or scale the GameObject, this
     * method gives you wrong value. If you want to get the moved, rotated and
     * scaled value of the AABB max, use the getRealAabbMax method.
     *
     * @return the axis alligned bounding box's maximum x, y and z values
     *
     * @see #getRealAabbMax()
     * @see Transform#getAbsoluteScale()
     * @see Transform#getAbsolutePosition()
     */
    @NotNull @ReadOnly
    public Vector3f getOriginalAabbMax() {
        return renderable.getAabbMax();
    }

    /**
     * Returns the axis alligned bounding box's maximum x, y and z values. This
     * value depends on the GameObject's position, rotation and scale (world
     * space), so even if you move, rotate or scale the GameObject, this method
     * gives you the right values. If you want to get the original value of the
     * AABB max (what is not depends on the GameObject's position, rotation and
     * scale), use the getOriginalAabbMax method.
     * <br>
     * Note that if this Component doesn't assigned to a GameObject (if
     * getGameObject returns null), this method returns the original AABB max.
     *
     * @return the axis alligned bounding box's maximum x, y and z values
     *
     * @see #getOriginalAabbMax()
     * @see Transform#getAbsoluteScale()
     * @see Transform#getAbsolutePosition()
     * @see #getGameObject()
     */
    @NotNull @ReadOnly
    public Vector3f getRealAabbMax() {
        if (getGameObject() == null) {
            return getOriginalAabbMax();
        } else {
            refresh();
            return new Vector3f(aabbMax);
        }
    }

    /**
     * Determines whether the Mesh casts shadow.
     *
     * @return true if the Mesh casts shadow, false otherwise
     *
     * @see Settings#isShadowMapping()
     */
    public boolean isCastShadow() {
        return castShadow;
    }

    /**
     * Sets whether or not the Mesh casts shadow.
     *
     * @param castShadow true if the Mesh should cast shadows, false otherwise
     *
     * @see Settings#isShadowMapping()
     */
    public void setCastShadow(boolean castShadow) {
        this.castShadow = castShadow;
    }

    /**
     * Determines whether the Mesh receives shadows.
     *
     * @return true if the Mesh receives shadows, false otherwise
     *
     * @see Settings#isShadowMapping()
     */
    public boolean isReceiveShadows() {
        return receiveShadow;
    }

    /**
     * Sets whether or not the Mesh receives shadows.
     *
     * @param receiveShadows true if the Mesh should receive shadows, false
     *                       otherwise
     *
     * @see Settings#isShadowMapping()
     */
    public void setReceiveShadows(boolean receiveShadows) {
        this.receiveShadow = receiveShadows;
    }

    /**
     * Determines whether the Mesh is active.
     *
     * @return true if the Mesh is active, false otherwise
     */
    public boolean isRenderableActive() {
        return renderableActive;
    }

    /**
     * Sets whether or not the Mesh is active.
     *
     * @param renderableActive true if the Mesh is active, false otherwise
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

    public abstract int getFaceCount();

    public abstract boolean isTwoSided();

    //TODO integrate it to the pipeline
    //what about beforeDraw, afterDraw, etc.?
    public void draw() {
        renderable.draw();
    }

    @Override
    protected void detachFromGameObject() {
        getGameObject().getTransform().removeInvalidatable(this);
        super.detachFromGameObject();
        Scene.getRenderableComponents().remove(this);
        invalidate();
    }

    @Override
    protected void attachToGameObject(@NotNull GameObject object) {
        super.attachToGameObject(object);
        Scene.getRenderableComponents().add(this);
        getGameObject().getTransform().addInvalidatable(this);
        invalidate();
    }
}
