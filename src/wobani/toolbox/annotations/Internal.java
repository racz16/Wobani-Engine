package wobani.toolbox.annotations;

import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.*;

/**
 * Signs that the method used internally by the engine. Usually there is no
 * reason to call this method. However you may want to override it.
 */
@Documented
@Target({METHOD})
public @interface Internal {

}
