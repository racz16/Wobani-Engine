package wobani.rendering.postprocessing;

import wobani.resources.shaders.postprocessing.ReinhardToneMappingShader;
import wobani.toolbox.annotations.*;

/**
 * Performs Reinhard tone mapping on the frame.
 */
public class ReinhardToneMappingRenderer extends PostProcessingRenderer {

    /**
     * The only ReinhardToneMappingRenderer instance.
     */
    private static ReinhardToneMappingRenderer instance;

    /**
     * Initializes a new ReinhardToneMappingRenderer.
     */
    private ReinhardToneMappingRenderer() {
        refreshShader();
    }

    @Override
    protected void refreshShader() {
        if (shader == null || !shader.isUsable()) {
            shader = ReinhardToneMappingShader.getInstance();
        }
    }

    /**
     * Returns the ReinhardToneMappingRenderer instance.
     *
     * @return the ReinhardToneMappingRenderer instance
     */
    @NotNull
    public static ReinhardToneMappingRenderer getInstance() {
        if (instance == null) {
            instance = new ReinhardToneMappingRenderer();
        }
        return instance;
    }

    @Override
    public boolean isUsable() {
        return true;
    }
}
