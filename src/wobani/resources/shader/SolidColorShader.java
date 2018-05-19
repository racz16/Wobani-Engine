package wobani.resources.shader;

import java.io.*;
import java.util.*;
import org.joml.*;
import wobani.resources.*;
import wobani.toolbox.annotation.*;

/**
 * This shader can render Renderabes by drawing them with one color.
 */
public class SolidColorShader extends Shader {

    /**
     * The only SolidColorShader instance.
     */
    private static SolidColorShader instance;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;
    /**
     * The vertex shader's path.
     */
    private static final String vertexPath = "res/shaders/solidColor/vertexShader.glsl";
    /**
     * The fragment shader's path
     */
    private static final String fragmentPath = "res/shaders/solidColor/fragmentShader.glsl";

    /**
     * Inizializes a new SolidColorShader.
     */
    private SolidColorShader() {
        super(vertexPath, fragmentPath, null, null, null, null);
        List<File> paths = new ArrayList<>(2);
        paths.add(new File(vertexPath));
        paths.add(new File(fragmentPath));
        resourceId = new ResourceId(paths);
        ResourceManager.addShader(this);
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
     * @param color       Renderable's color
     */
    public void loadUniforms(@NotNull Matrix4f modelMatrix, @NotNull Vector3f color) {
        loadMatrix4("modelMatrix", modelMatrix);
        loadVector3("color", color);
    }

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public String toString() {
        return super.toString() + "\nSolidColorShader{" + "resourceId=" + resourceId + '}';
    }

}
