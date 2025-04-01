package sdk.facility.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import sdk.facility.dto.UploadDocuments;

public class PhotoValidator implements ConstraintValidator<ValidPhotos, UploadDocuments> {

    @Override
    public boolean isValid(UploadDocuments uploadDocuments, ConstraintValidatorContext context) {
        if (uploadDocuments == null) {
            context.buildConstraintViolationWithTemplate("Photo is required").addConstraintViolation();
            return false;
        }
        boolean isValid = true;
        context.disableDefaultConstraintViolation();
        // Validate Board Photo
        try {
            FileValidator.validateDocument(uploadDocuments.getBoardPhoto(), "Board Photo");
        } catch (IllegalArgumentException e) {
            context.buildConstraintViolationWithTemplate(e.getMessage())
                    .addPropertyNode("boardPhoto")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate Building Photo
        try {
            FileValidator.validateDocument(uploadDocuments.getBuildingPhoto(), "Building Photo");
        } catch (IllegalArgumentException e) {
            context.buildConstraintViolationWithTemplate(e.getMessage())
                    .addPropertyNode("buildingPhoto")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}