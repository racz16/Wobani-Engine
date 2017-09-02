package shaders.postProcessing;

import shaders.*;
import toolbox.annotations.*;

/**
 * Performs Reinhard tone mapping on the image.
 */
public class ReinhardToneMappingShader extends Shader {

    /**
     * The only ReinhardToneMappingShader instance.
     */
    private static ReinhardToneMappingShader instance;

    /**
     * Initializes a new ReinhardToneMappingShader.
     */
    private ReinhardToneMappingShader() {
        super("res/shaders/postProcessing/reinhardToneMapping/vertexShader.glsl", "res/shaders/postProcessing/reinhardToneMapping/fragmentShader.glsl", null, null, null);
    }

    /**
     * Returns the ReinhardToneMappingShader instance.
     *
     * @return the ReinhardToneMappingShader instance
     */
    @NotNull
    public static ReinhardToneMappingShader getInstance() {
        if (instance == null || !instance.isUsable()) {
            instance = new ReinhardToneMappingShader();
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
