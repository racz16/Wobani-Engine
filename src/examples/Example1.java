package examples;

import components.camera.*;
import components.light.*;
import components.renderables.*;
import core.*;
import java.util.*;
import org.joml.*;
import org.lwjgl.glfw.*;
import renderers.*;
import resources.*;
import resources.materials.*;
import resources.meshes.*;
import resources.splines.*;
import toolbox.*;
import window.Input.Key;
import window.*;
import window.eventHandlers.*;

/**
 * First demo level for the engine.
 */
public class Example1 {

    /**
     * Java Swing based window, allow you to tweak some settings.
     */
    private static Example1Window testWindow;
    /**
     * Bezier spline.
     */
    private static Spline spline;

    /**
     * Entry point, initializes the engine, the scene, then starts the game
     * loop. After run releases the resources.
     *
     * @param args don't cares
     */
    public static void main(String[] args) {
        WindowParameters parameters = new WindowParameters();
        GameLoop.initialize(parameters);
        testWindow = new Example1Window();
        if (!Window.isFullscreen()) {
            testWindow.setVisible(true);
        }
        initialize();

        GameLoop.run();
        if (testWindow != null) {
            testWindow.dispose();
            testWindow = null;
        }
    }

    /**
     * Initializes the scene.
     */
    private static void initialize() {
        try {
            dragons();
            boxes();
            camera();
            spline();
            lightSources();
            other();
        } catch (Exception ex) {
            Utility.logException(ex);
            ResourceManager.releaseResources();
            System.exit(1);
        }
    }

    /**
     * Adds dragons to the scene.
     */
    private static void dragons() {
        Material dragonMat = new Material(BlinnPhongRenderer.class);
        dragonMat.setSlot(Material.DIFFUSE, new MaterialSlot(new Vector4f(0.5f, 0.5f, 0.5f, 1f)));
        dragonMat.setSlot(Material.SPECULAR, new MaterialSlot(new Vector4f(0.7f, 0.7f, 0.7f, 1f)));

        GameObject dragon = StaticMesh.loadModelToGameObject("res/models/dragon.obj");
        dragon.getComponent(MeshComponent.class).setMaterial(dragonMat);
        dragon.setName("dragon");
        dragon.getTransform().setRelativePosition(new Vector3f(0, -5, -15));
        dragon.getTransform().setRelativeScale(new Vector3f(2.5f));

        GameObject dragon2 = StaticMesh.loadModelToGameObject("res/models/dragon.obj");
        dragon2.getComponent(MeshComponent.class).setMaterial(dragonMat);
        dragon2.setName("dragon2");
        dragon2.getTransform().setRelativePosition(new Vector3f(50, 0, 0));
        dragon.addChild(dragon2);

        dragon.addComponent(new Component() {
            @Override
            public void update() {
                getGameObject().getTransform().rotate(new Vector3f(0, 0.35f * Time.getDeltaTimeFactor(), 0));
            }
        });
        dragon2.addComponent(new Component() {
            @Override
            public void update() {
                getGameObject().getTransform().rotate(new Vector3f(0, 0.35f * Time.getDeltaTimeFactor(), 0));
            }
        });
        StaticMesh mesh = (StaticMesh) dragon.getComponent(MeshComponent.class).getMesh();
        mesh.setDataStorePolicy(ResourceManager.ResourceState.HDD);
        mesh.setVramTimeLimit(1000);
        mesh.setRamTimeLimit(2000);
    }

    /**
     * Adds boxes to the scene.
     */
    private static void boxes() {
        Material boxMaterial = new Material(BlinnPhongRenderer.class);
        boxMaterial.setSlot(Material.SPECULAR, new MaterialSlot(new Vector4f(0.3f, 0.3f, 0.3f, 0.75f)));
        boxMaterial.setSlot(Material.NORMAL, new MaterialSlot("res/textures/normal9.jpg", false));
//        boxMaterial.setSlot(Material.NORMAL, new MaterialSlot("res/textures/normal7.png", false));
//        boxMaterial.getSlot(Material.NORMAL).setFloatParameter(MaterialSlot.POM_USE_FLOAT, 1f);
//        boxMaterial.getSlot(Material.NORMAL).setFloatParameter(MaterialSlot.POM_SCALE_FLOAT, 0.3f);
//        boxMaterial.getSlot(Material.NORMAL).setFloatParameter(MaterialSlot.POM_MIN_LAYERS_FLOAT, 50f);
//        boxMaterial.getSlot(Material.NORMAL).setFloatParameter(MaterialSlot.POM_MAX_LAYERS_FLOAT, 100f);

        GameObject box = new GameObject("bigBox");
        box.getTransform().setRelativePosition(new Vector3f(0, -40, -20));
        box.getTransform().setRelativeScale(new Vector3f(50f));
        for (Mesh m : StaticMesh.loadModel("res/models/box.obj")) {
            box.addComponent(new MeshComponent(m, boxMaterial));
        }
    }

    /**
     * Adds light sources to the scene.
     */
    private static void lightSources() {
        List<StaticMesh> boxModel = StaticMesh.loadModel("res/models/box.obj");
        //directional light
        GameObject light = new GameObject("directionalLight");
        light.getTransform().setRelativeRotation(new Vector3f(-45, 10, 0));
        light.addComponent(new DirectionalLightComponent());
        Scene.setDirectionalLight(light.getComponent(DirectionalLightComponent.class));

        //point light
        GameObject pointLight = new GameObject("pointLight");
        pointLight.getTransform().setRelativePosition(new Vector3f(-5, -1, 0));
        for (StaticMesh mod : boxModel) {
            pointLight.addComponent(new MeshComponent(mod));
        }
        PointLightComponent plc = new PointLightComponent();
        plc.setDiffuseColor(new Vector3f(1, 0, 0));
        pointLight.addComponent(plc);

        //spotlight
        GameObject spotLight = new GameObject("spotLight");
        spotLight.getTransform().setRelativePosition(new Vector3f(0, -1, 0));
        for (StaticMesh mod : boxModel) {
            spotLight.addComponent(new MeshComponent(mod));
        }
        SpotLightComponent slc = new SpotLightComponent();
        slc.setDiffuseColor(new Vector3f(0, 1, 0));
        spotLight.addComponent(slc);
        spotLight.addComponent(new Component() {
            private float t = 0;

            @Override
            public void update() {
                getGameObject().getTransform().setAbsolutePosition(spline.getApproximatedPosition(t));
                t += 0.0005f * Time.getDeltaTimeFactor();
                SpotLightComponent slc = getGameObject().getComponent(SpotLightComponent.class);
                if (GLFW.glfwGetTime() % 2 > 1) {
                    slc.setActive(true);
                } else {
                    slc.setActive(false);
                }
            }
        });
    }

    /**
     * Adds a camera to the scene.
     */
    private static void camera() {
        GameObject camera = new GameObject("camera");
        camera.getTransform().setRelativePosition(new Vector3f(0, 0, 30));
        camera.addComponent(new FreeCameraComponent());
        Scene.setCamera(camera.getComponent(Camera.class));
    }

    /**
     * Adds a spline to the scene.
     */
    private static void spline() {
//        CatmullRomSpline spline = new CatmullRomSpline();
        BezierSpline spline = new BezierSpline();
        Example1.spline = spline;
        spline.setStep(0.01f);

        for (int i = 0; i < 6; i++) {
            float x = i % 2 == 0 ? 5 : -5;
            float y = 3 * i;
            spline.addControlPointToTheEnd(new Vector3f(x, y, 0));
        }
        spline.normalizeHelperPoints(5);
//        spline.setTension(1);
        spline.setLoopSpline(true);

        GameObject splineGameObject = new GameObject("Spline");
        Material splineMat = new Material(SolidColorRenderer.class);
        splineMat.setSlot(Material.DIFFUSE, new MaterialSlot(new Vector4f(0, 0, 1, 1)));
        splineGameObject.addComponent(new SplineComponent(spline, splineMat));
    }

    /**
     * Other initializations.
     */
    private static void other() {
        GameObject stats = new GameObject();
        stats.addComponent(new Component() {
            long lastUpdate = System.nanoTime();
            long updateLengthSum;

            @Override
            public void update() {
                long now = System.nanoTime();
                long updateLength = now - lastUpdate;
                lastUpdate = now;
                updateLengthSum += updateLength;

                if (updateLengthSum >= 1000000000) {
                    testWindow.updateStats();
                    updateLengthSum = 0;
                }
                testWindow.update();
            }
        });
        Window.setMouseShape("res/textures/cursors/cross.png", new Vector2i(210));
        Window.setIcon("res/textures/normal12.png");
        Input.addKeyboardEventHandler(new KeyboardEventHandler() {
            @Override
            public void keyCallback(Input.Key key, int scancode, Input.KeyStatus action, boolean shiftPressed, boolean controlPressed, boolean altPressed, boolean superPressed) {
                if (key == Key.KEY_ESCAPE) {
                    Window.setWindowShouldClose(true);
                }
            }

            @Override
            public void charCallback(int codepoint) {
            }

            @Override
            public void charModsCallback(int codepoint, boolean shiftPressed, boolean controlPressed, boolean altPressed, boolean superPressed) {
            }
        });

    }

}
