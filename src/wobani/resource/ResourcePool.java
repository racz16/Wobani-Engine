package wobani.resource;

import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.util.*;

/**
 For creating resources more efficiently.
 */
public abstract class ResourcePool{

    /**
     Collection of unused resources.
     */
    private final Stack<Integer> resourcePool = new Stack<>();
    /**
     Maximum number of created but not used resources stored in the pool.
     */
    private int maxPoolSize = 10;

    /**
     Returns one resource's id.

     @return one resource's id
     */
    public int getResource(){
        refreshBufferObjectPool();
        return resourcePool.pop();
    }

    /**
     If there is no resource in the pool it creates max pool size number of new resources.
     */
    private void refreshBufferObjectPool(){
        if(resourcePool.isEmpty()){
            int[] resources = new int[maxPoolSize];
            createResources(resources);
            for(int resource : resources){
                resourcePool.push(resource);
            }
        }
    }

    /**
     Fills the given array with new resources.

     @param resources array to store2D new resource ids
     */
    protected abstract void createResources(@NotNull int[] resources);

    /**
     Returns the Resource Pool's maximum size. When you create a new resource the system first tries to get one from the
     Resource Pool. If it's empty it fills the pool with max pool size number of resources.
     */
    public int getMaxPoolSize(){
        return maxPoolSize;
    }

    /**
     Sets the Resource Pool's maximum size. When you create a new resource the system first tries to get one from the
     Resource Pool. If it's empty it fills the pool with max pool size number of resources.

     @param size Resource Pool's maximum size

     @throws IllegalArgumentException if size is not positive
     */
    public void setMaxPoolSize(int size){
        if(size <= 0){
            throw new IllegalArgumentException("Size is not positive");
        }
        this.maxPoolSize = size;
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }

        ResourcePool that = (ResourcePool) o;

        if(maxPoolSize != that.maxPoolSize){
            return false;
        }
        return resourcePool.equals(that.resourcePool);
    }

    @Override
    public int hashCode(){
        int result = resourcePool.hashCode();
        result = 31 * result + maxPoolSize;
        return result;
    }

    @Override
    public String toString(){
        return ResourcePool.class.getSimpleName() + "(" + "resourcePool: " + Utility
                .toString(resourcePool) + ", " + "maxPoolSize: " + maxPoolSize + ")";
    }
}
