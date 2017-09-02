package resources;

import java.util.*;
import resources.ResourceManager.ResourceState;
import toolbox.annotations.*;

/**
 * Stores meta data for loadable resources like it's path or it's size.
 */
public class LoadableResourceMetaData {

    /**
     * The resource's data store policy. VRAM means that the resource's data
     * will be stored in VRAM. RAM means that the resource's data may be removed
     * from VRAM to RAM if it's rarely used. HDD means that the resource's data
     * may be removed from VRAM or even from RAM if it's rarely used.
     */
    protected ResourceState dataStorePolicy;
    /**
     * Determines where the resource is currently stored.
     */
    private ResourceState state;
    /**
     * Resource's path.
     */
    private String path;
    /**
     * Time in miliseconds when the resource last time used.
     */
    private long lastActive;
    /**
     * If the elapsed time since this resource's last use is higher than this
     * value and the resource's data store policy is RAM or HDD, the resource's
     * data may be removed from VRAM.
     */
    private long vramTimeLimit = 30000;
    /**
     * If the elapsed time since this resource's last use is higher than this
     * value and the resource's data store policy is HDD, the resource's data
     * may be removed from VRAM or even from RAM.
     */
    private long ramTimeLimit = 120000;
    /**
     * The stored resource's size in bytes.
     */
    private int dataSize;

    /**
     * Retursn the stored resource's size.
     *
     * @return resource's size (in bytes)
     */
    public int getDataSize() {
        return dataSize;
    }

    /**
     * Sets the resource's data size to the given value.
     *
     * @param size data size (in bytes)
     *
     * @throws IllegalArgumentException size can't be negative
     */
    public void setDataSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size can't be negative");
        }
        dataSize = size;
    }

    /**
     * Returns the resource's state. It determines where the resource is
     * currently stored.
     *
     * @return the resource's state
     */
    @Nullable
    public ResourceState getState() {
        return state;
    }

    /**
     * Sets the resource's state to the given value.
     *
     * @param rs state
     *
     * @throws NullPointerException parameter can't be null
     *
     */
    public void setState(@NotNull ResourceState rs) {
        if (rs == null) {
            throw new NullPointerException();
        }
        state = rs;
    }

    /**
     * Returns the resource's data store policy. VRAM means that the resource's
     * data will be stored in VRAM. RAM means that the resource's data may be
     * removed from VRAM to RAM if it's rarely used. HDD means that the
     * resource's data may be removed from VRAM or even from RAM if it's rarely
     * used.
     *
     * @return the resource's data store policy
     */
    @Nullable
    public ResourceState getDataStorePolicy() {
        return dataStorePolicy;
    }

    /**
     * Sets the resource's data store policy to the given value.
     *
     * @param rs data store policy
     *
     * @throws NullPointerException parameter can't be null
     */
    public void setDataStorePolicy(@NotNull ResourceState rs) {
        if (rs == null) {
            throw new NullPointerException();
        }
        dataStorePolicy = rs;
    }

    /**
     * Returns the VRAM time limit. If the elapsed time since this resource's
     * last use is higher than this value and the resource's data store policy
     * is RAM or HDD, the resource's data may be removed from VRAM.
     *
     * @return VRAM time limit (in miliseconds)
     */
    public long getVramTimeLimit() {
        return vramTimeLimit;
    }

    /**
     * Sets the VRAM time limit to the given value. If the elapsed time since
     * this resource's last use is higher than this value and the resource's
     * data store policy is RAM or HDD, the resource's data may be removed from
     * VRAM.
     *
     * @param vramTimeLimit VRAM time limit (in miliseconds)
     *
     * @throws IllegalArgumentException VRAM time limit have to be higher than 0
     * and lower than RAM time limit
     */
    public void setVramTimeLimit(long vramTimeLimit) {
        if (vramTimeLimit <= 0 || vramTimeLimit >= ramTimeLimit) {
            throw new IllegalArgumentException("VRAM time limit have to be higher than 0 and lower than RAM time limit");
        }
        this.vramTimeLimit = vramTimeLimit;
    }

    /**
     * Returns the RAM time limit. If the elapsed time since this resource's
     * last use is higher than this value and the resource's data store policy
     * is HDD, the resource's data may be removed from VRAM or even from RAM.
     *
     * @return RAM time limit (in miliseconds)
     */
    public long getRamTimeLimit() {
        return ramTimeLimit;
    }

    /**
     * Sets the RAM time limit to the given value. If the elapsed time since
     * this resource's last use is higher than this value and the resource's
     * data store policy is HDD, the resource's data may be removed from VRAM or
     * even from RAM.
     *
     * @param ramTimeLimit RAM time limit (in miliseconds)
     *
     * @throws IllegalArgumentException RAM time limit have to be higher than
     * VRAM time limit
     */
    public void setRamTimeLimit(long ramTimeLimit) {
        if (vramTimeLimit >= ramTimeLimit) {
            throw new IllegalArgumentException("RAM time limit have to be higher than VRAM time limit");
        }
        this.ramTimeLimit = ramTimeLimit;
    }

    /**
     * Returns the resource's path.
     *
     * @return resource's path
     */
    @Nullable
    public String getPath() {
        return path;
    }

    /**
     * Sets the resource's path to the given value.
     *
     * @param path resource's path
     *
     * @throws NullPointerException path can't be null
     */
    public void setPath(@NotNull String path) {
        if (path == null) {
            throw new NullPointerException();
        }
        this.path = path;
    }

    /**
     * Returns the time when the resource last time used.
     *
     * @return the time when the resource last time used (in miliseconds)
     */
    public long getLastActive() {
        return lastActive;
    }

    /**
     * Sets the resource's last activation time to now.
     */
    public void setLastActiveToNow() {
        lastActive = System.currentTimeMillis();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.dataStorePolicy);
        hash = 53 * hash + Objects.hashCode(this.state);
        hash = 53 * hash + Objects.hashCode(this.path);
        hash = 53 * hash + (int) (this.lastActive ^ (this.lastActive >>> 32));
        hash = 53 * hash + (int) (this.vramTimeLimit ^ (this.vramTimeLimit >>> 32));
        hash = 53 * hash + (int) (this.ramTimeLimit ^ (this.ramTimeLimit >>> 32));
        hash = 53 * hash + this.dataSize;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LoadableResourceMetaData other = (LoadableResourceMetaData) obj;
        if (this.lastActive != other.lastActive) {
            return false;
        }
        if (this.vramTimeLimit != other.vramTimeLimit) {
            return false;
        }
        if (this.ramTimeLimit != other.ramTimeLimit) {
            return false;
        }
        if (this.dataSize != other.dataSize) {
            return false;
        }
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        if (this.dataStorePolicy != other.dataStorePolicy) {
            return false;
        }
        if (this.state != other.state) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LoadableResourceMetaData{" + "dataStorePolicy=" + dataStorePolicy
                + ", state=" + state + ", path=" + path + ", lastActive=" + lastActive
                + ", vramTimeLimit=" + vramTimeLimit + ", ramTimeLimit=" + ramTimeLimit
                + ", dataSize=" + dataSize + '}';
    }

}
