package wobani.component.renderable;

import wobani.resources.spline.Spline;
import wobani.toolbox.annotation.NotNull;
import wobani.material.Material;

/**
 * Contains a Spline what you van render or can be used as a path of an object.
 */
public class SplineComponent extends RenderableComponent<Spline> {

    /**
     * Initializes a new SplineComponent to the given value.
     *
     * @param spline Spline
     */
    public SplineComponent(@NotNull Spline spline) {
        super(spline);
    }

    /**
     * Initializes a new SplineComponent to the given values.
     *
     * @param spline   Spline
     * @param material Material
     */
    public SplineComponent(@NotNull Spline spline, @NotNull Material material) {
        super(spline, material);
    }

    /**
     * Determines whether the Spline is a loop spline.
     *
     * @return true if the Spline is loop Spline, false otherwise
     */
    public boolean isLoopSpline() {
        return getRenderable().isLoopSpline();
    }

    @Override
    public int getFaceCount() {
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append(super.toString()).append("\n")
                .append("SplineComponent(")
                .append(")");
        return res.toString();
    }
}
