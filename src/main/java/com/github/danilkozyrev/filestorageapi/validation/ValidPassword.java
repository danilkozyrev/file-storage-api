package com.github.danilkozyrev.filestorageapi.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * The annotated element must be a valid password.
 *
 * @see PasswordConstraintValidator
 */
@Constraint(validatedBy = {PasswordConstraintValidator.class})
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidPassword {

    String message() default "password is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
