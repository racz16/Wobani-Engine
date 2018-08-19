package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.GL31;
import wobani.toolbox.OpenGlConstants;
import wobani.toolbox.annotation.NotNull;

/**
 * Object oriented wrapper class above the native Uniform Buffer Object.
 */
public class Ubo extends IndexBindableBufferObject {

    /**
     * Initializes a new UBO.
     */
    public Ubo() {
        super(GL31.GL_UNIFORM_BUFFER);
    }

    /**
     * Initializes a new UBO to the given value.
     *
     * @param label label
     */
    public Ubo(@NotNull String label) {
        this();
        setLabel(label);
    }

    @NotNull
    @Override
    protected String getTypeName() {
        return "UBO";
    }

    @Override
    protected int getMaxDataSize() {
        return OpenGlConstants.MAX_UNIFORM_BLOCK_SIZE;
    }

    @Override
    protected int getHighestValidBindingPoint() {
        return getAvailableBindingPointCount() - 1;
    }

    /**
     * Returns the number of the valid binding points to the UBOs.
     *
     * @return the number of the valid binding points to the UBOs
     */
    public static int getAvailableBindingPointCount() {
        return OpenGlConstants.MAX_UNIFORM_BUFFER_BINDINGS;
    }

    @Override
    public String toString() {
        return super.toString() + "\n" +
                Ubo.class.getSimpleName() + "(" + ")";
    }
}
