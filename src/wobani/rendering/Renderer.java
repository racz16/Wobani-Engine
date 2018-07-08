package wobani.rendering;

public abstract class Renderer{

    /**
     Determines whether the GeometryRenderer is active.
     */
    private boolean active = true;

    public abstract void render();

    /**
     Releases all of the renderer's resources. After calling this method, the render is no longer usable.
     */
    public abstract void release();

    /**
     Determines wheter this GeometryRenderer is usable. If it returns false, you can't use if for anything.

     @return true if usable, false otherwise
     */
    public abstract boolean isUsable();

    /**
     Administrates that it has been romoved from the rendering pipeline, releases the parameters.
     */
    public abstract void removeFromRenderingPipeline();

    /**
     Determines whether the GeometryRenderer is active.

     @return true if the GeometryRenderer is active, false otherwise
     */
    public boolean isActive(){
        return active;
    }

    /**
     Determines whether or not the GeometryRenderer is active.

     @param active true if the GeometryRenderer should be active, false otherwise
     */
    public void setActive(boolean active){
        this.active = active;
    }

}
