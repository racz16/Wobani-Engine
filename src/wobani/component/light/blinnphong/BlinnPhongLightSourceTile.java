package wobani.component.light.blinnphong;

import java.nio.*;
import java.util.*;
import java.util.logging.*;
import org.joml.*;
import static wobani.component.light.blinnphong.BlinnPhongShaderHelper.ACTIVE_ADDRESS;
import static wobani.component.light.blinnphong.BlinnPhongShaderHelper.LIGHT_SIZE;
import static wobani.component.light.blinnphong.BlinnPhongShaderHelper.LIGHT_SOURCES_OFFSET;
import static wobani.component.light.blinnphong.BlinnPhongShaderHelper.TYPE_ADDRESS;
import wobani.resources.buffers.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

public class BlinnPhongLightSourceTile {

    /**
     * The LightSources UBO's lights.
     */
    private final List<BlinnPhongPositionalLightComponent> lights = new ArrayList<>();
    /**
     * The LightSources UBO.
     */
    private Ssbo ssbo;

    private int count = 0;

    private final Vector2i tileCenter = new Vector2i();

    private static final Logger LOG = Logger.getLogger(BlinnPhongLightSourceTile.class.getName());

    public BlinnPhongLightSourceTile(@NotNull Vector2i center) {
	tileCenter.set(center);
	createSsbo();
	extendListTo(1);
    }

    public void refresh(@NotNull BlinnPhongPositionalLightComponent light) {
	removeIfNeeded(light);
	addIfNeeded(light);
	refreshIfNeeded(light);
	if (rec) {
	    rec = false;
	    for (BlinnPhongPositionalLightComponent bpplc : lights) {
		bpplc.refreshLightInVram();
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
	    changeSsboSizeTo(size);
	    extendListTo(size);
	}
    }

    private void setSsboMetadata(@NotNull BlinnPhongPositionalLightComponent light, int shaderIndex) {
	light.setShaderIndex(shaderIndex);
	light.setTilePosition(new Vector2i(tileCenter));
    }

    private void changeSsboSizeTo(int lightCount) {
	rec = true;
	ssbo.bind();
	ssbo.allocateMemory(lightCount * LIGHT_SIZE + LIGHT_SOURCES_OFFSET, false);
	ssbo.unbind();
    }

    private void extendListTo(int size) {
	while (lights.size() < size) {
	    lights.add(null);
	}
    }

    private void shrinkListTo(int size) {
	while (lights.size() > size) {
	    lights.remove(size);
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
	ssbo.bind();
	ssbo.storeData(new int[]{0}, light.getShaderIndex() * LIGHT_SIZE + ACTIVE_ADDRESS + LIGHT_SOURCES_OFFSET);
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
    private long lastActive;
    private long vramTimeLimit = 30000;

    public long getActionTimeLimit() {
	return vramTimeLimit;
    }

    public void setActionTimeLimit(long actionTimeLimit) {
	if (actionTimeLimit <= 0) {
	    throw new IllegalArgumentException("VRAM time limit have to be higher than 0");
	}
	this.vramTimeLimit = actionTimeLimit;
    }

    public long getLastActive() {
	return lastActive;
    }

    public void setLastActiveToNow() {
	lastActive = System.currentTimeMillis();
    }

    public void updateSsboState() {
	int elementCount = 0;
	for (int i = 0; i < lights.size(); i++) {
	    if (lights.get(i) != null) {
		elementCount++;
	    }
	}
	if (elementCount > lights.size() / 2) {
	    setLastActiveToNow();
	}
	long elapsedTime = System.currentTimeMillis() - getLastActive();
	if (elapsedTime > getActionTimeLimit()) {
	    if (lights.size() == 1) {
		release();
		BlinnPhongLightSources.removeTile(tileCenter);
	    } else {
		int placeIndex = -1;
		for (int lightIndex = 0; lightIndex < lights.size(); lightIndex++) {
		    if (lights.get(lightIndex) != null && placeIndex != -1) {
			lights.set(placeIndex, lights.get(lightIndex));
			lights.set(lightIndex, null);
			lights.get(placeIndex).setShaderIndex(placeIndex);
			placeIndex = -1;
		    }
		    if (lights.get(lightIndex) == null && placeIndex == -1) {
			placeIndex = lightIndex;
		    }
		}
		changeSsboSizeTo(lights.size() / 2);
		shrinkListTo(lights.size() / 2);
		for (BlinnPhongPositionalLightComponent bpplc : lights) {
		    bpplc.refreshLightInVram();
		    //FIXME: ssbo kicsinyítésekor és nagyításakor üres helyeket invalidálni
		    //nagyításkor az új helyen memóriszemét lehet, amit fényforrásként értelmezhet
		    //kicsinyítéskor az átrendezés miatt üres helyen memóriaszemét lehet
		    //legalább mindne üres helyre egy 0-t az aktiv mezőbe be kéne rakni az ssbo-ba
		}
	    }
	}
    }

    public void bindTo(int index) {
	ssbo.bindToBindingPoint(index);
    }

    /**
     * Creates the UBO.
     */
    private void createSsbo() {
	if (!isUsable()) {
	    createSsboUnsafe();
	    LOG.fine("Light SSBO created");
	}
    }

    /**
     * Creates the UBO.
     */
    private void createSsboUnsafe() {
	ssbo = new Ssbo();
	ssbo.bind();
	ssbo.setName("BP Positional Lights");
	ssbo.allocateMemory(LIGHT_SIZE + LIGHT_SOURCES_OFFSET, false);
	ssbo.unbind();
    }

    /**
     * Releases the UBO. After calling this mathod, you can't use the
 LightSources UBO and can't makeUsable it. Note that some renderers (like
 the BlinnPhongRenderer) may expect to access to the LightSources UBO
 (which isn't possible after calling this method).
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

    //TODO: equals, hashCode, toString
}
