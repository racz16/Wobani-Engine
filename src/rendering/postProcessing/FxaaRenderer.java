package rendering.postProcessing;

import resources.shaders.postProcessing.FxaaShader;
import toolbox.annotations.*;

/**
 * Performs FXAA.
 */
public class FxaaRenderer extends PostProcessingRenderer {

    /**
     * The only FxaaRenderer instance.
     */
    private static FxaaRenderer instance;

    /**
     * Initializes a new FxaaRenderer.
     */
    private FxaaRenderer() {
        refreshShader();
    }

    @Override
    protected void refreshShader() {
        if (shader == null || !shader.isUsable()) {
            shader = FxaaShader.getInstance();
        }
    }

    /**
     * Returns the FxaaRenderer instance.
     *
     * @return the FxaaRenderer instance
     */
    @NotNull
    public static FxaaRenderer getInstance() {
        if (instance == null) {
            instance = new FxaaRenderer();
        }
        return instance;
    }

    @Override
    public boolean isUsable() {
        return true;
    }

}
