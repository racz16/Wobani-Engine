package wobani.resources.shader.postprocessing;

import wobani.rendering.*;
import wobani.resources.*;
import wobani.resources.shader.*;
import wobani.toolbox.annotation.*;

import java.io.*;
import java.util.*;

/**
 Performs gamma correction on the image.

 @see Settings#getGamma() */
public class GammaCorrectionShader extends Shader{

    /**
     The vertex shader's path.
     */
    private static final String vertexPath = "res/shaders/postProcessing/gammaCorrection/vertexShader.glsl";
    /**
     The fragment shader's path
     */
    private static final String fragmentPath = "res/shaders/postProcessing/gammaCorrection/fragmentShader.glsl";
    /**
     The only GammaCorrectionShader instance.
     */
    private static GammaCorrectionShader instance;
    /**
     The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     Initializes a new GammaCorrectionShader.
     */
    private GammaCorrectionShader(){
        super(vertexPath, fragmentPath, null, null, null);
        List<File> paths = new ArrayList<>(2);
        paths.add(new File(vertexPath));
        paths.add(new File(fragmentPath));
        resourceId = new ResourceId(paths);
        ResourceManager.addShader(this);
    }

    /**
     Returns the GammaCorrectionShader instance.

     @return the GammaCorrectionShader instance
     */
    @NotNull
    public static GammaCorrectionShader getInstance(){
        if(instance == null || !instance.isUsable()){
            instance = new GammaCorrectionShader();
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
        connectUniform("gamma");
    }

    /**
     Loads the gamma value to the shader as a uniform variable.

     @see Settings#getGamma()
     */
    public void loadGammaUniform(){
        float gamma = RenderingPipeline.getParameters().getValueOrDefault(RenderingPipeline.GAMMA, 1f);
        loadFloat("gamma", gamma);
    }

    @NotNull
    @Override
    public ResourceId getResourceId(){
        return resourceId;
    }

    @Override
    public String toString(){
        return super.toString() + "\nGammaCorrectionShader{" + "resourceId=" + resourceId + '}';
    }

}
