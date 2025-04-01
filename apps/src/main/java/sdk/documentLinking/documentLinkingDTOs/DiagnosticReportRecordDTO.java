package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a record containing diagnostic reports, which could include various types of medical reports
 * such as lab tests, imaging reports, or other diagnostic findings.
 * <p>
 * This DTO extends {@link SnomedCode} for standardized medical coding and includes a list of reports,
 * which can contain different report formats or types.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosticReportRecordDTO extends SnomedCode {

    /**
     * A list of diagnostic reports.
     */
    private List<Object> reports;
}
