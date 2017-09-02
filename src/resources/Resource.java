package resources;

/**
 * Basic methods for resources.
 */
public interface Resource {

    /**
     * Returns the resource's data size.
     *
     * @return the resource's data size (in bytes)
     */
    public int getDataSize();

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
