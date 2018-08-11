package wobani.resource;

import wobani.resource.ResourceManager.*;
import wobani.toolbox.annotation.*;

import java.io.*;
import java.util.*;

/**
 Stores meta data for loadable resource like it's paths or it's size.
 */
public class DataStoreManager{

    /**
     Resource's paths.
     */
    private final List<File> paths = new ArrayList<>();
    /**
     The resource's data store policy. ACTION means that the resource's data will be stored in VRAM the VRAM or in the
     sound system, ready to use. RAM means that the resource's data may be removed from ACTION to RAM if it's rarely
     used. HDD means that the resource's data may be removed from ACTION or even from RAM if it's rarely used.
     */
    protected ResourceState dataStorePolicy;
    /**
     Determines where the resource is currently stored.
     */
    private ResourceState state;
    /**
     Time in miliseconds when the resource last time used.
     */
    private long lastActive;
    /**
     If the elapsed time since this resource's last use is higher than this value and the resource's data store policy is
     RAM or HDD, the resource's data may be removed from ACTION.
     */
    private long actionTimeLimit = 30000;
    /**
     If the elapsed time since this resource's last use is higher than this value and the resource's data store policy is
     HDD, the resource's data may be removed from ACTION or even from RAM.
     */
    private long cacheTimeLimit = 120000;
    /**
     The stored resource's size in bytes.
     */
    private int dataSize;

    /**
     Retursn the stored resource's size.

     @return resource's size (in bytes)
     */
    public int getDataSize(){
        return dataSize;
    }

    /**
     Sets the resource's data size to the given value.

     @param size data size (in bytes)

     @throws IllegalArgumentException size can't be negative
     */
    public void setDataSize(int size){
        if(size <= 0){
            throw new IllegalArgumentException("Size can't be negative");
        }
        dataSize = size;
    }

    /**
     Returns the resource's state. It determines where the resource is currently stored.

     @return the resource's state
     */
    @Nullable
    public ResourceState getState(){
        return state;
    }

    /**
     Sets the resource's state to the given value.

     @param rs state

     @throws NullPointerException parameter can't be null
     */
    public void setState(@NotNull ResourceState rs){
        if(rs == null){
            throw new NullPointerException();
        }
        state = rs;
    }

    /**
     Returns the resource's data store policy. ACTION means that the resource's data will be stored in VRAM or in the
     sound system, ready to use. RAM means that the resource's data may be removed from ACTION to RAM if it's rarely
     used. HDD means that the resource's data may be removed from ACTION or even from RAM if it's rarely used.

     @return the resource's data store policy
     */
    @Nullable
    public ResourceState getDataStorePolicy(){
        return dataStorePolicy;
    }

    /**
     Sets the resource's data store policy to the given value.

     @param rs data store policy

     @throws NullPointerException parameter can't be null
     */
    public void setDataStorePolicy(@NotNull ResourceState rs){
        if(rs == null){
            throw new NullPointerException();
        }
        dataStorePolicy = rs;
    }

    /**
     Returns the ACTION time limit. If the elapsed time since this resource's last use is higher than this value and the
     resource's data store policy is RAM or HDD, the resource's data may be removed from ACTION.

     @return VRAM time limit (in miliseconds)
     */
    public long getActionTimeLimit(){
        return actionTimeLimit;
    }

    /**
     Sets the ACTION time limit to the given value. If the elapsed time since this resource's last use is higher than
     this value and the resource's data store policy is RAM or HDD, the resource's data may be removed from ACTION.

     @param actionTimeLimit ACTION time limit (in miliseconds)

     @throws IllegalArgumentException ACTION time limit have to be higher than 0 and lower than RAM time limit
     */
    public void setActionTimeLimit(long actionTimeLimit){
        if(actionTimeLimit <= 0 || actionTimeLimit >= cacheTimeLimit){
            throw new IllegalArgumentException("VRAM time limit have to be higher than 0 and lower than RAM time limit");
        }
        this.actionTimeLimit = actionTimeLimit;
    }

    /**
     Returns the RAM time limit. If the elapsed time since this resource's last use is higher than this value and the
     resource's data store policy is HDD, the resource's data may be removed from ACTION or even from RAM.

     @return RAM time limit (in miliseconds)
     */
    public long getCacheTimeLimit(){
        return cacheTimeLimit;
    }

    /**
     Sets the RAM time limit to the given value. If the elapsed time since this resource's last use is higher than this
     value and the resource's data store policy is HDD, the resource's data may be removed from ACTION or even from RAM.

     @param cacheTimeLimit RAM time limit (in miliseconds)

     @throws IllegalArgumentException RAM time limit have to be higher than ACTION time limit
     */
    public void setCacheTimeLimit(long cacheTimeLimit){
        if(actionTimeLimit >= cacheTimeLimit){
            throw new IllegalArgumentException("RAM time limit have to be higher than VRAM time limit");
        }
        this.cacheTimeLimit = cacheTimeLimit;
    }

    /**
     Returns the resource's paths.

     @return resource's paths
     */
    @Nullable
    public List<File> getPaths(){
        return paths;
    }

    /**
     Sets the resource's paths to the given value.

     @param paths resource's paths
     */
    public void setPaths(@NotNull List<File> paths){
        for(File path : paths){
            this.paths.add(new File(path.getPath()));
        }
    }

    /**
     Returns the time when the resource last time used.

     @return the time when the resource last time used (in miliseconds)
     */
    public long getLastActive(){
        return lastActive;
    }

    /**
     Sets the resource's last activation time to now.
     */
    public void setLastActiveToNow(){
        lastActive = System.currentTimeMillis();
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.dataStorePolicy);
        hash = 53 * hash + Objects.hashCode(this.state);
        hash = 53 * hash + Objects.hashCode(this.paths);
        hash = 53 * hash + (int) (this.lastActive ^ (this.lastActive >>> 32));
        hash = 53 * hash + (int) (this.actionTimeLimit ^ (this.actionTimeLimit >>> 32));
        hash = 53 * hash + (int) (this.cacheTimeLimit ^ (this.cacheTimeLimit >>> 32));
        hash = 53 * hash + this.dataSize;
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }
        final DataStoreManager other = (DataStoreManager) obj;
        if(this.lastActive != other.lastActive){
            return false;
        }
        if(this.actionTimeLimit != other.actionTimeLimit){
            return false;
        }
        if(this.cacheTimeLimit != other.cacheTimeLimit){
            return false;
        }
        if(this.dataSize != other.dataSize){
            return false;
        }
        if(!Objects.equals(this.paths, other.paths)){
            return false;
        }
        if(this.dataStorePolicy != other.dataStorePolicy){
            return false;
        }
        if(this.state != other.state){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return "DataStoreManager{" + "dataStorePolicy=" + dataStorePolicy + ", state=" + state + ", path=" + paths + ", lastActive=" + lastActive + ", actionTimeLimit=" + actionTimeLimit + ", cacheTimeLimit=" + cacheTimeLimit + ", dataSize=" + dataSize + '}';
    }

}
