package components.renderables;

import core.*;
import java.util.*;
import materials.*;
import org.joml.*;
import renderers.*;
import resources.meshes.*;
import toolbox.annotations.*;

/**
 * Encapsulates a Mesh and a Material. Renderers can render this Component.
 *
 * @see Mesh
 * @see Material
 * @see Renderer
 *
 */
public class MeshComponent extends Component {

    /**
     * Mesh to render.
     */
    private Mesh mesh;
    /**
     * The Mesh's Material.
     */
    private Material material;
    /**
     * Determines whether the Mesh is active.
     */
    private boolean meshActive = true;
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
     * Determines whether the Mesh rendered two sided.
     */
    private boolean twoSided = false;
    /**
     * The mesh's original axis alligned bouning box's minimum values.
     */
    private final Vector3f originalAabbMin = new Vector3f();
    /**
     * The mesh's calculated axis alligned bouning box's minimum values.
     */
    private final Vector3f aabbMin = new Vector3f();
    /**
     * The mesh's original axis alligned bouning box's maximum values.
     */
    private final Vector3f originalAabbMax = new Vector3f();
    /**
     * The mesh's calculated axis alligned bouning box's maximum values.
     */
    private final Vector3f aabbMax = new Vector3f();
    /**
     * The mesh's original furthest vertex distance value.
     */
    private float originalFurthestVertexDistance;
    /**
     * The mesh's calculated furthest vertex distance value.
     */
    private float furthestVertexDistance;
    /**
     * Determines whether the Component is valid.
     */
    private boolean valid;

    /**
     * Initializes a new MeshComponent to the given value.
     *
     * @param mesh mesh
     */
    public MeshComponent(@NotNull Mesh mesh) {
        setMesh(mesh);
        setMaterial(new Material(BlinnPhongRenderer.class));
    }

    /**
     * Initializes a new MeshComponent to the given values.
     *
     * @param mesh     mesh
     * @param material mesh's material
     */
    public MeshComponent(@NotNull Mesh mesh, @NotNull Material material) {
        setMesh(mesh);
        setMaterial(material);
    }

    /**
     * Returns the Mesh.
     *
     * @return mesh
     */
    @NotNull
    public Mesh getMesh() {
        return mesh;
    }

    /**
     * Sets the Mesh to the given value.
     *
     * @param mesh mesh
     *
     * @throws NullPointerException mesh can't be null
     */
    public void setMesh(@NotNull Mesh mesh) {
        if (mesh == null) {
            throw new NullPointerException();
        }
        Mesh old = this.mesh;
        this.mesh = mesh;
        Scene.refreshMeshComponent(this, old);
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
        Scene.refreshMeshComponent(this, old);
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
        if (!valid || !originalAabbMin.equals(mesh.getAabbMin()) || !originalAabbMax.equals(mesh.getAabbMax()) || originalFurthestVertexDistance != mesh.getFurthestVertexDistance()) {
            originalAabbMin.set(mesh.getAabbMin());
            originalAabbMax.set(mesh.getAabbMax());
            originalFurthestVertexDistance = mesh.getFurthestVertexDistance();

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
            furthestVertexDistance = originalFurthestVertexDistance * getGameObject().getTransform().getAbsoluteScale().get(getGameObject().getTransform().getAbsoluteScale().maxComponent());
            valid = true;
        }
    }

    /**
     * Returns the distance between the origin and the Mesh's furthest vertex.
     * This value is not depends on the GameObject's scale (object space), so if
     * you scale the GameObject, this method gives you wrong value. If you want
     * to get the scaled value of the furthest vertex distance (what is depends
     * on the GameObject's scale), use the getRealFurthestVertexDistance method.
     *
     * @return furthest vertex distance
     *
     * @see #getRealFurthestVertexDistance()
     * @see Transform#getAbsoluteScale()
     */
    public float getOriginalFurthestVertexDistance() {
        return mesh.getFurthestVertexDistance();
    }

    /**
     * Returns the distance between the origin and the Mesh's furthest vertex.
     * This value depends on the GameObject's scale (world space), so even if
     * you scale the GameObject, this method gives you the right value. If you
     * want to get the original value of the furthest vertex distance (what is
     * not depends on the GameObject's scale), use the
     * getOriginalFurthestVertexDistance method.
     * <br>
     * Note that if this Component doesn't assigned to a GameObject (if
     * getGameObject returns null), this method returns the original furthest
     * vertex distance.
     *
     * @return furthest vertex distance
     *
     * @see #getOriginalFurthestVertexDistance()
     * @see Transform#getAbsoluteScale()
     * @see #getGameObject()
     */
    public float getRealFurthestVertexDistance() {
        if (getGameObject() == null) {
            return getOriginalFurthestVertexDistance();
        } else {
            refresh();
            return furthestVertexDistance;
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
        return mesh.getAabbMin();
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
        return mesh.getAabbMax();
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

    /**
     * Determines whether the Mesh is active.
     *
     * @return true if the Mesh is active, false otherwise
     */
    public boolean isMeshActive() {
        return meshActive;
    }

    /**
     * Sets whether or not the Mesh is active.
     *
     * @param renderableActive true if the Mesh is active, false otherwise
     */
    public void setMeshActive(boolean renderableActive) {
        this.meshActive = renderableActive;
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

    @Override
    protected void removeFromGameObject() {
        getGameObject().getTransform().removeInvalidatable(this);
        super.removeFromGameObject();
        Scene.removeMeshComponent(this);
        invalidate();
    }

    @Override
    protected void addToGameObject(@NotNull GameObject object) {
        super.addToGameObject(object);
        Scene.addMeshComponent(this);
        getGameObject().getTransform().addInvalidatable(this);
        invalidate();
    }

    @Override
    public int hashCode() {
        int hash = 5 + super.hashCode();
        hash = 89 * hash + Objects.hashCode(this.mesh);
        hash = 89 * hash + Objects.hashCode(this.material);
        hash = 89 * hash + (this.meshActive ? 1 : 0);
        hash = 89 * hash + (this.materialActive ? 1 : 0);
        hash = 89 * hash + (this.castShadow ? 1 : 0);
        hash = 89 * hash + (this.receiveShadow ? 1 : 0);
        hash = 89 * hash + (this.twoSided ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final MeshComponent other = (MeshComponent) obj;
        if (this.meshActive != other.meshActive) {
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
        if (this.twoSided != other.twoSided) {
            return false;
        }
        if (!Objects.equals(this.mesh, other.mesh)) {
            return false;
        }
        if (!Objects.equals(this.material, other.material)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nMeshComponent{" + "mesh=" + mesh
                + ", material=" + material + ", meshActive=" + meshActive
                + ", materialActive=" + materialActive
                + ", castShadow=" + castShadow + ", receiveShadow=" + receiveShadow
                + ", twoSided=" + twoSided + '}';
    }

}
