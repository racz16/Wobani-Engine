package toolbox;

import core.*;
import toolbox.annotations.*;

public class AttachmentException extends RuntimeException {

    private final GameObject gameObject;
    private final Object attachment;

    public AttachmentException(@Nullable GameObject gameObject, @Nullable Object attachment) {
        this.gameObject = gameObject;
        this.attachment = attachment;
    }

    @Nullable
    public GameObject getGameObject() {
        return gameObject;
    }

    @Nullable
    public Object getAttachment() {
        return attachment;
    }

}
