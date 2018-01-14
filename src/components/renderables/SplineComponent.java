package components.renderables;

import materials.*;
import resources.splines.*;
import toolbox.annotations.*;

public class SplineComponent extends RenderableComponent<Spline> {

    public SplineComponent(@NotNull Spline spline) {
        super(spline);
    }

    public SplineComponent(@NotNull Spline spline, @NotNull Material material) {
        super(spline, material);
    }

    public boolean isLoopSpline() {
        return getRenderable().isLoopSpline();
    }

    @Override
    public int getFaceCount() {
        return 0;
    }

    @Override
    public boolean isTwoSided() {
        return false;
    }

}
