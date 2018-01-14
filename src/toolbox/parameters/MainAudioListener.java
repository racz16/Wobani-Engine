package toolbox.parameters;

import components.audio.*;
import toolbox.annotations.*;

public class MainAudioListener extends UniqueParameter<AudioListenerComponent> {

    public MainAudioListener(@NotNull AudioListenerComponent value) {
        super(value);
    }

    @Override
    public AudioListenerComponent getValue() {
        AudioListenerComponent ret = super.getValue();
        if (ret.getGameObject() == null) {
            throw new RuntimeException("main audio listener detached");
        }
        return ret;
    }

    @Override
    protected void addedToParameters(UniqueParameter<AudioListenerComponent> removed) {
        if (getValue().getGameObject() == null) {
            throw new NullPointerException();
        }
    }

}
