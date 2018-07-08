package wobani.window.eventhandler;

import org.joml.*;

/**
 * Interface for handling mouse events.
 */
public interface MouseEventHandler {

    /**
     * This method is called when the cursor is moved. The callback is provided
     * with the position, in screen coordinates, relative to the upper-left
     * corner of the client area of the window.
     *
     * @param position the new cursor position, relative to the left edge of the
     *                 client area
     */
    public void positionCallback(Vector2f position);

    /**
     * This method is called when the cursor enters or leaves the client area of
     * the window.
     *
     * @param entered true if the cursor entered the window's client area, false
     *                if it left it
     */
    public void enterCallback(boolean entered);

    /**
     * This method is called when a mouse button is pressed or released.
     *
     * @param button         the mouse button that was pressed or released in
     *                       (0;8) (where 0 is the left mouse button, 1 is the
     *                       right and 2 is the middle)
     * @param pressed        true if the specified button pressed, false if
     *                       released
     * @param shiftPressed   determines whether the shift is pressed
     * @param controlPressed determines whether the control is pressed
     * @param altPressed     determines whether the alt is pressed
     * @param superPressed   determines whether the super is pressed
     */
    public void buttonCallback(int button, boolean pressed, boolean shiftPressed, boolean controlPressed, boolean altPressed, boolean superPressed);

    /**
     * This method is called when a scrolling device is used. The scroll
     * callback receives all scrolling input, like that from a mouse wheel or a
     * touchpad scrolling area.
     *
     * @param offset the scroll offset along the x-axis and the y-axis
     */
    public void scrollCallback(Vector2f offset);
}
