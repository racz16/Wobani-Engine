package wobani.resources.shaders.postprocessing;

import java.io.*;
import java.util.*;
import wobani.resources.*;
import wobani.resources.shaders.*;
import wobani.toolbox.annotations.*;

/**
 * Performs FXAA.
 */
public class FxaaShader extends Shader {

    /**
     * The only FxaaShader instance.
     */
    private static FxaaShader instance;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;
    /**
     * The vertex shader's path.
     */
    private static final String vertexPath = "res/shaders/postProcessing/fxaa/vertexShader.glsl";
    /**
     * The fragment shader's path
     */
    private static final String fragmentPath = "res/shaders/postProcessing/fxaa/fragmentShader.glsl";

    /**
     * Initializes a new FxaaShader.
     */
    private FxaaShader() {
        super(vertexPath, fragmentPath, null, null, null);
        List<File> paths = new ArrayList<>(2);
        paths.add(new File(vertexPath));
        paths.add(new File(fragmentPath));
        resourceId = new ResourceId(paths);
        ResourceManager.addShader(this);
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

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public String toString() {
        return super.toString() + "\nFxaaShader{" + "resourceId=" + resourceId + '}';
    }

}
