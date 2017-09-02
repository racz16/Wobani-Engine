package shaders;

import org.joml.*;
import toolbox.annotations.*;

/**
 * This shader can render the shadow map.
 */
public class ShadowShader extends Shader {

    /**
     * The only ShadowShader instance.
     */
    private static ShadowShader instance;

    /**
     * Initializes a new ShadowShader.
     */
    private ShadowShader() {
        super("res/shaders/shadow/vertexShader.glsl", "res/shaders/shadow/fragmentShader.glsl", null, null, null);
    }

    /**
     * Returns the ShadowShader instance.
     *
     * @return the ShadowShader instance
     */
    @NotNull
    public static ShadowShader getInstance() {
        if (instance == null || !instance.isUsable()) {
            instance = new ShadowShader();
        }
        return instance;
    }

    @Override
    protected void connectUniforms() {
        connectUniform("projectionViewModelMatrix");
    }

    /**
     * Loads the projection view model matrix as a uniform variable.
     *
     * @param projectionViewModelMatrix projection view model matrix
     */
    public void loadProjectionViewModelMatrix(@NotNull Matrix4f projectionViewModelMatrix) {
        loadMatrix4("projectionViewModelMatrix", projectionViewModelMatrix);
    }

}
