package wobani.rendering.postprocessing;

import wobani.resource.opengl.shader.postprocessing.*;
import wobani.toolbox.annotation.*;

/**
 Performs FXAA.
 */
public class FxaaRenderer extends PostProcessingRenderer{

    /**
     The only FxaaRenderer instance.
     */
    private static FxaaRenderer instance;

    /**
     Initializes a new FxaaRenderer.
     */
    private FxaaRenderer(){
        refreshShader();
    }

    /**
     Returns the FxaaRenderer instance.

     @return the FxaaRenderer instance
     */
    @NotNull
    public static FxaaRenderer getInstance(){
        if(instance == null){
            instance = new FxaaRenderer();
        }
        return instance;
    }

    @Override
    protected void refreshShader(){
        if(shader == null || !shader.isUsable()){
            shader = FxaaShader.getInstance();
        }
    }

    @Override
    public boolean isUsable(){
        return true;
    }

}
