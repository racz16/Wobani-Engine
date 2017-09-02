package shaders;

import components.light.*;
import core.*;
import org.joml.*;
import resources.*;
import resources.materials.*;
import resources.textures.*;
import toolbox.annotations.*;

/**
 * This shader can draw meshes and splines by using the Blinn-Phong shading. You
 * can fill the materials with diffuse color or diffuse map, specular color or
 * specular map and normal map. If you set the appropirate parameters, the
 * specular map's alpha channel used as the glossiness value and the normal
 * map's alpha channel as a parallax map. If you don't fill the diffuse or
 * specular slots, the shader uses default values (basically you can even use
 * this Renderer with an empty material).
 *
 * @see MaterialSlot#GLOSSINESS_USE_FLOAT
 * @see MaterialSlot#POM_USE_FLOAT
 * @see MaterialSlot#POM_SCALE_FLOAT
 * @see MaterialSlot#POM_MIN_LAYERS_FLOAT
 * @see MaterialSlot#POM_MAX_LAYERS_FLOAT
 */
public class BlinnPhongShader extends Shader {

    /**
     * The only BlinnPhongShader instance.
     */
    private static BlinnPhongShader instance;

    /**
     * Inizializes a new Blinn-Phong shader.
     */
    private BlinnPhongShader() {
        super("res/shaders/blinnPhong/vertexShader.glsl", "res/shaders/blinnPhong/fragmentShader.glsl", null, null, null);
        ResourceManager.addShader("." + ResourceManager.getNextId(), this);
    }

    /**
     * Returns the BlinnPhongShader instance.
     *
     * @return the BlinnPhongShader instance
     */
    @NotNull
    public static BlinnPhongShader getInstance() {
        if (instance == null || !instance.isUsable()) {
            instance = new BlinnPhongShader();
        }
        return instance;
    }

    @Override
    protected void connectUniforms() {
        //matrices
        connectUniform("modelMatrix");
        connectUniform("inverseModelMatrix3x3");
        connectUniform("shadowProjectionViewMatrix");
        //material
        //diffuse
        connectUniform("material.isThereDiffuseMap");
        connectUniform("material.diffuse");
        connectUniform("material.diffuseTile");
        connectUniform("material.diffuseOffset");
        connectUniform("material.diffuseColor");
        //specular
        connectUniform("material.isThereSpecularMap");
        connectUniform("material.specular");
        connectUniform("material.specularTile");
        connectUniform("material.specularOffset");
        connectUniform("material.specularColor");
        //glossiness
        connectUniform("material.isThereGlossiness");
        //normal
        connectUniform("material.isThereNormalMap");
        connectUniform("material.normal");
        connectUniform("material.normalTile");
        connectUniform("material.normalOffset");
        //pom
        connectUniform("material.isTherePOM");
        connectUniform("material.POMScale");
        connectUniform("material.POMMinLayers");
        connectUniform("material.POMMaxLayers");
        //misc
        connectUniform("wireframe");
        connectUniform("viewPosition");
        connectUniform("shadowMap");
        connectUniform("receiveShadow");
        connectUniform("gamma");
        connectUniform("useNormalMap");
    }

    /**
     * Loads various global data as uniform variables to the shader like view
     * position, gamma value etc.
     */
    public void loadGlobalUniforms() {
        //directional light
        DirectionalLightComponent light = (DirectionalLightComponent) Scene.getDirectionalLight();
        loadMatrix4("shadowProjectionViewMatrix", light.getProjectionViewMatrix());
        //others
        loadVector3("viewPosition", Scene.getCamera().getGameObject().getTransform().getAbsolutePosition());
        loadFloat("gamma", Settings.getGamma());
        loadBoolean("wireframe", Settings.isWireframeMode());
    }

    /**
     * Loads per object data to the shader as uniform variables.
     *
     * @param modelMatrix model matrix
     * @param inverseModelMatrix3x3 the 3x3 inverse of the model matrix
     * @param receiveShadow true if the Renderable is receive shadows, false
     * otherwise
     */
    public void loadObjectUniforms(@NotNull Matrix4f modelMatrix, @NotNull Matrix3f inverseModelMatrix3x3, boolean receiveShadow) {
        loadMatrix4("modelMatrix", modelMatrix);
        loadMatrix3("inverseModelMatrix3x3", inverseModelMatrix3x3);
        loadBoolean("receiveShadow", receiveShadow);
    }

    /**
     * Loads the diffuse values from the given MaterialSlot to the shader as
     * uniform variables.
     *
     * @param slot the MaterialSlot, which may contains the texture, the color
     * and the parameters
     */
    private void loadDiffuseSlot(@Nullable MaterialSlot slot) {
        String isThereMapUniformName = "material.isThereDiffuseMap";
        String tileName = "material.diffuseTile";
        String offsetName = "material.diffuseOffset";
        String colorUniformName = "material.diffuseColor";
        int textureUnit = 1;
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
     * Loads the specular values from the given MaterialSlot to the shader as
     * uniform variables.
     *
     * @param slot the MaterialSlot, which may contains the texture, the color
     * and the parameters
     */
    private void loadSpecularSlot(@Nullable MaterialSlot slot) {
        String isThereMapUniformName = "material.isThereSpecularMap";
        String tileName = "material.specularTile";
        String offsetName = "material.specularOffset";
        String colorUniformName = "material.specularColor";
        String useGlossiness = "material.isThereGlossiness";
        int textureUnit = 2;
        Vector4f defaultValue = new Vector4f(0.5f);

        if (slot != null && slot.isActive()) {
            Texture2D texture = slot.getTexture();
            Vector4f color = slot.getColor();
            if (texture != null) {
                texture.bindToTextureUnit(textureUnit);
                loadBoolean(isThereMapUniformName, true);
                loadVector2(tileName, slot.getTextureTile());
                loadVector2(offsetName, slot.getTextureOffset());
                Float isThereGlossiness = slot.getFloatParameter(MaterialSlot.GLOSSINESS_USE_FLOAT);
                if (isThereGlossiness == null || isThereGlossiness != 1f) {
                    loadBoolean(useGlossiness, false);
                    if (color != null) {
                        loadVector4(colorUniformName, color);
                    } else {
                        loadVector4(colorUniformName, defaultValue);
                    }
                } else {
                    loadBoolean(useGlossiness, true);
                }
            } else if (color != null) {
                loadVector4(colorUniformName, color);
                loadBoolean(isThereMapUniformName, false);
            } else {
                loadVector4(colorUniformName, defaultValue);
                loadBoolean(isThereMapUniformName, false);
            }
        } else {
            loadVector4(colorUniformName, defaultValue);
            loadBoolean(isThereMapUniformName, false);
        }
    }

    /**
     * Loads the normal values from the given MaterialSlot to the shader as
     * uniform variables.
     *
     * @param slot the MaterialSlot, which may contains the texture, the color
     * and the parameters
     */
    private void loadNormalSlot(@Nullable MaterialSlot slot) {
        String isThereMapUniformName = "material.isThereNormalMap";
        String useNormalUniformName = "useNormalMap";
        String tileName = "material.normalTile";
        String offsetName = "material.normalOffset";
        String isTherePomUniformName = "material.isTherePOM";
        int textureUnit = 3;
        String POMScale = "material.POMScale";
        String POMMinLayers = "material.POMMinLayers";
        String POMMaxLayers = "material.POMMaxLayers";
        float defPOMScale = 0.1f;
        float defPOMMinLayers = 15f;
        float defPOMMaxLayers = 30f;

        if (slot != null && slot.isActive()) {
            Texture2D texture = slot.getTexture();
            if (texture != null) {
                texture.bindToTextureUnit(textureUnit);
                loadBoolean(isThereMapUniformName, true);
                loadBoolean(useNormalUniformName, true);
                loadVector2(tileName, slot.getTextureTile());
                loadVector2(offsetName, slot.getTextureOffset());
                Float usePom = slot.getFloatParameter(MaterialSlot.POM_USE_FLOAT);
                if (usePom != null && usePom == 1f) {
                    loadBoolean(isTherePomUniformName, true);
                    Float value = slot.getFloatParameter(MaterialSlot.POM_SCALE_FLOAT);
                    loadFloat(POMScale, value == null ? defPOMScale : value);
                    value = slot.getFloatParameter(MaterialSlot.POM_MIN_LAYERS_FLOAT);
                    loadFloat(POMMinLayers, value == null ? defPOMMinLayers : value);
                    value = slot.getFloatParameter(MaterialSlot.POM_MAX_LAYERS_FLOAT);
                    loadFloat(POMMaxLayers, value == null ? defPOMMaxLayers : value);
                } else {
                    loadBoolean(isTherePomUniformName, false);
                }
            } else {
                loadBoolean(isThereMapUniformName, false);
                loadBoolean(useNormalUniformName, false);
            }
        } else {
            loadBoolean(isThereMapUniformName, false);
            loadBoolean(useNormalUniformName, false);
        }
    }

    /**
     * Loads the given Material's data to the shader as uniform variables.
     *
     * @param material material
     */
    public void loadMaterial(@NotNull Material material) {
        //diffuse
        loadDiffuseSlot(material.getSlot(Material.DIFFUSE));
        //specular
        loadSpecularSlot(material.getSlot(Material.SPECULAR));
        //normal
        loadNormalSlot(material.getSlot(Material.NORMAL));
    }

    @Override
    public void connectTextureUnits() {
        connectTextureUnit("shadowMap", 0);
        connectTextureUnit("material.diffuse", 1);
        connectTextureUnit("material.specular", 2);
        connectTextureUnit("material.normal", 3);
    }

}
