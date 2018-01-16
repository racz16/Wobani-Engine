package toolbox.parameters;

import components.light.lightTypes.*;
import core.*;

public class BlinnPhongMainDirectionalLight extends UniqueParameter<BlinnPhongDirectionalLight> {

    public BlinnPhongMainDirectionalLight(BlinnPhongDirectionalLight value) {
        super(value);
    }

    @Override
    public BlinnPhongDirectionalLight getValue() {
        BlinnPhongDirectionalLight ret = super.getValue();
        if (ret.getGameObject() == null) {
            throw new RuntimeException("main directional light detached");
        }
        return ret;
    }

    @Override
    protected void removedFromParameters(UniqueParameter<BlinnPhongDirectionalLight> added) {
        MainCamera camera = Scene.getParameters().getParameter(MainCamera.class);
        if (camera != null) {
            camera.getValue().removeInvalidatable(getValue());
        }
    }

    @Override
    protected void addedToParameters(UniqueParameter<BlinnPhongDirectionalLight> removed) {
        if (getValue().getGameObject() == null) {
            throw new NullPointerException();
        }
        MainCamera camera = Scene.getParameters().getParameter(MainCamera.class);
        if (camera != null) {
            camera.getValue().addInvalidatable(getValue());
        }
        getValue().invalidate();
    }

}
