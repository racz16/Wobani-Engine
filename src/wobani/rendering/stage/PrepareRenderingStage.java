package wobani.rendering.stage;

import wobani.rendering.prepare.*;
import wobani.toolbox.annotation.*;

import java.util.*;

public class PrepareRenderingStage{

    /**
     List of geometry renderers. You may add to this list renderers like Blinn-Phong, shadow renderer or toon renderer to
     actually draw the scene's geometry.
     */
    private final List<PrepareRenderer> renderers = new ArrayList<>();

    public void render(){
        for(PrepareRenderer renderer : renderers){
            renderer.render();
        }
    }

    public boolean addRendererToTheEnd(@NotNull PrepareRenderer renderer){
        return addRenderer(getRendererCount(), renderer);
    }

    public boolean addRenderer(int index, @NotNull PrepareRenderer renderer){
        for(PrepareRenderer ren : renderers){
            if(ren.getClass() == renderer.getClass()){
                return false;
            }
        }
        renderers.add(index, renderer);
        return true;
    }

    /**
     Returns the indexth renderer of the specified stage.

     @param geometry specifies the list of Renderers
     @param index    index

     @return the indexth renderer of the specified stage
     */
    @NotNull
    public PrepareRenderer getRenderer(int index){
        return renderers.get(index);
    }

    /**
     Returns the number of the renderers in the specified stage.

     @param geometry specifies the list of Renderers

     @return the number of the renderers in the specified stage
     */
    public int getRendererCount(){
        return renderers.size();
    }

    /**
     Removes the specified stage's indexth renderer.

     @param geometry specifies the list of Renderers
     @param index    index

     @throws NullPointerException stage can't be null
     */
    public void removeRenderer(int index){//FIXME why public??
        renderers.remove(index).removeFromRenderingPipeline();
    }

    public void release(){
        while(!renderers.isEmpty()){
            removeRenderer(0);
        }
    }

}
