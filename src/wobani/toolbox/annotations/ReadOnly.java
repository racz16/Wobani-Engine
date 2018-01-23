package wobani.toolbox.annotations;

import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.*;

/**
 * Signs that this getter method returns a copied value. If you change it, the
 * original variable won't change.
 */
@Documented
@Target({METHOD})
public @interface ReadOnly {
}
