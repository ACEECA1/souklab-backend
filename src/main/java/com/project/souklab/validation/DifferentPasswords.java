package com.project.souklab.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level Bean Validation constraint that verifies the new password
 * is not identical to the current password in change-password requests.
 */
@Documented
@Constraint(validatedBy = DifferentPasswordsValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DifferentPasswords {

    String message() default "New password must be different from your current password.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
