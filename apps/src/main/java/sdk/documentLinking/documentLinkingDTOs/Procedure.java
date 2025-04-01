package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

import javax.validation.constraints.*;

/**
 * Represents a medical procedure performed on a patient.
 * <p>
 * This class extends {@link SnomedCode}, inheriting SNOMED code and description attributes,
 * and includes additional fields to capture the status, related complications, and the date of performance.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
public class Procedure extends SnomedCode {

    /**
     * The current status of the procedure (e.g., preparation, in-progress, completed).
     * This field must not be null.
     */
    @NotNull
    private DocLinkingEnums.ProcedureStatus status;

    /**
     * The SNOMED code representing the procedure performed.
     * This field must not be null.
     */
    @NotNull
    private SnomedCode procedure;

    /**
     * The SNOMED code representing any complications related to the procedure.
     * This field must not be null.
     */
    @NotNull
    private SnomedCode complications;

    /**
     * The date on which the procedure was performed.
     * This field must not be empty.
     */
    @NotEmpty
    private String performedDate;
}
