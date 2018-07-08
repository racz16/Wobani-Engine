package wobani.toolbox.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 Signs that you should bind the resource before calling this method.
 */
@Documented
@Target({METHOD})
public @interface Bind{
}
