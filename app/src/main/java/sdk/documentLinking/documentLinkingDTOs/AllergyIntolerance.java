package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

import javax.validation.constraints.*;

/**
 * Represents an allergy or intolerance recorded in a patient's health record.
 * <p>
 * This class extends {@link SnomedCode} to include SNOMED codes for both the allergy and its description.
 * It also captures clinical status, verification status, the date of recording, and the reaction observed.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
public class AllergyIntolerance extends SnomedCode {

    /**
     * The clinical status of the allergy or intolerance (e.g., active, resolved).
     * This field must not be null.
     */
    @NotNull
    private DocLinkingEnums.ClinicalStatus clinicalStatus;

    /**
     * The verification status of the allergy or intolerance (e.g., confirmed, unconfirmed).
     * This field must not be null.
     */
    @NotNull
    private DocLinkingEnums.VerificationStatus verificationStatus;

    /**
     * The date when the allergy or intolerance was recorded.
     * This field must not be empty.
     */
    @NotEmpty
    private String recordedDate;

    /**
     * A description of the allergic reaction or intolerance experienced by the patient.
     * This field must not be empty.
     */
    @NotEmpty
    private String reaction;
}