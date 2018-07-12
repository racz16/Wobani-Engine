package wobani.component.light.blinnphong;

import org.joml.Math;
import org.joml.*;
import org.lwjgl.*;
import wobani.toolbox.annotation.*;

import java.nio.*;

/**
 A helper class which helps the Blinn-Phong light sources in the communication with the VGA. It contains a FloatBuffer,
 an IntBuffer and all the methods necessary to fill these buffers and then you can pass them to the VGA.
 */
public class BlinnPhongShaderHelper{

    /**
     One light's size in the VRAM.
     */
    public static final int LIGHT_SIZE = 112;
    /**
     The type variable's address in the light struct.
     */
    public static final int TYPE_ADDRESS = 104;
    /**
     The active variable's address in the light struct.
     */
    public static final int ACTIVE_ADDRESS = 108;
    /**
     Offset in the VRAM from the 0 to the first light source.
     */
    public static final int LIGHT_SOURCES_OFFSET = 16;
    /**
     FloatBuffer for frequent shader updates.
     */
    private final FloatBuffer FLOAT_BUFFER;
    /**
     IntBuffer for frequent shader updates.
     */
    private final IntBuffer INT_BUFFER;

    /**
     Initializes a new BlinnPhongShaderHelper.
     */
    public BlinnPhongShaderHelper(){
        FLOAT_BUFFER = BufferUtils.createFloatBuffer(26);
        INT_BUFFER = BufferUtils.createIntBuffer(2);
    }

    /**
     Returns the FloatBuffer.

     @return the FloatBuffer
     */
    @NotNull
    public FloatBuffer getFloatBuffer(){
        return FLOAT_BUFFER;
    }

    /**
     Returns the IntBuffer.

     @return the IntBuffer
     */
    @NotNull
    public IntBuffer getIntBuffer(){
        return INT_BUFFER;
    }

    /**
     Sets the FloatBuffer's position to the given value.

     @param position position
     */
    public void setFloatBufferPosition(int position){
        FLOAT_BUFFER.position(position);
    }

    /**
     Sets the IntBuffer's position to the given value.

     @param position position
     */
    public void setIntBufferPosition(int position){
        INT_BUFFER.position(position);
    }

    /**
     Sets the FloatBuffer's limit to the given value.

     @param limit limit
     */
    public void setFloatBufferLimit(int limit){
        FLOAT_BUFFER.limit(limit);
    }

    /**
     Sets the IntBuffer's limit to the given value.

     @param limit limit
     */
    public void setIntBufferLimit(int limit){
        INT_BUFFER.limit(limit);
    }

    /**
     Returns the FloatBuffer's capacity.

     @return the FloatBuffer's capacity
     */
    public int getFloatBufferCapacity(){
        return FLOAT_BUFFER.capacity();
    }

    /**
     Returns the IntBuffer's capacity.

     @return the IntBuffer's capacity
     */
    public int getIntBufferCapacity(){
        return INT_BUFFER.capacity();
    }

    /**
     Sets the light source's position to the given value in the buffer.

     @param position position
     */
    public void setPosition(@NotNull Vector3f position){
        for(int i = 0; i < 3; i++){
            FLOAT_BUFFER.put(position.get(i));
        }
        FLOAT_BUFFER.put(-1);
    }

    /**
     Sets the light source's direction to the given value in the buffer.

     @param direction direction
     */
    public void setDirection(@NotNull Vector3f direction){
        for(int i = 0; i < 3; i++){
            FLOAT_BUFFER.put(direction.get(i));
        }
        FLOAT_BUFFER.put(-1);
    }

    /**
     Sets the light source's attenuation to the given values in the buffer.

     @param constant  attenuation constant component
     @param linear    attenuation linear component
     @param quadratic attenuation quadratic component
     */
    public void setAttenuation(float constant, float linear, float quadratic){
        FLOAT_BUFFER.put(constant);
        FLOAT_BUFFER.put(linear);
        FLOAT_BUFFER.put(quadratic);
        FLOAT_BUFFER.put(-1);
    }

    /**
     Sets the light source's diffuse, specular and ambient color to the given values in the buffer.

     @param diffuse  diffuse color
     @param specular specular color
     @param ambient  ambient color
     */
    public void setColor(@NotNull Vector3f diffuse, @NotNull Vector3f specular, @NotNull Vector3f ambient){
        setAmbient(ambient);
        setDiffuse(diffuse);
        setSpecular(specular);
    }

    /**
     Sets the light source's ambient color to the given value in the buffer.

     @param ambient ambient color
     */
    public void setAmbient(@NotNull Vector3f ambient){
        for(int i = 0; i < 3; i++){
            FLOAT_BUFFER.put(ambient.get(i));
        }
        FLOAT_BUFFER.put(-1);
    }

    /**
     Sets the light source's diffuse color to the given value in the buffer.

     @param diffuse diffuse color
     */
    public void setDiffuse(@NotNull Vector3f diffuse){
        for(int i = 0; i < 3; i++){
            FLOAT_BUFFER.put(diffuse.get(i));
        }
        FLOAT_BUFFER.put(-1);
    }

    /**
     Sets the light source's specular color to the given value in the buffer.

     @param specular specular color
     */
    public void setSpecular(@NotNull Vector3f specular){
        for(int i = 0; i < 3; i++){
            FLOAT_BUFFER.put(specular.get(i));
        }
        FLOAT_BUFFER.put(-1);
    }

    /**
     Sets the light source's cutoff to the given values in the buffer.

     @param cutoff      cutoff (in degrees)
     @param outerCutoff outer cutoff (in degrees)
     */
    public void setCutoff(float cutoff, float outerCutoff){
        FLOAT_BUFFER.put((float) Math.cos(Math.toRadians(cutoff)));
        FLOAT_BUFFER.put((float) Math.cos(Math.toRadians(outerCutoff)));
    }

    /**
     Sets the next 4 floats to -1 in the buffer (if you don't want to use them to anything but you have to fill the
     values).
     */
    public void setFloatNone(){
        for(int i = 0; i < 4; i++){
            FLOAT_BUFFER.put(-1);
        }
    }

    /**
     Sets the light source's meta data to the given values in the buffer.

     @param lightType light source's type in the shader
     @param active    true if the light source is active, false otherwise
     */
    public void setMetaData(int lightType, boolean active){
        INT_BUFFER.put(lightType);
        INT_BUFFER.put(active ? 1 : 0);
    }
}
