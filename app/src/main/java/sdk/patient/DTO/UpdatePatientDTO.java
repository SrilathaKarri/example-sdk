package sdk.patient.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sdk.base.enums.ResourceType;

/**
 * Data Transfer Object (DTO) class used for updating patient information.
 * This class includes optional fields like email, mobile number, and resource type.
 * It validates the provided fields to ensure the correct format and completeness.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePatientDTO {

        /**
         * The unique resource identifier associated with the patient.
         * This is a required field.
         */
        @NotBlank(message = "Resource ID is required")
        private String resourceId;

        /**
         * The email address of the patient (optional).
         * The email must be in a valid format.
         */
        @Email(message = "Invalid email format")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "Invalid email format"
        )
        private String emailId;  // Optional

        /**
         * The mobile number of the patient (optional).
         * It must be exactly 10 digits.
         */
        @Pattern(regexp = "\\d{10}", message = "Mobile number must be 10 digits")
        private String mobileNumber;  // Optional

        /**
         * The type of resource being updated (e.g., Patient).
         * This is a required field.
         */
        @NotNull(message = "Resource Type is required")
        private ResourceType resourceType;
}
