package wobani.component.light;

import java.nio.*;
import java.util.*;
import java.util.logging.*;
import org.lwjgl.*;
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
    //fényforrás tulajdonságait csak akkor frissíteni shaderben, ha a fényforrás aktív
    //egy framen belüli változások egyszeri frissítése a shaderben (camera ubo-nál is)
    //egy idő után csökkenjen ha túl sok az üres hely?
    //nondirectional light component?
    //	tárolhatná a last positiont
    //	meg amúgy itt kicsit kevesebb metódus kéne
    //	meg amúgy az ottani refreshLight hívást is egységesíteni lehetne
    //	másik tile-ba átsorolásnál hasznos lenne
    //componentek átnézése
    //osztályokhoz equals, hashcode, tostring
    //javadoc

    private enum LightType {
	DIRECTIONAL(0),
	POINT(1),
	SPOT(2);

	private final int code;

	private LightType(int code) {
	    this.code = code;
	}

	public int getCode() {
	    return code;
	}
    }

    private static final List<BlinnPhongDirectionalLightComponent> DIRECTIONAL = new ArrayList<>();
    private static final List<BlinnPhongLightComponent> NONDIRECTIONAL = new ArrayList<>();

    private static BlinnPhongDirectionalLightComponent directionalLight;
    private static Ubo ubo;
    private static LightSourceTile nondirectionalLights = new LightSourceTile();

    private static final DataStructure DATA = new DataStructure();

    /**
     * The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(BlinnPhongLightSources.class.getName());

    /**
     * To can't create BlinnPhongLightSources instance.
     */
    private BlinnPhongLightSources() {
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
		removeDirectionalBuffer();
		removeDirectionalFromUbo();
	    }
	    light.setShaderIndex(-1);
	}
    }

    private static void removeDirectionalBuffer() {
	DATA.setIntBufferPosition(0);
	DATA.setIntBufferLimit(1);
	DATA.setInactive();
	DATA.setIntBufferPosition(0);
    }

    /**
     * Removes the given light source from the UBO.
     */
    private static void removeDirectionalFromUbo() {
	ubo.bind();
	ubo.storeData(DATA.getIntBuffer(), DataStructure.ACTIVE_ADDRESS);
	ubo.unbind();
	LOG.fine("Directional light removed from the UBO");
    }

    //
    //refresh
    //
    private static void refreshDirectionalIfNeeded(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (isUsable()) {
	    if (light == directionalLight) {
		refreshDirectionalShader();
	    }
	} else {
	    recreate();
	}
    }

    private static void refreshDirectionalShader() {
	refreshDirectionalBuffer();
	refreshDirectionalInUbo();
    }

    private static void refreshDirectionalBuffer() {
	refreshDirectionalParameters();
	refreshDirectionalMetaData();
    }

    /**
     * Refreshes the given light source's parameters in the UBO.
     */
    private static void refreshDirectionalParameters() {
	DATA.setFloatBufferPosition(0);
	DATA.setFloatBufferLimit(16);
	DATA.setColor(directionalLight);
	DATA.setDirection(directionalLight);
	DATA.setFloatBufferPosition(0);
    }

    private static void refreshDirectionalMetaData() {
	DATA.setIntBufferPosition(0);
	DATA.setIntBufferLimit(2);
	DATA.setMetaData(LightType.DIRECTIONAL, directionalLight.isActive());
	DATA.setIntBufferPosition(0);
    }

    private static void refreshDirectionalInUbo() {
	ubo.bind();
	ubo.storeData(DATA.getFloatBuffer(), 0);
	ubo.storeData(DATA.getIntBuffer(), DataStructure.TYPE_ADDRESS);
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
    static void refreshPoint(@NotNull BlinnPhongPointLightComponent light) {
	if (light == null) {
	    throw new NullPointerException();
	}
	addNondirectionalToTheList(light);
	nondirectionalLights.refreshPoint(light);
    }

    @Internal
    static void refreshSpot(@NotNull BlinnPhongSpotLightComponent light) {
	if (light == null) {
	    throw new NullPointerException();
	}
	addNondirectionalToTheList(light);
	nondirectionalLights.refreshSpot(light);
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
	ubo.allocateMemory(DataStructure.LIGHT_SOURCE_SIZE, false);
	ubo.unbind();
	ubo.bindToBindingPoint(1);
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

    public static boolean isUsable() {
	return Utility.isUsable(ubo);
    }

    //
    //
    //
    private static class DataStructure {

	/**
	 * FloatBuffer for frequent UBO updates.
	 */
	private final FloatBuffer FLOAT_BUFFER;
	/**
	 * IntBuffer for frequent UBO updates.
	 */
	private final IntBuffer INT_BUFFER;

	/**
	 * One light's size in the UBO.
	 */
	public static final int LIGHT_SOURCE_SIZE = 112;
	/**
	 * The type variable's address in the UBO.
	 */
	public static final int TYPE_ADDRESS = 104;
	/**
	 * The active variable's address in the UBO.
	 */
	public static final int ACTIVE_ADDRESS = 108;

	public DataStructure() {
	    FLOAT_BUFFER = BufferUtils.createFloatBuffer(26);
	    INT_BUFFER = BufferUtils.createIntBuffer(2);
	}

	public FloatBuffer getFloatBuffer() {
	    return FLOAT_BUFFER;
	}

	public IntBuffer getIntBuffer() {
	    return INT_BUFFER;
	}

	public void setFloatBufferPosition(int pos) {
	    FLOAT_BUFFER.position(pos);
	}

	public void setIntBufferPosition(int pos) {
	    INT_BUFFER.position(pos);
	}

	public void setFloatBufferLimit(int limit) {
	    FLOAT_BUFFER.limit(limit);
	}

	public void setIntBufferLimit(int limit) {
	    INT_BUFFER.limit(limit);
	}

	public int getFloatBufferCapacity() {
	    return FLOAT_BUFFER.capacity();
	}

	public int getIntBufferCapacity() {
	    return INT_BUFFER.capacity();
	}

	/**
	 * Sets the given light source's position in the UBO.
	 *
	 * @param light BlinnPhongLightComponent
	 */
	private void setPosition(@NotNull BlinnPhongLightComponent light) {
	    for (int i = 0; i < 3; i++) {
		FLOAT_BUFFER.put(light.getGameObject().getTransform().getAbsolutePosition().get(i));
	    }
	    FLOAT_BUFFER.put(-1);
	}

	/**
	 * Sets the given light source's direction in the UBO.
	 *
	 * @param light BlinnPhongLightComponent
	 */
	private void setDirection(@NotNull BlinnPhongLightComponent light) {
	    for (int i = 0; i < 3; i++) {
		FLOAT_BUFFER.put(light.getGameObject().getTransform().getForwardVector().get(i));
	    }
	    FLOAT_BUFFER.put(-1);
	}

	/**
	 * Sets the light source's attenutation in the UBO.
	 *
	 * @param constant  attenutation constant component
	 * @param linear    attenutation linear component
	 * @param quadratic attenutation quadratic component
	 */
	private void setAttenutation(float constant, float linear, float quadratic) {
	    FLOAT_BUFFER.put(constant);
	    FLOAT_BUFFER.put(linear);
	    FLOAT_BUFFER.put(quadratic);
	    FLOAT_BUFFER.put(-1);
	}

	/**
	 * Sets the given light source's diffuse, specular and ambient color in
	 * the UBO.
	 *
	 * @param light BlinnPhongLightComponent
	 */
	private void setColor(@NotNull BlinnPhongLightComponent light) {
	    setAmbient(light);
	    setDiffuse(light);
	    setSpecular(light);
	}

	/**
	 * Sets the given light source's ambient color in the UBO.
	 *
	 * @param light BlinnPhongLightComponent
	 */
	private void setAmbient(@NotNull BlinnPhongLightComponent light) {
	    for (int i = 0; i < 3; i++) {
		FLOAT_BUFFER.put(light.getAmbientColor().get(i));
	    }
	    FLOAT_BUFFER.put(-1);
	}

	/**
	 * Sets the given light source's diffuse color in the UBO.
	 *
	 * @param light BlinnPhongLightComponent
	 */
	private void setDiffuse(@NotNull BlinnPhongLightComponent light) {
	    for (int i = 0; i < 3; i++) {
		FLOAT_BUFFER.put(light.getDiffuseColor().get(i));
	    }
	    FLOAT_BUFFER.put(-1);
	}

	/**
	 * Sets the given light source's specular color in the UBO.
	 *
	 * @param light BlinnPhongLightComponent
	 */
	private void setSpecular(@NotNull BlinnPhongLightComponent light) {
	    for (int i = 0; i < 3; i++) {
		FLOAT_BUFFER.put(light.getSpecularColor().get(i));
	    }
	    FLOAT_BUFFER.put(-1);
	}

	/**
	 * Sets the given light source's cutoff and outer cutoff in the UBO.
	 *
	 * @param light BlinnPhongSpotLightComponent
	 */
	private void setCutoff(@NotNull BlinnPhongSpotLightComponent light) {
	    FLOAT_BUFFER.put((float) Math.cos(Math.toRadians(light.getCutoff())));
	    FLOAT_BUFFER.put((float) Math.cos(Math.toRadians(light.getOuterCutoff())));
	}

	/**
	 * Sets the next 4 floats to -1 in the UBO (for example directional
	 * light's position etc).
	 */
	private void setFloatNone() {
	    for (int i = 0; i < 4; i++) {
		FLOAT_BUFFER.put(-1);
	    }
	}

	/**
	 * Refreshes the light source's type and activeness in the UBO.
	 *
	 * @param type   the light source's type
	 * @param active determines whether the Component is active
	 */
	private void setMetaData(@NotNull LightType type, boolean active) {
	    INT_BUFFER.put(type.getCode());
	    INT_BUFFER.put(active ? 1 : 0);
	}

	private void setInactive() {
	    INT_BUFFER.put(0);
	}

	private void setCount(int count) {
	    INT_BUFFER.put(count);
	}
    }

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

	private void refreshPoint(@NotNull BlinnPhongPointLightComponent light) {
	    addLightIfNeeded(light);
	    removeLightIfNeeded(light);
	    refreshPointIfNeeded(light);
	}

	private void refreshSpot(@NotNull BlinnPhongSpotLightComponent light) {
	    addLightIfNeeded(light);
	    removeLightIfNeeded(light);
	    refreshSpotIfNeeded(light);
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
	    ssbo.allocateMemory(size * DataStructure.LIGHT_SOURCE_SIZE + LIGHT_SOURCES_OFFSET, false);
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
	    refreshCountBuffer();
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

	private void refreshCountBuffer() {
	    DATA.setIntBufferPosition(0);
	    DATA.setIntBufferLimit(1);
	    DATA.setCount(count);
	    DATA.setIntBufferPosition(0);
	}

	private void refreshCountInSsbo() {
	    ssbo.bind();
	    ssbo.storeData(DATA.getIntBuffer(), 0);
	    ssbo.unbind();
	}

	//
	//remove
	//
	private void removeLightIfNeeded(@NotNull BlinnPhongLightComponent light) {
	    if (light.getGameObject() == null && light.getShaderIndex() != -1) {
		int shaderIndex = light.getShaderIndex();
		lights.set(shaderIndex, null);
		removeLightBuffer();
		removeLightFromSsbo(light);
		light.setShaderIndex(-1);
		setCount();
	    }
	}

	private void removeLightBuffer() {
	    DATA.setIntBufferPosition(0);
	    DATA.setIntBufferLimit(1);
	    DATA.setInactive();
	    DATA.setIntBufferPosition(0);
	}

	/**
	 * Removes the given light source from the UBO.
	 *
	 * @param light BlinnPhongDirectionalLightComponent
	 */
	private void removeLightFromSsbo(@NotNull BlinnPhongLightComponent light) {
	    ssbo.bind();
	    ssbo.storeData(DATA.getIntBuffer(), light.getShaderIndex() * DataStructure.LIGHT_SOURCE_SIZE + DataStructure.ACTIVE_ADDRESS + LIGHT_SOURCES_OFFSET);
	    ssbo.unbind();
	    LOG.fine("Nondirectional light removed from the SSBO");
	}

	//
	//refresh
	//
	private void refreshPointIfNeeded(@NotNull BlinnPhongPointLightComponent light) {
	    if (light.getGameObject() != null && light.getShaderIndex() != -1) {
		refreshPointBuffer(light);
		refreshLightInSsbo(light);
	    }
	}

	private void refreshSpotIfNeeded(@NotNull BlinnPhongSpotLightComponent light) {
	    if (light.getGameObject() != null && light.getShaderIndex() != -1) {
		refreshSpotBuffer(light);
		refreshLightInSsbo(light);
	    }
	}

	private void refreshPointBuffer(@NotNull BlinnPhongPointLightComponent light) {
	    refreshPointParameters(light);
	    refreshPointMetaData(light);
	}

	private void refreshSpotBuffer(@NotNull BlinnPhongSpotLightComponent light) {
	    refreshSpotParameters(light);
	    refreshSpotMetaData(light);
	}

	private void refreshPointParameters(@NotNull BlinnPhongPointLightComponent light) {
	    DATA.setFloatBufferPosition(0);
	    DATA.setFloatBufferLimit(24);
	    DATA.setColor(light);
	    DATA.setFloatNone();    //direction
	    DATA.setPosition(light);
	    DATA.setAttenutation(light.getConstant(), light.getLinear(), light.getQuadratic());
	    DATA.setFloatBufferPosition(0);
	}

	private void refreshPointMetaData(@NotNull BlinnPhongPointLightComponent light) {
	    DATA.setIntBufferPosition(0);
	    DATA.setIntBufferLimit(2);
	    DATA.setMetaData(LightType.POINT, light.isActive());
	    DATA.setIntBufferPosition(0);
	}

	private void refreshSpotMetaData(@NotNull BlinnPhongSpotLightComponent light) {
	    DATA.setIntBufferPosition(0);
	    DATA.setIntBufferLimit(2);
	    DATA.setMetaData(LightType.SPOT, light.isActive());
	    DATA.setIntBufferPosition(0);
	}

	/**
	 * Refreshes the given light source's parameters in the UBO.
	 *
	 * @param light BlinnPhongDirectionalLightComponent
	 */
	private void refreshSpotParameters(@NotNull BlinnPhongSpotLightComponent light) {
	    DATA.setFloatBufferPosition(0);
	    DATA.setFloatBufferLimit(26);
	    DATA.setColor(light);
	    DATA.setDirection(light);
	    DATA.setPosition(light);
	    DATA.setAttenutation(light.getConstant(), light.getLinear(), light.getQuadratic());
	    DATA.setCutoff(light);
	    DATA.setFloatBufferPosition(0);
	}

	private void refreshLightInSsbo(@NotNull BlinnPhongLightComponent light) {
	    ssbo.bind();
	    ssbo.storeData(DATA.getFloatBuffer(), light.getShaderIndex() * DataStructure.LIGHT_SOURCE_SIZE + LIGHT_SOURCES_OFFSET);
	    ssbo.storeData(DATA.getIntBuffer(), light.getShaderIndex() * DataStructure.LIGHT_SOURCE_SIZE + DataStructure.TYPE_ADDRESS + LIGHT_SOURCES_OFFSET);
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
	    ssbo.allocateMemory(DataStructure.LIGHT_SOURCE_SIZE + LIGHT_SOURCES_OFFSET, false);
	    ssbo.unbind();
	    ssbo.bindToBindingPoint(2);
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
