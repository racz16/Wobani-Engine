package wobani.component.light.blinnphong;

import java.nio.*;
import wobani.core.*;
import wobani.toolbox.annotation.*;

/**
 * Basic implementation of a point light source.
 *
 * @see GameObject
 */
public class BlinnPhongPointLightComponent extends BlinnPhongPositionalLightComponent {

    @Internal @NotNull
    @Override
    FloatBuffer computeLightParameters() {
	getHelper().setFloatBufferPosition(0);
	getHelper().setFloatBufferLimit(24);
	getHelper().setColor(getDiffuseColor(), getSpecularColor(), getAmbientColor());
	getHelper().setFloatNone();    //direction
	getHelper().setPosition(getGameObject().getTransform().getAbsolutePosition());
	getHelper().setAttenutation(getConstant(), getLinear(), getQuadratic());
	getHelper().setFloatBufferPosition(0);
	return getHelper().getFloatBuffer();
    }

    @Override
    protected int getLightType() {
	return 1;
    }

    @Override
    public String toString() {
	StringBuilder res = new StringBuilder()
		.append(super.toString()).append("\n")
		.append(BlinnPhongPointLightComponent.class.getSimpleName()).append("(")
		.append(")");
	return res.toString();
    }

}
