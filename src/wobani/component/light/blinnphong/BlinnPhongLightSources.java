package wobani.component.light.blinnphong;

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
    //componentek átnézése
    //osztályokhoz equals, hashcode, tostring
    //javadoc

    private static final List<BlinnPhongDirectionalLightComponent> DIRECTIONAL = new ArrayList<>();
    private static final List<BlinnPhongPositionalLightComponent> POSITIONAL = new ArrayList<>();

    private static final List<BlinnPhongLightComponent> DIRTY = new ArrayList<>();

    private static BlinnPhongDirectionalLightComponent directionalLight;
    private static Ubo ubo;
    private static LightSourceTile positionalLights = new LightSourceTile();

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
    static void refresh(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (light == null) {
	    throw new NullPointerException();
	}
	addToTheList(light);
	refreshInShader(light);
    }

    private static void addToTheList(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (!Utility.containsReference(DIRECTIONAL, light)) {
	    DIRECTIONAL.add(light);
	}
    }

    private static void refreshInShader(@NotNull BlinnPhongDirectionalLightComponent light) {
	addIfNeeded(light);
	removeIfNeeded(light);
	refreshIfNeeded(light);
    }

    //
    //add
    //
    private static void addIfNeeded(@NotNull BlinnPhongDirectionalLightComponent light) {
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
    private static void removeIfNeeded(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (!light.isTheMainDirectionalLight()) {
	    if (light == directionalLight && isUsable()) {
		removeFromUbo();
	    }
	    light.setShaderIndex(-1);
	}
    }

    /**
     * Removes the given light source from the UBO.
     */
    private static void removeFromUbo() {
	IntBuffer metadata = directionalLight.computeInactiveMetadata();
	ubo.bind();
	ubo.storeData(metadata, ACTIVE_ADDRESS);
	ubo.unbind();
	LOG.fine("Directional light removed from the UBO");
    }

    //
    //refresh
    //
    private static void refreshIfNeeded(@NotNull BlinnPhongDirectionalLightComponent light) {
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
    static void refresh(@NotNull BlinnPhongPositionalLightComponent light) {
	if (light == null) {
	    throw new NullPointerException();
	}
	addToTheList(light);
	positionalLights.refresh(light);
    }

    private static void addToTheList(@NotNull BlinnPhongPositionalLightComponent light) {
	if (!Utility.containsReference(POSITIONAL, light)) {
	    POSITIONAL.add(light);
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
	positionalLights = new LightSourceTile();
	positionalLights.recreate();
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
	releaseDirectional();
	releasePositionals();
    }

    private static void releaseUbo() {
	if (isUsable()) {
	    ubo.release();
	    ubo = null;
	    LOG.fine("Light UBO released");
	}
    }

    private static void releaseDirectional() {
	if (directionalLight != null) {
	    directionalLight.setShaderIndex(-1);
	}
	directionalLight = null;
    }

    private static void releasePositionals() {
	if (positionalLights != null && positionalLights.isUsable()) {
	    positionalLights.release();
	    positionalLights = null;
	}
    }

    public static void makeUpToDate() {
	if (!isUsable()) {
	    recreate();
	}
	BlinnPhongLightSources.refresh();
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
	private final List<BlinnPhongPositionalLightComponent> lights = new ArrayList<>();
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

	private void refresh(@NotNull BlinnPhongPositionalLightComponent light) {
	    addIfNeeded(light);
	    removeIfNeeded(light);
	    refreshIfNeeded(light);
	}

	//
	//add
	//
	private void addIfNeeded(@NotNull BlinnPhongPositionalLightComponent light) {
	    if (light.getGameObject() != null && light.getShaderIndex() == -1) {
		int shaderIndex = computeNewShaderIndex();
		extendStorageIfNeeded(shaderIndex);
		light.setShaderIndex(shaderIndex);
		add(light, shaderIndex);
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

	private void add(@NotNull BlinnPhongPositionalLightComponent light, int shaderIndex) {
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
	private void removeIfNeeded(@NotNull BlinnPhongPositionalLightComponent light) {
	    if (light.getGameObject() == null && light.getShaderIndex() != -1) {
		int shaderIndex = light.getShaderIndex();
		lights.set(shaderIndex, null);
		removeLFromSsbo(light);
		light.setShaderIndex(-1);
		setCount();
	    }
	}

	/**
	 * Removes the given light source from the UBO.
	 *
	 * @param light BlinnPhongDirectionalLightComponent
	 */
	private void removeLFromSsbo(@NotNull BlinnPhongPositionalLightComponent light) {
	    IntBuffer metadata = light.computeInactiveMetadata();
	    ssbo.bind();
	    ssbo.storeData(metadata, light.getShaderIndex() * LIGHT_SIZE + ACTIVE_ADDRESS + LIGHT_SOURCES_OFFSET);
	    ssbo.unbind();
	    LOG.fine("Positional light removed from the SSBO");
	}

	//
	//refresh
	//
	private void refreshIfNeeded(@NotNull BlinnPhongPositionalLightComponent light) {
	    if (light.getGameObject() != null && light.getShaderIndex() != -1) {
		refreshInSsbo(light);
	    }
	}

	private void refreshInSsbo(@NotNull BlinnPhongPositionalLightComponent light) {
	    FloatBuffer parameters = light.computeLightParameters();
	    IntBuffer metadata = light.computeLightMetadata();
	    ssbo.bind();
	    ssbo.storeData(parameters, light.getShaderIndex() * LIGHT_SIZE + LIGHT_SOURCES_OFFSET);
	    ssbo.storeData(metadata, light.getShaderIndex() * LIGHT_SIZE + TYPE_ADDRESS + LIGHT_SOURCES_OFFSET);
	    ssbo.unbind();
	    LOG.fine("Positional light refreshed in the SSBO");
	}

	//
	//resources
	//
	public void recreate() {
	    createSsbo();
	    for (BlinnPhongPositionalLightComponent bplc : POSITIONAL) {
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
	    ssbo.setName("BP Positional Lights");
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
	    releasePositionals();
	}

	private void releaseSsbo() {
	    if (isUsable()) {
		ssbo.release();
		ssbo = null;
		LOG.fine("Light SSBO released");
	    }
	}

	private void releasePositionals() {
	    for (BlinnPhongPositionalLightComponent light : lights) {
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
