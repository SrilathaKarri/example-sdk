package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;

/**
 * Represents the details of a medical consultation between a patient and a practitioner.
 * <p>
 * This DTO includes references to the care context, patient, practitioner, and appointment,
 * along with optional health records and contact details.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsultationDTO {

    /**
     * A reference identifier for the care context associated with the consultation.
     * This field must not be empty.
     */
    @NotEmpty(message = "Care context reference is required and cannot be empty")
    private String careContextReference;

    /**
     * A reference identifier for the patient involved in the consultation.
     * This field must not be empty.
     */
    @NotEmpty(message = "Patient reference is required and cannot be empty")
    private String patientReference;

    /**
     * A reference identifier for the practitioner providing the consultation.
     * This field must not be empty.
     */
    @NotEmpty(message = "Practitioner reference is required and cannot be empty")
    private String practitionerReference;

    /**
     * A reference identifier for the appointment associated with the consultation.
     * This field must not be empty.
     */
    @NotEmpty(message = "Appointment reference is required and cannot be empty")
    private String appointmentReference;

    /**
     * The ABHA (Ayushman Bharat Health Account) address of the patient, if available.
     */
    private String patientAbhaAddress;

    /**
     * A list of health records associated with the consultation.
     * These records are represented using {@link HealthInformationDTO}.
     */
    private List<HealthInformationDTO> healthRecords;

    /**
     * The mobile number of the patient for communication purposes.
     */
    private String mobileNumber;

    /**
     * A unique identifier for the request, used for tracking and reference.
     */
    private String requestId;
}
