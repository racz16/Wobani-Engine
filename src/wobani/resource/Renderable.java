package wobani.resource;

import org.joml.*;

/**
 Interface for renderables like meshes and splines.
 */
public interface Renderable extends Resource{

    /**
     Returns the Renderable's vertex count.

     @return vertex count
     */
    public int getVertexCount();

    /**
     Returns the distance between the origin and the furthest vertex. This value is not depends on a GameObject's scale
     (object space), so if you scale the mesh, this method gives you wrong value.

     @return furthest vertex distance
     */
    public float getRadius();

    /**
     Returns the axis aligned bounding box's minimum x, y and z values.

     @return the axis aligned bounding box's minimum x, y and z values
     */
    public Vector3f getAabbMin();

    /**
     Returns the axis aligned bounding box's maximum x, y and z values.

     @return the axis aligned bounding box's maximum x, y and z values
     */
    public Vector3f getAabbMax();

    /**
     Operations before draw, like binding the VAO.
     */
    public void beforeDraw();

    /**
     Draws this Renderable. Note that before calling this method, you should start a shader program, send uniform
     variables to the GPU and bind this mesh's VAO. Usually only Renderers calls this method.

     @see #beforeDraw()
     @see #afterDraw()
     */
    public void draw();

    /**
     Operations after draw.
     */
    public void afterDraw();
}
