package wobani.component.light.blinnphong;

import java.nio.*;
import org.joml.Math;
import org.joml.*;
import org.lwjgl.*;
import wobani.toolbox.annotation.*;

public class BlinnPhongShaderHelper {

    /**
     * FloatBuffer for frequent UBO updates.
     */
    private final FloatBuffer FLOAT_BUFFER;
    /**
     * IntBuffer for frequent UBO updates.
     */
    private final IntBuffer INT_BUFFER;

    public BlinnPhongShaderHelper() {
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
    public void setPosition(@NotNull Vector3f position) {
	for (int i = 0; i < 3; i++) {
	    FLOAT_BUFFER.put(position.get(i));
	}
	FLOAT_BUFFER.put(-1);
    }

    /**
     * Sets the given light source's direction in the UBO.
     *
     * @param light BlinnPhongLightComponent
     */
    public void setDirection(@NotNull Vector3f direction) {
	for (int i = 0; i < 3; i++) {
	    FLOAT_BUFFER.put(direction.get(i));
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
    public void setAttenutation(float constant, float linear, float quadratic) {
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
    public void setColor(@NotNull Vector3f diffuse, @NotNull Vector3f specular, @NotNull Vector3f ambient) {
	setAmbient(ambient);
	setDiffuse(diffuse);
	setSpecular(specular);
    }

    /**
     * Sets the given light source's ambient color in the UBO.
     *
     * @param light BlinnPhongLightComponent
     */
    public void setAmbient(@NotNull Vector3f ambient) {
	for (int i = 0; i < 3; i++) {
	    FLOAT_BUFFER.put(ambient.get(i));
	}
	FLOAT_BUFFER.put(-1);
    }

    /**
     * Sets the given light source's diffuse color in the UBO.
     *
     * @param light BlinnPhongLightComponent
     */
    public void setDiffuse(@NotNull Vector3f diffuse) {
	for (int i = 0; i < 3; i++) {
	    FLOAT_BUFFER.put(diffuse.get(i));
	}
	FLOAT_BUFFER.put(-1);
    }

    /**
     * Sets the given light source's specular color in the UBO.
     *
     * @param light BlinnPhongLightComponent
     */
    public void setSpecular(@NotNull Vector3f specular) {
	for (int i = 0; i < 3; i++) {
	    FLOAT_BUFFER.put(specular.get(i));
	}
	FLOAT_BUFFER.put(-1);
    }

    /**
     * Sets the given light source's cutoff and outer cutoff in the UBO.
     *
     * @param light BlinnPhongSpotLightComponent
     */
    public void setCutoff(float cutoff, float outerCutoff) {
	FLOAT_BUFFER.put((float) Math.cos(Math.toRadians(cutoff)));
	FLOAT_BUFFER.put((float) Math.cos(Math.toRadians(outerCutoff)));
    }

    /**
     * Sets the next 4 floats to -1 in the UBO (for example directional light's
     * position etc).
     */
    public void setFloatNone() {
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
    public void setMetaData(int lightType, boolean active) {
	INT_BUFFER.put(lightType);
	INT_BUFFER.put(active ? 1 : 0);
    }

    public void setInactive() {
	INT_BUFFER.put(0);
    }
}
