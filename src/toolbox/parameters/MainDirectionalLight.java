package toolbox.parameters;

import components.light.lightTypes.*;
import core.*;

public class MainDirectionalLight extends UniqueParameter<DirectionalLight> {

    public MainDirectionalLight(DirectionalLight value) {
        super(value);
    }

    @Override
    public DirectionalLight getValue() {
        DirectionalLight ret = super.getValue();
        if (ret.getGameObject() == null) {
            throw new RuntimeException("main directional light detached");
        }
        return ret;
    }

    @Override
    protected void removedFromParameters(UniqueParameter<DirectionalLight> added) {
        MainCamera camera = Scene.getParameters().getParameter(MainCamera.class);
        if (camera != null) {
            camera.getValue().removeInvalidatable(getValue());
        }
    }

    @Override
    protected void addedToParameters(UniqueParameter<DirectionalLight> removed) {
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
