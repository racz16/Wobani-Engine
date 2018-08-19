package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.GL43;
import wobani.toolbox.OpenGlConstants;
import wobani.toolbox.annotation.NotNull;

/**
 * Object oriented wrapper class above the native Shader Storage Buffer Object.
 */
public class Ssbo extends IndexBindableBufferObject {

    /**
     * Initializes a new SSBO.
     */
    public Ssbo() {
        super(GL43.GL_SHADER_STORAGE_BUFFER);
    }

    /**
     * Initializes a new SSBO to the given value.
     *
     * @param label label
     */
    public Ssbo(@NotNull String label) {
        this();
        setLabel(label);
    }

    @NotNull
    @Override
    protected String getTypeName() {
        return "SSBO";
    }

    @Override
    protected int getMaxDataSize() {
        return OpenGlConstants.MAX_SHADER_STORAGE_BLOCK_SIZE;
    }

    @Override
    protected int getHighestValidBindingPoint() {
        return getAvailableBindingPointCount() - 1;
    }

    /**
     * Returns the number of the valid binding points to the SSBOs.
     *
     * @return the number of the valid binding points to the SSBOs
     */
    public static int getAvailableBindingPointCount() {
        return OpenGlConstants.MAX_SHADER_STORAGE_BUFFER_BINDINGS;
    }

    @Override
    public String toString() {
        return super.toString() + "\n" +
                Ssbo.class.getSimpleName() + "(" + ")";
    }
}
