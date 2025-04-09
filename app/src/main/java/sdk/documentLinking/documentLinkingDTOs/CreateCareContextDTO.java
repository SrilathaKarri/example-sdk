package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

import javax.validation.constraints.*;

/**
 * DTO representing the details for creating a care context.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCareContextDTO {

    /**
     * Unique identifier reference for the patient.
     * Must be a valid UUID in string format.
     */
    @NotEmpty(message = "Patient reference is required and cannot be empty")
    @Pattern(regexp = "^[a-fA-F0-9-]{36}$", message = "Patient reference must be a valid 36-character UUID")
    private String patientReference;

    /**
     * A self-declared username
     * unique address that ensures the security of patient health-related data
     */
    private String patientAbhaAddress;

    /**
     * The practitioner reference as a 36-character UUID.
     */
    @NotEmpty(message = "Practitioner reference is required and cannot be empty")
    @Pattern(regexp = "^[a-fA-F0-9-]{36}$", message = "Practitioner reference must be a valid 36-character UUID")
    private String practitionerReference;

    /**
     * The reference identifier for the appointment.
     */
    @NotEmpty(message = "Appointment reference is required and cannot be empty")
    @Pattern(regexp = "^[a-fA-F0-9-]{36}$", message = "Appointment reference must be a valid 36-character UUID")
    private String appointmentReference;

    /**
     * The health information type associated with the care context.
     */
    @NotNull(message = "Health information type is required")
    private DocLinkingEnums.HealthInformationTypes hiType;

    /**
     * The date for the appointment.
     */
    @NotEmpty(message = "Appointment date is required and cannot be empty")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9] (AM|PM) - ([01]?[0-9]|2[0-3]):[0-5][0-9] (AM|PM)$",
            message = "Appointment date must be in the format 'hh:mm am/pm - hh:mm am/pm'")
    private String appointmentDate;

    /**
     * A flag to indicate whether to resend OTP.
     */
    @NotNull(message = "Resend OTP flag is required")
    private Boolean resendOtp;
}