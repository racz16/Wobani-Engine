package wobani.component.light;

import wobani.resources.buffers.Ssbo;
import java.nio.*;
import java.util.*;
import java.util.logging.*;
import org.lwjgl.*;
import wobani.resources.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 * Stores the Blinn-Phong light sources for the UBO.
 */
public class SsboLights {

    //TODO: delete
    private static final int MAX_LIGHTS = 16;

    /**
     * The LightSources UBO's lights.
     */
    private static List<BlinnPhongLightComponent> lights = new ArrayList<>();
    /**
     * The highest active light source's index in the UBO.
     */
    private static int maxLightIndex = -1;
    /**
     * The LightSources UBO.
     */
    private static Ssbo ssbo;
    /**
     * FloatBuffer for frequent UBO updates.
     */
    private static final FloatBuffer FLOAT_BUFFER;
    /**
     * IntBuffer for frequent UBO updates.
     */
    private static final IntBuffer INT_BUFFER;
    /**
     * One light's size in the UBO.
     */
    private static final int LIGHT_SIZE_UBO = 112;
    /**
     * One float's size in the UBO.
     */
    private static final int FLOAT_SIZE_UBO = 4;
    /**
     * The all UBO's size.
     */
    private static final int SIZE_UBO = (MAX_LIGHTS + 1) * LIGHT_SIZE_UBO + FLOAT_SIZE_UBO;
    /**
     * The max light index variable's address in the UBO.
     */
    private static final int MAX_LIGHT_INDEX_ADDRESS = (MAX_LIGHTS + 1) * LIGHT_SIZE_UBO;
    /**
     * The type variable's address in the UBO.
     */
    private static final int TYPE_ADDRESS = 104;
    /**
     * The active variable's address in the UBO.
     */
    private static final int ACTIVE_ADDRESS = 108;
    /**
     * The directional light's type code in the UBO.
     */
    private static final int DIRECTIONAL_LIGHT_TYPE = 0;
    /**
     * The point light' type code in the UBO.
     */
    private static final int POINT_LIGHT_TYPE = 1;
    /**
     * The spot light' type code in the UBO.
     */
    private static final int SPOT_LIGHT_TYPE = 2;
    /**
     * The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(SsboLights.class.getName());

    static {
	createSsbo();
	FLOAT_BUFFER = BufferUtils.createFloatBuffer(26);
	INT_BUFFER = BufferUtils.createIntBuffer(2);
    }

    /**
     * To can't create BlinnPhongLightSources instance.
     */
    private SsboLights() {
    }

    //
    //add-----------------------------------------------------------------------
    //
    /**
     * Adds the given light source to the UBO.
     *
     * @param light BlinnPhongDirectionalLightComponent
     */
    @Internal
    static void addLight(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (Utility.isUsable(ssbo)) {
	    light.setUboIndex(0);
	    light.refreshUbo();
	}
    }

    /**
     * Adds the given light source to the UBO.
     *
     * @param light BlinnPhongPointLightComponent
     */
    @Internal
    static void addLight(@NotNull BlinnPhongPointLightComponent light) {
	if (Utility.isUsable(ssbo)) {
	    addNonDirectionalLight(light);
	}
    }

    /**
     * Adds the given light source to the UBO.
     *
     * @param light BlinnPhongSpotLightComponent
     */
    @Internal
    static void addLight(@NotNull BlinnPhongSpotLightComponent light) {
	if (Utility.isUsable(ssbo)) {
	    addNonDirectionalLight(light);
	}
    }

    /**
     * Adds the given nondirectional light source to the UBO.
     *
     * @param light nondirectional light source
     */
    private static void addNonDirectionalLight(@NotNull BlinnPhongLightComponent light) {
	int lightIndex = computeNewLightUboIndex();
	addNonDirectionalLight(light, lightIndex);
    }

    /**
     * Computes the new light source's index. It returns -1 if there is not
     * enough space to store one more light source.
     *
     * @return the new light source's index
     */
    private static int computeNewLightUboIndex() {
	for (int i = 0; i < lights.size(); i++) {
	    if (lights.get(i) == null) {
		return i;
	    }
	}
	return -1;
    }

    /**
     * Adds the given nondirectional light source to the given index in the UBO.
     *
     * @param light nondirectional light source
     * @param index light source's index
     */
    private static void addNonDirectionalLight(@NotNull BlinnPhongLightComponent light, int index) {
	if (index != -1) {
	    light.setUboIndex(index);
	    lights.set(index, light);
	    light.refreshUbo();
	    refreshMaxLightIndexAfterAdd(index);
	}
    }

    /**
     * Refreshes the max light index after you added a new light source to the
     * given index.
     *
     * @param index added light source's index
     */
    private static void refreshMaxLightIndexAfterAdd(int index) {
	if (index > maxLightIndex) {
	    maxLightIndex = index;
	}
	refreshMaxLightIndexInUbo();
    }

    //
    //remove--------------------------------------------------------------------
    //
    /**
     * Removes the given light source from the UBO.
     *
     * @param light BlinnPhongDirectionalLightComponent
     */
    @Internal
    static void removeLight(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (Utility.isUsable(ssbo)) {
	    removeLight((BlinnPhongLightComponent) light);
	    LOG.fine("Directional light removed from the SSBO");
	}
    }

    /**
     * Removes the given light source from the UBO.
     *
     * @param light BlinnPhongPointLightComponent
     */
    @Internal
    static void removeLight(@NotNull BlinnPhongPointLightComponent light) {
	if (Utility.isUsable(ssbo)) {
	    removeNonDirectionalLight(light);
	    LOG.fine("Point light removed from the UBO");
	}
    }

    /**
     * Removes the given light source from the UBO.
     *
     * @param light BlinnPhongSpotLightComponent
     */
    @Internal
    static void removeLight(@NotNull BlinnPhongSpotLightComponent light) {
	if (Utility.isUsable(ssbo)) {
	    removeNonDirectionalLight(light);
	    LOG.fine("Spot light removed from the UBO");
	}
    }

    /**
     * Removes the given nondirectional light source from the UBO.
     *
     * @param light nondirectional light source
     */
    private static void removeNonDirectionalLight(@NotNull BlinnPhongLightComponent light) {
	int index = light.getUboIndex();
	lights.set(index, null);
	removeLight(light);
	refreshMaxLightIndexAfterRemove(index);
    }

    /**
     * Refreshes the max light index after you removed a light source from the
     * given index.
     *
     * @param index removed light source's index
     */
    private static void refreshMaxLightIndexAfterRemove(int index) {
	if (index == getMaxLightIndex()) {
	    maxLightIndex = computeMaxLightIndex();
	    refreshMaxLightIndexInUbo();
	}
    }

    /**
     * Computes the max light index after you removed a light source from the
     * UBO.
     *
     * @return the max light index
     */
    private static int computeMaxLightIndex() {
	for (int i = getMaxLightIndex() - 1; i >= 0; i--) {
	    if (lights.get(i) != null) {
		return i;
	    }
	}
	return -1;
    }

    /**
     * Refreshes the max light index in the UBO.
     */
    private static void refreshMaxLightIndexInUbo() {
	INT_BUFFER.limit(1);
	INT_BUFFER.position(0);
	INT_BUFFER.put(getMaxLightIndex() + 1);
	INT_BUFFER.position(0);
	ssbo.bind();
	ssbo.storeData(INT_BUFFER, MAX_LIGHT_INDEX_ADDRESS);
	ssbo.unbind();
    }

    //
    //update--------------------------------------------------------------------
    //
    /**
     * Refreshes the given light source in the UBO.
     *
     * @param light BlinnPhongDirectionalLightComponent
     */
    @Internal
    static void refreshLight(@NotNull BlinnPhongDirectionalLightComponent light) {
	if (Utility.isUsable(ssbo)) {
	    refreshghtParameters(light);
	    refreshLightMeta(DIRECTIONAL_LIGHT_TYPE, light.isActive());
	    refreshLightInUbo(light);
	    LOG.fine("Directional light refreshed in the SSBO");
	}
    }

    /**
     * Refreshes the given light source's parameters in the UBO.
     *
     * @param light BlinnPhongDirectionalLightComponent
     */
    private static void refreshghtParameters(@NotNull BlinnPhongDirectionalLightComponent light) {
	FLOAT_BUFFER.position(0);
	setFloatNone(); //position
	setDirection(light);
	setFloatNone(); //attenutation
	setColor(light);
	FLOAT_BUFFER.position(0);
    }

    /**
     * Refreshes the given light source in the UBO.
     *
     * @param light BlinnPhongPointLightComponent
     */
    @Internal
    static void refreshLight(@NotNull BlinnPhongPointLightComponent light) {
	if (Utility.isUsable(ssbo)) {
	    refreshLightParameters(light);
	    refreshLightMeta(POINT_LIGHT_TYPE, light.isActive());
	    refreshLightInUbo(light);
	    LOG.fine("Point light refreshed in the UBO");
	}
    }

    /**
     * Refreshes the given light source's parameters in the UBO.
     *
     * @param light BlinnPhongPointLightComponent
     */
    private static void refreshLightParameters(@NotNull BlinnPhongPointLightComponent light) {
	FLOAT_BUFFER.position(0);
	setPosition(light);
	setFloatNone(); //direction
	setAttenutation(light.getConstant(), light.getLinear(), light.getQuadratic());
	setColor(light);
	FLOAT_BUFFER.position(0);
    }

    /**
     * Refreshes the given light source in the UBO.
     *
     * @param light BlinnPhongSpotLightComponent
     */
    @Internal
    static void refreshLight(@NotNull BlinnPhongSpotLightComponent light) {
	if (Utility.isUsable(ssbo)) {
	    refreshLightParameters(light);
	    refreshLightMeta(SPOT_LIGHT_TYPE, light.isActive());
	    refreshLightInUbo(light);
	    LOG.fine("Spot light refreshed in the UBO");
	}
    }

    /**
     * Refreshes the given light source's parameters in the UBO.
     *
     * @param light BlinnPhongSpotLightComponent
     */
    private static void refreshLightParameters(@NotNull BlinnPhongSpotLightComponent light) {
	FLOAT_BUFFER.position(0);
	setPosition(light);
	setDirection(light);
	setAttenutation(light.getConstant(), light.getLinear(), light.getQuadratic());
	setColor(light);
	setCutoff(light);
	FLOAT_BUFFER.position(0);
    }

    /**
     * Refreshes the light source's type and activeness in the UBO.
     *
     * @param type   the light source's type
     * @param active determines whether the Component is active
     */
    private static void refreshLightMeta(int type, boolean active) {
	INT_BUFFER.limit(2);
	INT_BUFFER.position(0);
	INT_BUFFER.put(type);
	INT_BUFFER.put(active ? 1 : 0);
	INT_BUFFER.position(0);
    }

    //
    //general-------------------------------------------------------------------
    //
    /**
     * Sets the given light source's position in the UBO.
     *
     * @param light BlinnPhongLightComponent
     */
    private static void setPosition(@NotNull BlinnPhongLightComponent light) {
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
    private static void setDirection(@NotNull BlinnPhongLightComponent light) {
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
    private static void setAttenutation(float constant, float linear, float quadratic) {
	FLOAT_BUFFER.put(constant);
	FLOAT_BUFFER.put(linear);
	FLOAT_BUFFER.put(quadratic);
	FLOAT_BUFFER.put(-1);
    }

    /**
     * Sets the given light source's diffuse, specular and ambient color in the
     * UBO.
     *
     * @param light BlinnPhongLightComponent
     */
    private static void setColor(@NotNull BlinnPhongLightComponent light) {
	setAmbient(light);
	setDiffuse(light);
	setSpecular(light);
    }

    /**
     * Sets the given light source's ambient color in the UBO.
     *
     * @param light BlinnPhongLightComponent
     */
    private static void setAmbient(@NotNull BlinnPhongLightComponent light) {
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
    private static void setDiffuse(@NotNull BlinnPhongLightComponent light) {
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
    private static void setSpecular(@NotNull BlinnPhongLightComponent light) {
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
    private static void setCutoff(@NotNull BlinnPhongSpotLightComponent light) {
	FLOAT_BUFFER.put((float) Math.cos(Math.toRadians(light.getCutoff())));
	FLOAT_BUFFER.put((float) Math.cos(Math.toRadians(light.getOuterCutoff())));
    }

    /**
     * Sets the next 4 floats to -1 in the UBO (for example directional light's
     * position etc).
     */
    private static void setFloatNone() {
	for (int i = 0; i < 4; i++) {
	    FLOAT_BUFFER.put(-1);
	}
    }

    /**
     * Refreshes the given light source in the UBO.
     *
     * @param light BlinnPhongLightComponent
     */
    private static void refreshLightInUbo(@NotNull BlinnPhongLightComponent light) {
	ssbo.bind();
	ssbo.storeData(FLOAT_BUFFER, 0 * LIGHT_SIZE_UBO);
	ssbo.storeData(INT_BUFFER, 0 * LIGHT_SIZE_UBO + TYPE_ADDRESS);
	ssbo.unbind();
    }

    /**
     * Removes the given light source from the UBO.
     *
     * @param light BlinnPhongLightComponent
     */
    private static void removeLight(@NotNull BlinnPhongLightComponent light) {
	INT_BUFFER.limit(1);
	INT_BUFFER.position(0);
	INT_BUFFER.put(0);
	INT_BUFFER.position(0);
	removeLightFromUbo(light);
    }

    /**
     * Removes the given light source from the UBO.
     *
     * @param light BlinnPhongLightComponent
     */
    private static void removeLightFromUbo(@NotNull BlinnPhongLightComponent light) {
	ssbo.bind();
	ssbo.storeData(INT_BUFFER, light.getUboIndex() * LIGHT_SIZE_UBO + ACTIVE_ADDRESS);
	ssbo.unbind();
	light.setUboIndex(-1);
    }

    //
    //misc----------------------------------------------------------------------
    //
    /**
     * Returns the maximum number of lights that the UBO can store.
     *
     * @return the maximum number of lights that the UBO can store
     */
    public static int getMaxNumberOfLights() {
	return MAX_LIGHTS;
    }

    /**
     * The highest active light source's index in the UBO.
     *
     * @return the highest active light source's index in the UBO
     */
    public static int getMaxLightIndex() {
	return maxLightIndex;
    }

    /**
     * Creates the UBO.
     */
    private static void createSsbo() {
	if (!Utility.isUsable(ssbo)) {
	    createSsboWithoutInspection();
	    LOG.fine("Light SSBO created");
	}
    }

    /**
     * Creates the UBO.
     */
    private static void createSsboWithoutInspection() {
	ssbo = new Ssbo();
	ssbo.bind();
	ssbo.allocateMemory(112, false);
	ssbo.unbind();
	ssbo.bindToBindingPoint(3);
    }

    /**
     * Releases the UBO. After calling this mathod, you can't use the
     * LightSources UBO and can't recreate it. Note that some renderers (like
     * the BlinnPhongRenderer) may expect to access to the LightSources UBO
     * (which isn't possible after calling this method).
     */
    public static void releaseSsbo() {
	ssbo.release();
	ssbo = null;
	maxLightIndex = -1;
	removeAllLights();
	lights = null;
	LOG.fine("Light SSBO released");
    }

    /**
     * Removes all light sources from the UBO.
     */
    private static void removeAllLights() {
	for (BlinnPhongLightComponent light : lights) {
	    if (light != null) {
		light.setUboIndex(-1);
	    }
	}
    }
}
