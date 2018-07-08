package wobani.resources;

import org.joml.*;

/**
 Interface for meshes and splines.
 */
public interface Renderable extends Resource{

    /**
     Returns the Renderable's vertex count.

     @return vertex count
     */
    public int getVertexCount();

    /**
     Returns the distance between the origin and the furthest vertex. This value is not depends on a GameObject's scale
     (object space), so if you scale the mesh, this method gives you wrong value. If you want to get the scaled value of
     the furthest vertex distance (what is depends on the GameObject's scale), use the MeshComponent's or
     SplineComponent's getScaledFurthestVertexDistance() method.

     @return furthest vertex distance

     @see MeshComponent#getRealFurthestVertexDistance()
     @see SplineComponent#getRealFurthestVertexDistance()
     */
    public float getRadius();

    /**
     Returns the axis alligned bounding box's minimum x, y and z values.

     @return the axis alligned bounding box's minimum x, y and z values
     */
    public Vector3f getAabbMin();

    /**
     Returns the axis alligned bounding box's maximum x, y and z values.

     @return the axis alligned bounding box's maximum x, y and z values
     */
    public Vector3f getAabbMax();

    /**
     Operations before draw, like binding the VAO.
     */
    public void beforeDraw();

    /**
     Draws this Renderable. Note that before calling this method, you should start a shader program, enable vertex
     attributes, send uniform variables to the GPU and bind this mesh's VAO. Usually only Renderers calls this method.
     */
    public void draw();

    /**
     Operations after draw.
     */
    public void afterDraw();
}
