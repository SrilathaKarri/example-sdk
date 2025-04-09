package sdk.facility.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhotoValidator.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhotos {
    String message() default "Invalid file format or size. Supported formats: JPG, PNG, PDF. Maximum size: 1MB.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
