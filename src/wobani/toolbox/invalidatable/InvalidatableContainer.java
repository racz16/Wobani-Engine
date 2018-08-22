package wobani.toolbox.invalidatable;

import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.util.*;

/**
 Contains a list of Invalidatables, what you can invalidate.

 @param <T> the container object's type
 */
public class InvalidatableContainer<T>{

    /**
     List of Invalidatables.
     */
    private final List<Invalidatable> invalidatables = new ArrayList<>();
    /**
     Prevents the InvalidatableContainer to store2D this object.
     */
    private final T container;
    /**
     Prevents invalidation mechanism from causing deadlock.
     */
    private boolean invalidatable = true;

    /**
     Initializes a new InvalidatableContainer to the given value.

     @param container prevents the InvalidatableContainer to store2D this object
     */
    public InvalidatableContainer(@Nullable T container){
        this.container = container;
    }

    /**
     Adds the given Invalidatable to the list of Invalidatables.

     @param invalidatable Invalidatable

     @throws NullPointerException     can't add null to the list of Invalidatables
     @throws IllegalArgumentException invalidatable can't be this
     */
    public void addInvalidatable(@NotNull Invalidatable invalidatable){
        if(invalidatable == null){
            throw new NullPointerException();
        }
        if(invalidatable == container){
            throw new IllegalArgumentException("Invalidatable can't be this");
        }
        addInvalidatableUnsafe(invalidatable);
    }

    /**
     Adds the given Invalidatable to the list of Invalidatables.

     @param invalidatable Invalidatable
     */
    private void addInvalidatableUnsafe(@NotNull Invalidatable invalidatable){
        if(!containsInvalidatable(invalidatable)){
            invalidatables.add(invalidatable);
        }
    }

    /**
     Returns true if the list of Invalidatables contains the specified element.

     @param invalidatable Invalidatable

     @return true if the list of Invalidatables contains the specified element, false otherwise
     */
    public boolean containsInvalidatable(@Nullable Invalidatable invalidatable){
        return Utility.containsReference(invalidatables, invalidatable);
    }

    /**
     Returns the specified invalidatable.

     @param index invalidatable's index

     @return the specified invalidatable
     */
    @NotNull
    public Invalidatable getInvalidatable(int index){
        return invalidatables.get(index);
    }

    /**
     Removes the parameter from the list of Invalidatables.

     @param invalidatable Invalidatable
     */
    public void removeInvalidatable(@Nullable Invalidatable invalidatable){
        Utility.removeReference(invalidatables, invalidatable);
    }

    /**
     Returns the number of Invalidatables stored in this object.

     @return the number of Invalidatables stored
     */
    public int size(){
        return invalidatables.size();
    }

    /**
     Invalidates the Invalidatables.
     */
    public void invalidate(){
        if(invalidatable){
            invalidatable = false;
            invalidateAllInvalidatables();
            invalidatable = true;
        }
    }

    /**
     Invalidates all of the Invalidatables.
     */
    private void invalidateAllInvalidatables(){
        for(Invalidatable inv : invalidatables){
            inv.invalidate();
        }
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.invalidatables);
        hash = 83 * hash + (this.invalidatable ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }
        final InvalidatableContainer other = (InvalidatableContainer) obj;
        if(this.invalidatable != other.invalidatable){
            return false;
        }
        if(!Objects.equals(this.invalidatables, other.invalidatables)){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(InvalidatableContainer.class.getSimpleName()).append("(")
                .append(" size: ").append(invalidatables.size()).append(")");
        return res.toString();
    }

}
