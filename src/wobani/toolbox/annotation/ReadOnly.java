package wobani.toolbox.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 Signs that this getter method returns a copied value. If you change it, the original variable won't change.
 */
@Documented
@Target({METHOD})
public @interface ReadOnly{
}
