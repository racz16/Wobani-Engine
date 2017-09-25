package components.camera;

import core.*;
import org.joml.*;
import toolbox.*;
import window.*;

/**
 * This class extends the DefaultCameraComponent and allows the user to control
 * the camera movement and rotation by the keyboard.
 *
 * @see GameObject
 */
public class FreeCameraComponent extends CameraComponent {

    /**
     * Move speed.
     */
    private float moveSpeed = 1;
    /**
     * Rotate speed.
     */
    private float rotateSpeed = 1;
    /**
     * Sprint boost. Note that the final speed is move speed multiplied by this
     * sprint speed. So sprint boost is not the final speed, it's just a
     * multiplying factor.
     */
    private float sprintBoost = 1.5f;

    /**
     * This method allows the user to control camera movement and rotation the
     * following ways: W - move forward, A - move left, S - move backward, D -
     * move right, Q - turn left, E - turn right, SHIFT - boost movement speed.
     *
     */
    @Override
    public void update() {
        if (getGameObject() != null) {
            Vector3f forwardSpeed = getGameObject().getTransform().getForwardVector().mul(moveSpeed * Time.getDeltaTimeFactor());
            Vector3f rightSpeed = getGameObject().getTransform().getRightVector().mul(moveSpeed * Time.getDeltaTimeFactor());
            if (Input.isKeyPressed(Input.Key.KEY_LEFT_SHIFT)) {
                forwardSpeed.mul(sprintBoost);
                rightSpeed.mul(sprintBoost);
            }
            //move
            if (Input.isKeyPressed(Input.Key.KEY_W)) {
                getGameObject().getTransform().move(forwardSpeed);
            }
            if (Input.isKeyPressed(Input.Key.KEY_S)) {
                getGameObject().getTransform().move(forwardSpeed.negate());
            }
            if (Input.isKeyPressed(Input.Key.KEY_D)) {
                getGameObject().getTransform().move(rightSpeed);
            }
            if (Input.isKeyPressed(Input.Key.KEY_A)) {
                getGameObject().getTransform().move(rightSpeed.negate());
            }
            //rotate
            if (Input.isKeyPressed(Input.Key.KEY_Q)) {
                getGameObject().getTransform().rotate(new Vector3f(0, rotateSpeed * Time.getDeltaTimeFactor(), 0));
            }
            if (Input.isKeyPressed(Input.Key.KEY_E)) {
                getGameObject().getTransform().rotate(new Vector3f(0, -rotateSpeed * Time.getDeltaTimeFactor(), 0));
            }
        }
    }

    /**
     * Returns move speed.
     *
     * @return move speed
     */
    public float getMoveSpeed() {
        return moveSpeed;
    }

    /**
     * Sets the move speed to the given value.
     *
     * @param moveSpeed move speed
     */
    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    /**
     * Returns rotate speed.
     *
     * @return rotate speed
     */
    public float getRotateSpeed() {
        return rotateSpeed;
    }

    /**
     * Sets the rotate speed to the given value.
     *
     * @param rotateSpeed rotate speed
     */
    public void setRotateSpeed(float rotateSpeed) {
        this.rotateSpeed = rotateSpeed;
    }

    /**
     * Returns sprint boost. Note that the final speed is move speed multiplied
     * by this sprint speed. So sprint boost is not the final speed, it's just a
     * multiplying factor.
     *
     * @return sprint boost
     */
    public float getSprintBoost() {
        return sprintBoost;
    }

    /**
     * Sets the sprint boost to the given value. Note that the final speed is
     * move speed multiplied by this sprint speed. So sprint boost is not the
     * final speed, it's just a multiplying factor.
     *
     * @param sprintBoost sprint boost
     */
    public void setSprintBoost(float sprintBoost) {
        this.sprintBoost = sprintBoost;
    }

    @Override
    public int hashCode() {
        int hash = 3 + super.hashCode();
        hash = 19 * hash + Float.floatToIntBits(this.moveSpeed);
        hash = 19 * hash + Float.floatToIntBits(this.rotateSpeed);
        hash = 19 * hash + Float.floatToIntBits(this.sprintBoost);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final FreeCameraComponent other = (FreeCameraComponent) obj;
        if (Float.floatToIntBits(this.moveSpeed) != Float.floatToIntBits(other.moveSpeed)) {
            return false;
        }
        if (Float.floatToIntBits(this.rotateSpeed) != Float.floatToIntBits(other.rotateSpeed)) {
            return false;
        }
        if (Float.floatToIntBits(this.sprintBoost) != Float.floatToIntBits(other.sprintBoost)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nFreeCameraComponent{" + "moveSpeed=" + moveSpeed
                + ", rotateSpeed=" + rotateSpeed + ", sprintBoost=" + sprintBoost + '}';
    }

}
