package sdk.documentLinking.documentLinkingDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

import javax.validation.constraints.*;
import java.util.List;

/**
 * Represents a comprehensive DTO for linking health documents to patient records.
 * This DTO includes references to patients, practitioners, appointment details,
 * and associated health information.
 * <p>
 * It ensures the validation of critical fields like patient and practitioner IDs,
 * appointment dates, and health information types.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HealthDocumentLinkingDTO {

    /**
     * The unique identifier for the patient, expected to be a UUID (32 or 36 characters).
     * This field is required and validated for UUID format.
     */
    @NotEmpty(message = "Patient reference is required and cannot be empty")
    @Pattern(regexp = "^[a-fA-F0-9]{32}$|^[a-fA-F0-9-]{36}$",
            message = "Patient reference must be a valid 32 or 36-character UUID")
    private String patientReference;

    /**
     * The unique identifier for the practitioner, expected to be a UUID (32 or 36 characters).
     * This field is required and validated for UUID format.
     */
    @NotEmpty(message = "Practitioner reference is required and cannot be empty")
    @Pattern(regexp = "^[a-fA-F0-9]{32}$|^[a-fA-F0-9-]{36}$",
            message = "Practitioner reference must be a valid 32 or 36-character UUID")
    private String practitionerReference;

    /**
     * The address of the patient, typically an identifier like ABHA address.
     * This field is required and cannot be empty.
     */
    @NotEmpty(message = "Patient address is required and cannot be empty")
    private String patientAddress;

    /**
     * The full name of the patient.
     * This field is required and cannot be empty.
     */
    @NotEmpty(message = "Patient name is required and cannot be empty")
    private String patientName;

    /**
     * The start date of the appointment in ISO 8601 format (e.g., "2023-03-31T10:00:00Z").
     * This field is required and validated for proper date-time format.
     */
    @NotNull(message = "Appointment start date is required and cannot be empty")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private String appointmentStartDate;

    /**
     * The end date of the appointment in ISO 8601 format.
     * This field is required and validated for proper date-time format.
     */
    @NotNull(message = "Appointment end date is required and cannot be empty")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private String appointmentEndDate;

    /**
     * The priority of the appointment (e.g., "Emergency", "Follow-up").
     * This field is optional.
     */
    private String appointmentPriority;

    /**
     * The ID of the organization responsible for the appointment.
     * This field is required and cannot be empty.
     */
    @NotEmpty(message = "Organization ID is required and cannot be empty")
    private String organizationId;

    /**
     * The appointment slot information, if applicable.
     * This field is optional.
     */
    private String appointmentSlot;

    /**
     * A reference for the appointment, often used for linking with external systems.
     * This field is optional.
     */
    private String reference;

    /**
     * The patient's ABHA address, must end with "@sbx" or "@abdm".
     * This field is optional but validated if provided.
     */
    @Pattern(regexp = "@(?:sbx|abdm)$",
            message = "Patient ABHA address must end with @sbx or @abdm")
    private String patientAbhaAddress;

    /**
     * The type of health information being linked (e.g., Immunization, Prescription).
     * This field is required and cannot be null.
     */
    @NotNull(message = "Health information type is required")
    private DocLinkingEnums.HealthInformationTypes hiType;

    /**
     * The patient's mobile number, validated to be exactly 10 digits.
     * This field is required and cannot be empty.
     */
    @NotEmpty(message = "Mobile number is required and cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    private String mobileNumber;

    /**
     * A list of health records associated with this appointment or patient.
     * This field is optional.
     */
    private List<HealthInformationDTO> healthRecords;
}
