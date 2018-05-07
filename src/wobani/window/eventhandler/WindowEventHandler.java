package wobani.window.eventhandler;

import org.joml.*;

/**
 * Interface for handling window events.
 */
public interface WindowEventHandler {

    /**
     * This method is called when the user attempts to close the window, for
     * example by clicking the close widget in the title bar.
     */
    public void closeCallback();

    /**
     * This method is called when the window is resized. The callback is
     * provided with the size, in screen coordinates, of the client area of the
     * window.
     *
     * @param newSize new size of the window's client area
     */
    public void sizeCallback(Vector2i newSize);

    /**
     * This method is called when the framebuffer of the window is resized.
     *
     * @param newSize new size of the window's framebuffer
     */
    public void frameBufferSizeCallback(Vector2i newSize);

    /**
     * This method is called when the window is moved. The callback is provided
     * with the screen position of the upper-left corner of the client area of
     * the window.
     *
     * @param newPosition new position of the window
     */
    public void positionCallback(Vector2i newPosition);

    /**
     * This method is called when the window is minimized or restored from
     * minization.
     *
     * @param minimized true if the window minimized, false if restored from
     *                  minimization
     */
    public void minimizationCallback(boolean minimized);

    /**
     * This method is called when the window gains or loses input focus.
     *
     * @param focused true if the window gains the focus, false if it loses the
     *                focus
     */
    public void focusCallback(boolean focused);
}
