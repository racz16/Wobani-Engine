package resources;

import java.io.*;
import java.util.*;
import toolbox.*;
import toolbox.annotations.*;

public class ResourceId {

    private static int nextId = 0;
    private int id;
    private final List<File> paths = new ArrayList<>();
    private int index;

    private ResourceId(@Nullable List<File> paths, int index) {
        id = getNextId();
        this.index = index;
        if (paths != null) {
            for (File path : paths) {
                this.paths.add(path.getAbsoluteFile());
            }
        }
    }

    public ResourceId() {
        this(null, 0);
    }

    public ResourceId(@Nullable List<File> paths) {
        this(paths, 0);
    }

    public ResourceId(@NotNull File path) {
        this(Utility.wrapObjectByList(path), 0);
    }

    public int getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public int getNumberOfPaths() {
        return paths.size();
    }

    @NotNull @ReadOnly
    public File getPath(int index) {
        return new File(paths.get(index).getAbsolutePath());
    }

    private static int getNextId() {
        return nextId++;
    }

    public static List<ResourceId> getResourceIds(@NotNull File path, int segments) {
        List<ResourceId> ids = new ArrayList<>(segments);
        List<File> paths = new ArrayList<>(1);
        paths.add(path);

        for (int i = 0; i < segments; i++) {
            ids.add(new ResourceId(paths, i));
        }
        return ids;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.id;
        hash = 89 * hash + Objects.hashCode(this.paths);
        hash = 89 * hash + this.index;
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
        final ResourceId other = (ResourceId) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.index != other.index) {
            return false;
        }
        if (paths.size() != other.paths.size()) {
            return false;
        }
        for (int i = 0; i < paths.size(); i++) {
            if (!paths.get(i).equals(other.paths.get(i))) {
                return false;
            }
        }
        return true;
    }

}
