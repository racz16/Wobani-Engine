package wobani.resources.buffers;

import org.lwjgl.opengl.*;
import wobani.resources.*;

/**
 * Object oriented wrapper class above the native Uniform Buffer Object.
 */
public class Ubo extends BufferObjectBase {

    /**
     * Initializes a new UBO.
     */
    public Ubo() {
	super(GL31.GL_UNIFORM_BUFFER);
    }

    @Override
    protected void addToResourceManager() {
	ResourceManager.addUbo(this);
    }

}
