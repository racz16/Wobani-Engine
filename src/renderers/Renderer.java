package renderers;

/**
 * Renderers can render the scene, do post processing effects or draw ui
 * elements.
 */
public abstract class Renderer {

    /**
     * Determines whether the Renderer is active.
     */
    private boolean active = true;
    /**
     * Number of rendered Meshes and Splines in last frame.
     */
    protected int numberOfRenderedElements;
    /**
     * Number of rendered faces in last frame.
     */
    protected int numberOfRenderedFaces;

    /**
     * Renders the meshes and splines using this Renderer in their material, if
     * it's a scene renderer. If it's not it may render post processing effects,
     * ui elements or a shadow map.
     */
    public abstract void render();

    /**
     * Releases all of the renderer's resources. After calling this method, the
     * render is no longer usable.
     */
    public abstract void release();

    /**
     * Determines wheter this Renderer is usable. If it returns false, you can't
     * use if for anything.
     *
     * @return true if usable, false otherwise
     */
    public abstract boolean isUsable();

    /**
     * Administrates that it has been romoved from the rendering pipeline,
     * releases the parameters.
     */
    protected abstract void removeFromRenderingPipeline();

    /**
     * Determines whether the Renderer is active.
     *
     * @return true if the Renderer is active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Determines whether or not the Renderer is active.
     *
     * @param active true if the Renderer should be active, false otherwise
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the number of faces rendererd by this Renderer in the last frame.
     *
     * @return the number of faces rendererd by this Renderer in the last frame
     */
    public int getNumberOfRenderedFaces() {
        return numberOfRenderedFaces;
    }

    /**
     * Returns the number of meshes and splines rendererd by this Renderer in
     * the last frame.
     *
     * @return the number of meshes and splines rendererd by this Renderer in
     * the last frame
     */
    public int getNumberOfRenderedElements() {
        return numberOfRenderedElements;
    }

    /**
     * Determines whether the Renderer is a Geometry Renderer. If it's a
     * Geometry Renderer, most likely it renders a part of the scene. If it's
     * not (called Screen Space Renderer), it not renders any geometry from the
     * scene, it rather performs post processing effects or draw UI elements.
     *
     * @return true if it's a Geometry Renderer, false if it's a Screen Space
     * Renderer
     */
    public abstract boolean isGeometryRenderer();
}
