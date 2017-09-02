package window.eventHandlers;

/**
 * Interface for handling joystick events.
 */
public interface JoystickEventHandler {

    /**
     * This method is called when a joystick is connected to or disconnected
     * from the system.
     *
     * @param id the joystick that was connected or disconnected
     * @param connected true if the joystick is connected, false if disconnected
     */
    public void joystickCallback(int id, boolean connected);
}
