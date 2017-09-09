package resources.shaders;

import java.io.*;
import java.util.*;
import resources.*;
import toolbox.annotations.*;

/**
 * This shader can draw meshes and splines by using the Blinn-Phong shading. You
 * can fill the materials with diffuse color or diffuse map, specular color or
 * specular map and normal map. If you set the appropirate parameters, the
 * specular map's alpha channel used as the glossiness value and the normal
 * map's alpha channel as a parallax map. If you don't fill the diffuse or
 * specular slots, the shader uses default values (basically you can even use
 * this Renderer with an empty material).
 */
public class SkyBoxShader extends Shader {

    /**
     * The only SkyBoxShader instance.
     */
    private static SkyBoxShader instance;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;
    /**
     * The vertex shader's path.
     */
    private static final String vertexPath = "res/shaders/skybox/vertexShader.glsl";
    /**
     * The fragment shader's path
     */
    private static final String fragmentPath = "res/shaders/skybox/fragmentShader.glsl";

    /**
     * Inizializes a new SkyBoxShader shader.
     */
    private SkyBoxShader() {
        super(vertexPath, fragmentPath, null, null, null);
        List<File> paths = new ArrayList<>(2);
        paths.add(new File(vertexPath));
        paths.add(new File(fragmentPath));
        resourceId = new ResourceId(paths);
        ResourceManager.addShader(this);
    }

    /**
     * Returns the SkyBoxShader instance.
     *
     * @return the SkyBoxShader instance
     */
    @NotNull
    public static SkyBoxShader getInstance() {
        if (instance == null || !instance.isUsable()) {
            instance = new SkyBoxShader();
        }
        return instance;
    }

    @Override
    protected void connectUniforms() {
        connectUniform("cubeMap");
    }

    @Override
    public void connectTextureUnits() {
        connectTextureUnit("cubeMap", 0);
    }

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public String toString() {
        return "SkyBoxShader{" + "resourceId=" + resourceId + '}';
    }

}
