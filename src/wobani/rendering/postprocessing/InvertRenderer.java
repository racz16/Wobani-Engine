package wobani.rendering.postprocessing;

import wobani.resources.shader.postprocessing.*;
import wobani.toolbox.annotation.*;

/**
 Inverts the frame's colors.
 */
public class InvertRenderer extends PostProcessingRenderer{

    /**
     The only InvertRenderer instance.
     */
    protected static InvertRenderer instance;

    /**
     Initializes a new InvertRenderer.
     */
    private InvertRenderer(){
        refreshShader();
    }

    /**
     Returns the InvertRenderer instance.

     @return the InvertRenderer instance
     */
    @NotNull
    public static InvertRenderer getInstance(){
        if(instance == null){
            instance = new InvertRenderer();
        }
        return instance;
    }

    @Override
    protected void refreshShader(){
        if(shader == null || !shader.isUsable()){
            shader = InvertShader.getInstance();
        }
    }

    @Override
    public boolean isUsable(){
        return true;
    }

}
