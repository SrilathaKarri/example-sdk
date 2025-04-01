package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

import javax.validation.constraints.*;

/**
 * Represents a request for a medication to be prescribed or dispensed to a patient.
 * This can include prescriptions, over-the-counter medications, or other requests.
 * <p>
 * Medication requests are vital for tracking prescription workflows
 * and ensuring accurate medication administration.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
public class MedicationRequest extends SnomedCode {

    /**
     * The current status of the medication request,
     * such as active, completed, or on-hold.
     * This field is required and cannot be null.
     */
    @NotNull(message = "Medication request status is required")
    private DocLinkingEnums.MedicationRequestStatus status;

    /**
     * The date when the medication request was authored,
     * This field is required and cannot be empty.
     */
    @NotEmpty(message = "Authored date is required and cannot be empty")
    private String authoredOn;

    /**
     * Instructions for how the medication should be administered,
     * including dosage, frequency, and route of administration.
     * This field is required and cannot be null.
     */
    @NotNull(message = "Dosage instruction is required")
    private DosageInstruction dosageInstruction;

    /**
     * The specific medication being requested,
     * identified using SNOMED or other coding systems.
     */
    private SnomedCode medication;

    /**
     * The reason for the medication request,
     * such as treatment of a condition or preventive care.
     */
    private SnomedCode reasonCode;
}
