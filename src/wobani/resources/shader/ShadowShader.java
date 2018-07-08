package wobani.resources.shader;

import org.joml.*;
import wobani.resources.*;
import wobani.toolbox.annotation.*;

import java.io.*;
import java.util.*;

/**
 This shader can render the shadow map.
 */
public class ShadowShader extends Shader{

    /**
     The vertex shader's path.
     */
    private static final String vertexPath = "res/shaders/shadow/vertexShader.glsl";
    /**
     The fragment shader's path
     */
    private static final String fragmentPath = "res/shaders/shadow/fragmentShader.glsl";
    /**
     The only ShadowShader instance.
     */
    private static ShadowShader instance;
    /**
     The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     Initializes a new ShadowShader.
     */
    private ShadowShader(){
        super(vertexPath, fragmentPath, null, null, null);
        List<File> paths = new ArrayList<>(2);
        paths.add(new File(vertexPath));
        paths.add(new File(fragmentPath));
        resourceId = new ResourceId(paths);
        ResourceManager.addShader(this);
    }

    /**
     Returns the ShadowShader instance.

     @return the ShadowShader instance
     */
    @NotNull
    public static ShadowShader getInstance(){
        if(instance == null || !instance.isUsable()){
            instance = new ShadowShader();
        }
        return instance;
    }

    @Override
    protected void connectUniforms(){
        connectUniform("projectionViewModelMatrix");
    }

    /**
     Loads the projection view model matrix as a uniform variable.

     @param projectionViewModelMatrix projection view model matrix
     */
    public void loadProjectionViewModelMatrix(@NotNull Matrix4f projectionViewModelMatrix){
        loadMatrix4("projectionViewModelMatrix", projectionViewModelMatrix);
    }

    @NotNull
    @Override
    public ResourceId getResourceId(){
        return resourceId;
    }

    @Override
    public String toString(){
        return super.toString() + "\nShadowShader{" + "resourceId=" + resourceId + '}';
    }

}
