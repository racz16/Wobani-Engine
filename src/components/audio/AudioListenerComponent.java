package components.audio;

import core.*;
import java.util.*;
import org.joml.*;
import org.lwjgl.openal.*;

/**
 * The scene's audio listener and it's position, orientation and velocity
 * affects how we hear audio sources' sounds.
 *
 * @see Scene#getAudioListener()
 * @see Scene#setAudioListener(AudioListenerComponent audioListener)
 */
public class AudioListenerComponent extends Component {

    /**
     * Listener's position in the last frame.
     */
    private final Vector3f lastPosition = new Vector3f();

    @Override
    public void update() {
        if (getGameObject() != null) {
            Vector3f currentPosition = new Vector3f(getGameObject().getTransform().getAbsolutePosition());
            if (Scene.getAudioListener() == this) {
                Vector3f velocity = new Vector3f();
                currentPosition.sub(lastPosition, velocity);
                AL10.alListener3f(AL10.AL_POSITION, currentPosition.x, currentPosition.y, currentPosition.z);
                AL10.alListener3f(AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
                Vector3f forward = getGameObject().getTransform().getForwardVector();
                Vector3f up = getGameObject().getTransform().getUpVector();
                float[] orientation = {forward.x, forward.y, forward.z, up.x, up.y, up.z};
                AL10.alListenerfv(AL10.AL_ORIENTATION, orientation);
            }
            lastPosition.set(currentPosition);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.lastPosition);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final AudioListenerComponent other = (AudioListenerComponent) obj;
        if (!Objects.equals(this.lastPosition, other.lastPosition)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nAudioListenerComponent{"
                + "lastPosition=" + lastPosition + '}';
    }

}
