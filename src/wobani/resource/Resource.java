package wobani.resource;

/**
 Basic methods for resource.
 */
public interface Resource{

    /**
     Returns the resource's data size in CACHE.

     @return the resource's data size in CACHE (in bytes)
     */
    int getCacheDataSize();

    /**
     Returns the resource's data size in action (this usually means the VRAM).

     @return the resource's data size in action (in bytes)
     */
    int getActiveDataSize();

    /**
     Updates the resource.
     */
    void update();

    /**
     Releases the resource.
     */
    void release();

    /**
     Determines whether this resource is usable.

     @return true if usable, false otherwise
     */
    boolean isUsable();

    /**
     Returns the resource's unique resource id.

     @return the resource's unique resource id.
     */
    ResourceId getResourceId();
}
