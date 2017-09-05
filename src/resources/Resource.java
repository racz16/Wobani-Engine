package resources;

/**
 * Basic methods for resources.
 */
public interface Resource {

    /**
     * Returns the resource's data size in RAM.
     *
     * @return the resource's data size in RAM (in bytes)
     */
    public int getDataSizeInRam();

    /**
     * Returns the resource's data size in action (this means the VRAM or the
     * sound system).
     *
     * @return the resource's data size in action (in bytes)
     */
    public int getDataSizeInAction();

    /**
     * Updates the resource.
     */
    public void update();

    /**
     * Releases the resource.
     */
    public void release();

    /**
     * Determines wheter this resource is usable. If it returns false, you can't
     * use if for anything.
     *
     * @return true if usable, false otherwise
     */
    public boolean isUsable();
}
