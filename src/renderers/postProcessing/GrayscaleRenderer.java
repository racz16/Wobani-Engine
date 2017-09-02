package renderers.postProcessing;

import shaders.postProcessing.*;
import toolbox.annotations.*;

/**
 * Makes the frame grayscaled.
 */
public class GrayscaleRenderer extends PostProcessingBase {

    /**
     * The only GrayscaleRenderer instance.
     */
    private static GrayscaleRenderer instance;

    /**
     * Initializes a new GrayscaleRenderer.
     */
    private GrayscaleRenderer() {
        refreshShader();
    }

    @Override
    protected void refreshShader() {
        if (shader == null || !shader.isUsable()) {
            shader = GrayscaleShader.getInstance();
        }
    }

    /**
     * Returns the GrayscaleRenderer instance.
     *
     * @return the GrayscaleRenderer instance
     */
    @NotNull
    public static GrayscaleRenderer getInstance() {
        if (instance == null) {
            instance = new GrayscaleRenderer();
        }
        return instance;
    }

    @Override
    public boolean isUsable() {
        return true;
    }

}
