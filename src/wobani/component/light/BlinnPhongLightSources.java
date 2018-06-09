package wobani.component.light;

import java.nio.*;
import java.util.*;
import java.util.logging.*;
import wobani.resources.buffers.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 * Stores the Blinn-Phong light sources for the UBO.
 */
public class BlinnPhongLightSources {
    //tile baseddé átalakítani
    //	mozgásra pozíció szerint updatelni a tile-t
    //	honann veszem az előző pozíciót?
    //	shaderben 4 lightsources ssbo-t csinálni, mind a 4-en végigmenni
    //	rendererben kiválasztani a 4 legközelebbi ssbo-t
    //	    lehet, hogy ez a binding point-os történet se lesz ilyen egyszerű, kelleni fog OpenGL függvény
    //egy idő után csökkenjen ha túl sok az üres hely?
    //nondirectional light component?
    //	tárolhatná a last positiont
    //	meg amúgy itt kicsit kevesebb metódus kéne
    //	meg amúgy az ottani refreshLight hívást is egységesíteni lehetne
    //	másik tile-ba átsorolásnál hasznos lenne
    //componentek átnézése
    //osztályokhoz equals, hashcode, tostring
    //javadoc

    private static final List<BlinnPhongDirectionalLightComponent> DIRECTIONAL = new ArrayList<>();
    private static final List<BlinnPhongLightComponent> NONDIRECTIONAL = new ArrayList<>();

    private static final List<BlinnPhongLightComponent> DIRTY = new ArrayList<>();

    private static BlinnPhongDirectionalLightComponent directionalLight;
    private static Ubo ubo;
    private static LightSourceTile nondirectionalLights = new LightSourceTile();

    /**
     * One light's size in the UBO.
     */
    public static final int LIGHT_SIZE = 112;
    /**
     * The type variable's address in the UBO.
     */
    public static final int TYPE_ADDRESS = 104;
    /**
     * The active variable's address in the UBO.
     */
    public static final int ACTIVE_ADDRESS = 108;

    /**
     * The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(BlinnPhongLightSources.class.getName());

    /**
     * To can't create BlinnPhongLightSources instance.
     */
    private BlinnPhongLightSources() {
    }

    @Internal
    static void makeDirty(@NotNull BlinnPhongLightComponent light) {
	if (light == null) {
	    throw new NullPointerException();
	}
	if (!Utility.containsReference(DIRTY, light)) {
	    DIRTY.add(light);
	}
    }

    public static void refresh() {
	for (BlinnPhongLightComponent light : DIRTY) {
	    light.refreshShader();
	}
	DIRTY.clear();
    }

    //
    //directional---------------------------------------------------------------
    //
    /**
     * Adds the given light source to the UBO.
     *
     * @param light BlinnPhongDirectionalLightComponent
     */
    @Internal
    static void refreshDirectional(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (light == null) {
	    throw new NullPointerException();
	}
	addDirectionalToTheList(light);
	refreshDirectionalInShader(light);
    }

    private static void addDirectionalToTheList(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (!Utility.containsReference(DIRECTIONAL, light)) {
	    DIRECTIONAL.add(light);
	}
    }

    private static void refreshDirectionalInShader(@NotNull BlinnPhongDirectionalLightComponent light) {
	addDirectionalIfNeeded(light);
	removeDirectionalIfNeeded(light);
	refreshDirectionalIfNeeded(light);
    }

    //
    //add
    //
    private static void addDirectionalIfNeeded(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (light.isTheMainDirectionalLight()) {
	    light.setShaderIndex(0);
	    if (light != directionalLight) {
		directionalLight = light;
	    }
	}
    }

    //
    //remove
    //
    private static void removeDirectionalIfNeeded(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (!light.isTheMainDirectionalLight()) {
	    if (light == directionalLight && isUsable()) {
		removeDirectionalFromUbo();
	    }
	    light.setShaderIndex(-1);
	}
    }

    /**
     * Removes the given light source from the UBO.
     */
    private static void removeDirectionalFromUbo() {
	IntBuffer metadata = directionalLight.computeInactiveMetadata();
	ubo.bind();
	ubo.storeData(metadata, ACTIVE_ADDRESS);
	ubo.unbind();
	LOG.fine("Directional light removed from the UBO");
    }

    //
    //refresh
    //
    private static void refreshDirectionalIfNeeded(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (isUsable()) {
	    if (light == directionalLight) {
		refreshDirectionalInUbo();
	    }
	} else {
	    recreate();
	}
    }

    private static void refreshDirectionalInUbo() {
	FloatBuffer parameters = directionalLight.computeLightParameters();
	IntBuffer metadata = directionalLight.computeLightMetadata();
	ubo.bind();
	ubo.storeData(parameters, 0);
	ubo.storeData(metadata, TYPE_ADDRESS);
	ubo.unbind();
	LOG.fine("Directional light refreshed in the UBO");
    }

    //
    //recreate
    //
    private static void recreateDirectional() {
	for (BlinnPhongDirectionalLightComponent bpdlc : DIRECTIONAL) {
	    bpdlc.refreshShader();
	}
    }

    //
    //nondirectional------------------------------------------------------------
    //
    @Internal
    static void refreshNondirectional(@NotNull BlinnPhongLightComponent light) {
	if (light == null) {
	    throw new NullPointerException();
	}
	addNondirectionalToTheList(light);
	nondirectionalLights.refreshLight(light);
    }

    private static void addNondirectionalToTheList(@NotNull BlinnPhongLightComponent light) {
	if (!Utility.containsReference(NONDIRECTIONAL, light)) {
	    NONDIRECTIONAL.add(light);
	}
    }

    //
    //resources-----------------------------------------------------------------
    //
    public static void initialize() {
	createUbo();
    }

    public static void recreate() {
	createUbo();
	recreateDirectional();
	nondirectionalLights = new LightSourceTile();
	nondirectionalLights.recreate();
    }

    private static void createUbo() {
	if (!isUsable()) {
	    createUboWithoutInspection();
	    LOG.fine("Light UBO created");
	}
    }

    /**
     * Creates the UBO.
     */
    private static void createUboWithoutInspection() {
	ubo = new Ubo();
	ubo.bind();
	ubo.setName("BP Directional Light");
	ubo.allocateMemory(LIGHT_SIZE, false);
	ubo.unbind();
	ubo.bindToBindingPoint(2);
    }

    public static void release() {
	releaseUbo();
	releaseDirectionalLight();
	releaseNondirectionalLights();
    }

    private static void releaseUbo() {
	if (isUsable()) {
	    ubo.release();
	    ubo = null;
	    LOG.fine("Light UBO released");
	}
    }

    private static void releaseDirectionalLight() {
	if (directionalLight != null) {
	    directionalLight.setShaderIndex(-1);
	}
	directionalLight = null;
    }

    private static void releaseNondirectionalLights() {
	if (nondirectionalLights != null && nondirectionalLights.isUsable()) {
	    nondirectionalLights.release();
	    nondirectionalLights = null;
	}
    }

    public static void makeUpToDate() {
	if (!isUsable()) {
	    recreate();
	}
	refresh();
    }

    public static boolean isUsable() {
	return Utility.isUsable(ubo);
    }

    //
    //
    //
    private static class LightSourceTile {

	/**
	 * The LightSources UBO's lights.
	 */
	private final List<BlinnPhongLightComponent> lights = new ArrayList<>();
	/**
	 * The LightSources UBO.
	 */
	private Ssbo ssbo;

	private int count = 0;

	private static final int LIGHT_SOURCES_OFFSET = 16;

	private static final Logger LOG = Logger.getLogger(LightSourceTile.class.getName());

	public LightSourceTile() {
	    createSsbo();
	    extendListTo(1);
	}

	private void refreshLight(@NotNull BlinnPhongLightComponent light) {
	    addLightIfNeeded(light);
	    removeLightIfNeeded(light);
	    refreshLightIfNeeded(light);
	}

	//
	//add
	//
	private void addLightIfNeeded(@NotNull BlinnPhongLightComponent light) {
	    if (light.getGameObject() != null && light.getShaderIndex() == -1) {
		int shaderIndex = computeNewShaderIndex();
		extendStorageIfNeeded(shaderIndex);
		light.setShaderIndex(shaderIndex);
		addLight(light, shaderIndex);
		setCount();
	    }
	}

	/**
	 * Computes the new light source's index. It returns -1 if there is not
	 * enough space to store one more light source.
	 *
	 * @return the new light source's index
	 */
	private int computeNewShaderIndex() {
	    for (int i = 0; i < lights.size(); i++) {
		if (lights.get(i) == null) {
		    return i;
		}
	    }
	    return lights.size();
	}

	private void extendStorageIfNeeded(int shaderIndex) {
	    if (shaderIndex == lights.size()) {
		int size = lights.size() * 2;
		extendSsboTo(size);
		extendListTo(size);
	    }
	}

	private void extendSsboTo(int size) {
	    ssbo.bind();
	    ssbo.allocateMemory(size * LIGHT_SIZE + LIGHT_SOURCES_OFFSET, false);
	    ssbo.unbind();
	}

	private void extendListTo(int size) {
	    while (lights.size() < size) {
		lights.add(null);
	    }
	}

	private void addLight(@NotNull BlinnPhongLightComponent light, int shaderIndex) {
	    if (shaderIndex >= lights.size()) {
		lights.add(light);
	    } else {
		lights.set(shaderIndex, light);
	    }
	}

	private void setCount() {
	    count = computeCount();
	    refreshCountInSsbo();
	}

	private int computeCount() {
	    for (int i = lights.size() - 1; i >= 0; i--) {
		if (lights.get(i) != null) {
		    return i + 1;
		}
	    }
	    return 0;
	}

	private void refreshCountInSsbo() {
	    ssbo.bind();
	    ssbo.storeData(new int[]{count}, 0);
	    ssbo.unbind();
	    LOG.fine("Nondirectional light removed from the SSBO");
	}

	//
	//remove
	//
	private void removeLightIfNeeded(@NotNull BlinnPhongLightComponent light) {
	    if (light.getGameObject() == null && light.getShaderIndex() != -1) {
		int shaderIndex = light.getShaderIndex();
		lights.set(shaderIndex, null);
		removeLightFromSsbo(light);
		light.setShaderIndex(-1);
		setCount();
	    }
	}

	/**
	 * Removes the given light source from the UBO.
	 *
	 * @param light BlinnPhongDirectionalLightComponent
	 */
	private void removeLightFromSsbo(@NotNull BlinnPhongLightComponent light) {
	    IntBuffer metadata = light.computeInactiveMetadata();
	    ssbo.bind();
	    ssbo.storeData(metadata, light.getShaderIndex() * LIGHT_SIZE + ACTIVE_ADDRESS + LIGHT_SOURCES_OFFSET);
	    ssbo.unbind();
	    LOG.fine("Nondirectional light removed from the SSBO");
	}

	//
	//refresh
	//
	private void refreshLightIfNeeded(@NotNull BlinnPhongLightComponent light) {
	    if (light.getGameObject() != null && light.getShaderIndex() != -1) {
		refreshLightInSsbo(light);
	    }
	}

	private void refreshLightInSsbo(@NotNull BlinnPhongLightComponent light) {
	    FloatBuffer parameters = light.computeLightParameters();
	    IntBuffer metadata = light.computeLightMetadata();
	    ssbo.bind();
	    ssbo.storeData(parameters, light.getShaderIndex() * LIGHT_SIZE + LIGHT_SOURCES_OFFSET);
	    ssbo.storeData(metadata, light.getShaderIndex() * LIGHT_SIZE + TYPE_ADDRESS + LIGHT_SOURCES_OFFSET);
	    ssbo.unbind();
	    LOG.fine("Nondirectional light refreshed in the SSBO");
	}

	//
	//resources
	//
	public void recreate() {
	    createSsbo();
	    for (BlinnPhongLightComponent bplc : NONDIRECTIONAL) {
		bplc.refreshShader();
	    }
	}

	/**
	 * Creates the UBO.
	 */
	private void createSsbo() {
	    if (!isUsable()) {
		createSsboWithoutInspection();
		LOG.fine("Light SSBO created");
	    }
	}

	/**
	 * Creates the UBO.
	 */
	private void createSsboWithoutInspection() {
	    ssbo = new Ssbo();
	    ssbo.bind();
	    ssbo.setName("BP Nondirectional Lights");
	    ssbo.allocateMemory(LIGHT_SIZE + LIGHT_SOURCES_OFFSET, false);
	    ssbo.unbind();
	    ssbo.bindToBindingPoint(3);
	}

	/**
	 * Releases the UBO. After calling this mathod, you can't use the
	 * LightSources UBO and can't recreate it. Note that some renderers
	 * (like the BlinnPhongRenderer) may expect to access to the
	 * LightSources UBO (which isn't possible after calling this method).
	 */
	public void release() {
	    releaseSsbo();
	    releaseNondirectionalLight();
	}

	private void releaseSsbo() {
	    if (isUsable()) {
		ssbo.release();
		ssbo = null;
		LOG.fine("Light SSBO released");
	    }
	}

	private void releaseNondirectionalLight() {
	    for (BlinnPhongLightComponent light : lights) {
		if (light != null) {
		    light.setShaderIndex(-1);
		}
	    }
	    lights.clear();
	}

	public boolean isUsable() {
	    return Utility.isUsable(ssbo);
	}
    }
}
