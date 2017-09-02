package renderers.postProcessing;

import shaders.postProcessing.ReinhardToneMappingShader;
import toolbox.annotations.*;

/**
 * Performs Reinhard tone mapping on the frame.
 */
public class ReinhardToneMappingRenderer extends PostProcessingBase {

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
        if (instance == null || !instance.isUsable()) {
            instance = new ReinhardToneMappingRenderer();
        }
        return instance;
    }
}
