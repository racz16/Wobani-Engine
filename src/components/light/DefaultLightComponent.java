package components.light;

import components.light.lightTypes.*;
import core.*;
import java.nio.*;
import java.util.*;
import org.joml.*;
import org.lwjgl.*;
import resources.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * This abstract class stores the light's diffuse, specular and ambient
 * components.
 *
 * @see GameObject
 * @see DirectionalLight
 * @see PointLight
 * @see SpotLight
 */
public abstract class DefaultLightComponent extends Component {

    /**
     * Diffuse color.
     */
    private final Vector3f diffuseColor = new Vector3f(1);
    /**
     * Specular color.
     */
    private final Vector3f specularColor = new Vector3f(1);
    /**
     * Ambient color.
     */
    private final Vector3f ambientColor = new Vector3f(0.1f);
    /**
     * The LightSources UBO's lights.
     */
    private static DefaultLightComponent[] lights = new DefaultLightComponent[16];
    /**
     * The highest valid UBO index.
     */
    private static int maxLightIndex = -1;
    /**
     * The maximum number of lights that the UBO can store.
     */
    protected static final int maxLights = 16;
    /**
     * The LightSources UBO.
     */
    protected static Ubo ubo;
    /**
     * The light's index in the UBO.
     */
    private int uboIndex = -1;
    /**
     * FloatBuffer for frequent UBO updates.
     */
    protected static FloatBuffer temp;

    static {
        createUbo();
        temp = BufferUtils.createFloatBuffer(26);
    }

    /**
     * Returns the diffuse color.
     *
     * @return diffuse color
     */
    @NotNull @ReadOnly
    public Vector3f getDiffuseColor() {
        return new Vector3f(diffuseColor);
    }

    /**
     * Sets the diffuse color to the given value. All of diffuse color's
     * components must be min. 0.
     *
     * @param diffuse diffuse color
     *
     * @throws IllegalArgumentException color can't be lower than 0
     */
    public void setDiffuseColor(@NotNull Vector3f diffuse) {
        if (!Utility.isHdrColor(diffuse)) {
            throw new IllegalArgumentException("Diffuse color can't be lower than 0");
        }
        this.diffuseColor.set(diffuse);
        updateUbo();
    }

    /**
     * Returns the specular color.
     *
     * @return diffuse color
     */
    @NotNull @ReadOnly
    public Vector3f getSpecularColor() {
        return new Vector3f(specularColor);
    }

    /**
     * Sets the specular color to the given value. All of specular color's
     * components must be min. 0.
     *
     * @param specular specular color
     *
     * @throws IllegalArgumentException color can't be lower than 0
     */
    public void setSpecularColor(@NotNull Vector3f specular) {
        if (!Utility.isHdrColor(specular)) {
            throw new IllegalArgumentException("Specular color can't be lower than 0");
        }
        this.specularColor.set(specular);
        updateUbo();
    }

    /**
     * Returns the ambient color.
     *
     * @return diffuse color
     */
    @NotNull @ReadOnly
    public Vector3f getAmbientColor() {
        return new Vector3f(ambientColor);
    }

    /**
     * Sets the ambient color to the given value. All of ambient color's
     * components must be min. 0.
     *
     * @param ambient ambient color
     *
     * @throws IllegalArgumentException color can't be lower than 0
     */
    public void setAmbientColor(@NotNull Vector3f ambient) {
        if (!Utility.isHdrColor(ambient)) {
            throw new IllegalArgumentException("Ambient color can't be lower than 0");
        }
        this.ambientColor.set(ambient);
        updateUbo();
    }

    @Override
    protected void removeFromGameObject() {
        getGameObject().getTransform().removeInvalidatable(this);
        super.removeFromGameObject();
        invalidate();
        removeLightFromUbo();
    }

    @Override
    protected void addToGameObject(@NotNull GameObject g) {
        super.addToGameObject(g);
        invalidate();
        getGameObject().getTransform().addInvalidatable(this);
        addLightToUbo();
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        updateUbo();
    }

    @Override
    public void invalidate() {
        updateUbo();
    }

    /**
     * Adds the light to the UBO.
     */
    protected void addLightToUbo() {
        if (getGameObject() == null || getUboIndex() != -1 || ubo == null || !ubo.isUsable()) {
            return;
        }
        int free = -1;
        for (int i = 0; i < lights.length; i++) {
            if (lights[i] == null) {
                free = i;
                break;
            }
        }
        if (free != -1) {
            if (free > maxLightIndex) {
                maxLightIndex = free;
            }
            setUboIndex(free);
            lights[free] = this;
            updateUbo();
            updateMaxLightSources();
        }
    }

    /**
     * Removes the light from the UBO.
     */
    protected void removeLightFromUbo() {
        int index = getUboIndex();
        if (getGameObject() != null || index == -1 || ubo == null || !ubo.isUsable()) {
            return;
        }
        lights[index] = null;
        IntBuffer ib = BufferUtils.createIntBuffer(1);
        ib.put(0);
        ubo.bind();
        ubo.storeData(ib, index * 112 + 108);
        ubo.unbind();
        setUboIndex(-1);
        if (index == getMaxLightIndex()) {
            for (int i = getMaxLightIndex() - 1; i >= 0; i--) {
                if (lights[i] != null) {
                    maxLightIndex = i;
                    break;
                }
            }
            if (maxLightIndex == index) {
                maxLightIndex = -1;
            }
        }
        updateMaxLightSources();
    }

    /**
     * Updates the max light sources number in the UBO.
     */
    private static void updateMaxLightSources() {
        IntBuffer ib = BufferUtils.createIntBuffer(1);
        ib.put(getMaxLightIndex() + 1);
        ib.flip();
        ubo.bind();
        ubo.storeData(ib, 1904);
        ubo.unbind();
    }

    /**
     * Returns the maximum number of lights that the UBO can store.
     *
     * @return the maximum number of lights that the UBO can store
     */
    public static int getMaxNumberOfLights() {
        return maxLights;
    }

    /**
     * The highest valid UBO index.
     *
     * @return the highest valid UBO index
     */
    public static int getMaxLightIndex() {
        return maxLightIndex;
    }

    /**
     * Returns the light's UBO index.
     *
     * @return the light's UBO index.
     */
    protected int getUboIndex() {
        return uboIndex;
    }

    /**
     * Sets the light's UBO index to the given value.
     *
     * @param index new UBO index
     */
    protected void setUboIndex(int index) {
        uboIndex = index;

    }

    /**
     * Updates the light in the UBO.
     */
    protected abstract void updateUbo();

    /**
     * Creates the UBO.
     */
    private static void createUbo() {
        if (ubo == null || !ubo.isUsable()) {
            ubo = new Ubo();
            ubo.bind();
            ubo.allocateMemory(1908, false);
            ubo.unbind();
            ubo.bindToBindingPoint(1);
        }
    }

    /**
     * Releases the UBO. After calling this mathod, you can't use the
     * LightSources UBO and can't recreate it. Note that some renderers (like
     * the BlinnPhongRenderer) may expect to access to the LightSources UBO
     * (which isn't possible after calling this method).
     */
    public static void releaseUbo() {
        ubo.release();
        ubo = null;
        maxLightIndex = -1;
        for (DefaultLightComponent light : lights) {
            if (light != null) {
                light.setUboIndex(-1);
            }
        }
        lights = null;
    }

    @Override
    public int hashCode() {
        int hash = 7 + super.hashCode();
        hash = 11 * hash + Objects.hashCode(this.diffuseColor);
        hash = 11 * hash + Objects.hashCode(this.specularColor);
        hash = 11 * hash + Objects.hashCode(this.ambientColor);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final DefaultLightComponent other = (DefaultLightComponent) obj;
        if (!Objects.equals(this.diffuseColor, other.diffuseColor)) {
            return false;
        }
        if (!Objects.equals(this.specularColor, other.specularColor)) {
            return false;
        }
        if (!Objects.equals(this.ambientColor, other.ambientColor)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nDefaultLightComponent{"
                + "diffuseColor=" + diffuseColor + ", specularColor=" + specularColor
                + ", ambientColor=" + ambientColor + ", uboIndex=" + uboIndex + '}';
    }

}
