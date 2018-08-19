package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.GL30;
import wobani.toolbox.Utility;
import wobani.toolbox.annotation.NotNull;
import wobani.toolbox.annotation.ReadOnly;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Buffer Objects which you can bind to a binding point.
 */
public abstract class IndexBindableBufferObject extends BufferObject {

    /**
     * The Buffer Object's binding points.
     */
    private final Set<Integer> bindingPoints = new HashSet<>();

    /**
     * Initializes a new IndexBindableBufferObject to the given target.
     *
     * @param target Buffer Object's target
     */
    public IndexBindableBufferObject(int target) {
        super(target);
    }

    /**
     * Checks whether the binding is possible.
     *
     * @param bindingPoint binding point
     * @throws IllegalArgumentException if binding point is lower than 0 or higher than the highest valid binding point
     */
    private void bindingGeneral(int bindingPoint) {
        checkRelease();
        if (bindingPoint < 0 || bindingPoint > getHighestValidBindingPoint()) {
            throw new IllegalArgumentException("Binding point can't be lower than 0 or higher than the highest valid binding point");
        }
    }

    /**
     * Binds the Buffer Object to the given binding point.
     *
     * @param bindingPoint binding point
     */
    public void bindToBindingPoint(int bindingPoint) {
        bindingGeneral(bindingPoint);
        this.bindingPoints.add(bindingPoint);
        GL30.glBindBufferBase(getTarget(), bindingPoint, getId());
    }

    /**
     * Unbinds the Buffer Object from the given binding point.
     *
     * @param bindingPoint binding point
     * @throws IllegalArgumentException if the Buffer Object isn't bound to the given binding point
     */
    public void unbindFromBindingPoint(int bindingPoint) {
        bindingGeneral(bindingPoint);
        if (!bindingPoints.contains(bindingPoint)) {
            throw new IllegalArgumentException("The Buffer Object not bound to the given binding point");
        }
        bindingPoints.remove(bindingPoint);
        GL30.glBindBufferBase(getTarget(), bindingPoint, 0);

    }

    /**
     * Returns the Buffer Object's binding points.
     *
     * @return the Buffer Object's binding points
     */
    @NotNull
    @ReadOnly
    public Collection<Integer> getBindingPoint() {
        return Collections.unmodifiableCollection(bindingPoints);
    }

    /**
     * Returns the highest valid binding point.
     *
     * @return the highest valid binding point
     */
    protected abstract int getHighestValidBindingPoint();

    @Override
    public String toString() {
        return super.toString() + "\n" +
                IndexBindableBufferObject.class.getSimpleName() + "(" +
                "bindingPoints: " + Utility.toString(bindingPoints) + ")";
    }
}
