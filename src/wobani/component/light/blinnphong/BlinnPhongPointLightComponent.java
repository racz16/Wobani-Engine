package wobani.component.light.blinnphong;

import wobani.toolbox.annotation.*;

import java.nio.*;

/**
 Basic implementation of a point light source.
 */
public class BlinnPhongPointLightComponent extends BlinnPhongPositionalLightComponent{

    @Internal
    @NotNull
    @Override
    protected FloatBuffer computeLightParameters(){
        getHelper().setFloatBufferPosition(0);
        getHelper().setFloatBufferLimit(24);
        getHelper().setColor(getDiffuseColor(), getSpecularColor(), getAmbientColor());
        setTransformParameters();
        getHelper().setFloatBufferPosition(0);
        return getHelper().getFloatBuffer();
    }

    /**
     Sets the light transform related parameters like direction or position.
     */
    private void setTransformParameters(){
        getHelper().setFloatNone();    //direction
        getHelper().setPosition(getGameObject().getTransform().getAbsolutePosition());
        getHelper().setAttenutation(getConstant(), getLinear(), getQuadratic());
    }

    @Override
    protected int getLightShaderType(){
        return 1;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(super.toString()).append("\n")
                .append(BlinnPhongPointLightComponent.class.getSimpleName()).append("(").append(")");
        return res.toString();
    }

}
