package wobani.component.light.blinnphong;

import wobani.core.*;
import wobani.toolbox.annotation.*;

import java.nio.*;

/**
 Basic implementation of a spot light source.

 @see GameObject */
public class BlinnPhongSpotLightComponent extends BlinnPhongPositionalLightComponent{

    /**
     Cutoff component (in degrees).
     */
    private float cutoff = 12.5f;
    /**
     Outer cutoff component (in degrees).
     */
    private float outerCutoff = 15.0f;

    /**
     Returns the cutoff component.

     @return cutoff component (in degrees)
     */
    public float getCutoff(){
        return cutoff;
    }

    /**
     Sets the cutoff component to the given value. Note that the cutoff component must be higher than 0 and lower than
     the outer cutoff component.

     @param cutoff cutoff component (in degrees)

     @throws IllegalArgumentException cutoff component must be higher than 0 and lower than the outer cutoff component
     */
    public void setCutoff(float cutoff){
        if(cutoff <= 0 || cutoff >= outerCutoff){
            throw new IllegalArgumentException("Cutoff component must be higher than 0 and lower than the outer cutoff component");
        }
        this.cutoff = cutoff;
        makeDirty();
    }

    /**
     Returns the outer cutoff component.

     @return outer cutoff component (in degrees)
     */
    public float getOuterCutoff(){
        return outerCutoff;
    }

    /**
     Sets the outer cutoff component to the given value. Note that the outer cutoff component must be higher than the
     cutoff component.

     @param outerCutoff outer cutoff component (in degrees)

     @throws IllegalArgumentException outer cutoff component must be higher than the cutoff component
     */
    public void setOuterCutoff(float outerCutoff){
        if(cutoff >= outerCutoff){
            throw new IllegalArgumentException("Cutoff component must be lower than the outer cutoff component");
        }
        this.outerCutoff = outerCutoff;
        makeDirty();
    }

    @Internal
    @NotNull
    @Override
    protected FloatBuffer computeLightParameters(){
        getHelper().setFloatBufferPosition(0);
        getHelper().setFloatBufferLimit(26);
        getHelper().setColor(getDiffuseColor(), getSpecularColor(), getAmbientColor());
        setTransformParameters();
        getHelper().setCutoff(getCutoff(), getOuterCutoff());
        getHelper().setFloatBufferPosition(0);
        return getHelper().getFloatBuffer();
    }

    /**
     Sets the light transform related parameters like direction or position.
     */
    private void setTransformParameters(){
        getHelper().setDirection(getGameObject().getTransform().getForwardVector());
        getHelper().setPosition(getGameObject().getTransform().getAbsolutePosition());
        getHelper().setAttenuation(getConstant(), getLinear(), getQuadratic());
    }

    @Override
    protected int getLightShaderType(){
        return 2;
    }

    @Override
    public int hashCode(){
        int hash = 3 + super.hashCode();
        hash = 97 * hash + Float.floatToIntBits(this.cutoff);
        hash = 97 * hash + Float.floatToIntBits(this.outerCutoff);
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(!super.equals(obj)){
            return false;
        }
        final BlinnPhongSpotLightComponent other = (BlinnPhongSpotLightComponent) obj;
        if(Float.floatToIntBits(this.cutoff) != Float.floatToIntBits(other.cutoff)){
            return false;
        }
        if(Float.floatToIntBits(this.outerCutoff) != Float.floatToIntBits(other.outerCutoff)){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(super.toString()).append("\n")
                .append(BlinnPhongSpotLightComponent.class.getSimpleName()).append("(").append("cutoff: ")
                .append(cutoff).append(", outer cutoff: ").append(outerCutoff).append(")");
        return res.toString();
    }

}
