package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

/**
 * Represents a statement regarding the use of a medication by a patient,
 * including prescribed, over-the-counter, and other medication types.
 * <p>
 * This DTO helps capture medication history and current medication
 * usage, which is essential for patient safety and care coordination.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
public class MedicationStatement extends SnomedCode {

    /**
     * The current status of the medication statement,
     * such as active, completed, or stopped.
     */
    private DocLinkingEnums.MedicationStatementStatus status;

    /**
     * The date when the medication statement was asserted or documented.
     */
    private String dateAsserted;

    /**
     * The reason for asserting the medication statement,
     * such as treatment of a condition or preventive care.
     */
    private SnomedCode reasonCode;

    /**
     * The specific medication being referenced,
     * identified using SNOMED or other coding systems.
     */
    private SnomedCode medication;
}
