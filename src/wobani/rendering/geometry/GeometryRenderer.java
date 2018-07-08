package wobani.rendering.geometry;

import wobani.rendering.*;

/**
 Renderers can render the scene, do post processing effects or draw ui elements.
 */
public abstract class GeometryRenderer extends Renderer{

    /**
     Number of rendered Meshes and Splines in last frame.
     */
    protected int numberOfRenderedElements;
    /**
     Number of rendered faces in last frame.
     */
    protected int numberOfRenderedFaces;

    /**
     Determines whether or not the GeometryRenderer is active.

     @param active true if the GeometryRenderer should be active, false otherwise
     */
    @Override
    public void setActive(boolean active){
        super.setActive(active);
        if(!active){
            numberOfRenderedFaces = 0;
            numberOfRenderedElements = 0;
        }
    }

    /**
     Returns the number of faces rendererd by this GeometryRenderer in the last frame.

     @return the number of faces rendererd by this GeometryRenderer in the last frame
     */
    public int getNumberOfRenderedFaces(){
        return numberOfRenderedFaces;
    }

    /**
     Returns the number of meshes and splines rendererd by this GeometryRenderer in the last frame.

     @return the number of meshes and splines rendererd by this GeometryRenderer in the last frame
     */
    public int getNumberOfRenderedElements(){
        return numberOfRenderedElements;
    }

}
