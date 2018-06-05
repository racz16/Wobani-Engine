package wobani.toolbox;

import wobani.toolbox.annotation.*;

/**
 * Interface for handling OpenGL errors.
 */
public interface OpenGlErrorEventHandler {

    /**
     * This method is called when an OpenGL error occurred.
     *
     * @param error OpenGL error
     */
    public void openGlErrorCallback(@NotNull OpenGlError error);
}
