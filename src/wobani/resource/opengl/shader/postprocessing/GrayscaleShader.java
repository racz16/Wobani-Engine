package wobani.resource.opengl.shader.postprocessing;

import wobani.resource.*;
import wobani.resource.opengl.shader.*;
import wobani.toolbox.annotation.*;

import java.io.*;
import java.util.*;

/**
 Makes the image grayscaled.
 */
public class GrayscaleShader extends Shader{

    /**
     The vertex shader's path.
     */
    private static final String vertexPath = "res/shaders/postProcessing/grayscale/vertexShader.glsl";
    /**
     The fragment shader's path
     */
    private static final String fragmentPath = "res/shaders/postProcessing/grayscale/fragmentShader.glsl";
    /**
     The only GrayscaleShader instance.
     */
    private static GrayscaleShader instance;
    /**
     The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     Initializes a new GrayscaleShader.
     */
    private GrayscaleShader(){
        super(vertexPath, fragmentPath, null, null, null);
        List<File> paths = new ArrayList<>(2);
        paths.add(new File(vertexPath));
        paths.add(new File(fragmentPath));
        resourceId = new ResourceId(paths);
        ResourceManager.addResource(this);
    }

    /**
     Returns the GrayscaleShader instance.

     @return the GrayscaleShader instance
     */
    @NotNull
    public static GrayscaleShader getInstance(){
        if(instance == null || !instance.isUsable()){
            instance = new GrayscaleShader();
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
        return super.toString() + "\nGrayscaleShader{" + "resourceId=" + resourceId + '}';
    }

}