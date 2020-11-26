package de.samply.share.broker.filter;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.ws.rs.NameBinding;

/**
 * This is necessary to bind a filter for the token of API calls which are annotated with @Secured
 * or to specify a role: @Secured({Permissions.ADMIN})
 */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Secured {

  /**
   * Get the access permissions roles.
   * @return the access permissions roles
   */
  AccessPermission[] value() default {};
}
