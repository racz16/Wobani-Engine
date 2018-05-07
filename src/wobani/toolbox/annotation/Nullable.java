package wobani.toolbox.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.*;

/**
 * Signs that the parameter or the return value could be null.
 */
@Documented
@Target({METHOD, PARAMETER})
public @interface Nullable {

}
