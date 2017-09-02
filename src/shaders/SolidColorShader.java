package shaders;

import org.joml.*;
import resources.*;
import toolbox.annotations.*;

/**
 * This shader can render Renderabes by drawing them with one color.
 */
public class SolidColorShader extends Shader {

    /**
     * The only SolidColorShader instance.
     */
    private static SolidColorShader instance;

    /**
     * Inizializes a new SolidColorShader.
     */
    private SolidColorShader() {
        super("res/shaders/solidColor/vertexShader.glsl", "res/shaders/solidColor/fragmentShader.glsl", null, null, null);
        ResourceManager.addShader("." + ResourceManager.getNextId(), this);
    }

    /**
     * Returns the SolidColorShader instance.
     *
     * @return the SolidColorShader instance
     */
    @NotNull
    public static SolidColorShader getInstance() {
        if (instance == null || !instance.isUsable()) {
            instance = new SolidColorShader();
        }
        return instance;
    }

    @Override
    protected void connectUniforms() {
        connectUniform("modelMatrix");
        connectUniform("color");
    }

    /**
     * Loads the model matrix and the Renderable's color as uniform variables.
     *
     * @param modelMatrix model matrix
     * @param color Renderable's color
     */
    public void loadUniforms(@NotNull Matrix4f modelMatrix, @NotNull Vector3f color) {
        loadMatrix4("modelMatrix", modelMatrix);
        loadVector3("color", color);
    }

}
