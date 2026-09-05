package com.project.souklab.validation;

import com.project.souklab.dto.auth.ChangePasswordRequestDTO;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit and Bean Validation tests for DifferentPasswordsValidator and @DifferentPasswords constraint.
 */
class DifferentPasswordsValidatorTest {

    private DifferentPasswordsValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;
    private Validator beanValidator;

    @BeforeEach
    void setUp() {
        validator = new DifferentPasswordsValidator();
        context = mock(ConstraintValidatorContext.class);
        builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

        when(context.getDefaultConstraintMessageTemplate())
                .thenReturn("New password must be different from your current password.");
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            beanValidator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("isValid: returns true when DTO is null")
    void isValid_whenDtoIsNull_returnsTrue() {
        boolean valid = validator.isValid(null, context);
        assertThat(valid).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @ParameterizedTest
    @CsvSource({
            "'', 'newPassword123'",
            "'oldPassword123', ''",
            "'   ', 'newPassword123'",
            "'oldPassword123', '   '"
    })
    @DisplayName("isValid: returns true when either password is blank to allow @NotBlank to report field errors")
    void isValid_whenEitherPasswordIsBlank_returnsTrue(String oldPassword, String newPassword) {
        ChangePasswordRequestDTO dto = ChangePasswordRequestDTO.builder()
                .oldPassword(oldPassword.trim().isEmpty() ? oldPassword : oldPassword)
                .newPassword(newPassword.trim().isEmpty() ? newPassword : newPassword)
                .build();

        boolean valid = validator.isValid(dto, context);
        assertThat(valid).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("isValid: returns true when new password is different from old password")
    void isValid_whenPasswordsAreDifferent_returnsTrue() {
        ChangePasswordRequestDTO dto = ChangePasswordRequestDTO.builder()
                .oldPassword("CurrentPassword123!")
                .newPassword("BrandNewPassword123!")
                .build();

        boolean valid = validator.isValid(dto, context);
        assertThat(valid).isTrue();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("isValid: returns false and attaches violation to newPassword property when passwords match")
    void isValid_whenPasswordsAreIdentical_returnsFalseAndBindsToNewPasswordNode() {
        ChangePasswordRequestDTO dto = ChangePasswordRequestDTO.builder()
                .oldPassword("IdenticalPassword123!")
                .newPassword("IdenticalPassword123!")
                .build();

        boolean valid = validator.isValid(dto, context);

        assertThat(valid).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("New password must be different from your current password.");
        verify(builder).addPropertyNode("newPassword");
        verify(nodeBuilder).addConstraintViolation();
    }

    @Test
    @DisplayName("Bean Validation: validates ChangePasswordRequestDTO through Validator engine")
    void beanValidation_whenPasswordsIdentical_generatesConstraintViolationOnNewPassword() {
        ChangePasswordRequestDTO dto = ChangePasswordRequestDTO.builder()
                .oldPassword("SameSecretPassword123!")
                .newPassword("SameSecretPassword123!")
                .build();

        var violations = beanValidator.validate(dto);

        assertThat(violations).hasSize(1);
        var violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("newPassword");
        assertThat(violation.getMessage()).isEqualTo("New password must be different from your current password.");
    }
}
