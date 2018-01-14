package toolbox.parameters;

import components.camera.*;
import core.*;
import toolbox.annotations.*;

public class MainCamera extends UniqueParameter<Camera> {

    public MainCamera(@NotNull Camera value) {
        super(value);
    }

    @Override
    public Camera getValue() {
        Camera ret = super.getValue();
        if (ret.getGameObject() == null) {
            throw new RuntimeException("main camera detached");
        }
        return ret;
    }

    @Override
    protected void removedFromParameters(@Nullable UniqueParameter<Camera> added) {
        MainDirectionalLight dirLight = Scene.getParameters().getParameter(MainDirectionalLight.class);
        if (dirLight != null) {
            getValue().removeInvalidatable(dirLight.getValue());
        }
    }

    @Override
    protected void addedToParameters(@Nullable UniqueParameter<Camera> removed) {
        if (getValue().getGameObject() == null) {
            throw new IllegalArgumentException();
        }
        MainDirectionalLight dirLight = Scene.getParameters().getParameter(MainDirectionalLight.class);
        if (dirLight != null) {
            getValue().addInvalidatable(dirLight.getValue());
        }
        getValue().invalidate();
    }

}
