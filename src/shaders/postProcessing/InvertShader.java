package shaders.postProcessing;

import shaders.*;
import toolbox.annotations.*;

/**
 * Inverts the image's colors.
 */
public class InvertShader extends Shader {

    /**
     * The only InvertShader instance.
     */
    private static InvertShader instance;

    /**
     * Initializes a new InvertShader.
     */
    private InvertShader() {
        super("res/shaders/postProcessing/invert/vertexShader.glsl", "res/shaders/postProcessing/invert/fragmentShader.glsl", null, null, null);
    }

    /**
     * Returns the InvertShader instance.
     *
     * @return the InvertShader instance
     */
    @NotNull
    public static InvertShader getInstance() {
        if (instance == null || !instance.isUsable()) {
            instance = new InvertShader();
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
