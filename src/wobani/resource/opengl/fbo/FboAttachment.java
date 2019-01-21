package wobani.resource.opengl.fbo;

import org.joml.*;
import wobani.resource.opengl.texture.*;

public interface FboAttachment{

    Vector2i getSize();

    Texture.TextureInternalFormat getInternalFormat();

    boolean isAllocated();

    boolean isMultisampled();

    int getSampleCount();

    boolean isUsable();

}
