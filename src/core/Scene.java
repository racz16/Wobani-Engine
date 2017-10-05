package core;

import components.audio.*;
import components.camera.*;
import components.light.lightTypes.*;
import components.renderables.*;
import java.util.*;
import materials.*;
import org.joml.*;
import renderers.*;
import resources.meshes.*;
import resources.splines.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * Scene contains all the GameObjects, MESHES, SPLINES, the camera, the
 * directional light and other custom LISTS.
 */
public class Scene {

    //TODO merging SPLINES and MESHES?
    /**
     * Contains all the GameObjects.
     */
    private static final List<GameObject> OBJECTS = new ArrayList<>();
    /**
     * Contains all the available MeshComponents.
     */
    private static final Map<Class<? extends Renderer>, Map<Mesh, List<MeshComponent>>> MESHES = new HashMap<>();
    /**
     * Contains all the available SplineComponents.
     */
    private static final Map<Class<? extends Renderer>, Map<Spline, List<SplineComponent>>> SPLINES = new HashMap<>();
    /**
     * Custom LISTS.
     */
    private static final Map<Class<?>, List<Component>> LISTS = new HashMap<>();
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

    static {
        addComponentListClass(Camera.class);
        addComponentListClass(DirectionalLight.class);
        addComponentListClass(PointLight.class);
        addComponentListClass(SpotLight.class);
    }

    /**
     * To can't create Scene instance.
     */
    private Scene() {
    }

    //
    //lists---------------------------------------------------------------------
    //
    /**
     * Adds the given key to the Scene's custom LISTS. After calling this
     * method, all Components that instances the given class, you add to a
     * GameObject will be presented in the Scene's corresponding list.
     *
     * @param key class
     */
    public static void addComponentListClass(@NotNull Class<?> key) {
        if (!LISTS.keySet().contains(key)) {
            LISTS.put(key, new ArrayList<>());
        }
    }

    /**
     * Removes the given key and the correspondig list from the Scene's custom
     * LISTS. After calling this method, Components that instances the given
     * class, you add to a GameObject no more will be presented in the Scene's
     * corresponding list.
     *
     * @param key class
     */
    public static void removeComponentListClass(@NotNull Class<?> key) {
        LISTS.remove(key);
    }

    /**
     * Returns the keys of the Scene's custom LISTS.
     *
     * @return the keys of the Scene's custom LISTS
     */
    @NotNull @ReadOnly
    public static Class<?>[] getComponentListClasses() {
        Class<?>[] classes = new Class<?>[LISTS.keySet().size()];
        LISTS.keySet().toArray(classes);
        return classes;
    }

    /**
     * Adds the given Component to the Scene's corresponding LISTS if it's the
     * instance of the list's class. Note that if the given Component is instace
     * of n list's class, it'll be presented in and only in these LISTS. You can
     * add more keys by calling the addComponentListClass method but Components
     * added to GameObjects in the past won't be presented in the new LISTS.
     *
     * @param component Component
     */
    static void addComponentToLists(@NotNull Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        for (Class<?> key : LISTS.keySet()) {
            List<Component> list = LISTS.get(key);
            if (key.isInstance(component) && !Utility.containsReference(list, component)) {
                list.add(component);
            }
        }
    }

    /**
     * Removes the given Component from the Scene's custom LISTS.
     *
     * @param component Component
     */
    static void removeComponentFromLists(@NotNull Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        for (Class<?> key : LISTS.keySet()) {
            List<Component> list = LISTS.get(key);
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
     *
     * @return the list of all the Components which are the given class's
     *         instances
     */
    @NotNull @ReadOnly
    public static <T> List<T> getListOfComponents(@NotNull Class<T> key) {
        if (key == null) {
            throw new NullPointerException();
        }
        List<T> list = new ArrayList<>();
        list.addAll((List<? extends T>) LISTS.get(key));
        return list;
    }

    //
    // GameObjects--------------------------------------------------------------
    //
    /**
     * Updates all GameObject's all Components.
     */
    static void updateComponents() {
        for (GameObject gameObject : OBJECTS) {
            gameObject.update();
        }
    }

    /**
     * Returns the number of the GameObjects.
     *
     * @return the number of the GameObjects
     */
    public static int getNumberOfGameObjects() {
        return OBJECTS.size();
    }

    /**
     * Adds the specified GameObject to the list of GameObjects. It doesn't add
     * already stored GameObject to the list.
     *
     * @param gameObject GameObject
     *
     * @return true if the GameObject added successfully, false otherwise
     *
     * @throws NullPointerException gameObject can't be null
     */
    static boolean addGameObject(@NotNull GameObject gameObject) {
        if (gameObject == null) {
            throw new NullPointerException();
        }
        if (Utility.containsReference(OBJECTS, gameObject)) {
            return false;
        } else {
            return OBJECTS.add(gameObject);
        }
    }

    /**
     * Returns the specified GameObject.
     *
     * @param i index
     *
     * @return GameObject
     */
    @NotNull
    public static GameObject getGameObject(int i) {
        return OBJECTS.get(i);
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
        if (meshComponent.getMesh() == null || meshComponent.getMaterial() == null) {
            return;
        }
        Map<Mesh, List<MeshComponent>> map = MESHES.get(meshComponent.getMaterial().getRenderer());
        if (map == null) {
            map = new HashMap<>();
            MESHES.put(meshComponent.getMaterial().getRenderer(), map);
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
        if (meshComponent.getGameObject() == null) {
            removeMeshComponent(meshComponent, meshComponent.getMaterial(), meshComponent.getMesh());
        }
    }

    /**
     * Removes the given MeshComponent from the Scene based on the given
     * material and mesh.
     *
     * @param meshComponent MeshComponent
     */
    private static void removeMeshComponent(@NotNull MeshComponent meshComponent, @Nullable Material fromMaterial, @Nullable Mesh fromMesh) {
        if (fromMesh == null || fromMaterial == null) {
            return;
        }
        Map<Mesh, List<MeshComponent>> map = MESHES.get(fromMaterial.getRenderer());
        if (map == null) {
            return;
        }
        List<MeshComponent> list = map.get(fromMesh);
        if (list == null) {
            return;
        }
        Utility.removeReference(list, meshComponent);
        if (list.isEmpty()) {
            map.remove(fromMesh);
            if (map.isEmpty()) {
                MESHES.remove(fromMaterial.getRenderer());
            }
        }
    }

    /**
     * Refreshes the given MeshComponent in the Scene's hierarchy after the
     * MeshComponent's Mesh changed.
     *
     * @param meshComponent meshComponent
     * @param from          meshComponent's old Mesh
     */
    public static void refreshMeshComponent(@NotNull MeshComponent meshComponent, @NotNull Mesh from) {
        removeMeshComponent(meshComponent, meshComponent.getMaterial(), from);
        addMeshComponent(meshComponent);
    }

    /**
     * Refreshes the given MeshComponent in the Scene's hierarchy after the
     * MeshComponent's Material changed.
     *
     * @param meshComponent meshComponent
     * @param from          meshComponent's old Material
     */
    public static void refreshMeshComponent(@NotNull MeshComponent meshComponent, @NotNull Material from) {
        removeMeshComponent(meshComponent, from, meshComponent.getMesh());
        addMeshComponent(meshComponent);
    }

    /**
     * Returns the array of Meshes using the specified Renderer.
     *
     * @param renderer Renderer
     *
     * @return the array of Meshes using the specified Renderer
     */
    @NotNull @ReadOnly
    public static Mesh[] getMeshes(@NotNull Class<? extends Renderer> renderer) {
        Map<Mesh, List<MeshComponent>> map = MESHES.get(renderer);
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
     * @param mesh     Mesh
     *
     * @return number of MeshComponents using the specified Renderer and Mesh
     */
    public static int getNumberOfMeshComponents(@NotNull Class<? extends Renderer> renderer, @NotNull Mesh mesh) {
        if (MESHES.get(renderer) == null || MESHES.get(renderer).get(mesh) == null) {
            return 0;
        } else {
            return MESHES.get(renderer).get(mesh).size();
        }
    }

    /**
     * Returns the indexth MeshComponent using the specified Renderer and Mesh.
     *
     * @param renderer Renderer
     * @param mesh     Mesh
     * @param index    index
     *
     * @return MeshComponent
     */
    @Nullable
    public static MeshComponent getMeshComponent(@NotNull Class<? extends Renderer> renderer, @NotNull Mesh mesh, int index) {
        if (MESHES.get(renderer) == null || MESHES.get(renderer).get(mesh) == null) {
            return null;
        } else {
            return MESHES.get(renderer).get(mesh).get(index);
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
        if (splineComponent.getSpline() == null || splineComponent.getMaterial() == null) {
            return;
        }
        Map<Spline, List<SplineComponent>> map = SPLINES.get(splineComponent.getMaterial().getRenderer());
        if (map == null) {
            map = new HashMap<>();
            SPLINES.put(splineComponent.getMaterial().getRenderer(), map);
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
        if (splineComponent.getGameObject() == null) {
            removeSplineComponent(splineComponent, splineComponent.getMaterial(), splineComponent.getSpline());
        }
    }

    /**
     * Removes the given SplineComponent from the Scene based on the given
     * material and spline.
     *
     * @param splineComponent SplineComponent
     */
    private static void removeSplineComponent(@NotNull SplineComponent splineComponent, @Nullable Material fromMaterial, @Nullable Spline fromSpline) {
        if (fromSpline == null || fromMaterial == null) {
            return;
        }
        Map<Spline, List<SplineComponent>> map = SPLINES.get(fromMaterial.getRenderer());
        if (map == null) {
            return;
        }
        List<SplineComponent> list = map.get(fromSpline);
        if (list == null) {
            return;
        }
        Utility.removeReference(list, fromSpline);
        if (list.isEmpty()) {
            map.remove(fromSpline);
            if (map.isEmpty()) {
                SPLINES.remove(fromMaterial.getRenderer());
            }
        }
    }

    /**
     * Refreshes the given SplineComponent in the Scene's hierarchy after the
     * SplineComponent's Spline changed.
     *
     * @param splineComponent splineComponent
     * @param from            splineComponent's old Spline
     */
    public static void refreshSplineComponent(@NotNull SplineComponent splineComponent, @NotNull Spline from) {
        removeSplineComponent(splineComponent, splineComponent.getMaterial(), from);
        addSplineComponent(splineComponent);
    }

    /**
     * Refreshes the given SplineComponent in the Scene's hierarchy after the
     * SplineComponent's Material changed.
     *
     * @param splineComponent Scene
     * @param from            Scene's old Material
     */
    public static void refreshSplineComponent(@NotNull SplineComponent splineComponent, @NotNull Material from) {
        removeSplineComponent(splineComponent, from, splineComponent.getSpline());
        addSplineComponent(splineComponent);
    }

    /**
     * Returns the array of Splines using the specified Renderer.
     *
     * @param renderer Renderer
     *
     * @return the array of Splines using the specified Renderer
     */
    @NotNull @ReadOnly
    public static Spline[] getSplines(@NotNull Class<? extends Renderer> renderer) {
        Map<Spline, List<SplineComponent>> map = SPLINES.get(renderer);
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
     * @param spline   Spline
     *
     * @return number of SplineComponents using the specified Renderer and
     *         Spline
     */
    public static int getNumberOfSplineComponents(@NotNull Class<? extends Renderer> renderer, @NotNull Spline spline) {
        if (SPLINES.get(renderer) == null || SPLINES.get(renderer).get(spline) == null) {
            return 0;
        } else {
            return SPLINES.get(renderer).get(spline).size();
        }
    }

    /**
     * Returns the indexth SplineComponent using the specified Renderer and
     * Spline.
     *
     * @param renderer Renderer
     * @param spline   Spline
     * @param index    index
     *
     * @return SplineComponent
     */
    @Nullable
    public static SplineComponent getSplineComponent(@NotNull Class<? extends Renderer> renderer, @NotNull Spline spline, int index) {
        if (SPLINES.get(renderer) == null || SPLINES.get(renderer).get(spline) == null) {
            return null;
        } else {
            return SPLINES.get(renderer).get(spline).get(index);
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
     *                              can't be null
     */
    public static void setCamera(@NotNull Camera camera) {
        if (camera == null || camera.getGameObject() == null) {
            throw new NullPointerException();
        }

        if (Scene.camera != null && Scene.directionalLight != null) {
            Scene.camera.removeInvalidatable(Scene.directionalLight);
        }
        Scene.camera = camera;
        if (Scene.directionalLight != null) {
            camera.addInvalidatable(Scene.directionalLight);
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
     *                              can't be null
     */
    public static void setDirectionalLight(@NotNull DirectionalLight directionalLight) {
        if (directionalLight == null || directionalLight.getGameObject() == null) {
            throw new NullPointerException();
        }

        if (Scene.camera != null && Scene.directionalLight != null) {
            Scene.camera.removeInvalidatable(Scene.directionalLight);
        }
        Scene.directionalLight = directionalLight;
        if (Scene.camera != null) {
            Scene.camera.addInvalidatable(Scene.directionalLight);
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
     *                              can't be null
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
