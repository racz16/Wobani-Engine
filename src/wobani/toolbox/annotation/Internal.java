package wobani.toolbox.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 Signs that the method used internally by the engine. Usually there is no reason to call this method. However you may
 want to override it.
 */
@Documented
@Target({METHOD})
public @interface Internal{

}
