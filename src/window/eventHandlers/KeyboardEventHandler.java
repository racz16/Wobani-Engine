package window.eventHandlers;

import window.Input.Key;
import window.Input.KeyStatus;

/**
 * Interface for handling keyboard events.
 */
public interface KeyboardEventHandler {

    /**
     * This method is called when a key is pressed, repeated or released. It
     * deals with physical keys, with key tokens named after their use on the
     * standard US keyboard layout. If you want to input text, use the other two
     * methods.
     *
     * @param key            the keyboard key that was pressed or released
     * @param scancode       the system-specific scancode of the key
     * @param action         determines whether the key pressed, repeated or
     *                       released
     * @param shiftPressed   determines whether the shift is pressed
     * @param controlPressed determines whether the control is pressed
     * @param altPressed     determines whether the alt is pressed
     * @param superPressed   determines whether the super is pressed
     */
    public void keyCallback(Key key, int scancode, KeyStatus action, boolean shiftPressed, boolean controlPressed, boolean altPressed, boolean superPressed);

    /**
     * This method is called when a Unicode character is input. As it deals with
     * characters, it is keyboard layout dependent, whereas keyCallback is not.
     * Characters do not map 1:1 to physical keys, as a key may produce zero,
     * one or more characters. If you want to know whether a specific physical
     * key was pressed or released, see the keyCallback instead. The character
     * callback behaves as system text input normally does and will not be
     * called if modifier keys are held down that would prevent normal text
     * input on that platform, for example a Super (Command) key on macOS or Alt
     * key on Windows. There is SetCharModsCallback that receives these events.
     *
     * @param codepoint the Unicode code point of the character (you may cast it
     *                  to char)
     */
    public void charCallback(int codepoint);

    /**
     * This method is called when a Unicode character is input regardless of
     * what modifier keys are used. The character with modifiers callback is
     * intended for implementing custom Unicode character input. For regular
     * Unicode text input, see charCallback. Like the character callback, the
     * character with modifiers callback deals with characters and is keyboard
     * layout dependent. Characters do not map 1:1 to physical keys, as a key
     * may produce zero, one or more characters. If you want to know whether a
     * specific physical key was pressed or released, see keyCallback instead.
     *
     * @param codepoint      the Unicode code point of the character (you may
     *                       cast it to char)
     * @param shiftPressed   determines whether the shift is pressed
     * @param controlPressed determines whether the control is pressed
     * @param altPressed     determines whether the alt is pressed
     * @param superPressed   determines whether the super is pressed
     */
    public void charModsCallback(int codepoint, boolean shiftPressed, boolean controlPressed, boolean altPressed, boolean superPressed);
}
