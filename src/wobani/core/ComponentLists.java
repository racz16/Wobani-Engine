package wobani.core;

import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.util.*;

/**
 Stores Components in user defined lists. For example if you want to track the Camera interface, you have to call the
 addToTrackedTypes method and pass Camera.class. After that you can get all Components which implement the Camera
 interface and attached to a GameObject by calling the getComponent method. If a Component implements two or more tracked
 interfaces, it'll be stored in all appropirate slots.
 */
public class ComponentLists{

    /**
     Stores Components in user defined lists.
     */
    private final Map<Class<?>, List<Component>> lists = new HashMap<>();

    /**
     Adds the given type to the tracked types. After calling this method, all Components that instances of the given
     type, and you attached to a GameObject will be presented in the appropirate lists. Note that Components attached to
     GameObjectContainer in the past won't be presented in the new list.

     @param type type you want to track

     @throws NullPointerException type can't be null
     */
    public void addToTrackedTypes(@NotNull Class<?> type){
        if(type == null){
            throw new NullPointerException();
        }
        if(!lists.keySet().contains(type)){
            lists.put(type, new ArrayList<>());
        }
    }

    /**
     Removes the given type and the correspondig list from the tracked Components. After calling this method, Components
     that instances the given type, and you attached to a GameObject no more will be presented in the appropirate list.

     @param type type you don't want to track
     */
    public void removeFromTrackedTypes(@Nullable Class<?> type){
        lists.remove(type);
    }

    /**
     Returns the tracked types.

     @return the tracked types
     */
    @NotNull
    @ReadOnly
    public Class<?>[] getTrackedTypes(){
        Class<?>[] classes = new Class<?>[lists.keySet().size()];
        lists.keySet().toArray(classes);
        return classes;
    }

    /**
     Adds the given Component to the appropirate lists. Note that if the given Component is instace of more list's types,
     it'll be presented in and only in these lists. You can add more types by calling the addToTrackedTypes method but
     Components attached to GameObjectContainer in the past won't be presented in the new lists.

     @param component Component you want to add

     @throws NullPointerException component can't be null
     */
    @Internal
    void addComponentToLists(@NotNull Component component){
        if(component == null){
            throw new NullPointerException();
        }
        for(Class<?> type : lists.keySet()){
            addComponentToList(component, type);
        }
    }

    /**
     Adds the given Component to the specified list.

     @param component Component you want to add
     @param type      tracked type
     */
    private void addComponentToList(@NotNull Component component, @NotNull Class<?> type){
        List<Component> list = lists.get(type);
        if(type.isInstance(component) && !Utility.containsReference(list, component)){
            list.add(component);
        }
    }

    /**
     Removes the given Component from the appropirate lists.

     @param component Component you want to remove

     @throws NullPointerException component can't be null
     */
    @Internal
    void removeComponentFromLists(@NotNull Component component){
        if(component == null){
            throw new NullPointerException();
        }
        for(Class<?> key : lists.keySet()){
            removeComponentFromList(component, key);
        }
    }

    /**
     Removes the given Component from the specified list.

     @param component Component you want to remove
     @param type      tracked type
     */
    private void removeComponentFromList(@NotNull Component component, @NotNull Class<?> type){
        List<Component> list = lists.get(type);
        if(type.isInstance(component) && Utility.containsReference(list, component)){
            Utility.removeReference(list, component);
        }
    }

    /**
     Returns the number of Components implements the given type and attached to a GameObject.

     @param <T>  type
     @param type type

     @return number of Components implements the given type and attached to a GameObject
     */
    public <T> int getComponentCount(@Nullable Class<? extends T> type){
        List<?> list = lists.get(type);
        return list == null ? 0 : list.size();
    }

    /**
     Returns the indexth Component which implements the given type and attached to a GameObject. Note, that this only
     works if the appropirate type is tracked. You can track types by calling the addToTrackedTypes method.

     @param <T>   type
     @param type  type
     @param index Component's index

     @return the indexth Component which implements the given type and attached to a GameObject
     */
    @NotNull
    public <T> T getComponent(@NotNull Class<? extends T> type, int index){
        return (T) lists.get(type).get(index);
    }

    /**
     Refreshes all the tracked types' lists. It can be useful when you added a new tracked type, and you want to fill
     this type's list with the previously created Components.
     */
    public void refreshTrackedTypes(){
        GameObjectContainer objects = Scene.getGameObjects();
        for(int i = 0; i < objects.size(); i++){
            GameObject object = objects.getGameObject(i);
            refreshGameObjectComponents(object);
        }
    }

    /**
     Adds all the given GameObject's Components to the appropirate lists.

     @param object GameObject
     */
    private void refreshGameObjectComponents(@NotNull GameObject object){
        ComponentContainer components = object.getComponents();
        for(int j = 0; j < components.size(); j++){
            Component component = components.get(j);
            addComponentToLists(component);
        }
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.lists);
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
        final ComponentLists other = (ComponentLists) obj;
        if(!Objects.equals(this.lists, other.lists)){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(ComponentLists.class.getSimpleName()).append("(");
        for(Class<?> cs : lists.keySet()){
            int elements = lists.get(cs).size();
            res.append(" ").append(cs).append(": ").append(elements).append(",");
        }
        res.append(")");
        return res.toString();
    }

}
