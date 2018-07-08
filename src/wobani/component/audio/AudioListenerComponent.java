package wobani.component.audio;

import org.joml.*;
import wobani.core.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.util.*;

/**
 The scene's audio listener and it's position, orientation and velocity affects how we hear audio sources' sounds.
 */
public class AudioListenerComponent extends Component{

    /**
     Listener's position in the last frame.
     */
    private final Vector3f lastPosition = new Vector3f();

    @Override
    public void invalidate(){
        super.invalidate();
        if(getGameObject() != null){
            refreshListener();
        }
    }

    /**
     Refreshes the audio listener.
     */
    private void refreshListener(){
        if(isTheMainAudioListener()){
            refreshListenerPositionAndVelocity();
            refreshListenerOrientation();
        }
        refreshLastPosition();
    }

    /**
     Refreshes the audio listener's position and velocity.
     */
    private void refreshListenerPositionAndVelocity(){
        Vector3f currentPosition = new Vector3f(getGameObject().getTransform().getAbsolutePosition());
        Vector3f velocity = new Vector3f();
        currentPosition.sub(lastPosition, velocity);
        OpenAl.setAudioListenerPosition(currentPosition);
        OpenAl.setAudioListenerVelocity(velocity);
    }

    /**
     Refreshes the audio listener's orientation.
     */
    private void refreshListenerOrientation(){
        Vector3f forward = getGameObject().getTransform().getForwardVector();
        Vector3f up = getGameObject().getTransform().getUpVector();
        OpenAl.setAudioListenerOrientation(forward, up);
    }

    /**
     Refreshes the audio listener's last position.
     */
    private void refreshLastPosition(){
        Vector3f currentPosition = new Vector3f(getGameObject().getTransform().getAbsolutePosition());
        lastPosition.set(currentPosition);
    }

    /**
     Returns true if it's the Scene's main audio listener.

     @return true if it's the Scene's main audio listener, false otherwise
     */
    private boolean isTheMainAudioListener(){
        AudioListenerComponent audioListener = Scene.getParameters().getValue(Scene.MAIN_AUDIO_LISTENER);
        return audioListener == this;
    }

    @Internal
    @Override
    protected void detachFromGameObject(){
        getGameObject().getTransform().removeInvalidatable(this);
        super.detachFromGameObject();
        invalidate();
    }

    @Internal
    @Override
    protected void attachToGameObject(@NotNull GameObject g){
        super.attachToGameObject(g);
        getGameObject().getTransform().addInvalidatable(this);
        invalidate();
    }

    @Override
    public int hashCode(){
        int hash = 7 + super.hashCode();
        hash = 89 * hash + Objects.hashCode(this.lastPosition);
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(!super.equals(obj)){
            return false;
        }
        final AudioListenerComponent other = (AudioListenerComponent) obj;
        if(!Objects.equals(this.lastPosition, other.lastPosition)){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(super.toString()).append("\n")
                .append(AudioListenerComponent.class.getSimpleName()).append("(").append(")");
        return res.toString();
    }

}
