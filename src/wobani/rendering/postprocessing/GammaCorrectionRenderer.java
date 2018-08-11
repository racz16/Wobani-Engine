package wobani.rendering.postprocessing;

import wobani.resource.opengl.shader.postprocessing.*;
import wobani.toolbox.annotation.*;

/**
 Performs gamma correction.

 @see Settings#getGamma() */
public class GammaCorrectionRenderer extends PostProcessingRenderer{

    /**
     The only GammaCorrectionRenderer instance.
     */
    private static GammaCorrectionRenderer instance;

    /**
     Initializes a new GammaCorrectionRenderer.
     */
    private GammaCorrectionRenderer(){
        refreshShader();
    }

    /**
     Returns the GammaCorrectionRenderer instance.

     @return the GammaCorrectionRenderer instance
     */
    @NotNull
    public static GammaCorrectionRenderer getInstance(){
        if(instance == null){
            instance = new GammaCorrectionRenderer();
        }
        return instance;
    }

    @Override
    protected void refreshShader(){
        if(shader == null || !shader.isUsable()){
            shader = GammaCorrectionShader.getInstance();
        }
    }

    @Override
    protected void beforeDrawQuad(){
        super.beforeDrawQuad();
        ((GammaCorrectionShader) shader).loadGammaUniform();
    }

    @Override
    public boolean isUsable(){
        return true;
    }

}
