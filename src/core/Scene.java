package core;

import components.audio.*;
import components.camera.*;
import components.light.lightTypes.*;
import components.renderables.*;
import java.util.*;
import org.joml.*;
import renderers.*;
import resources.meshes.*;
import resources.splines.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * Scene contains all the GameObjects, meshes, splines, the camera, the
 * directional light and other custom lists.
 */
public class Scene {

    //TODO skybox
    //TODO merging splines and meshes?
    /**
     * Contains all the GameObjects.
     */
    private static final List<GameObject> objects = new ArrayList<>();
    /**
     * Contains all the available MeshComponents.
     */
    private static final Map<Class, Map<Mesh, List<MeshComponent>>> meshes = new HashMap<>();
    /**
     * Contains all the available SplineComponents.
     */
    private static final Map<Class, Map<Spline, List<SplineComponent>>> splines = new HashMap<>();
    /**
     * The scene's main camera.
     */
    private static Camera camera;
    /**
     * The scene's directional light.
     */
    private static DirectionalLight directionalLight;
    /**
     * The scene's audio listener.
     */
    private static AudioListenerComponent audioListener;
    /**
     * Custom lists.
     */
    private static final Map<Class, List<Component>> lists = new HashMap<>();

    static {
        lists.put(Camera.class, new ArrayList<>());
        lists.put(DirectionalLight.class, new ArrayList<>());
        lists.put(PointLight.class, new ArrayList<>());
        lists.put(SpotLight.class, new ArrayList<>());
    }

    //
    //lists---------------------------------------------------------------------
    //
    /**
     * Adds the given key to the Scene's custom lists. After calling this
     * method, all Components that instances the given class, you add to a
     * GameObject will be presented in the Scene's corresponding list.
     *
     * @param key class
     */
    public static void addComponentListClass(@NotNull Class key) {
        if (!lists.keySet().contains(key)) {
            lists.put(key, new ArrayList<>());
        }
    }

    /**
     * Removes the given key and the correspondig list from the Scene's custom
     * lists. After calling this method, Components that instances the given
     * class, you add to a GameObject no more will be presented in the Scene's
     * corresponding list.
     *
     * @param key class
     */
    public static void removeComponentListClass(@NotNull Class key) {
        lists.remove(key);
    }

    /**
     * Returns the keys of the Scene's custom lists.
     *
     * @return the keys of the Scene's custom lists
     */
    @NotNull
    public static Class[] getComponentListClasses() {
        Class[] classes = new Class[lists.keySet().size()];
        lists.keySet().toArray(classes);
        return classes;
    }

    /**
     * Adds the given Component to the Scene's corresponding lists if it's the
     * instance of the list's class. Note that if the given Component is instace
     * of n list's class, it'll be presented in and only in these lists. You can
     * add more keys by calling the addComponentListClass method but Components
     * added to GameObjects in the past won't be presented in the new lists.
     *
     * @param component Component
     */
    static void addComponentToLists(@NotNull Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        for (Class key : lists.keySet()) {
            List<Component> list = lists.get(key);
            if (key.isInstance(component) && !Utility.containsReference(list, component)) {
                list.add(component);
            }
        }
    }

    /**
     * Removes the given Component from the Scene's custom lists.
     *
     * @param component Component
     */
    static void removeComponentFromLists(@NotNull Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        for (Class key : lists.keySet()) {
            List<Component> list = lists.get(key);
            if (key.isInstance(component) && Utility.containsReference(list, component)) {
                Utility.removeReference(list, component);
            }
        }
    }

    /**
     * Returns the list of all the Components which are the given class's
     * instances.
     *
     * @param <T> type
     * @param key class
     * @return the list of all the Components which are the given class's
     * instances
     */
    @NotNull @ReadOnly
    public static <T> List<T> getListOfComponents(@NotNull Class<T> key) {
        if (key == null) {
            throw new NullPointerException();
        }
        List<T> list = new ArrayList<>();
        list.addAll((Collection<? extends T>) lists.get(key));
        return list;
    }

    //
    // GameObjects--------------------------------------------------------------
    //
    /**
     * Updates all GameObject's all Components.
     */
    static void updateComponents() {
        for (GameObject gameObject : objects) {
            gameObject.update();
        }
    }

    /**
     * Returns the number of the GameObjects.
     *
     * @return the number of the GameObjects
     */
    public static int getNumberOfGameObjects() {
        return objects.size();
    }

    /**
     * Adds the specified GameObject to the list of GameObjects. It doesn't add
     * already stored GameObject to the list.
     *
     * @param gameObject GameObject
     * @return true if the GameObject added successfully, false otherwise
     *
     * @throws NullPointerException gameObject can't be null
     */
    static boolean addGameObject(@NotNull GameObject gameObject) {
        if (gameObject == null) {
            throw new NullPointerException();
        }
        if (Utility.containsReference(objects, gameObject)) {
            return false;
        } else {
            return objects.add(gameObject);
        }
    }

    /**
     * Returns the specified GameObject.
     *
     * @param i index
     * @return GameObject
     */
    @NotNull
    public static GameObject getGameObject(int i) {
        return objects.get(i);
    }

    //
    //meshes--------------------------------------------------------------------
    //
    /**
     * Adds the given MeshComponent to the Scene. It is only poossible to add it
     * if it's connected to a GameObject. There is really no reason to call this
     * method, MeshComponents automatically added to the Scene when they
     * connected to a GameObject and removed when disconnected from a
     * GameObject.
     *
     * @param meshComponent MeshComponent
     */
    public static void addMeshComponent(@NotNull MeshComponent meshComponent) {
        Map<Mesh, List<MeshComponent>> map = meshes.get(meshComponent.getMaterial().getRenderer());
        if (map == null) {
            map = new HashMap<>();
            meshes.put(meshComponent.getMaterial().getRenderer(), map);
        }
        List<MeshComponent> list = map.get(meshComponent.getMesh());
        if (list == null) {
            list = new ArrayList<>();
            map.put(meshComponent.getMesh(), list);
        }
        if (!Utility.containsReference(list, meshComponent) && meshComponent.getGameObject() != null) {
            list.add(meshComponent);
        }
    }

    /**
     * Removes the given MeshComponent from the Scene. It is only poossible to
     * remove it if it's not connected to a GameObject. There is really no
     * reason to call this method, MeshComponents automatically added to the
     * Scene when they connected to a GameObject and removed when disconnected
     * from a GameObject.
     *
     * @param meshComponent MeshComponent
     */
    public static void removeMeshComponent(@NotNull MeshComponent meshComponent) {
        Map<Mesh, List<MeshComponent>> map = meshes.get(meshComponent.getMaterial().getRenderer());
        if (map == null) {
            return;
        }
        List<MeshComponent> list = map.get(meshComponent.getMesh());
        if (list == null) {
            return;
        }
        if (meshComponent.getGameObject() == null) {
            Utility.removeReference(list, meshComponent);
            if (list.isEmpty()) {
                map.remove(meshComponent.getMesh());
                if (map.isEmpty()) {
                    meshes.remove(meshComponent.getMaterial().getRenderer());
                }
            }
        }
    }

    /**
     * Returns the array of Meshes using the specified Renderer.
     *
     * @param renderer Renderer
     * @return the array of Meshes using the specified Renderer
     */
    @NotNull @ReadOnly
    public static Mesh[] getMeshes(@NotNull Class<? extends Renderer> renderer) {
        Map<Mesh, List<MeshComponent>> map = meshes.get(renderer);
        if (map == null) {
            return new Mesh[0];
        } else {
            Mesh[] ret = new Mesh[map.keySet().size()];
            map.keySet().toArray(ret);
            return ret;
        }
    }

    /**
     * Returns the number of MeshComponents using the specified Renderer and
     * Mesh.
     *
     * @param renderer Renderer
     * @param mesh Mesh
     * @return number of MeshComponents using the specified Renderer and Mesh
     */
    public static int getNumberOfMeshComponents(@NotNull Class<? extends Renderer> renderer, @NotNull Mesh mesh) {
        if (meshes.get(renderer) == null || meshes.get(renderer).get(mesh) == null) {
            return 0;
        } else {
            return meshes.get(renderer).get(mesh).size();
        }
    }

    /**
     * Returns the indexth MeshComponent using the specified Renderer and Mesh.
     *
     * @param renderer Renderer
     * @param mesh Mesh
     * @param index index
     * @return MeshComponent
     */
    @Nullable
    public static MeshComponent getMeshComponent(Class<? extends Renderer> renderer, Mesh mesh, int index) {
        if (meshes.get(renderer) == null || meshes.get(renderer).get(mesh) == null) {
            return null;
        } else {
            return meshes.get(renderer).get(mesh).get(index);
        }
    }

    //
    //splines-------------------------------------------------------------------
    //
    /**
     * Adds the given SplineComponent to the Scene. It is only poossible to add
     * it if it's connected to a GameObject. There is really no reason to call
     * this method, SplineComponents automatically added to the Scene when they
     * connected to a GameObject and removed when disconnected from a
     * GameObject.
     *
     * @param splineComponent SplineComponent
     */
    public static void addSplineComponent(@NotNull SplineComponent splineComponent) {
        Map<Spline, List<SplineComponent>> map = splines.get(splineComponent.getMaterial().getRenderer());
        if (map == null) {
            map = new HashMap<>();
            splines.put(splineComponent.getMaterial().getRenderer(), map);
        }
        List<SplineComponent> list = map.get(splineComponent.getSpline());
        if (list == null) {
            list = new ArrayList<>();
            map.put(splineComponent.getSpline(), list);
        }
        if (!Utility.containsReference(list, splineComponent)) {
            list.add(splineComponent);
        }
    }

    /**
     * Removes the given SplineComponent from the Scene. It is only poossible to
     * remove it if it's not connected to a GameObject. There is really no
     * reason to call this method, SplineComponents automatically added to the
     * Scene when they connected to a GameObject and removed when disconnected
     * from a GameObject.
     *
     * @param splineComponent SplineComponent
     */
    public static void removeSplineComponent(@NotNull SplineComponent splineComponent) {
        Map<Spline, List<SplineComponent>> map = splines.get(splineComponent.getMaterial().getRenderer());
        if (map == null) {
            return;
        }
        List<SplineComponent> list = map.get(splineComponent.getSpline());
        if (list == null) {
            return;
        }
        if (splineComponent.getGameObject() == null) {
            Utility.removeReference(list, splineComponent);
            if (list.isEmpty()) {
                map.remove(splineComponent.getSpline());
                if (map.isEmpty()) {
                    splines.remove(splineComponent.getMaterial().getRenderer());
                }
            }
        }
    }

    /**
     * Returns the array of Splines using the specified Renderer.
     *
     * @param renderer Renderer
     * @return the array of Splines using the specified Renderer
     */
    @NotNull @ReadOnly
    public static Spline[] getSplines(@NotNull Class<? extends Renderer> renderer) {
        Map<Spline, List<SplineComponent>> map = splines.get(renderer);
        if (map == null) {
            return new Spline[0];
        } else {
            Spline[] ret = new Spline[map.keySet().size()];
            map.keySet().toArray(ret);
            return ret;
        }
    }

    /**
     * Returns the number of SplineComponents using the specified Renderer and
     * Spline.
     *
     * @param renderer Renderer
     * @param spline Spline
     * @return number of SplineComponents using the specified Renderer and
     * Spline
     */
    public static int getNumberOfSplineComponents(@NotNull Class<? extends Renderer> renderer, @NotNull Spline spline) {
        if (splines.get(renderer) == null || splines.get(renderer).get(spline) == null) {
            return 0;
        } else {
            return splines.get(renderer).get(spline).size();
        }
    }

    /**
     * Returns the indexth SplineComponent using the specified Renderer and
     * Spline.
     *
     * @param renderer Renderer
     * @param spline Spline
     * @param index index
     * @return SplineComponent
     */
    @Nullable
    public static SplineComponent getSplineComponent(Class<? extends Renderer> renderer, Spline spline, int index) {
        if (splines.get(renderer) == null || splines.get(renderer).get(spline) == null) {
            return null;
        } else {
            return splines.get(renderer).get(spline).get(index);
        }
    }

    //
    //main camera and light-----------------------------------------------------
    //
    /**
     * Returns the scene's main camera.
     *
     * @return the scene's main camera
     */
    @Nullable
    public static Camera getCamera() {
        return camera;
    }

    /**
     * Sets the scene's main camera to the given value.
     *
     * @param camera main camera
     *
     * @throws NullPointerException the given parameter and it's GameObject
     * can't be null
     */
    public static void setCamera(@NotNull Camera camera) {
        if (camera == null || camera.getGameObject() == null) {
            throw new NullPointerException();
        }

        if (Scene.camera != null && Scene.directionalLight != null) {
            Scene.camera.removeInvalidatable((Invalidatable) Scene.directionalLight);
        }
        Scene.camera = camera;
        if (Scene.directionalLight != null) {
            camera.addInvalidatable((Invalidatable) Scene.directionalLight);
        }
        camera.invalidate();
    }

    /**
     * Returns the scene's directional light.
     *
     * @return the scene's directional light
     */
    @Nullable
    public static DirectionalLight getDirectionalLight() {
        return directionalLight;
    }

    /**
     * Sets the scene's directional light to the given value.
     *
     * @param directionalLight scene's directional light
     *
     * @throws NullPointerException the given parameter and it's GameObject
     * can't be null
     */
    public static void setDirectionalLight(@NotNull DirectionalLight directionalLight) {
        if (directionalLight == null || directionalLight.getGameObject() == null) {
            throw new NullPointerException();
        }

        if (Scene.camera != null && Scene.directionalLight != null) {
            Scene.camera.removeInvalidatable((Invalidatable) Scene.directionalLight);
        }
        Scene.directionalLight = directionalLight;
        if (Scene.camera != null) {
            Scene.camera.addInvalidatable((Invalidatable) Scene.directionalLight);
        }
        Scene.directionalLight.invalidate();
    }

    /**
     * Returns the scene's audio listener.
     *
     * @return the scene's audio listener
     */
    @Nullable
    public static AudioListenerComponent getAudioListener() {
        return audioListener;
    }

    /**
     * Sets the scene's audio listener to the given value.
     *
     * @param audioListener scene's audio listener
     *
     * @throws NullPointerException the given parameter and it's GameObject
     * can't be null
     */
    public static void setAudioListener(@NotNull AudioListenerComponent audioListener) {
        if (audioListener == null || audioListener.getGameObject() == null) {
            throw new NullPointerException();
        }

        Scene.audioListener = audioListener;
    }

    /**
     * Return the scene's environment color.
     *
     * @return the scene's environment color
     */
    @NotNull @ReadOnly
    public static Vector3f getEnvironmentColor() {
        return OpenGl.getClearColor();
    }

    /**
     * Sets the environment color to the given value. All of environtment
     * color's components must be min. 0.
     *
     * @param environmentColor environment color
     *
     * @throws IllegalArgumentException environtment color can't be lower than 0
     */
    public static void setEnvironmentColor(@NotNull Vector3f environmentColor) {
        if (!Utility.isHdrColor(environmentColor)) {
            throw new IllegalArgumentException("Environtment color can't be lower than 0");
        }
        OpenGl.setClearColor(new Vector4f(environmentColor, 1));
    }

}
