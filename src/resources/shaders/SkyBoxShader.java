package resources.shaders;

import java.io.*;
import java.util.*;
import materials.*;
import resources.*;
import toolbox.annotations.*;

/**
 * This shader can draw a skybox. In theory it can render any number of meshes
 * and splines, but in practice it's adivsed to only use one cube. To render the
 * cube, it's material must contain a CubeMapTexture in the diffuse slot. If
 * there is no CubeMapTexture in the diffuse slot, the entire cube will be
 * filled with mid-grey color.
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
        connectUniform("isThereCubeMap");
    }

    /**
     * Loads the skybox's material properties to the shader as uniform
     * variables.
     *
     * @param material material
     */
    public void loadUniforms(@NotNull Material material) {
        if (material.getSlot(Material.DIFFUSE) != null && material.getSlot(Material.DIFFUSE).getEnvironmentProbe() != null) {
            material.getSlot(Material.DIFFUSE).getEnvironmentProbe().bindToTextureUnit(0);
            loadBoolean("isThereCubeMap", true);
        } else {
            loadBoolean("isThereCubeMap", false);
        }
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
