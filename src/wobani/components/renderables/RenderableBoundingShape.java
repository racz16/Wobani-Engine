package wobani.components.renderables;

import java.util.*;
import java.util.logging.*;
import org.joml.*;
import wobani.core.*;
import wobani.resources.*;
import wobani.toolbox.annotations.*;
import wobani.toolbox.invalidatable.*;

/**
 * A RenderableComponent's AABB and bounding sphere.
 */
public class RenderableBoundingShape implements Invalidatable {

    /**
     * The Renderable's calculated axis alligned bouning box's minimum values.
     */
    private final Vector3f aabbMin = new Vector3f();
    /**
     * The Renderable's calculated axis alligned bouning box's maximum values.
     */
    private final Vector3f aabbMax = new Vector3f();
    /**
     * The Renderable's calculated furthest vertex distance value.
     */
    private float radius;
    /**
     * Determines whether the Component is valid.
     */
    private boolean valid = false;
    /**
     * RenderableComponent.
     */
    private RenderableComponent<? extends Renderable> renderableComponent;
    /**
     * The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(RenderableBoundingShape.class.getName());

    /**
     * Initializes a new RenderableBoundingSpahe to the given values.
     *
     * @param renderableComponent RenderableComponent
     */
    public RenderableBoundingShape(@NotNull RenderableComponent<? extends Renderable> renderableComponent) {
        if (renderableComponent == null) {
            throw new NullPointerException();
        }
        this.renderableComponent = renderableComponent;
    }

    /**
     * Refreshes the bounding shape's data.
     */
    private void refresh() {
        if (!valid) {
            refreshAabb();
            refreshRadius();
            valid = true;
            LOG.finer("Bounding shape refreshed");
        }
    }

    /**
     * Refreshes the AABB.
     */
    private void refreshAabb() {
        Vector4f[] bb = computeBoundingBox();
        aabbMin.set(Float.POSITIVE_INFINITY);
        aabbMax.set(Float.NEGATIVE_INFINITY);
        for (Vector4f bbCornerPoint : bb) {
            refreshAabb(bbCornerPoint);
        }
    }

    /**
     * Computes the AABB in world space.
     *
     * @return the AABB in world space
     */
    private Vector4f[] computeBoundingBox() {
        Vector4f[] aabb = computeOriginalAabb();
        Matrix4f modelMatrix = renderableComponent.getGameObject().getTransform().getModelMatrix();
        for (int i = 0; i < aabb.length; i++) {
            aabb[i].mul(modelMatrix);
        }
        return aabb;
    }

    /**
     * Computes the AABB in object space.
     *
     * @return the AABB in object space
     */
    private Vector4f[] computeOriginalAabb() {
        Vector3f originalAabbMin = getOriginalAabbMin();
        Vector3f originalAabbMax = getOriginalAabbMax();
        Vector4f[] cornerPoints = new Vector4f[8];
        cornerPoints[0] = new Vector4f(originalAabbMax.x, originalAabbMax.y, originalAabbMax.z, 1);//right-top-front
        cornerPoints[1] = new Vector4f(originalAabbMax.x, originalAabbMin.y, originalAabbMax.z, 1);//right-bottom-front
        cornerPoints[2] = new Vector4f(originalAabbMax.x, originalAabbMax.y, originalAabbMin.z, 1);//right-top-back
        cornerPoints[3] = new Vector4f(originalAabbMax.x, originalAabbMin.y, originalAabbMin.z, 1);//right-bottom-back
        cornerPoints[4] = new Vector4f(originalAabbMin.x, originalAabbMax.y, originalAabbMax.z, 1);//left-top-front
        cornerPoints[5] = new Vector4f(originalAabbMin.x, originalAabbMin.y, originalAabbMax.z, 1);//left-bottom-front
        cornerPoints[6] = new Vector4f(originalAabbMin.x, originalAabbMax.y, originalAabbMin.z, 1);//left-top-back
        cornerPoints[7] = new Vector4f(originalAabbMin.x, originalAabbMin.y, originalAabbMin.z, 1);//left-bottom-back
        return cornerPoints;
    }

    /**
     * Refreshes the AABB by using the given corner point of the Renderable's
     * bounding box.
     *
     * @param bbCornerPoint one of the Renderable's bounding box's corner points
     */
    private void refreshAabb(@NotNull Vector4f bbCornerPoint) {
        for (int j = 0; j < 3; j++) {
            refreshAabbMinAndMax(bbCornerPoint, j);
        }
    }

    /**
     * Refreshes the AABB by using the specified coordinate of the given corner
     * point of the Renderable's bounding box.
     *
     * @param bbCornerPoint one of the Renderable's bounding box's corner points
     * @param index         index of the corner point's coordinate
     */
    private void refreshAabbMinAndMax(@NotNull Vector4f bbCornerPoint, int index) {
        if (bbCornerPoint.get(index) < aabbMin.get(index)) {
            aabbMin.setComponent(index, bbCornerPoint.get(index));
        }
        if (bbCornerPoint.get(index) > aabbMax.get(index)) {
            aabbMax.setComponent(index, bbCornerPoint.get(index));
        }
    }

    /**
     * Refreshes the radius.
     */
    private void refreshRadius() {
        float originalRadius = renderableComponent.getRenderable().getRadius();
        Vector3f absoluteScale = renderableComponent.getGameObject().getTransform().getAbsoluteScale();
        radius = originalRadius * absoluteScale.get(absoluteScale.maxComponent());
    }

    /**
     * Returns the distance between the origin and the Renderable's furthest
     * vertex. This value is not depends on the GameObject's scale (object
     * space), so if you scale the GameObject, this method gives you wrong
     * value. If you want to get the scaled value of the furthest vertex
     * distance (what is depends on the GameObject's scale), use the
     * getRealRadius method.
     *
     * @return furthest vertex distance
     *
     * @see #getRealRadius()
     * @see Transform#getAbsoluteScale()
     */
    public float getOriginalRadius() {
        return renderableComponent.getRenderable().getRadius();
    }

    /**
     * Returns the distance between the origin and the Renderable's furthest
     * vertex. This value depends on the GameObject's scale (world space), so
     * even if you scale the GameObject, this method gives you the right value.
     * If you want to get the original value of the furthest vertex distance
     * (what is not depends on the GameObject's scale), use the
     * getOriginalRadius method.
     * <br>
     * Note that if the Component doesn't attached to a GameObject, this method
     * returns the original furthest vertex distance.
     *
     * @return furthest vertex distance
     *
     * @see #getOriginalRadius()
     * @see Transform#getAbsoluteScale()
     */
    public float getRealRadius() {
        if (renderableComponent.getGameObject() == null) {
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
        return renderableComponent.getRenderable().getAabbMin();
    }

    /**
     * Returns the axis alligned bounding box's minimum x, y and z values. This
     * value depends on the GameObject's position, rotation and scale (world
     * space), so even if you move, rotate or scale the GameObject, this method
     * gives you the right values. If you want to get the original value of the
     * AABB min (what is not depends on the GameObject's position, rotation and
     * scale), use the getOriginalAabbMin method.
     * <br>
     * Note that if the Component doesn't attached to a GameObject, this method
     * returns the original AABB min.
     *
     * @return the axis alligned bounding box's minimum x, y and z values
     *
     * @see #getOriginalAabbMin()
     * @see Transform#getAbsoluteScale()
     * @see Transform#getAbsolutePosition()
     */
    @NotNull @ReadOnly
    public Vector3f getRealAabbMin() {
        if (renderableComponent.getGameObject() == null) {
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
        return renderableComponent.getRenderable().getAabbMax();
    }

    /**
     * Returns the axis alligned bounding box's maximum x, y and z values. This
     * value depends on the GameObject's position, rotation and scale (world
     * space), so even if you move, rotate or scale the GameObject, this method
     * gives you the right values. If you want to get the original value of the
     * AABB max (what is not depends on the GameObject's position, rotation and
     * scale), use the getOriginalAabbMax method.
     * <br>
     * Note that if the Component doesn't attached to a GameObject, this method
     * returns the original AABB max.
     *
     * @return the axis alligned bounding box's maximum x, y and z values
     *
     * @see #getOriginalAabbMax()
     * @see Transform#getAbsoluteScale()
     * @see Transform#getAbsolutePosition()
     */
    @NotNull @ReadOnly
    public Vector3f getRealAabbMax() {
        if (renderableComponent.getGameObject() == null) {
            return getOriginalAabbMax();
        } else {
            refresh();
            return new Vector3f(aabbMax);
        }
    }

    @Override
    public void invalidate() {
        valid = false;
    }

    @Override
    public int hashCode() {
        int hash = 5 + super.hashCode();
        hash = 53 * hash + (this.valid ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.renderableComponent);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final RenderableBoundingShape other = (RenderableBoundingShape) obj;
        if (this.valid != other.valid) {
            return false;
        }
        if (!Objects.equals(this.renderableComponent, other.renderableComponent)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append(super.toString()).append("\n")
                .append("RenderableBoundingShape(")
                .append(" radius: ").append(getRealRadius())
                .append(", AABB min: ").append(getRealAabbMin())
                .append(", AABB max: ").append(getRealAabbMax())
                .append(")");
        return res.toString();
    }

}
