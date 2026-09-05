# Custom Validation Package (`com.project.souklab.validation`)

Custom Jakarta Bean Validation constraints and validators for cross-field consistency.

---

## Classes Reference

| Class | Type | Responsibility |
| :--- | :---: | :--- |
| [`DifferentPasswords`](DifferentPasswords.java) | `@Constraint` Annotation | Class-level validation annotation ensuring new password differs from current password and matches confirmation password. |
| [`DifferentPasswordsValidator`](DifferentPasswordsValidator.java) | `ConstraintValidator` | Implementation validating password inequality and confirmation matching rules across password reset and change DTOs. |
