package wobani.window;

import java.nio.*;
import java.util.*;
import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickButtons;
import wobani.toolbox.annotation.*;

/**
 * Represents a joystick.
 */
public class Joystick {

    /**
     * A joystick axe.
     */
    public enum JoystickAxe {
	/**
	 * Left stick's horizontal direction.
	 */
	LEFT_STICK_HORIZONTAL(0),
	/**
	 * Left stick's vertical direction.
	 */
	LEFT_STICK_VERTICAL(1),
	/**
	 * Trigger buttons (LT and RT).
	 */
	TRIGGERS(2),
	/**
	 * Right stick's horizontal direction.
	 */
	RIGHT_STICK_HORIZONTAL(3),
	/**
	 * Right stick's vertical direction.
	 */
	RIGHT_STICK_VERTICAL(4);

	/**
	 * Axe's default index.
	 */
	private final int index;

	/**
	 * Initializes a new JoystickAxe to the given value.
	 *
	 * @param index axe's default index
	 */
	private JoystickAxe(int index) {
	    this.index = index;
	}

	/**
	 * Returns the axe's default index.
	 *
	 * @return the axe's default index
	 */
	public int getIndex() {
	    return index;
	}
    }

    /**
     * A joystick button.
     */
    public enum JoystickButton {
	/**
	 * The down button (Xbox: A, Playstation: X).
	 */
	DOWN(0),
	/**
	 * The right button (Xbox: B, Playstation: circle).
	 */
	RIGHT(1),
	/**
	 * The left button (Xbox: X, Playstation: square).
	 */
	LEFT(2),
	/**
	 * The up button (Xbox: Y, Playstation: triangle).
	 */
	UP(3),
	/**
	 * Left shoulder button (LB).
	 */
	LEFT_SHOULDER(4),
	/**
	 * Right shoulder button (RB).
	 */
	RIGHT_SHOULDER(5),
	/**
	 * Back button.
	 */
	BACK(6),
	/**
	 * Start button.
	 */
	START(7),
	/**
	 * Left stick's button.
	 */
	LEFT_STICK(8),
	/**
	 * Right stick's button.
	 */
	RIGHT_STICK(9),
	/**
	 * Up direction on the D-pad.
	 */
	UP_D_PAD(10),
	/**
	 * Right direction on the D-pad.
	 */
	RIGHT_D_PAD(11),
	/**
	 * Down direction on the D-pad.
	 */
	DOWN_D_PAD(12),
	/**
	 * Left direction on the D-pad.
	 */
	LEFT_D_PAD(13);

	/**
	 * Button's default index.
	 */
	private final int index;

	/**
	 * Initializes a new JoystickButton to the given value.
	 *
	 * @param index button's default index
	 */
	private JoystickButton(int index) {
	    this.index = index;
	}

	/**
	 * Returns the button's default index.
	 *
	 * @return the button's default index
	 */
	public int getIndex() {
	    return index;
	}
    }

    //axes----------------------------------------------------------------------
    /**
     * Joystick's axe configuration.
     */
    private final JoystickAxe[] axes = {
	JoystickAxe.LEFT_STICK_HORIZONTAL,
	JoystickAxe.LEFT_STICK_VERTICAL,
	JoystickAxe.TRIGGERS,
	JoystickAxe.RIGHT_STICK_HORIZONTAL,
	JoystickAxe.RIGHT_STICK_VERTICAL};
    //buttons-------------------------------------------------------------------
    /**
     * Joystick's button configuration.
     */
    private final JoystickButton[] buttons = {
	JoystickButton.DOWN,
	JoystickButton.RIGHT,
	JoystickButton.LEFT,
	JoystickButton.UP,
	JoystickButton.LEFT_SHOULDER,
	JoystickButton.RIGHT_SHOULDER,
	JoystickButton.BACK,
	JoystickButton.START,
	JoystickButton.LEFT_STICK,
	JoystickButton.RIGHT_STICK,
	JoystickButton.UP_D_PAD,
	JoystickButton.RIGHT_D_PAD,
	JoystickButton.DOWN_D_PAD,
	JoystickButton.LEFT_D_PAD};

    /**
     * Joystick's name.
     */
    private String name;
    /**
     * Joystick's slot.
     */
    private int slot;

    /**
     * Initializes a new Joystick to the givan values.
     *
     * @param name joystick's name
     * @param slot joystick's slot
     *
     * @throws NullPointerException     name can't be null
     * @throws IllegalArgumentException slot must be in the (0;15) interval
     */
    Joystick(@NotNull String name, int slot) {
	if (name == null) {
	    throw new NullPointerException();
	}
	if (slot < GLFW.GLFW_JOYSTICK_1 || slot > GLFW.GLFW_JOYSTICK_LAST) {
	    throw new IllegalArgumentException("Slot must be in the (0;15) interval");
	}
	initializeWithoutInspection(name, slot);
    }

    /**
     * Initializes the Joystick to the given values.
     *
     * @param name joystick's name
     * @param slot joystick's slot
     */
    private void initializeWithoutInspection(@NotNull String name, int slot) {
	this.name = name;
	this.slot = slot;
    }

    /**
     * Returns the joystick's name.
     *
     * @return the joystick's name
     */
    @NotNull
    public String getName() {
	return name;
    }

    /**
     * Returns the joystick's slot.
     *
     * @return the joystick's slot
     */
    public int getSlot() {
	return slot;
    }

    /**
     * Swaps the two specified axes. It may be helpful if the default
     * configuration already swapped these axes.
     *
     * @param first  first axe
     * @param second second axe
     */
    public void swapAxes(@NotNull JoystickAxe first, @NotNull JoystickAxe second) {
	if (first == second) {
	    return;
	}
	int firstIndex = axes[first.getIndex()].getIndex();
	int secondIndex = axes[second.getIndex()].getIndex();
	swapAxes(first.getIndex(), firstIndex, second.getIndex(), secondIndex);
    }

    /**
     * Swaps the two specified axes. It may be helpful if the default
     * configuration already swapped these axes.
     *
     * @param firstIndex1  original first axe
     * @param firstIndex2  configured first axe
     * @param secondIndex1 original second axe
     * @param secondIndex2 configured second axe
     */
    private void swapAxes(int firstIndex1, int firstIndex2, int secondIndex1, int secondIndex2) {
	JoystickAxe f = axes[firstIndex1];
	JoystickAxe s = axes[secondIndex1];
	axes[firstIndex2] = s;
	axes[secondIndex2] = f;
    }

    /**
     * Returns the specified axe's position in (-1;1).
     *
     * @param axe one of the joystick's axes
     *
     * @return the specified axe's position
     */
    public float getAxePosition(@NotNull JoystickAxe axe) {
	JoystickAxe real = axes[axe.getIndex()];
	FloatBuffer fb = glfwGetJoystickAxes(slot);
	return fb.get(real.getIndex());
    }

    /**
     * Swaps the two specified buttons. It may be helpful if the default
     * configuration already swapped these buttons.
     *
     * @param first  first button
     * @param second second button
     */
    public void swapButtons(@NotNull JoystickButton first, @NotNull JoystickButton second) {
	if (first == second) {
	    return;
	}
	int firstIndex = buttons[first.getIndex()].getIndex();
	int secondIndex = buttons[second.getIndex()].getIndex();
	swapButtons(first.getIndex(), firstIndex, second.getIndex(), secondIndex);
    }

    /**
     * Swaps the two specified buttons. It may be helpful if the default
     * configuration already swapped these buttons.
     *
     * @param firstIndex1  original first button
     * @param firstIndex2  configured first button
     * @param secondIndex1 original second button
     * @param secondIndex2 configured second button
     */
    private void swapButtons(int firstIndex1, int firstIndex2, int secondIndex1, int secondIndex2) {
	JoystickButton f = buttons[firstIndex1];
	JoystickButton s = buttons[secondIndex1];
	buttons[firstIndex2] = s;
	buttons[secondIndex2] = f;
    }

    /**
     * Determines whether the specified button is pressed.
     *
     * @param button one of the joystick's buttons
     *
     * @return true if the specified button is pressed, false otherwise
     */
    public boolean isButtonDown(@NotNull JoystickButton button) {
	JoystickButton real = buttons[button.getIndex()];
	ByteBuffer bb = glfwGetJoystickButtons(slot);
	return bb.get(real.getIndex()) != 0;
    }

    /**
     * Determines whether this joystick is still connected to the computer.
     *
     * @return true if it is still connected, false otherwise
     */
    public boolean isUsable() {
	return Input.getJoystick(slot) == this;
    }

    @Override
    public int hashCode() {
	int hash = 3;
	hash = 23 * hash + Arrays.deepHashCode(this.axes);
	hash = 23 * hash + Arrays.deepHashCode(this.buttons);
	hash = 23 * hash + Objects.hashCode(this.name);
	hash = 23 * hash + this.slot;
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final Joystick other = (Joystick) obj;
	if (this.slot != other.slot) {
	    return false;
	}
	if (!Objects.equals(this.name, other.name)) {
	    return false;
	}
	if (!Arrays.deepEquals(this.axes, other.axes)) {
	    return false;
	}
	if (!Arrays.deepEquals(this.buttons, other.buttons)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	StringBuilder res = new StringBuilder()
		.append(Joystick.class.getSimpleName()).append("(")
		.append(" name: ").append(name)
		.append(", axes: ").append(Arrays.toString(axes))
		.append(", buttons: ").append(Arrays.toString(buttons))
		.append(", slot: ").append(slot)
		.append(")");
	return res.toString();
    }

}
