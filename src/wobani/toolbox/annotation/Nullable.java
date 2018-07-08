package wobani.toolbox.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 Signs that the parameter or the return value could be null.
 */
@Documented
@Target({METHOD, PARAMETER})
public @interface Nullable{

}
