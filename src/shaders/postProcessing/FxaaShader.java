package shaders.postProcessing;

import resources.*;
import shaders.*;
import toolbox.annotations.*;

/**
 * Performs FXAA.
 */
public class FxaaShader extends Shader {

    /**
     * The only FxaaShader instance.
     */
    private static FxaaShader instance;

    /**
     * Initializes a new FxaaShader.
     */
    private FxaaShader() {
        super("res/shaders/postProcessing/fxaa/vertexShader.glsl", "res/shaders/postProcessing/fxaa/fragmentShader.glsl", null, null, null);
        ResourceManager.addShader("." + ResourceManager.getNextId(), this);
    }

    /**
     * Returns the FxaaShader instance.
     *
     * @return the FxaaShader instance
     */
    @NotNull
    public static FxaaShader getInstance() {
        if (instance == null || !instance.isUsable()) {
            instance = new FxaaShader();
        }
        return instance;
    }

    @Override
    public void connectTextureUnits() {
        connectTextureUnit("image", 0);
    }

    @Override
    protected void connectUniforms() {
        connectUniform("image");
    }

}
