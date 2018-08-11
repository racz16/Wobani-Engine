package wobani.component.audio;

import org.joml.*;
import wobani.core.*;
import wobani.resource.audio.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.parameter.*;

import java.util.*;

/**
 Audio sources can emit sounds. The audio sources' position, orientation and velocity affect how we hear these sounds. If
 you want to play or pause the sound, change the attenuation or substitute the sound effects, you should use the model
 behind it.

 @see #getSource() */
public class AudioSourceComponent extends Component{

    /**
     The audio source's position in the last frame.
     */
    private final Vector3f lastPosition = new Vector3f();
    /**
     The OpenAL audio source.
     */
    private AudioSource source;
    /**
     Determines whether the audio source is directional.
     */
    private boolean directionalSource = true;

    /**
     Initializes a new AudioSourceComponent to the given value.

     @param source audio source
     */
    public AudioSourceComponent(@NotNull AudioSource source){
        setSource(source);
    }

    /**
     Returns the OpenAL audio source. The Component is mainly just an adapter and this return value is the model. This
     means that if you want to play or pause the sound, change the attenuation or substitute the sound effects, you
     should use this return value.

     @return OpenAL audio source
     */
    @NotNull
    public AudioSource getSource(){
        return source;
    }

    /**
     Sets the OpenAL source to the given value.

     @param source OpenAL audio source

     @throws NullPointerException source can't be null
     */
    private void setSource(@NotNull AudioSource source){
        if(source == null){
            throw new NullPointerException();
        }
        this.source = source;
    }

    /**
     Determines whether the audio source is directional. If it returns true it emits sound along the GameObject's forward
     vector. If it returns true it emits sound towards the audio listener.

     @return true if it's a directional audio source, false otherwise
     */
    public boolean isDirectionalSource(){
        return directionalSource;
    }

    /**
     Sets whether or not the audio source should be a directional audio source. If it's a directional source, it emits
     sound along the GameObject's forward vector. If it's not, it emits sound towards the audio listener.

     @param directional true if it should be a directional audio source, false otherwise
     */
    public void setDirectionalSource(boolean directional){
        directionalSource = directional;
    }

    @Override
    public void update(){
        if(!Utility.isUsable(source)){
            //TODO: reload
        }
    }

    @Override
    public void invalidate(){
        super.invalidate();
        if(getGameObject() != null && Utility.isUsable(source)){
            refreshSource();
        }
    }

    /**
     Refreshes the audio source.
     */
    private void refreshSource(){
        refreshPositionAndVelocity();
        if(isDirectionalSource()){
            refreshDirectionalSource();
        }else{
            refreshNonDirectionalSource();
        }
        refreshLastPosition();
    }

    /**
     Refreshes the audio source's position and velocity.
     */
    private void refreshPositionAndVelocity(){
        Vector3f currentPosition = new Vector3f(getGameObject().getTransform().getAbsolutePosition());
        Vector3f velocity = new Vector3f();
        currentPosition.sub(lastPosition, velocity);
        source.setPosition(currentPosition);
        source.setVelocity(velocity);
    }

    /**
     Refreshes the audio source if it's a directional source.
     */
    private void refreshDirectionalSource(){
        Vector3f forward = getGameObject().getTransform().getForwardVector();
        source.setDirection(forward);
    }

    /**
     Refreshes the audio source if it's not a directional source.
     */
    private void refreshNonDirectionalSource(){
        Parameter<AudioListenerComponent> mainAudio = Scene.getParameters().get(Scene.MAIN_AUDIO_LISTENER);
        if(mainAudio == null){
            refreshSourceIfThereIsNoListener();
        }else{
            refreshSourceIfThereIsAListener();
        }
    }

    /**
     Refreshes the audio source if it's not a directional source and there is an audio listener in the world.
     */
    private void refreshSourceIfThereIsAListener(){
        Vector3f currentPosition = new Vector3f(getGameObject().getTransform().getAbsolutePosition());
        Parameter<AudioListenerComponent> mainAudio = Scene.getParameters().get(Scene.MAIN_AUDIO_LISTENER);
        Vector3f direction = new Vector3f();
        mainAudio.getValue().getGameObject().getTransform().getAbsolutePosition().sub(currentPosition, direction);
        source.setDirection(direction.normalize());
    }

    /**
     Refreshes the audio source if it's not a directional source and there isn't an audio listener in the world.
     */
    private void refreshSourceIfThereIsNoListener(){
        Vector3f forward = getGameObject().getTransform().getForwardVector();
        source.setDirection(forward);
    }

    /**
     Refreshes the audio source's last position.
     */
    private void refreshLastPosition(){
        Vector3f currentPosition = new Vector3f(getGameObject().getTransform().getAbsolutePosition());
        lastPosition.set(currentPosition);
    }

    @Internal
    @Override
    protected void detachFromGameObject(){
        getGameObject().getTransform().removeInvalidatable(this);
        super.detachFromGameObject();
        invalidate();
        source.stop();
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
        hash = 17 * hash + Objects.hashCode(this.source);
        hash = 17 * hash + Objects.hashCode(this.lastPosition);
        hash = 17 * hash + (this.directionalSource ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(!super.equals(obj)){
            return false;
        }
        final AudioSourceComponent other = (AudioSourceComponent) obj;
        if(this.directionalSource != other.directionalSource){
            return false;
        }
        if(!Objects.equals(this.source, other.source)){
            return false;
        }
        if(!Objects.equals(this.lastPosition, other.lastPosition)){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(super.toString()).append("\n")
                .append(AudioSourceComponent.class.getSimpleName()).append("(").append(" source: ").append(source)
                .append(")");
        return res.toString();
    }

}
