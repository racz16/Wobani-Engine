package wobani.window;

import java.util.*;
import org.joml.*;
import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;
import wobani.window.eventhandler.*;

/**
 * For handling mouse, keyboard or joystick input.
 */
//TODO time, clipboard, drag'n drop input
public class Input {

    //
    //mouse---------------------------------------------------------------------
    //
    /**
     * List of the registered mouse event handlers.
     */
    private final static List<MouseEventHandler> mouseEventHandlers = new ArrayList<>();
    /**
     * Cursor pos callback.
     */
    private static GLFWCursorPosCallback cursorPosCallback;
    /**
     * Cursor enter callback.
     */
    private static GLFWCursorEnterCallback cursorEnterCallback;
    /**
     * Mouse button callback.
     */
    private static GLFWMouseButtonCallback mouseButtonCallback;
    /**
     * Scroll callback.
     */
    private static GLFWScrollCallback scrollCallback;
    //
    //keyboard------------------------------------------------------------------
    //
    /**
     * List of the registered keyboard event handlers.
     */
    private final static List<KeyboardEventHandler> keyboardEventHandlers = new ArrayList<>();
    /**
     * Key callback.
     */
    private static GLFWKeyCallback keyCallback;
    /**
     * Char callback.
     */
    private static GLFWCharCallback charCallback;
    /**
     * Char modifyers callback.
     */
    private static GLFWCharModsCallback charModsCallback;
    //
    //joystick------------------------------------------------------------------
    //
    /**
     * Array of the joysticks.
     */
    private static final Joystick[] joysticks = new Joystick[16];
    /**
     * Number of connected joysticks.
     */
    private static int joystickCount = 0;
    /**
     * Joystick callback.
     */
    private static GLFWJoystickCallback joystickCallback;
    /**
     * List of the registered joystick event handlers.
     */
    private final static List<JoystickEventHandler> joystickEventHandlers = new ArrayList<>();

    /**
     * Key satus.
     */
    public enum KeyStatus {
        /**
         * The key is pressed.
         */
        PRESS(GLFW_PRESS),
        /**
         * The key is pressec continuously.
         */
        REPEAT(GLFW_REPEAT),
        /**
         * The key is released.
         */
        RELEASE(GLFW_RELEASE);

        /**
         * Status's GLFW code.
         */
        private final int code;

        /**
         * Initializes a new KeyStatus to the given value.
         *
         * @param code status's GLFW code
         */
        private KeyStatus(int code) {
            this.code = code;
        }

        /**
         * Returns the status's GLFW code.
         *
         * @return the status's GLFW code
         */
        public int getCode() {
            return code;
        }
    }

    /**
     * Key.
     */
    public enum Key {
        /**
         * If the key is KEY_UNKNOWN, the scancode is used to identify the key,.
         */
        KEY_UNKNOWN(-1),
        /**
         * Space key.
         */
        KEY_SPACE(32),
        /**
         * Apostrophe key.
         */
        KEY_APOSTROPHE(39),
        /**
         * Comma key.
         */
        KEY_COMMA(44),
        /**
         * Minus key.
         */
        KEY_MINUS(45),
        /**
         * Period key.
         */
        KEY_PERIOD(46),
        /**
         * Slash key.
         */
        KEY_SLASH(47),
        /**
         * 0 key.
         */
        KEY_0(48),
        /**
         * 1 key.
         */
        KEY_1(49),
        /**
         * 2 key.
         */
        KEY_2(50),
        /**
         * 3 key.
         */
        KEY_3(51),
        /**
         * 4 key.
         */
        KEY_4(52),
        /**
         * 5 key.
         */
        KEY_5(53),
        /**
         * 6 key.
         */
        KEY_6(54),
        /**
         * 7 key.
         */
        KEY_7(55),
        /**
         * 8 key.
         */
        KEY_8(56),
        /**
         * 9 key.
         */
        KEY_9(57),
        /**
         * Semicolon key.
         */
        KEY_SEMICOLON(59),
        /**
         * Equal key.
         */
        KEY_EQUAL(61),
        /**
         * A key.
         */
        KEY_A(65),
        /**
         * B key.
         */
        KEY_B(66),
        /**
         * C key.
         */
        KEY_C(67),
        /**
         * D key.
         */
        KEY_D(68),
        /**
         * E key.
         */
        KEY_E(69),
        /**
         * F key.
         */
        KEY_F(70),
        /**
         * G key.
         */
        KEY_G(71),
        /**
         * H key.
         */
        KEY_H(72),
        /**
         * I key.
         */
        KEY_I(73),
        /**
         * J key.
         */
        KEY_J(74),
        /**
         * K key.
         */
        KEY_K(75),
        /**
         * L key.
         */
        KEY_L(76),
        /**
         * M key.
         */
        KEY_M(77),
        /**
         * N key.
         */
        KEY_N(78),
        /**
         * O key.
         */
        KEY_O(79),
        /**
         * P key.
         */
        KEY_P(80),
        /**
         * Q key.
         */
        KEY_Q(81),
        /**
         * R key.
         */
        KEY_R(82),
        /**
         * S key.
         */
        KEY_S(83),
        /**
         * T key.
         */
        KEY_T(84),
        /**
         * U key.
         */
        KEY_U(85),
        /**
         * V key.
         */
        KEY_V(86),
        /**
         * W key.
         */
        KEY_W(87),
        /**
         * X key.
         */
        KEY_X(88),
        /**
         * Y key.
         */
        KEY_Y(89),
        /**
         * Z key.
         */
        KEY_Z(90),
        /**
         * Left bracket key.
         */
        KEY_LEFT_BRACKET(91),
        /**
         * Backslash key.
         */
        KEY_BACKSLASH(92),
        /**
         * Right bracket key.
         */
        KEY_RIGHT_BRACKET(93),
        /**
         * Grave accent key.
         */
        KEY_GRAVE_ACCENT(96),
        /**
         * World 1 key.
         */
        KEY_WORLD_1(161),
        /**
         * World 2 key.
         */
        KEY_WORLD_2(162),
        /**
         * Escape key.
         */
        KEY_ESCAPE(256),
        /**
         * Enter key.
         */
        KEY_ENTER(257),
        /**
         * Tab key.
         */
        KEY_TAB(258),
        /**
         * Backspace key.
         */
        KEY_BACKSPACE(259),
        /**
         * Insert key.
         */
        KEY_INSERT(260),
        /**
         * Delete key.
         */
        KEY_DELETE(261),
        /**
         * Right key.
         */
        KEY_RIGHT(262),
        /**
         * Left key.
         */
        KEY_LEFT(263),
        /**
         * Down key.
         */
        KEY_DOWN(264),
        /**
         * Up key.
         */
        KEY_UP(265),
        /**
         * Page up key.
         */
        KEY_PAGE_UP(266),
        /**
         * Page down key.
         */
        KEY_PAGE_DOWN(267),
        /**
         * Home key.
         */
        KEY_HOME(268),
        /**
         * End key.
         */
        KEY_END(269),
        /**
         * Caps lock key.
         */
        KEY_CAPS_LOCK(280),
        /**
         * Scroll lock key.
         */
        KEY_SCROLL_LOCK(281),
        /**
         * Num lock key.
         */
        KEY_NUM_LOCK(282),
        /**
         * Print screen key.
         */
        KEY_PRINT_SCREEN(283),
        /**
         * Pause key.
         */
        KEY_PAUSE(284),
        /**
         * F1 key.
         */
        KEY_F1(290),
        /**
         * F2 key.
         */
        KEY_F2(291),
        /**
         * F3 key.
         */
        KEY_F3(292),
        /**
         * F4 key.
         */
        KEY_F4(293),
        /**
         * F5 key.
         */
        KEY_F5(294),
        /**
         * F6 key.
         */
        KEY_F6(295),
        /**
         * F7 key.
         */
        KEY_F7(296),
        /**
         * F8 key.
         */
        KEY_F8(297),
        /**
         * F9 key.
         */
        KEY_F9(298),
        /**
         * F10 key.
         */
        KEY_F10(299),
        /**
         * F11 key.
         */
        KEY_F11(300),
        /**
         * F12 key.
         */
        KEY_F12(301),
        /**
         * F13 key.
         */
        KEY_F13(302),
        /**
         * F14 key.
         */
        KEY_F14(303),
        /**
         * F15 key.
         */
        KEY_F15(304),
        /**
         * F16 key.
         */
        KEY_F16(305),
        /**
         * F17 key.
         */
        KEY_F17(306),
        /**
         * F18 key.
         */
        KEY_F18(307),
        /**
         * F19 key.
         */
        KEY_F19(308),
        /**
         * F20 key.
         */
        KEY_F20(309),
        /**
         * F21 key.
         */
        KEY_F21(310),
        /**
         * F22 key.
         */
        KEY_F22(311),
        /**
         * F23 key.
         */
        KEY_F23(312),
        /**
         * F24 key.
         */
        KEY_F24(313),
        /**
         * F25 key.
         */
        KEY_F25(314),
        /**
         * Keypad 0 key.
         */
        KEY_KP_0(320),
        /**
         * Keypad 1 key.
         */
        KEY_KP_1(321),
        /**
         * Keypad 2 key.
         */
        KEY_KP_2(322),
        /**
         * Keypad 3 key.
         */
        KEY_KP_3(323),
        /**
         * Keypad 4 key.
         */
        KEY_KP_4(324),
        /**
         * Keypad 5 key.
         */
        KEY_KP_5(325),
        /**
         * Keypad 6 key.
         */
        KEY_KP_6(326),
        /**
         * Keypad 7 key.
         */
        KEY_KP_7(327),
        /**
         * Keypad 8 key.
         */
        KEY_KP_8(328),
        /**
         * Keypad 9 key.
         */
        KEY_KP_9(329),
        /**
         * Keypad decimal key.
         */
        KEY_KP_DECIMAL(330),
        /**
         * Keypad divide key.
         */
        KEY_KP_DIVIDE(331),
        /**
         * Keypad multiply key.
         */
        KEY_KP_MULTIPLY(332),
        /**
         * Keypad subtract key.
         */
        KEY_KP_SUBTRACT(333),
        /**
         * Keypad add key.
         */
        KEY_KP_ADD(334),
        /**
         * Keypad enter key.
         */
        KEY_KP_ENTER(335),
        /**
         * Keypad equal key.
         */
        KEY_KP_EQUAL(336),
        /**
         * Left shift key.
         */
        KEY_LEFT_SHIFT(340),
        /**
         * Left control key.
         */
        KEY_LEFT_CONTROL(341),
        /**
         * Left alt key.
         */
        KEY_LEFT_ALT(342),
        /**
         * Left super key.
         */
        KEY_LEFT_SUPER(343),
        /**
         * Right shift key.
         */
        KEY_RIGHT_SHIFT(344),
        /**
         * Right control key.
         */
        KEY_RIGHT_CONTROL(345),
        /**
         * Right alt key.
         */
        KEY_RIGHT_ALT(346),
        /**
         * Right super key.
         */
        KEY_RIGHT_SUPER(347),
        /**
         * Menu key.
         */
        KEY_MENU(348);

        /**
         * Key's GLFW code.
         */
        private final int code;
        /**
         * LUT for keys.
         */
        private static final Key[] KEYS = new Key[GLFW_KEY_LAST + 1];

        static {
            for (Key key : Key.values()) {
                if (key != Key.KEY_UNKNOWN) {
                    KEYS[key.getCode()] = key;
                }
            }
        }

        /**
         * Initializes a new Key to the given value.
         *
         * @param code key's GLFW code
         */
        private Key(int code) {
            this.code = code;
        }

        /**
         * Returns the key's GLFW code.
         *
         * @return the key's GLFW code
         */
        public int getCode() {
            return code;
        }

        /**
         * Returns the corresponding key to the specified GLFW keycode.
         *
         * @param key GLFW keycode
         *
         * @return key
         */
        @Nullable
        public static Key getKey(int key) {
            if (key == Key.KEY_UNKNOWN.getCode()) {
                return Key.KEY_UNKNOWN;
            } else {
                if (key < 0 || key >= KEYS.length) {
                    return null;
                } else {
                    return KEYS[key];
                }
            }
        }

        /**
         * Returns the layout-specific name of the key (if it's printable). This
         * is typically the character that key would produce without any
         * modifier keys, intended for displaying key bindings to the user. For
         * dead keys, it is typically the diacritic it would add to a character.
         * If the key is KEY_UNKNOWN, the scancode is used to identify the key,
         * otherwise the scancode is ignored. If you specify a non-printable
         * key, or KEY_UNKNOWN and a scancode that maps to a non-printable key,
         * this function returns null.
         *
         * @param scancode the system-specific scancode of the key (only used if
         *                 the key is KEY_UNKNOWN)
         *
         * @return the layout-specific name of the key
         */
        @Nullable
        public String getKeyName(int scancode) {
            return glfwGetKeyName(code, scancode);
        }

    }

    /**
     * The mouse's input mode.
     */
    public enum MouseInputMode {
        /**
         * Makes the cursor visible and behaving normally.
         */
        NORMAL(GLFW_CURSOR_NORMAL),
        /**
         * Makes the cursor invisible when it is over the client area of the
         * window but does not restrict the cursor from leaving.
         */
        HIDDEN(GLFW_CURSOR_HIDDEN),
        /**
         * Hides and grabs the cursor, providing virtual and unlimited cursor
         * movement. This is useful for implementing for example 3D camera
         * controls.
         */
        DISABLED(GLFW_CURSOR_DISABLED);

        /**
         * Input mode's GLFW code.
         */
        private final int code;

        /**
         * Initializes a new MouseInputMode to the given value.
         *
         * @param code input mode's GLFW code
         */
        private MouseInputMode(int code) {
            this.code = code;
        }

        /**
         * Returns the input mode's GLFW code.
         *
         * @return the input mode's GLFW code
         */
        public int getCode() {
            return code;
        }
    }

    /**
     * To can't create Input instance.
     */
    private Input() {
    }

    /**
     * Initializes the Input.
     */
    public static void initialize() {
        for (int i = GLFW_JOYSTICK_1; i <= GLFW_JOYSTICK_LAST; i++) {
            if (glfwJoystickPresent(i)) {
                joysticks[i] = new Joystick(glfwGetJoystickName(i), i);
                joystickCount++;
            }
        }
        addJoystickCallbacks();
    }

    //
    //mouse---------------------------------------------------------------------
    //
    /**
     * Adds the given mouse event handler to the list of mouse event handlers.
     *
     * @param eh mouse event handler
     *
     * @throws NullPointerException parameter can't be null
     */
    public static void addMouseEventHandler(@NotNull MouseEventHandler eh) {
        if (eh == null) {
            throw new NullPointerException();
        }
        addMouseEventHandlerUnsafe(eh);
    }

    /**
     * Adds the given mouse event handler to the list of mouse event handlers.
     *
     * @param eh mouse event handler
     */
    private static void addMouseEventHandlerUnsafe(@NotNull MouseEventHandler eh) {
        if (!Utility.containsReference(mouseEventHandlers, eh)) {
            mouseEventHandlers.add(eh);
        }
        if (mouseEventHandlers.size() == 1) {
            addMouseCallbacks();
        }
    }

    /**
     * Removes the given mouse event handler from the list of mouse event
     * handlers.
     *
     * @param eh mouse event handler
     *
     * @throws NullPointerException parameter can't be null
     */
    public static void removeMouseEventHandler(@NotNull MouseEventHandler eh) {
        if (eh == null) {
            throw new NullPointerException();
        }
        Utility.removeReference(mouseEventHandlers, eh);
        if (mouseEventHandlers.isEmpty()) {
            removeMouseCallbacks();
        }
    }

    /**
     * Removes the specified mouse event handler from the list of mouse event
     * handlers.
     *
     * @param index mouse event handler's index
     */
    public static void removeMouseEventHandler(int index) {
        mouseEventHandlers.remove(index);
        if (mouseEventHandlers.isEmpty()) {
            removeMouseCallbacks();
        }
    }

    /**
     * Removes all the mouse event handlers.
     */
    public static void removeAllMouseEventHandlers() {
        mouseEventHandlers.clear();
        removeMouseCallbacks();
    }

    /**
     * Returns the number of registered mouse event handlers.
     *
     * @return number of registered mouse event handlers
     */
    public static int getMouseEventHandlerCount() {
        return mouseEventHandlers.size();
    }

    /**
     * Creates the mouse callbacks.
     */
    private static void addMouseCallbacks() {
        addCursorPosCallback();
        addCursorEnterCallback();
        addMouseButtonCallback();
        addScrollCallback();
    }

    /**
     * Adds the cursor pos callback.
     */
    private static void addCursorPosCallback() {
        if (cursorPosCallback == null) {
            glfwSetCursorPosCallback(Window.getId(), cursorPosCallback = new GLFWCursorPosCallback() {
                @Override
                public void invoke(long window, double xpos, double ypos) {
                    for (MouseEventHandler eventHandler : mouseEventHandlers) {
                        eventHandler.positionCallback(new Vector2f((float) xpos, (float) ypos));
                    }
                }
            });
        }
    }

    /**
     * Adds the cursor enter callback.
     */
    private static void addCursorEnterCallback() {
        if (cursorEnterCallback == null) {
            glfwSetCursorEnterCallback(Window.getId(), cursorEnterCallback = new GLFWCursorEnterCallback() {
                @Override
                public void invoke(long window, boolean entered) {
                    for (MouseEventHandler eventHandler : mouseEventHandlers) {
                        eventHandler.enterCallback(entered);
                    }
                }
            });
        }
    }

    /**
     * Adds the mouse button callback.
     */
    private static void addMouseButtonCallback() {
        if (mouseButtonCallback == null) {
            glfwSetMouseButtonCallback(Window.getId(), mouseButtonCallback = new GLFWMouseButtonCallback() {
                @Override
                public void invoke(long window, int button, int action, int mods) {
                    for (MouseEventHandler eventHandler : mouseEventHandlers) {
                        eventHandler.buttonCallback(button, action == KeyStatus.PRESS.getCode(), isShiftPressed(mods), isControlPressed(mods), isAltPressed(mods), isSuperPressed(mods));
                    }
                }
            });
        }
    }

    /**
     * Adds the scroll callback.
     */
    private static void addScrollCallback() {
        if (scrollCallback == null) {
            glfwSetScrollCallback(Window.getId(), scrollCallback = new GLFWScrollCallback() {
                @Override
                public void invoke(long window, double xoffset, double yoffset) {
                    for (MouseEventHandler eventHandler : mouseEventHandlers) {
                        eventHandler.scrollCallback(new Vector2f((float) xoffset, (float) yoffset));
                    }
                }
            });
        }
    }

    /**
     * Releases all the mouse callbacks.
     */
    private static void removeMouseCallbacks() {
        releaseCursorPosCallback();
        releaseCursorEnterCallback();
        releaseMouseButtonCallback();
        releaseScrollCallback();
    }

    /**
     * Releases the cursor pos callback.
     */
    private static void releaseCursorPosCallback() {
        if (cursorPosCallback != null) {
            cursorPosCallback.free();
            cursorPosCallback = null;
        }
    }

    /**
     * Releases the cursor enter callback.
     */
    private static void releaseCursorEnterCallback() {
        if (cursorEnterCallback != null) {
            cursorEnterCallback.free();
            cursorEnterCallback = null;
        }
    }

    /**
     * Releases the mouse button callback.
     */
    private static void releaseMouseButtonCallback() {
        if (mouseButtonCallback != null) {
            mouseButtonCallback.free();
            mouseButtonCallback = null;
        }
    }

    /**
     * Releases the scroll callback.
     */
    private static void releaseScrollCallback() {
        if (scrollCallback != null) {
            scrollCallback.free();
            scrollCallback = null;
        }
    }

    /**
     * Returns the position of the cursor, in screen coordinates, relative to
     * the upper-left corner of the client area of the window.
     *
     * @return the mouse's position
     */
    @NotNull
    public static Vector2f getCursorPosition() {
        double[] xpos = new double[1];
        double[] ypos = new double[1];
        glfwGetCursorPos(Window.getId(), xpos, ypos);
        return new Vector2f((float) xpos[0], (float) ypos[0]);
    }

    /**
     * Sets the position, in screen coordinates, of the cursor relative to the
     * upper-left corner of the client area of the window.
     *
     * @param pos cursor's new position
     */
    public static void setCursorPosition(@NotNull Vector2f pos) {
        glfwSetCursorPos(Window.getId(), pos.x, pos.y);
    }

    /**
     * Returns the mouse's input mode.
     *
     * @return the mouse's input mode
     */
    @NotNull
    public static MouseInputMode getMouseInputMode() {
        int code = glfwGetInputMode(Window.getId(), GLFW_CURSOR);
        for (MouseInputMode im : MouseInputMode.values()) {
            if (im.getCode() == code) {
                return im;
            }
        }
        return null;
    }

    /**
     * Sets the mouse's input mode to the given value.
     *
     * @param im input mode
     */
    public static void setMouseInputMode(@NotNull MouseInputMode im) {
        glfwSetInputMode(Window.getId(), GLFW_CURSOR, im.getCode());
    }

    /**
     * Returns the last state reported for the specified mouse button.
     *
     * @param button mouse button in (0;8) (where 0 is the left mouse button, 1
     *               is the right and 2 is the middle)
     *
     * @return true if the buttin pressed, false otherwise
     *
     * @throws IllegalArgumentException button must be in (0;8)
     */
    public static boolean isMouseButtonPressed(int button) {
        if (button < GLFW_MOUSE_BUTTON_1 || button > GLFW_MOUSE_BUTTON_LAST) {
            throw new IllegalArgumentException("Button must be in (0;8)");
        }
        return glfwGetMouseButton(Window.getId(), button) == KeyStatus.PRESS.getCode();
    }

    //
    //keyboard------------------------------------------------------------------
    //
    /**
     * Adds the given keyboard event handler to the list of keyboard event
     * handlers.
     *
     * @param eh keyboard event handler
     *
     * @throws NullPointerException parameter can't be null
     */
    public static void addKeyboardEventHandler(@NotNull KeyboardEventHandler eh) {
        if (eh == null) {
            throw new NullPointerException();
        }
        addKeyboardEventHandlerUnsafe(eh);
    }

    /**
     * Adds the given keyboard event handler to the list of keyboard event
     * handlers.
     *
     * @param eh keyboard event handler
     */
    private static void addKeyboardEventHandlerUnsafe(@NotNull KeyboardEventHandler eh) {
        if (!Utility.containsReference(keyboardEventHandlers, eh)) {
            keyboardEventHandlers.add(eh);
        }
        if (keyboardEventHandlers.size() == 1) {
            addKeyboardCallbacks();
        }
    }

    /**
     * Removes the given keyboard event handler from the list of keyboard event
     * handlers.
     *
     * @param eh keyboard event handler
     *
     * @throws NullPointerException parameter can't be null
     */
    public static void removeKeyboardEventHandler(@NotNull KeyboardEventHandler eh) {
        if (eh == null) {
            throw new NullPointerException();
        }
        Utility.removeReference(keyboardEventHandlers, eh);
        if (keyboardEventHandlers.isEmpty()) {
            removeKeyboardCallbacks();
        }
    }

    /**
     * Removes the specified keyboard event handler from the list of keyboard
     * event handlers.
     *
     * @param index keyboard event handler's index
     */
    public static void removeKeyboardEventHandler(int index) {
        keyboardEventHandlers.remove(index);
        if (keyboardEventHandlers.isEmpty()) {
            removeKeyboardCallbacks();
        }
    }

    /**
     * Removes all the keyboard event handlers.
     */
    public static void removeAllKeyboardEventHandlers() {
        keyboardEventHandlers.clear();
        removeKeyboardCallbacks();
    }

    /**
     * Returns the number of registered keyboard event handlers.
     *
     * @return number of registered keyboard event handlers
     */
    public static int getKeyboardEventHandlerCount() {
        return keyboardEventHandlers.size();
    }

    /**
     * Creates the keyboard callbacks.
     */
    private static void addKeyboardCallbacks() {
        addKeyCallback();
        addCharCallback();
        addCharModsCallback();
    }

    /**
     * Adds the key callback.
     */
    private static void addKeyCallback() {
        if (keyCallback == null) {
            glfwSetKeyCallback(Window.getId(), keyCallback = new GLFWKeyCallback() {
                @Override
                public void invoke(long window, int key, int scancode, int action, int mods) {
                    for (KeyboardEventHandler eventHandler : keyboardEventHandlers) {
                        KeyStatus status = null;
                        for (KeyStatus stat : KeyStatus.values()) {
                            if (stat.getCode() == action) {
                                status = stat;
                                break;
                            }
                        }
                        eventHandler.keyCallback(Key.getKey(key), scancode, status, isShiftPressed(mods), isControlPressed(mods), isAltPressed(mods), isSuperPressed(mods));
                    }
                }
            });
        }
    }

    /**
     * Adds the char callback.
     */
    private static void addCharCallback() {
        if (charCallback == null) {
            glfwSetCharCallback(Window.getId(), charCallback = new GLFWCharCallback() {
                @Override
                public void invoke(long window, int codepoint) {
                    for (KeyboardEventHandler eventHandler : keyboardEventHandlers) {
                        eventHandler.charCallback(codepoint);
                    }
                }
            });
        }
    }

    /**
     * Adds the char mods callback.
     */
    private static void addCharModsCallback() {
        if (charModsCallback == null) {
            glfwSetCharModsCallback(Window.getId(), charModsCallback = new GLFWCharModsCallback() {
                @Override
                public void invoke(long window, int codepoint, int mods) {
                    for (KeyboardEventHandler eventHandler : keyboardEventHandlers) {
                        eventHandler.charModsCallback(codepoint, isShiftPressed(mods), isControlPressed(mods), isAltPressed(mods), isSuperPressed(mods));
                    }
                }
            });
        }
    }

    /**
     * Releases all the keyboard callbacks.
     */
    private static void removeKeyboardCallbacks() {
        releaseKeyCallback();
        releaseCharCallback();
        releaseCharModsCallback();
    }

    /**
     * Releases the key callback.
     */
    private static void releaseKeyCallback() {
        if (keyCallback != null) {
            keyCallback.free();
            keyCallback = null;
        }
    }

    /**
     * Releases the char callback.
     */
    private static void releaseCharCallback() {
        if (charCallback != null) {
            charCallback.free();
            charCallback = null;
        }
    }

    /**
     * Releases the char mods callback.
     */
    private static void releaseCharModsCallback() {
        if (charModsCallback != null) {
            charModsCallback.free();
            charModsCallback = null;
        }
    }

    /**
     * Returns the last state reported for the specified key. It deals with
     * physical keys, with key tokens named after their use on the standard US
     * keyboard layout. If you want to input text, add a KeyboardEventListener
     * and override the charCallback or the charModsCallback method.
     *
     * @param key key
     *
     * @return true if the specified key is pressed, false otherwise
     */
    public static boolean isKeyPressed(@NotNull Key key) {
        return glfwGetKey(Window.getId(), key.getCode()) == KeyStatus.PRESS.getCode();
    }

    /**
     * Returns the platform dependent scancode of the specified key.
     *
     * @param key key
     *
     * @return scancode
     */
    public static int getKeyScancode(@NotNull Key key) {
        return glfwGetKeyScancode(key.getCode());
    }

    /**
     * Determines whether the shift button is pressed.
     *
     * @param mods modifyers
     *
     * @return true if the shift button is pressed, false otherwise
     */
    private static boolean isShiftPressed(int mods) {
        return (mods & 1) == 1;
    }

    /**
     * Determines whether the control button is pressed.
     *
     * @param mods modifyers
     *
     * @return true if the control button is pressed, false otherwise
     */
    private static boolean isControlPressed(int mods) {
        return (mods & 2) == 2;
    }

    /**
     * Determines whether the alt button is pressed.
     *
     * @param mods modifyers
     *
     * @return true if the alt button is pressed, false otherwise
     */
    private static boolean isAltPressed(int mods) {
        return (mods & 4) == 4;
    }

    /**
     * Determines whether the super button is pressed.
     *
     * @param mods modifyers
     *
     * @return true if the super button is pressed, false otherwise
     */
    private static boolean isSuperPressed(int mods) {
        return (mods & 8) == 8;
    }

    //
    //joystick------------------------------------------------------------------
    //
    /**
     * Adds the given joystick event handler to the list of joystick event
     * handlers.
     *
     * @param eh joystick event handler
     *
     * @throws NullPointerException parameter can't be null
     */
    public static void addJoystickEventHandler(@NotNull JoystickEventHandler eh) {
        if (eh == null) {
            throw new NullPointerException();
        }
        if (!Utility.containsReference(joystickEventHandlers, eh)) {
            joystickEventHandlers.add(eh);
        }
    }

    /**
     * Removes the given joystick event handler from the list of joystick event
     * handlers.
     *
     * @param eh joystick event handler
     *
     * @throws NullPointerException parameter can't be null
     */
    public static void removeJoystickEventHandler(@NotNull JoystickEventHandler eh) {
        if (eh == null) {
            throw new NullPointerException();
        }
        Utility.removeReference(joystickEventHandlers, eh);
    }

    /**
     * Removes the specified joystick event handler from the list of joystick
     * event handlers.
     *
     * @param index joystick event handler's index
     */
    public static void removeJoystickEventHandler(int index) {
        joystickEventHandlers.remove(index);
    }

    /**
     * Removes all the joystick event handlers.
     */
    public static void removeAllJoystickEventHandlers() {
        joystickEventHandlers.clear();
    }

    /**
     * Returns the number of registered joystick event handlers.
     *
     * @return number of registered joystick event handlers
     */
    public static int getJoystickEventHandlerCount() {
        return joystickEventHandlers.size();
    }

    /**
     * Creates the joystick callbacks.
     */
    private static void addJoystickCallbacks() {
        if (joystickCallback == null) {
            glfwSetJoystickCallback(joystickCallback = new GLFWJoystickCallback() {
                @Override
                public void invoke(int jid, int event) {
                    joystickCallbackInvoke(jid, event);
                }
            });
        }
    }

    /**
     * Manages the joystick events.
     *
     * @param jid   the joystick that was connected or disconnected
     * @param event connection or disconnection event
     */
    private static void joystickCallbackInvoke(int jid, int event) {
        manageJoystickConnections(jid, event);
        for (JoystickEventHandler eventHandler : joystickEventHandlers) {
            eventHandler.joystickCallback(jid, event == GLFW_CONNECTED);
        }
    }

    /**
     * Manages a joystick's connection or disconnection.
     *
     * @param jid   the joystick that was connected or disconnected
     * @param event connection or disconnection event
     */
    private static void manageJoystickConnections(int jid, int event) {
        if (event == GLFW_CONNECTED) {
            joysticks[jid] = new Joystick(glfwGetJoystickName(jid), jid);
            joystickCount++;
        } else if (event == GLFW_DISCONNECTED) {
            joysticks[jid] = null;
            joystickCount--;
        }
    }

    /**
     * Releases all the joystick callbacks.
     */
    private static void removeJoystickCallbacks() {
        if (joystickCallback != null) {
            joystickCallback.free();
            joystickCallback = null;
        }
    }

    /**
     * Returns the specified joystick. If there is no connected joystick in the
     * given slot, it returns null.
     *
     * @param index index in (0;15)
     *
     * @return the specified joystick
     */
    @Nullable
    public static Joystick getJoystick(int index) {
        return joysticks[index];
    }

    /**
     * Return the number of the connected joysticks.
     *
     * @return the number of the connected joysticks
     */
    public static int getJoystickCount() {
        return joystickCount;
    }

    //
    //misc----------------------------------------------------------------------
    //
    /**
     * Releases the input related resources.
     */
    public static void release() {
        removeMouseCallbacks();
        removeJoystickCallbacks();
        removeKeyboardCallbacks();
    }

}
