package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

import javax.validation.constraints.*;

/**
 * Represents a medical condition identified in a patient's health record.
 * <p>
 * This DTO extends {@link SnomedCode} to include a SNOMED code for the condition
 * and captures the clinical status to reflect the current state of the condition.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
public class Condition extends SnomedCode {

    /**
     * The clinical status of the condition (e.g., active, resolved, recurrence).
     * This field must not be null.
     */
    @NotNull
    private DocLinkingEnums.ClinicalStatus clinicalStatus;
}
