package shaders.postProcessing;

import resources.*;
import shaders.*;
import toolbox.annotations.*;

/**
 * Makes the image grayscaled.
 */
public class GrayscaleShader extends Shader {

    /**
     * The only GrayscaleShader instance.
     */
    private static GrayscaleShader instance;

    /**
     * Initializes a new GrayscaleShader.
     */
    private GrayscaleShader() {
        super("res/shaders/postProcessing/grayscale/vertexShader.glsl", "res/shaders/postProcessing/grayscale/fragmentShader.glsl", null, null, null);
        ResourceManager.addShader("." + ResourceManager.getNextId(), this);
    }

    /**
     * Returns the GrayscaleShader instance.
     *
     * @return the GrayscaleShader instance
     */
    @NotNull
    public static GrayscaleShader getInstance() {
        if (instance == null || !instance.isUsable()) {
            instance = new GrayscaleShader();
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
