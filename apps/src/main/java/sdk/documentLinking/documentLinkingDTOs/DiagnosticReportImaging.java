package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

/**
 * Represents a diagnostic report related to medical imaging procedures (e.g., X-rays, MRIs, CT scans).
 * <p>
 * This DTO extends {@link DiagnosticReport} and includes a reference to the imaging document,
 * such as an image file or a report generated from the imaging system.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosticReportImaging extends DiagnosticReport {

    /**
     * A reference to the document containing the imaging results (e.g., an image file or a diagnostic report).
     * This field must not be null.
     */
    @NotNull
    private DocumentReference imaging;
}
