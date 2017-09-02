package shaders.postProcessing;

import core.*;
import shaders.*;
import toolbox.annotations.*;

/**
 * Performs gamma correction on the image.
 *
 * @see Settings#getGamma()
 */
public class GammaCorrectionShader extends Shader {

    /**
     * The only GammaCorrectionShader instance.
     */
    private static GammaCorrectionShader instance;

    /**
     * Initializes a new GammaCorrectionShader.
     */
    private GammaCorrectionShader() {
        super("res/shaders/postProcessing/gammaCorrection/vertexShader.glsl", "res/shaders/postProcessing/gammaCorrection/fragmentShader.glsl", null, null, null);
    }

    /**
     * Returns the GammaCorrectionShader instance.
     *
     * @return the GammaCorrectionShader instance
     */
    @NotNull
    public static GammaCorrectionShader getInstance() {
        if (instance == null || !instance.isUsable()) {
            instance = new GammaCorrectionShader();
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
        connectUniform("gamma");
    }

    /**
     * Loads the gamma value to the shader as a uniform variable.
     *
     * @see Settings#getGamma()
     */
    public void loadGammaUniform() {
        loadFloat("gamma", Settings.getGamma());
    }

}
