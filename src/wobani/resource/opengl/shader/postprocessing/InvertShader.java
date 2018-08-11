package wobani.resource.opengl.shader.postprocessing;

import wobani.resource.*;
import wobani.resource.opengl.shader.*;
import wobani.toolbox.annotation.*;

import java.io.*;
import java.util.*;

/**
 Inverts the image's colors.
 */
public class InvertShader extends Shader{

    /**
     The vertex shader's path.
     */
    private static final String vertexPath = "res/shaders/postProcessing/invert/vertexShader.glsl";
    /**
     The fragment shader's path
     */
    private static final String fragmentPath = "res/shaders/postProcessing/invert/fragmentShader.glsl";
    /**
     The only InvertShader instance.
     */
    private static InvertShader instance;
    /**
     The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     Initializes a new InvertShader.
     */
    private InvertShader(){
        super(vertexPath, fragmentPath, null, null, null);
        List<File> paths = new ArrayList<>(2);
        paths.add(new File(vertexPath));
        paths.add(new File(fragmentPath));
        resourceId = new ResourceId(paths);
        ResourceManager.addResource(this);
    }

    /**
     Returns the InvertShader instance.

     @return the InvertShader instance
     */
    @NotNull
    public static InvertShader getInstance(){
        if(instance == null || !instance.isUsable()){
            instance = new InvertShader();
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
        return super.toString() + "\nInvertShader{" + "resourceId=" + resourceId + '}';
    }

}
