package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

import javax.validation.constraints.*;

/**
 * Represents a diagnostic report that summarizes the results of medical tests, procedures, or evaluations.
 * <p>
 * This DTO includes essential details such as the report's status, category, conclusion, and the date it was recorded.
 * It extends {@link SnomedCode} for standardized medical coding.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosticReport extends SnomedCode {

    /**
     * The current status of the diagnostic report (e.g., registered, preliminary, final).
     * This field must not be null.
     */
    @NotNull
    private DocLinkingEnums.DiagnosticReportStatus status;

    /**
     * The category of the diagnostic report, represented using a SNOMED code.
     * This field must not be null.
     */
    @NotNull
    private SnomedCode category;

    /**
     * A summary or conclusion of the diagnostic findings.
     * This field must not be empty.
     */
    @NotEmpty
    private String conclusion;

    /**
     * A SNOMED code representing the conclusion of the diagnostic report for standardized documentation.
     * This field must not be null.
     */
    @NotNull
    private SnomedCode conclusionCode;

    /**
     * The date when the diagnostic report was recorded.
     * This field must not be empty and should follow the proper date format.
     */
    @NotEmpty
    private String recordedDate;
}
