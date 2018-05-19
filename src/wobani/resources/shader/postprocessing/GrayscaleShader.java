package wobani.resources.shader.postprocessing;

import java.io.*;
import java.util.*;
import wobani.resources.*;
import wobani.resources.shader.*;
import wobani.toolbox.annotation.*;

/**
 * Makes the image grayscaled.
 */
public class GrayscaleShader extends Shader {

    /**
     * The only GrayscaleShader instance.
     */
    private static GrayscaleShader instance;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;
    /**
     * The vertex shader's path.
     */
    private static final String vertexPath = "res/shaders/postProcessing/grayscale/vertexShader.glsl";
    /**
     * The fragment shader's path
     */
    private static final String fragmentPath = "res/shaders/postProcessing/grayscale/fragmentShader.glsl";

    /**
     * Initializes a new GrayscaleShader.
     */
    private GrayscaleShader() {
	super(vertexPath, fragmentPath, null, null, null, null);
	List<File> paths = new ArrayList<>(2);
	paths.add(new File(vertexPath));
	paths.add(new File(fragmentPath));
	resourceId = new ResourceId(paths);
	ResourceManager.addShader(this);
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

    @NotNull
    @Override
    public ResourceId getResourceId() {
	return resourceId;
    }

    @Override
    public String toString() {
	return super.toString() + "\nGrayscaleShader{" + "resourceId=" + resourceId + '}';
    }

}
