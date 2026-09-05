package com.project.souklab.validation;

import com.project.souklab.dto.auth.ChangePasswordRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that newPassword does not match oldPassword in a ChangePasswordRequestDTO.
 * Binds any constraint violation to the "newPassword" property node so it surfaces as a
 * field error matching the application's 422 error envelope.
 */
public class DifferentPasswordsValidator implements ConstraintValidator<DifferentPasswords, ChangePasswordRequestDTO> {

    @Override
    public boolean isValid(ChangePasswordRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        String oldPassword = dto.getOldPassword();
        String newPassword = dto.getNewPassword();

        if (oldPassword == null || oldPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
            return true;
        }

        if (newPassword.equals(oldPassword)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("newPassword")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
