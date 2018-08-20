package wobani.resource.opengl.shader;

import org.joml.*;
import wobani.component.camera.*;
import wobani.core.*;
import wobani.material.*;
import wobani.rendering.*;
import wobani.resource.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.annotation.*;

import java.io.*;
import java.util.*;

/**
 * This shader can draw meshes and splines by using the Blinn-Phong shading. You can fill the materials with diffuse color
 * or diffuse map, specular color or specular map and normal map. If you set the appropirate parameters, the specular map's
 * alpha channel used as the glossiness value and the normal map's alpha channel as a parallax map. If you don't fill the
 * diffuse or specular slots, the shader uses default values (basically you can even use this Renderer with an empty
 * material).
 */
public class BlinnPhongShader extends Shader{

    /**
     * The vertex shader's path.
     */
    private static final String vertexPath = "res/shaders/blinnPhong/vertexShader.glsl";
    /**
     * The fragment shader's path
     */
    private static final String fragmentPath = "res/shaders/blinnPhong/fragmentShader.glsl";
    /**
     * The only BlinnPhongShader instance.
     */
    private static BlinnPhongShader instance;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     * Inizializes a new Blinn-Phong shader.
     */
    public BlinnPhongShader(){
        super(vertexPath, fragmentPath, null, null, null);
        List<File> paths = new ArrayList<>(2);
        paths.add(new File(vertexPath));
        paths.add(new File(fragmentPath));
        resourceId = new ResourceId(paths);
        ResourceManager.addResource(this);
    }

    @Override
    protected void connectUniforms(){
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
        //reflection
        connectUniform("material.isThereReflectionMap");
        connectUniform("material.reflection");
        connectUniform("material.isThereRefractionMap");
        connectUniform("material.refraction");
        connectUniform("material.refractionIndex");
        connectUniform("material.isThereEnvironmentIntensityMap");
        connectUniform("material.environmentIntensity");
        connectUniform("material.environmentIntensityColor");
        connectUniform("material.environmentIntensityTile");
        connectUniform("material.environmentIntensityOffset");

        connectUniform("material.isThereParallaxCorrection");
        connectUniform("material.geometryProxyRadius");
        connectUniform("material.environmentProbePosition");
        //misc
        connectUniform("wireframe");
        connectUniform("viewPosition");
        connectUniform("shadowMap");
        connectUniform("receiveShadow");
        connectUniform("gamma");
        connectUniform("useNormalMap");
    }

    /**
     * Loads various global data as uniform variables to the shader like view position, gamma value etc.
     */
    public void loadGlobalUniforms(){
        Camera mainCamera = Scene.getParameters().getValue(Scene.MAIN_CAMERA);
        loadMatrix4("shadowProjectionViewMatrix", RenderingPipeline.getParameters()
                .getValue(RenderingPipeline.SHADOW_PROJECTION_VIEW_MATRIX));
        //others
        loadVector3("viewPosition", mainCamera.getGameObject().getTransform().getAbsolutePosition());
        float gamma = RenderingPipeline.getParameters().getValueOrDefault(RenderingPipeline.GAMMA, 1f);
        //FIXME: should be sRGB isn't it?
        loadBoolean("gamma", gamma != 1);
        boolean wireframe = RenderingPipeline.getParameters()
                .getValueOrDefault(RenderingPipeline.WIREFRAME_MODE, false);
        loadBoolean("wireframe", wireframe);
    }

    /**
     * Loads per object data to the shader as uniform variables.
     *
     * @param modelMatrix           model matrix
     * @param inverseModelMatrix3x3 the 3x3 inverse of the model matrix
     * @param receiveShadow         true if the Renderable is receive shadows, false otherwise
     */
    public void loadObjectUniforms(@NotNull Matrix4f modelMatrix, @NotNull Matrix3f inverseModelMatrix3x3, boolean receiveShadow){
        loadMatrix4("modelMatrix", modelMatrix);
        loadMatrix3("inverseModelMatrix3x3", inverseModelMatrix3x3);
        loadBoolean("receiveShadow", receiveShadow);
    }

    /**
     * Loads the diffuse values from the given Material to the shader as uniform variables.
     *
     * @param material renderable's material
     */
    private void loadDiffuseSlot(@Nullable Material material){
        MaterialSlot slot = material.getSlot(Material.DIFFUSE);
        String isThereMapUniformName = "material.isThereDiffuseMap";
        String tileName = "material.diffuseTile";
        String offsetName = "material.diffuseOffset";
        String colorUniformName = "material.diffuseColor";
        int textureUnit = 1;
        Vector3f defaultValue = new Vector3f(0.5f);

        if(slot != null && slot.isActive()){
            Texture2D texture = slot.getTexture();
            Vector4f color = slot.getColor();
            if(texture != null){
                texture.bindToTextureUnit(textureUnit);
                loadBoolean(isThereMapUniformName, true);
                loadVector2(tileName, slot.getTextureTile());
                loadVector2(offsetName, slot.getTextureOffset());
            }else if(color != null){
                loadVector3(colorUniformName, new Vector3f(color.x, color.y, color.z));
                loadBoolean(isThereMapUniformName, false);
            }else{
                loadVector3(colorUniformName, defaultValue);
                loadBoolean(isThereMapUniformName, false);
            }
        }else{
            loadVector3(colorUniformName, defaultValue);
            loadBoolean(isThereMapUniformName, false);
        }
    }

    /**
     * Loads the specular values from the given Material to the shader as uniform variables.
     *
     * @param material renderable's material
     */
    private void loadSpecularSlot(@Nullable Material material){
        MaterialSlot slot = material.getSlot(Material.SPECULAR);
        String isThereMapUniformName = "material.isThereSpecularMap";
        String tileName = "material.specularTile";
        String offsetName = "material.specularOffset";
        String colorUniformName = "material.specularColor";
        String useGlossiness = "material.isThereGlossiness";
        int textureUnit = 2;
        Vector4f defaultValue = new Vector4f(0.5f);

        if(slot != null && slot.isActive()){
            Texture2D texture = slot.getTexture();
            Vector4f color = slot.getColor();
            if(texture != null){
                texture.bindToTextureUnit(textureUnit);
                loadBoolean(isThereMapUniformName, true);
                loadVector2(tileName, slot.getTextureTile());
                loadVector2(offsetName, slot.getTextureOffset());
                Float isThereGlossiness = slot.getParameters().getValue(MaterialSlot.USE_GLOSSINESS);
                if(isThereGlossiness == null || isThereGlossiness != 1f){
                    loadBoolean(useGlossiness, false);
                    if(color != null){
                        loadVector4(colorUniformName, color);
                    }else{
                        loadVector4(colorUniformName, defaultValue);
                    }
                }else{
                    loadBoolean(useGlossiness, true);
                }
            }else if(color != null){
                loadVector4(colorUniformName, color);
                loadBoolean(isThereMapUniformName, false);
            }else{
                loadVector4(colorUniformName, defaultValue);
                loadBoolean(isThereMapUniformName, false);
            }
        }else{
            loadVector4(colorUniformName, defaultValue);
            loadBoolean(isThereMapUniformName, false);
        }
    }

    /**
     * Loads the normal values from the given Material to the shader as uniform variables.
     *
     * @param material renderable's material
     */
    private void loadNormalSlot(@Nullable Material material){
        MaterialSlot slot = material.getSlot(Material.NORMAL);
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

        if(slot != null && slot.isActive()){
            Texture2D texture = slot.getTexture();
            if(texture != null){
                texture.bindToTextureUnit(textureUnit);
                loadBoolean(isThereMapUniformName, true);
                loadBoolean(useNormalUniformName, true);
                loadVector2(tileName, slot.getTextureTile());
                loadVector2(offsetName, slot.getTextureOffset());
                Float usePom = material.getParameters().getValue(MaterialSlot.USE_POM);
                if(usePom != null && usePom == 1f){
                    loadBoolean(isTherePomUniformName, true);
                    Float value = material.getParameters().getValue(MaterialSlot.POM_SCALE);
                    loadFloat(POMScale, value == null ? defPOMScale : value);
                    value = material.getParameters().getValue(MaterialSlot.POM_MIN_LAYERS);
                    loadFloat(POMMinLayers, value == null ? defPOMMinLayers : value);
                    value = material.getParameters().getValue(MaterialSlot.POM_MAX_LAYERS);
                    loadFloat(POMMaxLayers, value == null ? defPOMMaxLayers : value);
                }else{
                    loadBoolean(isTherePomUniformName, false);
                }
            }else{
                loadBoolean(isThereMapUniformName, false);
                loadBoolean(useNormalUniformName, false);
            }
        }else{
            loadBoolean(isThereMapUniformName, false);
            loadBoolean(useNormalUniformName, false);
        }
    }

    /**
     * Loads the reflection and refraction values from the given Material to the shader as uniform variables.
     *
     * @param material renderable's material
     */
    private void loadEnvironmentSlots(@Nullable Material material){
        //reflection
        MaterialSlot reflectionSlot = material.getSlot(Material.REFLECTION);
        String isThereReflectionMap = "material.isThereReflectionMap";
        int reflectionTextureUnit = 4;
        boolean reflectionUsable = reflectionSlot != null && reflectionSlot.isActive() && reflectionSlot
                .getEnvironmentProbe() != null;
        //refraction
        MaterialSlot refractionSlot = material.getSlot(Material.REFRACTION);
        String isThereRefractionMap = "material.isThereRefractionMap";
        int refractionTextureUnit = 5;
        String refractionIndex = "material.refractionIndex";
        float index = material.getParameters().getValue(Material.REFRACTION_INDEX) == null ? 1f / 1.33f : material
                .getParameters().getValue(Material.REFRACTION_INDEX);
        boolean refractionUsable = refractionSlot != null && refractionSlot.isActive() && refractionSlot
                .getEnvironmentProbe() != null;
        //intensity
        MaterialSlot intensitySlot = material.getSlot(Material.ENVIRONMENT_INTENSITY);
        String isThereIntensityMap = "material.isThereEnvironmentIntensityMap";
        String intensityColor = "material.environmentIntensityColor";
        String tileName = "material.environmentIntensityTile";
        String offsetName = "material.environmentIntensityOffset";
        int intensityTextureUnit = 6;
        //parallax correction
        String isThereParallaxCorrection = "material.isThereParallaxCorrection";
        String parallaxCorrectionValue = "material.geometryProxyRadius";
        String environmentProbePosition = "material.environmentProbePosition";

        if(reflectionUsable || refractionUsable){
            if(!reflectionUsable && refractionUsable){
                loadBoolean(isThereReflectionMap, false);

                loadBoolean(isThereRefractionMap, true);
                refractionSlot.getEnvironmentProbe().bindToTextureUnit(refractionTextureUnit);
                loadFloat(refractionIndex, index);
            }else if(reflectionUsable && !refractionUsable){
                loadBoolean(isThereReflectionMap, true);
                reflectionSlot.getEnvironmentProbe().bindToTextureUnit(reflectionTextureUnit);

                loadBoolean(isThereRefractionMap, false);
            }else{
                loadBoolean(isThereReflectionMap, true);
                reflectionSlot.getEnvironmentProbe().bindToTextureUnit(reflectionTextureUnit);

                loadBoolean(isThereRefractionMap, true);
                refractionSlot.getEnvironmentProbe().bindToTextureUnit(refractionTextureUnit);
                loadFloat(refractionIndex, index);
            }
            if(reflectionUsable){
                boolean pc = reflectionSlot.getEnvironmentProbe().isParallaxCorrection();
                loadBoolean(isThereParallaxCorrection, pc);
                if(pc){
                    loadFloat(parallaxCorrectionValue, reflectionSlot.getEnvironmentProbe()
                            .getParallaxCorrectionValue());
                    loadVector3(environmentProbePosition, reflectionSlot.getEnvironmentProbe().getPosition());
                }
            }
            if(intensitySlot != null && intensitySlot.isActive()){
                Texture2D texture = intensitySlot.getTexture();
                Vector4f color = intensitySlot.getColor();
                if(texture != null){
                    texture.bindToTextureUnit(intensityTextureUnit);
                    loadBoolean(isThereIntensityMap, true);
                    loadVector2(tileName, intensitySlot.getTextureTile());
                    loadVector2(offsetName, intensitySlot.getTextureOffset());
                }else if(color != null){
                    loadVector3(intensityColor, new Vector3f(color.x, color.y, color.z));
                    loadBoolean(isThereIntensityMap, false);
                }else{
                    loadBoolean(isThereIntensityMap, false);
                    loadVector3(intensityColor, new Vector3f(1));
                }
            }else{
                loadBoolean(isThereIntensityMap, false);
                loadVector3(intensityColor, new Vector3f(1));
            }
        }else{
            loadBoolean(isThereReflectionMap, false);
            loadBoolean(isThereRefractionMap, false);
        }
    }

    /**
     * Loads the given Material's data to the shader as uniform variables.
     *
     * @param material material
     */
    public void loadMaterial(@NotNull Material material){
        //diffuse
        loadDiffuseSlot(material);
        //specular
        loadSpecularSlot(material);
        //normal
        loadNormalSlot(material);
        //reflection and refraction
        loadEnvironmentSlots(material);
    }

    @Override
    public void connectTextureUnits(){
        connectTextureUnit("shadowMap", 0);
        connectTextureUnit("material.diffuse", 1);
        connectTextureUnit("material.specular", 2);
        connectTextureUnit("material.normal", 3);
        connectTextureUnit("material.reflection", 4);
        connectTextureUnit("material.refraction", 5);
        connectTextureUnit("material.environmentIntensity", 6);
    }

    @NotNull
    @Override
    public ResourceId getResourceId(){
        return resourceId;
    }

    @Override
    public String toString(){
        return super.toString() + "\nBlinnPhongShader{" + "resourceId=" + resourceId + '}';
    }

}
