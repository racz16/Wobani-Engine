package wobani.resources.shaders.postProcessing;

import java.io.*;
import java.util.*;
import wobani.resources.*;
import wobani.resources.shaders.*;
import wobani.toolbox.annotations.*;

/**
 * Performs Reinhard tone mapping on the image.
 */
public class ReinhardToneMappingShader extends Shader {

    /**
     * The only ReinhardToneMappingShader instance.
     */
    private static ReinhardToneMappingShader instance;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;
    /**
     * The vertex shader's path.
     */
    private static final String vertexPath = "res/shaders/postProcessing/reinhardToneMapping/vertexShader.glsl";
    /**
     * The fragment shader's path
     */
    private static final String fragmentPath = "res/shaders/postProcessing/reinhardToneMapping/fragmentShader.glsl";

    /**
     * Initializes a new ReinhardToneMappingShader.
     */
    private ReinhardToneMappingShader() {
        super(vertexPath, fragmentPath, null, null, null);
        List<File> paths = new ArrayList<>(2);
        paths.add(new File(vertexPath));
        paths.add(new File(fragmentPath));
        resourceId = new ResourceId(paths);
        ResourceManager.addShader(this);
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

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public String toString() {
        return super.toString() + "\nReinhardToneMappingShader{" + "resourceId="
                + resourceId + '}';
    }

}
