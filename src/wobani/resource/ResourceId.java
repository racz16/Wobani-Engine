package wobani.resource;

import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.io.*;
import java.util.*;

/**
 Id for resource. It has three components: id, paths and index. If you create a dynamic resource (not loaded from file),
 like an FBO, a generated texture, etc., it gets a globally unique id which identifys the resource. The paths list will
 be empty and the index will be 0.<br> If you load one resource from one or more files, like a like a simple texture or a
 cubemap texture, the id and the index will be 0, and the file paths will identify the resource.<br> If you load one file
 to more resource (like a model to meshes), the id will be 0, the path will be the file's path and the index will be the
 resource's index in the file (like the mesh's index in the model). In that case the file's path and the index will
 identify the resource.
 */
public class ResourceId{

    /**
     Stores the next globally unique id.
     */
    private static int nextId = 0;
    /**
     Reesource's paths.
     */
    private final List<File> paths = new ArrayList<>();
    /**
     Resource's id.
     */
    private int id;
    /**
     Resource's index in the file.
     */
    private int index;

    /**
     Initializes a new ResourceId to the given values. Note that it sets the index to 0, if the number of paths is
     greater than 1. Only dynamic resource (not loaded from a file) get unique id.

     @param paths resource files' paths
     @param index resource' index in the file
     */
    private ResourceId(@Nullable List<File> paths, int index){
        if(paths == null || paths.isEmpty()){
            id = getNextId();
        }else{
            id = 0;
            for(File path : paths){
                this.paths.add(new File(path.getPath()));
            }
        }
        this.index = index;
    }

    /**
     Initializes a new ResourceId with a unique id.
     */
    public ResourceId(){
        this(null, 0);
    }

    /**
     Initializes a new ResourceId to the given value.

     @param paths resource's files
     */
    public ResourceId(@Nullable List<File> paths){
        this(paths, 0);
    }

    /**
     Initializes a new ResourceId to the given value.

     @param path resource's path
     */
    public ResourceId(@NotNull File path){
        this(Utility.wrapObjectByList(path), 0);
    }

    /**
     Returns the next unique resource id.

     @return the next unique resource id
     */
    private static int getNextId(){
        return nextId++;
    }

    /**
     Creates the specified number of ResourceIds. It can be useful when you load 1 file and than segment it to numerous
     resource like when you load a model and create more than one meshes. In that case each resource get the same path,
     but also get a unique index.

     @param path     resource's file
     @param segments number of ResourceIds you want to create

     @return list of ResourceIds
     */
    @NotNull
    public static List<ResourceId> getResourceIds(@NotNull File path, int segments){
        List<ResourceId> ids = new ArrayList<>(segments);
        List<File> paths = new ArrayList<>(1);
        paths.add(path);

        for(int i = 0; i < segments; i++){
            ids.add(new ResourceId(paths, i));
        }
        return ids;
    }

    /**
     Returns the resource's id. It can be 0 if the resource is not dynamic (loaded from files) In that case file paths
     identify the resource. If it's dynamic, this id is globally unique.

     @return the resource's id
     */
    public int getId(){
        return id;
    }

    /**
     Returns the index of the resource. If the resource is dynamic (not loaded from a file) or the file loaded to just
     one resource it returns 0. If a file loaded to various resource it returns this resource's index in the file.

     @return the index of the resource
     */
    public int getIndex(){
        return index;
    }

    /**
     Returns the number of files loaded to create this resource.

     @return the number of files loaded to create this resource
     */
    public int getNumberOfPaths(){
        return paths.size();
    }

    /**
     Returns the indexth path of the resource.

     @param index index

     @return the indexth path of the resource
     */
    @NotNull
    @ReadOnly
    public File getPath(int index){
        return new File(paths.get(index).getPath());
    }

    @Override
    public int hashCode(){
        int hash = 3;
        hash = 67 * hash + this.id;
        hash = 67 * hash + Objects.hashCode(this.paths);
        hash = 67 * hash + this.index;
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
        final ResourceId other = (ResourceId) obj;
        if(this.id != other.id){
            return false;
        }
        if(this.index != other.index){
            return false;
        }
        if(paths.size() != other.paths.size()){
            return false;
        }
        for(int i = 0; i < paths.size(); i++){
            if(!paths.get(i).equals(other.paths.get(i))){
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString(){
        return "ResourceId{" + "id=" + id + ", paths=" + paths + ", index=" + index + '}';
    }

}
