package wobani.resources.buffers;

import org.lwjgl.opengl.*;
import wobani.resources.*;

/**
 * Object oriented wrapper class above the native Shader Storage Buffer Object.
 */
public class Ssbo extends BufferObjectBase {

    /**
     * Initializes a new SSBO.
     */
    public Ssbo() {
	super(GL43.GL_SHADER_STORAGE_BUFFER);
    }

    @Override
    protected void addToResourceManager() {
	ResourceManager.addSsbo(this);
    }

}
