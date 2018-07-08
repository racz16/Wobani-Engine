package wobani.toolbox;

import wobani.toolbox.annotation.*;

/**
 Interface for handling OpenGL events.
 */
public interface OpenGlDebugEventHandler{

    /**
     This method is called when an OpenGL event occurred.

     @param event OpenGL event
     */
    public void openGlDebugCallback(@NotNull OpenGlEvent event);
}
