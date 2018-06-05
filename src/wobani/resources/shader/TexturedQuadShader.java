package wobani.resources.shader;

import java.io.*;
import java.util.*;
import wobani.resources.*;
import wobani.toolbox.annotation.*;

/**
 * This shader can render an image to a quad.
 */
public class TexturedQuadShader extends Shader {

    /**
     * The only TexturedQuadShader instance.
     */
    private static TexturedQuadShader instance;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;
    /**
     * The vertex shader's path.
     */
    private static final String vertexPath = "res/shaders/texturedQuad/vertexShader.glsl";
    /**
     * The fragment shader's path
     */
    private static final String fragmentPath = "res/shaders/texturedQuad/fragmentShader.glsl";

    /**
     * Initializes a new TexturedQuadShader.
     */
    private TexturedQuadShader() {
	super(vertexPath, fragmentPath, null, null, null, null);
	List<File> paths = new ArrayList<>(2);
	paths.add(new File(vertexPath));
	paths.add(new File(fragmentPath));
	resourceId = new ResourceId(paths);
	ResourceManager.addShader(this);
    }

    /**
     * Returns the TexturedQuadShader instance.
     *
     * @return the TexturedQuadShader instance
     */
    @NotNull
    public static TexturedQuadShader getInstance() {
	if (instance == null || !instance.isUsable()) {
	    instance = new TexturedQuadShader();
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
	return super.toString() + "\nTexturedQuadShader{" + "resourceId="
		+ resourceId + '}';
    }

}
