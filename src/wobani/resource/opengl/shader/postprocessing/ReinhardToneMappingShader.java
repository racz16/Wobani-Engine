package wobani.resource.opengl.shader.postprocessing;

import wobani.resource.*;
import wobani.resource.opengl.shader.*;
import wobani.toolbox.annotation.*;

import java.io.*;
import java.util.*;

/**
 Performs Reinhard tone mapping on the image.
 */
public class ReinhardToneMappingShader extends Shader{

    /**
     The vertex shader's path.
     */
    private static final String vertexPath = "res/shaders/postProcessing/reinhardToneMapping/vertexShader.glsl";
    /**
     The fragment shader's path
     */
    private static final String fragmentPath = "res/shaders/postProcessing/reinhardToneMapping/fragmentShader.glsl";
    /**
     The only ReinhardToneMappingShader instance.
     */
    private static ReinhardToneMappingShader instance;
    /**
     The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     Initializes a new ReinhardToneMappingShader.
     */
    private ReinhardToneMappingShader(){
        super(vertexPath, fragmentPath, null, null, null);
        List<File> paths = new ArrayList<>(2);
        paths.add(new File(vertexPath));
        paths.add(new File(fragmentPath));
        resourceId = new ResourceId(paths);
        ResourceManager.addResource(this);
    }

    /**
     Returns the ReinhardToneMappingShader instance.

     @return the ReinhardToneMappingShader instance
     */
    @NotNull
    public static ReinhardToneMappingShader getInstance(){
        if(instance == null || !instance.isUsable()){
            instance = new ReinhardToneMappingShader();
        }
        return instance;
    }

    @Override
    public void connectTextureUnits(){
        connectTextureUnit("image", 0);
    }

    @Override
    protected void connectUniforms(){
        connectUniform("image");
    }

    @NotNull
    @Override
    public ResourceId getResourceId(){
        return resourceId;
    }

    @Override
    public String toString(){
        return super.toString() + "\nReinhardToneMappingShader{" + "resourceId=" + resourceId + '}';
    }

}
