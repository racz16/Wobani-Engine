package wobani.resources.shader;

import java.io.*;
import java.util.*;
import org.joml.*;
import wobani.material.*;
import wobani.rendering.*;
import wobani.resources.*;
import wobani.resources.texture.texture2d.*;
import wobani.toolbox.annotation.*;

public class EnvironmentShader extends Shader {

    /**
     * The only BlinnPhongShader instance.
     */
    private static EnvironmentShader instance;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;
    /**
     * The vertex shader's path.
     */
    private static final String vertexPath = "res/shaders/environment/vertexShader.glsl";
    /**
     * The fragment shader's path
     */
    private static final String fragmentPath = "res/shaders/environment/fragmentShader.glsl";

    /**
     * Inizializes a new Blinn-Phong shader.
     */
    private EnvironmentShader() {
        super(vertexPath, fragmentPath, null, null, null);
        List<File> paths = new ArrayList<>(2);
        paths.add(new File(vertexPath));
        paths.add(new File(fragmentPath));
        resourceId = new ResourceId(paths);
        ResourceManager.addShader(this);
    }

    /**
     * Returns the BlinnPhongShader instance.
     *
     * @return the BlinnPhongShader instance
     */
    @NotNull
    public static EnvironmentShader getInstance() {
        if (instance == null || !instance.isUsable()) {
            instance = new EnvironmentShader();
        }
        return instance;
    }

    @Override
    protected void connectUniforms() {
        //matrices
        connectUniform("modelMatrix");
        connectUniform("inverseModelMatrix3x3");
        connectUniform("projectionMatrix");
        connectUniform("viewMatrix");
        //material
        //diffuse
        connectUniform("material.isThereDiffuseMap");
        connectUniform("material.diffuse");
        connectUniform("material.diffuseTile");
        connectUniform("material.diffuseOffset");
        connectUniform("material.diffuseColor");
        //misc
        connectUniform("gamma");
    }

    /**
     * Loads various global data as uniform variables to the shader like view
     * position, gamma value etc.
     */
    public void loadGlobalUniforms(@NotNull Matrix4f projectionMatrix) {
        float gamma = RenderingPipeline.getParameters().getValueOrDefault(RenderingPipeline.GAMMA, 1f);
        //FIXME: should be sRGB isn't it?
        loadBoolean("gamma", gamma != 1);
        loadMatrix4("projectionMatrix", projectionMatrix);
    }

    public void loadViewMatrix(@NotNull Matrix4f viewMatrix) {
        loadMatrix4("viewMatrix", viewMatrix);
    }

    /**
     * Loads per object data to the shader as uniform variables.
     *
     * @param modelMatrix           model matrix
     * @param inverseModelMatrix3x3 the 3x3 inverse of the model matrix
     * @param receiveShadow         true if the Renderable is receive shadows,
     *                              false otherwise
     */
    public void loadObjectUniforms(@NotNull Matrix4f modelMatrix, @NotNull Matrix3f inverseModelMatrix3x3) {
        loadMatrix4("modelMatrix", modelMatrix);
        loadMatrix3("inverseModelMatrix3x3", inverseModelMatrix3x3);
    }

    /**
     * Loads the diffuse values from the given Material to the shader as uniform
     * variables.
     *
     * @param material renderable's material
     */
    private void loadDiffuseSlot(@Nullable Material material) {
        MaterialSlot slot = material.getSlot(Material.DIFFUSE);
        String isThereMapUniformName = "material.isThereDiffuseMap";
        String tileName = "material.diffuseTile";
        String offsetName = "material.diffuseOffset";
        String colorUniformName = "material.diffuseColor";
        int textureUnit = 0;
        Vector3f defaultValue = new Vector3f(0.5f);

        if (slot != null && slot.isActive()) {
            Texture2D texture = slot.getTexture();
            Vector4f color = slot.getColor();
            if (texture != null) {
                texture.bindToTextureUnit(textureUnit);
                loadBoolean(isThereMapUniformName, true);
                loadVector2(tileName, slot.getTextureTile());
                loadVector2(offsetName, slot.getTextureOffset());
            } else if (color != null) {
                loadVector3(colorUniformName, new Vector3f(color.x, color.y, color.z));
                loadBoolean(isThereMapUniformName, false);
            } else {
                loadVector3(colorUniformName, defaultValue);
                loadBoolean(isThereMapUniformName, false);
            }
        } else {
            loadVector3(colorUniformName, defaultValue);
            loadBoolean(isThereMapUniformName, false);
        }
    }

    /**
     * Loads the given Material's data to the shader as uniform variables.
     *
     * @param material material
     */
    public void loadMaterial(@NotNull Material material) {
        loadDiffuseSlot(material);
    }

    @Override
    public void connectTextureUnits() {
        connectTextureUnit("material.diffuse", 0);
    }

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }
}
