package toolbox.annotations;

import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.*;

/**
 * Signs that you should bind the resource before calling this method.
 */
@Documented
@Target({METHOD})
public @interface Bind {
}
