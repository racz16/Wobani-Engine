package rendering.stages;

import java.util.*;
import rendering.geometry.*;
import toolbox.annotations.*;

public class GeometryRenderingStage {

    /**
     * List of geometry renderers. You may add to this list renderers like
     * Blinn-Phong, shadow renderer or toon renderer to actually draw the
     * scene's geometry.
     */
    private final List<GeometryRenderer> renderers = new ArrayList<>();

    public void render() {
        for (GeometryRenderer renderer : renderers) {
            renderer.render();
        }
    }

    public boolean addRendererToTheEnd(@NotNull GeometryRenderer renderer) {
        return addRenderer(getRendererCount(), renderer);
    }

    public boolean addRenderer(int index, @NotNull GeometryRenderer renderer) {
        for (GeometryRenderer ren : renderers) {
            if (ren.getClass() == renderer.getClass()) {
                return false;
            }
        }
        renderers.add(index, renderer);
        return true;
    }

    /**
     * Returns the indexth renderer of the specified stage.
     *
     * @param geometry specifies the list of Renderers
     * @param index    index
     *
     * @return the indexth renderer of the specified stage
     */
    @NotNull
    public GeometryRenderer getRenderer(int index) {
        return renderers.get(index);
    }

    /**
     * Returns the number of the renderers in the specified stage.
     *
     * @param geometry specifies the list of Renderers
     *
     * @return the number of the renderers in the specified stage
     */
    public int getRendererCount() {
        return renderers.size();
    }

    /**
     * Removes the specified stage's indexth renderer.
     *
     * @param geometry specifies the list of Renderers
     * @param index    index
     *
     * @throws NullPointerException stage can't be null
     */
    public void removeRenderer(int index) {//FIXME miért public??
        renderers.remove(index).removeFromRenderingPipeline();
    }

    public void release() {
        while (!renderers.isEmpty()) {
            removeRenderer(0);
        }
    }

}
