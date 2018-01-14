package components.light.lightTypes;

import toolbox.invalidatable.Invalidatable;
import core.*;
import toolbox.annotations.*;

/**
 * This interface signs that the Component is a light source.
 */
public interface Light extends Invalidatable {

    public GameObject getGameObject();

    public void addInvalidatable(@NotNull Invalidatable invalidatable);

    public void removeInvalidatable(@Nullable Invalidatable invalidatable);

    public boolean isActive();

}
