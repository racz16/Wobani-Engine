package wobani.component.light.blinnphong;

import java.nio.*;
import java.util.*;
import java.util.logging.*;
import org.joml.*;
import wobani.resources.buffers.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 * Stores the Blinn-Phong light sources for the UBO.
 */
public class BlinnPhongLightSources {
    //egy idő után csökkenjen ha túl sok az üres hely?
    //componentek átnézése
    //osztályokhoz equals, hashcode, tostring
    //javadoc

    private static final List<BlinnPhongDirectionalLightComponent> DIRECTIONAL = new ArrayList<>();
    private static final List<BlinnPhongPositionalLightComponent> POSITIONAL = new ArrayList<>();

    private static final List<BlinnPhongLightComponent> DIRTY = new ArrayList<>();

    private static BlinnPhongDirectionalLightComponent directionalLight;
    private static Ubo ubo;

    private static Ssbo zeroSsbo;
    //private static LightSourceTile positionalLights = new LightSourceTile();

    private static Map<Vector2i, LightSourceTile> tiles = new HashMap<>();
    private static int tileSize = 100;

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
    //positional----------------------------------------------------------------
    //
    @Internal
    static void refresh(@NotNull BlinnPhongPositionalLightComponent light) {
	if (light == null) {
	    throw new NullPointerException();
	}
	addToTheList(light);
	//positionalLights.refresh(light);
	refreshInTiles(light);
    }

    private static void addToTheList(@NotNull BlinnPhongPositionalLightComponent light) {
	if (!Utility.containsReference(POSITIONAL, light)) {
	    POSITIONAL.add(light);
	}
    }

    private static void refreshInTiles(@NotNull BlinnPhongPositionalLightComponent light) {
	LightSourceTile oldTile = tiles.get(light.getTilePosition());
	LightSourceTile newTile = null;
	if (light.getGameObject() != null) {
	    newTile = getTile(light.getGameObject().getTransform().getAbsolutePosition());
	}
	if (oldTile != null) {
	    oldTile.refresh(light);
	}
	if (light.getGameObject() != null) {
	    Vector3f absolutePosition = light.getGameObject().getTransform().getAbsolutePosition();
	    if (newTile == null) {
		newTile = new LightSourceTile(computeTilePosition(absolutePosition));
		tiles.put(computeTilePosition(absolutePosition), newTile);
	    }
	    if (newTile != oldTile) {
		newTile.refresh(light);
	    }
	}
    }

    @Nullable
    private static LightSourceTile getTile(@NotNull Vector3f absolutePosition) {
	return tiles.get(computeTilePosition(absolutePosition));
    }

    private static List<LightSourceTile> getClosestTiles(@NotNull Vector3f absolutePosition) {
	List<LightSourceTile> list = new ArrayList<>();
	Vector2i pos1 = computeTilePosition(absolutePosition);
	int x, y;
	if (java.lang.Math.abs(pos1.x + tileSize - absolutePosition.x) < java.lang.Math.abs(pos1.x - tileSize - absolutePosition.x)) {
	    x = tileSize;
	} else {
	    x = -tileSize;
	}
	if (java.lang.Math.abs(pos1.y + tileSize - absolutePosition.z) < java.lang.Math.abs(pos1.y - tileSize - absolutePosition.z)) {
	    y = tileSize;
	} else {
	    y = -tileSize;
	}
	Vector2i pos2 = new Vector2i(pos1.x + x, pos1.y + y), pos3 = new Vector2i(pos1.x + x, pos1.y), pos4 = new Vector2i(pos1.x, pos1.y + y);
	list.add(tiles.get(pos1));
	list.add(tiles.get(pos2));
	list.add(tiles.get(pos3));
	list.add(tiles.get(pos4));
	return list;
    }

    @NotNull
    private static Vector2i computeTilePosition(@NotNull Vector3f absolutePosition) {
	int x = computeTilePositionCoordinate(absolutePosition.x());
	int y = computeTilePositionCoordinate(absolutePosition.z());
	return new Vector2i(x, y);
    }

    private static int computeTilePositionCoordinate(float value) {
	int ret = (int) (value / tileSize) * tileSize;
	if (value > 0) {
	    return ret + tileSize / 2;
	} else {
	    return ret - tileSize / 2;
	}
    }

    //
    //resources-----------------------------------------------------------------
    //
    public static void setTileSize(int size) {
	if (size <= 0) {
	    throw new IllegalArgumentException("Tile size must be positive");
	}
	tileSize = size;
	release();
	recreate();
    }

    public static void initialize() {
	createUbo();
	createZeroSsbo();
    }

    public static void recreate() {
	createUbo();
	createZeroSsbo();
	recreateDirectional();
	tiles = new HashMap<>();
	for (BlinnPhongPositionalLightComponent bplc : POSITIONAL) {
	    bplc.refreshShader();
	}
    }

    private static void createUbo() {
	if (!isUsable()) {
	    createUboWithoutInspection();
	    LOG.fine("Light UBO created");
	}
    }

    private static void createZeroSsbo() {
	zeroSsbo = new Ssbo();
	zeroSsbo.bind();
	zeroSsbo.allocateMemory(4, false);
	zeroSsbo.storeData(new int[]{0}, 0);
	zeroSsbo.unbind();
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
	releaseZeroSsbo();
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

    private static void releaseZeroSsbo() {
	zeroSsbo.release();
	zeroSsbo = null;
    }

    private static void releaseDirectional() {
	if (directionalLight != null) {
	    directionalLight.setShaderIndex(-1);
	}
	directionalLight = null;
    }

    private static void releasePositionals() {
	for (LightSourceTile lst : tiles.values()) {
	    lst.release();
	}
	tiles = null;
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

    public static void bindClosestLightSources(@NotNull Vector3f absolutePosition) {
	List<LightSourceTile> lsts = getClosestTiles(absolutePosition);
	for (int i = 0; i < 4; i++) {
	    LightSourceTile lst = lsts.get(i);
	    if (lst != null) {
		lst.bindTo(i + 3);
	    } else {
		zeroSsbo.bindToBindingPoint(i + 3);
	    }
	}
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

	private final Vector2i tileCenter = new Vector2i();

	private static final Logger LOG = Logger.getLogger(LightSourceTile.class.getName());

	public LightSourceTile(@NotNull Vector2i center) {
	    tileCenter.set(center);
	    createSsbo();
	    extendListTo(1);
	}

	private void refresh(@NotNull BlinnPhongPositionalLightComponent light) {
	    removeIfNeeded(light);
	    addIfNeeded(light);
	    refreshIfNeeded(light);
	    if (rec) {
		rec = false;
		for (BlinnPhongPositionalLightComponent bpplc : lights) {
		    bpplc.refreshShader();
		}
	    }
	}

	//
	//add
	//
	private void addIfNeeded(@NotNull BlinnPhongPositionalLightComponent light) {
	    if (addCondition(light)) {
		System.out.println("ADDED");
		int shaderIndex = computeNewShaderIndex();
		extendStorageIfNeeded(shaderIndex);
		setSsboMetadata(light, shaderIndex);
		lights.set(shaderIndex, light);
		setCount();
	    }
	}

	private boolean addCondition(@NotNull BlinnPhongPositionalLightComponent light) {
	    boolean attached = light.getGameObject() != null;
	    boolean hasShaderIndex = light.getShaderIndex() != -1;
	    boolean thisTileContains = tileCenter.equals(light.getTilePosition());
	    boolean shouldBeInThisTile = false;
	    if (light.getGameObject() != null) {
		Vector3f absolutePosition = light.getGameObject().getTransform().getAbsolutePosition();
		Vector2i validTilePosition = BlinnPhongLightSources.computeTilePosition(absolutePosition);
		shouldBeInThisTile = tileCenter.equals(validTilePosition);
	    }
	    return attached && !hasShaderIndex && !thisTileContains && shouldBeInThisTile;
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

	private boolean rec;

	private void extendStorageIfNeeded(int shaderIndex) {
	    if (shaderIndex == lights.size()) {
		int size = lights.size() * 2;
		extendSsboTo(size);
		extendListTo(size);
	    }
	}

	private void setSsboMetadata(@NotNull BlinnPhongPositionalLightComponent light, int shaderIndex) {
	    light.setShaderIndex(shaderIndex);
	    light.setTilePosition(new Vector2i(tileCenter));
	}

	private void extendSsboTo(int size) {
	    rec = true;
	    ssbo.bind();
	    ssbo.allocateMemory(size * LIGHT_SIZE + LIGHT_SOURCES_OFFSET, false);
	    ssbo.unbind();
	}

	private void extendListTo(int size) {
	    while (lights.size() < size) {
		lights.add(null);
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
	    LOG.fine("Positional light removed from the SSBO");
	}

	//
	//remove
	//
	private void removeIfNeeded(@NotNull BlinnPhongPositionalLightComponent light) {
	    if (removeCondition(light)) {
		System.out.println("REMOVED");
		int shaderIndex = light.getShaderIndex();
		lights.set(shaderIndex, null);
		removeLFromSsbo(light);
		setSsboMetadataToInactive(light);
		setCount();
	    }
	}

	private boolean removeCondition(@NotNull BlinnPhongPositionalLightComponent light) {
	    boolean attached = light.getGameObject() != null;
	    boolean hasShaderIndex = light.getShaderIndex() != -1;
	    boolean thisTileContains = tileCenter.equals(light.getTilePosition());
	    boolean shouldBeInAnotherTile = false;
	    if (light.getGameObject() != null) {
		Vector3f absolutePosition = light.getGameObject().getTransform().getAbsolutePosition();
		Vector2i validTilePosition = BlinnPhongLightSources.computeTilePosition(absolutePosition);
		shouldBeInAnotherTile = !tileCenter.equals(validTilePosition);
	    }
	    return hasShaderIndex && thisTileContains && (!attached || shouldBeInAnotherTile);
	}

	private void setSsboMetadataToInactive(@NotNull BlinnPhongPositionalLightComponent light) {
	    light.setShaderIndex(-1);
	    light.setTilePosition(null);
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
	    if (refreshCondition(light)) {
		refreshInSsbo(light);
	    }
	}

	private boolean refreshCondition(@NotNull BlinnPhongPositionalLightComponent light) {
	    boolean attached = light.getGameObject() != null;
	    boolean hasShaderIndex = light.getShaderIndex() != -1;
	    boolean thisTileContains = tileCenter.equals(light.getTilePosition());

	    return attached && hasShaderIndex && thisTileContains;
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
	public void bindTo(int index) {
	    ssbo.bindToBindingPoint(index);
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
