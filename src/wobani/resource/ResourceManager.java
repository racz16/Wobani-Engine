package wobani.resource;

import wobani.rendering.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.util.*;

/**
 Manages the loaded models, textures and splines.
 <p>
 */
public class ResourceManager{

    private static final Map<Class<?>, Map<ResourceId, ?>> RESOURCES = new HashMap<>();

    public static void addResource(@NotNull Resource resource){
        if(getResource(resource.getResourceId(), resource.getClass()) == null){
            //System.out.println("-----");
            //System.out.println(resource.getClass().getSimpleName());
            for(Class<?> type : getAllAncestors(resource.getClass())){
                addResourceToType(resource, type);
                //System.out.println("  " + type.getSimpleName());
            }
            //System.out.println("-----");
        }
    }

    private static Set<Class<?>> getAllAncestors(@NotNull Class<?> type){
        List<Class<?>> res = new ArrayList<>();
        Class<?> currentType = type;
        searchInHierarchy(currentType, res);
        return new HashSet<>(res);
    }

    private static void searchInHierarchy(@NotNull Class<?> currentType, @NotNull List<Class<?>> res){
        do{
            addInterfaces(currentType, res);
            res.add(currentType);
            Class<?> superClass = currentType.getSuperclass();
            if(superClass == null){
                break;
            }
            currentType = superClass;
        }while(!currentType.equals(Object.class));
    }

    private static void addInterfaces(@NotNull Class<?> currentType, @NotNull List<Class<?>> res){
        Class<?>[] interfaces = currentType.getInterfaces();
        if(interfaces.length > 0){
            res.addAll(Arrays.asList(interfaces));
            for(Class<?> inter : interfaces){
                res.addAll(getAllAncestors(inter));
            }
        }
    }

    private static <T> void addResourceToType(@NotNull Resource resource, Class<T> type){
        Map<ResourceId, T> typedResourceMap = getResourceMap(type);
        if(typedResourceMap == null){
            typedResourceMap = new HashMap<>();
            RESOURCES.put(type, typedResourceMap);
        }
        typedResourceMap.put(resource.getResourceId(), (T) resource);
    }

    @Nullable
    private static <T> Map<ResourceId, T> getResourceMap(@NotNull Class<T> type){
        return (Map<ResourceId, T>) RESOURCES.get(type);
    }

    @Nullable
    public static Resource getResource(@NotNull ResourceId id){
        Map<ResourceId, ?> resources = RESOURCES.get(Resource.class);
        if(resources == null){
            return null;
        }else{
            return (Resource) resources.get(id);
        }
    }

    @Nullable
    public static <T> T getResource(@NotNull ResourceId id, @NotNull Class<T> type){
        Map<ResourceId, T> resource = getResourceMap(type);
        if(resource == null){
            return null;
        }else{
            return resource.get(id);
        }
    }

    @Nullable
    public static <T> Iterator<T> getResources(@NotNull Class<T> type){
        Map<ResourceId, T> typedResourceMap = getResourceMap(type);
        if(typedResourceMap == null){
            return null;
        }
        return typedResourceMap.values().iterator();
    }

    public static int getResourceCount(@NotNull Class<?> type){
        Map<ResourceId, Resource> typedResourceMap = (Map<ResourceId, Resource>) RESOURCES.get(type);
        if(typedResourceMap == null){
            return 0;
        }
        int size = 0;
        for(Resource resource : typedResourceMap.values()){
            if(resource.isUsable()){
                size += resource.getActiveDataSize();
            }
        }
        return size;
    }

    public static int getResourceSizeInAction(@NotNull Class<?> type){
        Map<ResourceId, Resource> typedResourceMap = (Map<ResourceId, Resource>) RESOURCES.get(type);
        if(typedResourceMap == null){
            return 0;
        }
        int size = 0;
        for(Resource resource : typedResourceMap.values()){
            if(resource.isUsable()){
                size += resource.getActiveDataSize();
            }
        }
        return size;
    }

    public static int getResourceSizeInCache(@NotNull Class<?> type){
        Map<ResourceId, Resource> typedResourceMap = (Map<ResourceId, Resource>) RESOURCES.get(type);
        if(typedResourceMap == null){
            return 0;
        }
        int size = 0;
        for(Resource resource : typedResourceMap.values()){
            if(resource.isUsable()){
                size += resource.getCacheDataSize();
            }
        }
        return size;
    }

    /**
     Resource's last update time (in miliseconds).
     */
    private static long lastUpdateTime = System.currentTimeMillis();
    /**
     Resources' update time period (in milliseconds).
     */
    private static long resourceUpdatePeriod = 5000;

    /**
     To can't initialize a new ResourceManager.
     */
    private ResourceManager(){
    }

    /**
     Updates all the resource.
     */
    public static void updateResources(){
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastUpdateTime > resourceUpdatePeriod){
            Iterator<Resource> resources = getResources(Resource.class);
            if(resources == null){
                return;
            }
            while(resources.hasNext()){
                resources.next().update();
            }

            lastUpdateTime = currentTime;
        }
    }

    /**
     Returns the resource' update time period.

     @return the resource' update time period (in miliseconds)
     */
    public static long getResourceUpdatePeriod(){
        return resourceUpdatePeriod;
    }

    /**
     Sets the resource' update time period to the given value.

     @param updatePeriod the resource' update time period

     @throws IllegalArgumentException update period can't be negative
     */
    public static void setResourceUpdatePeriod(long updatePeriod){
        if(updatePeriod < 0){
            throw new IllegalArgumentException("Update period can't be negative");
        }
        resourceUpdatePeriod = updatePeriod;
    }

    /**
     Releases the textures, meshes, splines, FBOs and the window.
     */
    public static void releaseResources(){
        Iterator<Resource> resources = getResources(Resource.class);
        if(resources == null){
            return;
        }
        while(resources.hasNext()){
            Resource res = resources.next();
            if(Utility.isUsable(res)){
                res.release();
            }
        }
        RESOURCES.clear();

        RenderingPipeline.release();
    }


    /**
     Resource's state.
     */
    public enum ResourceState{
        /**
         ACTIVE (means that the resource ready to use, stored like in VRAM or in the sound system).
         */
        ACTIVE,
        /**
         CACHE.
         */
        CACHE,
        /**
         STORAGE.
         */
        STORAGE
    }

}
