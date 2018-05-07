package wobani.toolbox.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.*;

/**
 * Signs that the parameter or the return value can't be null. If you give null
 * to a NotNull parameter, it can cause NullPointerException or other errors.
 */
@Documented
@Target({METHOD, PARAMETER})
public @interface NotNull {

}
