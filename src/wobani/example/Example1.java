package wobani.example;

import java.io.*;
import java.util.*;
import org.joml.*;
import org.lwjgl.glfw.*;
import wobani.component.audio.*;
import wobani.component.camera.*;
import wobani.component.environmentprobe.*;
import wobani.component.light.blinnphong.*;
import wobani.component.renderable.*;
import wobani.core.*;
import wobani.material.*;
import wobani.rendering.geometry.*;
import wobani.resources.*;
import wobani.resources.audio.*;
import wobani.resources.environmentprobe.*;
import wobani.resources.mesh.*;
import wobani.resources.spline.*;
import wobani.resources.texture.cubemaptexture.*;
import wobani.toolbox.*;
import wobani.toolbox.parameter.*;
import wobani.window.Input.Key;
import wobani.window.*;
import wobani.window.eventhandler.*;

public class Example1 {

    private static Example1Window testWindow;
    private static BezierSpline spline;
    private static boolean windowPositionChange;
    private static StaticCubeMapTexture skybox;

    public static void main(String[] args) {
	WindowParameters parameters = new WindowParameters();
	GameLoop.initialize(parameters);
	//Utility.setLoggingLevel(Level.FINE);
	testWindow = new Example1Window();
	if (!Window.isFullscreen()) {
	    testWindow.setVisible(true);
	}
	windowPositionChange = true;
	initialize();

	GameLoop.run();
	if (testWindow != null) {
	    testWindow.dispose();
	    testWindow = null;
	}
    }

    private static void initialize() {
	try {
	    createSphere();
	    createSkybox();
	    createDragons();
	    createCharacter();
	    createSpline();
	    createLightSources();
	    createMusic();
	    createMisc();
	} catch (Exception ex) {
	    Utility.logException(ex);
	    ResourceManager.releaseResources();
	    System.exit(1);
	}
    }

    //
    //sphere--------------------------------------------------------------------
    //
    private static void createSphere() {
	Material sphereMaterial = createSphereMaterial();
	GameObject sphere = StaticMesh.loadModelToGameObject(new File("res/models/sphere.obj"));
	sphere.getTransform().setRelativePosition(new Vector3f(0, -40, -20));
	sphere.getTransform().setRelativeScale(new Vector3f(30f));
	sphere.getComponents().getOne(MeshComponent.class).setMaterial(sphereMaterial);
	sphere.setName("sphere");
    }

    private static Material createSphereMaterial() {
	DynamicEnvironmentProbe probe = createReflectionProbe();
	Material sphereMaterial = new Material(BlinnPhongRenderer.class);
	sphereMaterial.setSlot(Material.SPECULAR, new MaterialSlot(new Vector4f(0.3f, 0.3f, 0.3f, 0.75f)));
	sphereMaterial.setSlot(Material.REFLECTION, new MaterialSlot(probe));
	return sphereMaterial;
    }

    private static DynamicEnvironmentProbe createReflectionProbe() {
	Scene.getComponentLists().addToTrackedTypes(DynamicEnvironmentProbeComponent.class);
	DynamicEnvironmentProbe probe = new DynamicEnvironmentProbe();
	DynamicEnvironmentProbeComponent probeComponent = new DynamicEnvironmentProbeComponent(probe);
	GameObject g = new GameObject();
	g.getTransform().setRelativePosition(new Vector3f(0, -40, -20));
	g.getComponents().add(probeComponent);
	probe.setParallaxCorrection(true);
	probe.setParallaxCorrectionValue(35);
	probe.setResolution(1024);
	return probe;
    }

    //
    //skybox--------------------------------------------------------------------
    //
    private static void createSkybox() {
	List<File> paths = new ArrayList<>(6);
	paths.add(new File("res/textures/ely_hills/hills_rt.tga"));
	paths.add(new File("res/textures/ely_hills/hills_lf.tga"));
	paths.add(new File("res/textures/ely_hills/hills_up.tga"));
	paths.add(new File("res/textures/ely_hills/hills_dn.tga"));
	paths.add(new File("res/textures/ely_hills/hills_bk.tga"));
	paths.add(new File("res/textures/ely_hills/hills_ft.tga"));
	skybox = new StaticCubeMapTexture(paths, true);
	StaticEnvironmentProbe skyboxProbe = new StaticEnvironmentProbe(skybox);
	Parameter<EnvironmentProbe> sky = new Parameter<>(skyboxProbe);
	Scene.getParameters().set(Scene.MAIN_SKYBOX, sky);
    }

    //
    //dragon--------------------------------------------------------------------
    //
    private static void createDragons() {
	Material dragonMaterial = createDragonMaterial();
	GameObject dragon1 = createDragon1(dragonMaterial);
	GameObject dragon2 = createDragon2(dragonMaterial);
	dragon1.getChildren().add(dragon2);
    }

    private static GameObject createDragon1(Material material) {
	GameObject dragon = StaticMesh.loadModelToGameObject(new File("res/models/dragon.obj"));
	dragon.getComponents().getOne(MeshComponent.class).setMaterial(material);
	dragon.getComponents().getOne(MeshComponent.class).setReflectable(true);
	dragon.setName("dragon");
	dragon.getTransform().setRelativePosition(new Vector3f(0, -5, -20));
	dragon.getTransform().setRelativeScale(new Vector3f(2.5f));
	dragon.getComponents().add(new Component() {
	    private final Vector3f rot = new Vector3f();

	    @Override
	    public void update() {
		getGameObject().getTransform().rotate(new Vector3f(0, 0.35f * Time.getDeltaTimeFactor(), 0));
	    }
	});
	return dragon;
    }

    private static GameObject createDragon2(Material material) {
	GameObject dragon2 = StaticMesh.loadModelToGameObject(new File("res/models/dragon.obj"));
	dragon2.getComponents().getOne(MeshComponent.class).setMaterial(material);
	dragon2.getComponents().getOne(MeshComponent.class).setReflectable(true);
	dragon2.setName("dragon2");
	dragon2.getTransform().setRelativePosition(new Vector3f(35, -40, 0));
	dragon2.getTransform().rotate(new Vector3f(45, 0, 0));
	dragon2.getComponents().add(new Component() {
	    @Override
	    public void update() {
		getGameObject().getTransform().rotate(new Vector3f(0, 0.35f * Time.getDeltaTimeFactor(), 0));
	    }
	});
	return dragon2;
    }

    private static Material createDragonMaterial() {
	Material dragonMat = new Material(BlinnPhongRenderer.class);
	dragonMat.setSlot(Material.DIFFUSE, new MaterialSlot(new Vector4f(0.5f, 0.5f, 0.5f, 1f)));
	dragonMat.setSlot(Material.SPECULAR, new MaterialSlot(new Vector4f(0.7f, 0.7f, 0.7f, 1f)));
	StaticEnvironmentProbe probe = new StaticEnvironmentProbe(skybox);
	dragonMat.setSlot(Material.REFLECTION, new MaterialSlot(probe));
	dragonMat.setSlot(Material.REFRACTION, new MaterialSlot(probe));
	dragonMat.setSlot(Material.ENVIRONTMENT_INTENSITY, new MaterialSlot(new Vector4f(1)));
	dragonMat.getParameters().set(Material.REFRACTION_INDEX, new Parameter<>(1f / 1.33f));
	return dragonMat;
    }

    //
    //character-----------------------------------------------------------------
    //
    private static void createCharacter() {
	GameObject character = new GameObject("character");
	character.getTransform().setRelativePosition(new Vector3f(0, 0, 30));
	//camera
	CameraComponent cam = new FreeCameraComponent();
	character.getComponents().add(cam);
	Parameter<Camera> mc = new ComponentParameter<>(cam);
	Scene.getParameters().set(Scene.MAIN_CAMERA, mc);
	//audio listener
	AudioListenerComponent alc = new AudioListenerComponent();
	character.getComponents().add(alc);
	Parameter<AudioListenerComponent> mal = new ComponentParameter<>(alc);
	Scene.getParameters().set(Scene.MAIN_AUDIO_LISTENER, mal);
    }

    //
    //spline
    //
    private static void createSpline() {
	createSplineResource();
	GameObject splineGameObject = new GameObject("Spline");
	Material splineMat = new Material(SolidColorRenderer.class);
	splineMat.setSlot(Material.DIFFUSE, new MaterialSlot(new Vector4f(0, 0, 1, 1)));
	splineGameObject.getComponents().add(new SplineComponent(spline, splineMat));
    }

    private static void createSplineResource() {
	spline = new BezierSpline();
	spline.setStep(0.01f);
	for (int i = 0; i < 6; i++) {
	    float x = i % 2 == 0 ? 5 : -5;
	    //x -= 10;
	    float y = 3 * i;
	    spline.addControlPointToTheEnd(new Vector3f(x, y, -5));
	}
	spline.normalizeHelperPoints(5);
	spline.setLoopSpline(true);
    }

    //
    //light sources-------------------------------------------------------------
    //
    private static void createLightSources() {
	createDirectionalLight();
	createPointLight();
	createSpotLight();
    }

    private static void createDirectionalLight() {
	GameObject light = new GameObject("directionalLight");
	light.getTransform().setRelativeRotation(new Vector3f(-45, 10, 0));
	light.getComponents().add(new BlinnPhongDirectionalLightComponent());
	BlinnPhongDirectionalLightComponent bpdl = light.getComponents().getOne(BlinnPhongDirectionalLightComponent.class);
	Parameter<BlinnPhongDirectionalLightComponent> mdl = new ComponentParameter<>(bpdl);
	Scene.getParameters().set(BlinnPhongRenderer.MAIN_DIRECTIONAL_LIGHT, mdl);
    }

    private static void createPointLight() {
	GameObject pointLight = StaticMesh.loadModelToGameObject(new File("res/models/box.obj"));
	pointLight.getTransform().setRelativePosition(new Vector3f(-5, -1, 0));
	pointLight.setName("pointLight");
	BlinnPhongPointLightComponent plc = new BlinnPhongPointLightComponent();
	plc.setDiffuseColor(new Vector3f(1, 0, 0));
	pointLight.getComponents().add(plc);
    }

    private static void createSpotLight() {
	GameObject spotLight = StaticMesh.loadModelToGameObject(new File("res/models/box.obj"));
	spotLight.getTransform().setRelativePosition(new Vector3f(0, -1, 0));
	spotLight.setName("spotLight");
	BlinnPhongSpotLightComponent slc = new BlinnPhongSpotLightComponent();
	slc.setDiffuseColor(new Vector3f(0, 1, 0));
	spotLight.getComponents().add(slc);
	spotLight.getComponents().add(new Component() {
	    private float t = 0;

	    @Override
	    public void update() {
		getGameObject().getTransform().setRelativePosition(spline.getApproximatedPosition(t));
		t += 0.0005f * Time.getDeltaTimeFactor();
		BlinnPhongSpotLightComponent slc = getGameObject().getComponents().getOne(BlinnPhongSpotLightComponent.class);
		if (GLFW.glfwGetTime() % 2 > 1) {
		    slc.setActive(true);
		} else {
		    slc.setActive(false);
		}
	    }
	});
    }

    //
    //music---------------------------------------------------------------------
    //
    private static void createMusic() {
	GameObject sound = StaticMesh.loadModelToGameObject(new File("res/models/box.obj"));
	sound.setName("sound");
	sound.getComponents().getOne(MeshComponent.class).getMaterial().setSlot(Material.DIFFUSE, new MaterialSlot(new Vector4f(0, 0, 1, 1)));
	AudioSource source = new AudioSource(AudioBuffer.loadSound(new File("res/sounds/music.ogg")));
	sound.getComponents().add(new AudioSourceComponent(source));
	source.play();
    }

    //
    //misc----------------------------------------------------------------------
    //
    private static void createMisc() {
	GameObject stats = new GameObject();
	stats.getComponents().add(new Component() {
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
	Window.setMouseShape(new File("res/textures/cursors/cross.png"), new Vector2i(210));
	Window.setIcon(new File("res/textures/normal12.png"));
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
	Window.setClientAreaSizeLimits(200, 200, 10000, 10000);
	Window.addEventHandler(new WindowEventHandler() {
	    @Override
	    public void closeCallback() {

	    }

	    @Override
	    public void sizeCallback(Vector2i newSize) {
		testWindow.updateSettingsWindowDimensions();
	    }

	    @Override
	    public void frameBufferSizeCallback(Vector2i newSize) {

	    }

	    @Override
	    public void positionCallback(Vector2i newPosition) {
		setWindowPositions(true);
	    }

	    @Override
	    public void minimizationCallback(boolean minimized) {

	    }

	    @Override
	    public void focusCallback(boolean focused) {

	    }
	});
    }

    public static void setWindowPositions(boolean isGlfwWindowChanged) {
	if (windowPositionChange) {
	    if (isGlfwWindowChanged) {
		int x = Window.getPosition().x - testWindow.getWidth();
		int y = Window.getPosition().y - Window.getFrameSize().y;
		if (testWindow.getX() != x || testWindow.getY() != y) {
		    testWindow.setLocation(x, y);
		}
	    } else {
		int x = testWindow.getX() + testWindow.getWidth();
		int y = testWindow.getY() + Window.getFrameSize().y;
		if (Window.getPosition().x != x || Window.getPosition().y != y) {
		    Window.setPosition(new Vector2i(x, y));
		}
	    }
	}
    }

}
