package wobani.component.light.blinnphong;

import java.nio.*;
import java.util.*;
import java.util.logging.*;
import org.joml.*;
import static wobani.component.light.blinnphong.BlinnPhongShaderHelper.LIGHT_SIZE;
import static wobani.component.light.blinnphong.BlinnPhongShaderHelper.TYPE_ADDRESS;
import wobani.resources.buffers.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 * Stores the Blinn-Phong light sources and refreshes them in the VRAM if
 * necessary.
 */
public class BlinnPhongLightSources {

    /**
     * List of all Blinn-Phong directional light sources.
     */
    private static final List<BlinnPhongDirectionalLightComponent> DIRECTIONAL = new ArrayList<>();
    /**
     * The main directional light source.
     */
    private static BlinnPhongDirectionalLightComponent directionalLight;
    /**
     * This UBO stores the main directional light source in the VRAM.
     */
    private static Ubo ubo;

    /**
     * List of all Blinn-Phong positional light sources.
     */
    private static final List<BlinnPhongPositionalLightComponent> POSITIONAL = new ArrayList<>();
    /**
     * Light source tiles for storing positional lights.
     */
    private static Map<Vector2i, BlinnPhongLightSourceTile> tiles = new HashMap<>();
    /**
     * The size of the light tiles.
     */
    private static int tileSize = 100;
    /**
     * This SSBO used when one of the 4 closest light tiles aren't exist.
     */
    private static Ssbo zeroSsbo;

    /**
     * List of all Blinn-Phong light sources need to update in the current
     * frame.
     */
    private static final List<BlinnPhongLightComponent> DIRTY = new ArrayList<>();

    /**
     * The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(BlinnPhongLightSources.class.getName());

    /**
     * To can't create BlinnPhongLightSources instance.
     */
    private BlinnPhongLightSources() {
    }

    /**
     * Signs that the given light souce needs to be updated in the VRAM because
     * it is changed.
     *
     * @param light Blinn-Phong light source
     *
     * @see #refresh()
     */
    @Internal
    static void makeDirty(@NotNull BlinnPhongLightComponent light) {
	if (light == null) {
	    throw new NullPointerException();
	}
	if (!Utility.containsReference(DIRTY, light)) {
	    DIRTY.add(light);
	}
    }

    /**
     * Refreshes in the VRAM all the Blinn-Phong light sources changes since the
     * last frame. If the VGA side objects are released, this method recreates
     * them.
     */
    public static void refresh() {
	makeUsable();
	for (BlinnPhongLightComponent light : DIRTY) {
	    light.refreshLightInVram();
	}
	DIRTY.clear();
    }

    //
    //directional---------------------------------------------------------------
    //
    /**
     * Refreshes the given directional light source in the VRAM. It adds,
     * removes or updates the light if necessary.
     *
     * @param light directional light sources
     *
     * @throws NullPointerException parameter can't be null
     */
    @Internal
    static void refresh(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (light == null) {
	    throw new NullPointerException();
	}
	addToTheListIfNeeded(light);
	refreshLight(light);
    }

    /**
     * Adds the given parameter to the list of directional lights (if it isn't
     * contained yet).
     *
     * @param light directional light source
     */
    private static void addToTheListIfNeeded(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (!Utility.containsReference(DIRECTIONAL, light)) {
	    DIRECTIONAL.add(light);
	}
    }

    /**
     * Adds, removes or updates the light source if necessary.
     *
     * @param light directional light sources
     */
    private static void refreshLight(@NotNull BlinnPhongDirectionalLightComponent light) {
	removeIfNeeded(light);
	addIfNeeded(light);
	refreshIfNeeded(light);
    }

    //
    //remove
    //
    /**
     * Removes the given light if it's not the main directional light and not
     * already removed.
     *
     * @param light directional light source
     */
    private static void removeIfNeeded(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (!light.isTheMainDirectionalLight()) {
	    if (light == directionalLight) {
		directionalLight = null;
	    }
	    light.setShaderIndex(-1);
	}
    }

    //
    //add
    //
    /**
     * Adds the given light if it's the main directional light and not already
     * added.
     *
     * @param light directional light source
     */
    private static void addIfNeeded(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (light.isTheMainDirectionalLight()) {
	    light.setShaderIndex(0);
	    if (light != directionalLight) {
		directionalLight = light;
	    }
	}
    }

    //
    //refresh
    //
    /**
     * Refreshes the given light in the UBO if it is the main directional light.
     *
     * @param light directional light source
     */
    private static void refreshIfNeeded(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (light == directionalLight) {
	    refreshDirectionalInUbo();
	}
    }

    /**
     * Refreshes the main directional light in the UBO.
     */
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
    //positional----------------------------------------------------------------
    //
    //FIXME: ha releaselek, nem azonnal, de a point light kiesik
    /**
     * Refreshes the given positional light source in the VRAM. It adds, removes
     * or updates the light if necessary.
     *
     * @param light positional light sources
     *
     * @throws NullPointerException parameter can't be null
     */
    @Internal
    static void refresh(@NotNull BlinnPhongPositionalLightComponent light) {
	if (light == null) {
	    throw new NullPointerException();
	}
	addToTheListIfNeeded(light);
	refreshInTiles(light);
    }

    /**
     * Adds the given parameter to the list of positional lights (if it isn't
     * contained yet).
     *
     * @param light positional light source
     */
    private static void addToTheListIfNeeded(@NotNull BlinnPhongPositionalLightComponent light) {
	if (!Utility.containsReference(POSITIONAL, light)) {
	    POSITIONAL.add(light);
	}
    }

    /**
     * Adds, removes or updates the light source in the tiles if necessary.
     *
     * @param light positional light sources
     */
    private static void refreshInTiles(@NotNull BlinnPhongPositionalLightComponent light) {
	refreshPreviousTile(light);
	refreshCurrentTile(light);
    }

    /**
     * Adds, removes or updates the light source in the previous tile if
     * necessary.
     *
     * @param light positional light sources
     */
    private static void refreshPreviousTile(@NotNull BlinnPhongPositionalLightComponent light) {
	BlinnPhongLightSourceTile previousTile = getPreviousTile(light);
	if (previousTile != null) {
	    previousTile.refresh(light);
	}
    }

    /**
     * Adds, removes or updates the light source in the current tile if
     * necessary. If the current tile doesn't exists, it creates the tile.
     *
     * @param light positional light sources
     */
    private static void refreshCurrentTile(@NotNull BlinnPhongPositionalLightComponent light) {
	if (light.getGameObject() != null) {
	    BlinnPhongLightSourceTile currentTile = getOrCreateTile(light);
	    currentTile.refresh(light);
	}
    }

    /**
     * Returns the given light's current tile. If it doesn't exist, this method
     * creates a new tile and adds the light to it.
     *
     * @param light positional light source
     *
     * @return the given light source's current tile
     */
    @NotNull
    private static BlinnPhongLightSourceTile getOrCreateTile(@NotNull BlinnPhongPositionalLightComponent light) {
	BlinnPhongLightSourceTile tile = getCurrentTile(light);
	if (tile == null) {
	    Vector3f absolutePosition = light.getGameObject().getTransform().getAbsolutePosition();
	    tile = new BlinnPhongLightSourceTile(computeTilePosition(absolutePosition));
	    tiles.put(computeTilePosition(absolutePosition), tile);
	}
	return tile;
    }

    /**
     * Returns the given light source's previous tile.
     *
     * @param light positional light source
     *
     * @return the given light source's previous tile
     */
    @Nullable
    private static BlinnPhongLightSourceTile getPreviousTile(@NotNull BlinnPhongPositionalLightComponent light) {
	return getTileBasedOnTileCenter(light.getTilePosition());
    }

    /**
     * Returns the given light source's current tile. If the light isn't
     * attached to a GameObject, this method returns null value.
     *
     * @param light positional light source
     *
     * @return the given light source's current tile
     */
    @Nullable
    private static BlinnPhongLightSourceTile getCurrentTile(@NotNull BlinnPhongPositionalLightComponent light) {
	if (light.getGameObject() != null) {
	    return getTileBasedOnPosition(light.getGameObject().getTransform().getAbsolutePosition());
	}
	return null;
    }

    /**
     * Returns the tile responsible for the given position.
     *
     * @param absolutePosition position
     *
     * @return the tile responsible for the given position
     */
    @Nullable
    private static BlinnPhongLightSourceTile getTileBasedOnPosition(@NotNull Vector3f absolutePosition) {
	return tiles.get(computeTilePosition(absolutePosition));
    }

    /**
     * Returns the tile corresponds to the given tile center.
     *
     * @param tileCenter tile's center position
     *
     * @return the tile corresponds to the given tile center
     */
    @Nullable
    private static BlinnPhongLightSourceTile getTileBasedOnTileCenter(@Nullable Vector2i tileCenter) {
	return tiles.get(tileCenter);
    }

    /**
     * Binds the 4 closest tile's SSBO to the given position.
     *
     * @param absolutePosition position
     */
    public static void bindClosestLightSources(@NotNull Vector3f absolutePosition) {
	List<BlinnPhongLightSourceTile> tiles = getClosestTiles(absolutePosition);
	for (int i = 0; i < tiles.size(); i++) {
	    bindTile(tiles.get(i), i);
	}
    }

    /**
     * Binds the given tile's SSBO to the binding point. If the tile is null,
     * this method binds the zero SSBO to the binding point.
     *
     * @param tile  tile
     * @param index index (binding point is index + 3)
     */
    private static void bindTile(@Nullable BlinnPhongLightSourceTile tile, int index) {
	if (tile != null) {
	    tile.bindTo(index + 3);
	} else {
	    zeroSsbo.bindToBindingPoint(index + 3);
	}
    }

    /**
     * Returns the 4 closest tiles to the given position.
     *
     * @param absolutePosition position
     *
     * @return the 4 closest tiles to the position
     */
    @NotNull
    private static List<BlinnPhongLightSourceTile> getClosestTiles(@NotNull Vector3f absolutePosition) {
	List<BlinnPhongLightSourceTile> closestTiles = new ArrayList<>();
	Vector2i closestTileCenter = computeTilePosition(absolutePosition);
	Vector2i offset = computeOffset(absolutePosition, closestTileCenter);
	for (Vector2i tileCenter : computeClosestTileCenters(closestTileCenter, offset)) {
	    closestTiles.add(tiles.get(tileCenter));
	}
	return closestTiles;
    }

    /**
     * Returns the 4 closest tile centers based on the given closest tile center
     * and offset.
     *
     * @param closestTileCenter the center of the closest tile
     * @param offset            offset
     *
     * @return the 4 closest tiles
     */
    @NotNull
    private static List<Vector2i> computeClosestTileCenters(@NotNull Vector2i closestTileCenter, @NotNull Vector2i offset) {
	List<Vector2i> tileCenters = new ArrayList<>();
	tileCenters.add(closestTileCenter);
	tileCenters.add(new Vector2i(closestTileCenter.x + offset.x, closestTileCenter.y + offset.y));
	tileCenters.add(new Vector2i(closestTileCenter.x + offset.x, closestTileCenter.y));
	tileCenters.add(new Vector2i(closestTileCenter.x, closestTileCenter.y + offset.y));
	return tileCenters;
    }

    /**
     * Returns the offset for the closest tiles. You can get the 4 closest tiles
     * by adding to the closest tile (0; 0), (offset.x; 0), (0; offset.y),
     * (offset.x; offset.y).
     *
     * @param absolutePosition  position
     * @param closestTileCenter closest tile's center
     *
     * @return the offset for the closest tiles
     */
    @NotNull
    private static Vector2i computeOffset(@NotNull Vector3f absolutePosition, @NotNull Vector2i closestTileCenter) {
	Vector2i offset = new Vector2i();
	offset.x = computeOffsetCoordinate(closestTileCenter.x, absolutePosition.x);
	offset.y = computeOffsetCoordinate(closestTileCenter.y, absolutePosition.y);
	return offset;
    }

    /**
     * Returns one of the offset vector's coordinates.
     *
     * @param closestTileCenterCoordinate one of the closest tile's coordinates
     * @param absolutePositionCoordinate  one of the absolute position's
     *                                    coordinates
     *
     * @return one of the offset vector's coordinates
     */
    private static int computeOffsetCoordinate(int closestTileCenterCoordinate, float absolutePositionCoordinate) {
	if (java.lang.Math.abs(closestTileCenterCoordinate + tileSize - absolutePositionCoordinate) < java.lang.Math.abs(closestTileCenterCoordinate - tileSize - absolutePositionCoordinate)) {
	    return tileSize;
	} else {
	    return -tileSize;
	}
    }

    /**
     * Computes the tile's (center) position based the given absolute position.
     *
     * @param absolutePosition object's absolute position
     *
     * @return the tile's (center) position
     */
    @NotNull
    static Vector2i computeTilePosition(@NotNull Vector3f absolutePosition) {
	int x = computeTilePositionCoordinate(absolutePosition.x());
	int y = computeTilePositionCoordinate(absolutePosition.z());
	return new Vector2i(x, y);
    }

    /**
     * Computes one of the tile's (center) position coordinates based the given
     * absolute position coordinate.
     *
     * @param absolutePositionCoordinate object's absolute position
     *
     * @return one coordinate of the tile's (center) position
     */
    private static int computeTilePositionCoordinate(float absolutePositionCoordinate) {
	int ret = (int) (absolutePositionCoordinate / tileSize) * tileSize;
	if (absolutePositionCoordinate > 0) {
	    return ret + tileSize / 2;
	} else {
	    return ret - tileSize / 2;
	}
    }

    /**
     * Removes the tile corresponds to the given tile center.
     *
     * @param tileCenter tile's center
     */
    @Internal
    static void removeTile(@NotNull Vector2i tileCenter) {
	tiles.put(tileCenter, null);
    }

    //
    //resources-----------------------------------------------------------------
    //
    static {
	initialize();
    }

    /**
     * Creates the VGA side buffer objects.
     */
    private static void initialize() {
	createUbo();
	createZeroSsbo();
    }

    /**
     * Recreates the whole system and fills the buffers with the correct light
     * data.
     */
    public static void makeUsable() {
	initialize();
	recreateDirectional();
	recreatePositionals();
    }

    /**
     * Refreshes the main directional light in the UBO.
     */
    private static void recreateDirectional() {
	if (directionalLight == null) {
	    for (BlinnPhongDirectionalLightComponent bpdlc : DIRECTIONAL) {
		bpdlc.refreshLightInVram();
	    }
	}
    }

    /**
     * Refreshes the positional lights in the SSBOs.
     */
    private static void recreatePositionals() {
	if (tiles == null) {
	    tiles = new HashMap<>();
	    for (BlinnPhongPositionalLightComponent bplc : POSITIONAL) {
		bplc.refreshLightInVram();
	    }
	}
    }

    /**
     * Releases the whole system including the VGA side buffers.
     */
    public static void release() {
	releaseUbo();
	releaseZeroSsbo();
	releaseDirectional();
	releasePositionals();
    }

    /**
     * Releases the main directional light source.
     */
    private static void releaseDirectional() {
	if (directionalLight != null) {
	    directionalLight.setShaderIndex(-1);
	}
	directionalLight = null;
    }

    /**
     * Releases the positional lights, tiles and the corresponding SSBOs.
     */
    private static void releasePositionals() {
	if (tiles != null) {
	    for (BlinnPhongLightSourceTile lst : tiles.values()) {
		lst.release();
	    }
	}
	tiles = null;
    }

    /**
     * Returns true if the system and the VGA side buffers are usable, and false
     * if thely're released.
     *
     * @return true if the system and the VGA side buffers are usable, false
     *         otherwise
     */
    public static boolean isUsable() {
	return Utility.isUsable(ubo);
    }

    /**
     * Returns the positional lights' tile size.
     *
     * @return the positional lights' tile size
     */
    public static int getTileSize() {
	return tileSize;
    }

    /**
     * Sets the positional lights' tile size to the given value.
     *
     * @param size tile size
     *
     * @throws IllegalArgumentException Tile size must be positive
     */
    public static void setTileSize(int size) {
	if (size <= 0) {
	    throw new IllegalArgumentException("Tile size must be positive");
	}
	setTileSizeUnsafe(size);
    }

    /**
     * Sets the positional lights' tile size to the given value.
     *
     * @param size tile size
     */
    private static void setTileSizeUnsafe(int size) {
	tileSize = size;
	if (!isUsable()) {
	    makeUsable();
	} else {
	    releasePositionals();
	    recreatePositionals();
	}
    }

    //
    //UBO
    //
    /**
     * Creates a new UBO for the main directional light if the last UBO isn't
     * usable.
     */
    private static void createUbo() {
	if (!isUsable()) {
	    createUboUnsafe();
	    LOG.fine("Light UBO created");
	}
    }

    /**
     * Creates a UBO for the main directional light and binds to the 2nd binding
     * point.
     */
    private static void createUboUnsafe() {
	ubo = new Ubo();
	ubo.bind();
	ubo.setName("BP Directional Light");
	ubo.allocateMemory(LIGHT_SIZE, false);
	ubo.unbind();
	ubo.bindToBindingPoint(2);
    }

    /**
     * Releases the main directional light's UBO if it's usable.
     */
    private static void releaseUbo() {
	if (isUsable()) {
	    ubo.release();
	    ubo = null;
	    LOG.fine("Light UBO released");
	}
    }

    //
    //zero SSBO
    //
    /**
     * Creates a new zero SSBO if the last one isn't usable and fills it with a
     * 0.
     */
    private static void createZeroSsbo() {
	if (!Utility.isUsable(zeroSsbo)) {
	    createZeroSsboUnsafe();
	    LOG.fine("Zero SSBO created");
	}
    }

    /**
     * Creates the zero SSBO and fills it with a 0.
     */
    private static void createZeroSsboUnsafe() {
	zeroSsbo = new Ssbo();
	zeroSsbo.bind();
	zeroSsbo.allocateMemory(4, false);
	zeroSsbo.storeData(new int[]{0}, 0);
	zeroSsbo.unbind();
    }

    /**
     * Releases the zero SSBO.
     */
    private static void releaseZeroSsbo() {
	if (Utility.isUsable(zeroSsbo)) {
	    zeroSsbo.release();
	    zeroSsbo = null;
	    LOG.fine("Zero SSBO released");
	}
    }

}
