package core.framework.api.search;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author neo
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Index {
    String index();

    String type();
}
