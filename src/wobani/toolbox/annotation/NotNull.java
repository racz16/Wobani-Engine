package wobani.toolbox.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 Signs that the parameter or the return value can't be null. If you give null to a NotNull parameter, it can cause
 NullPointerException or other errors.
 */
@Documented
@Target({METHOD, PARAMETER})
public @interface NotNull{

}
