package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a diagnostic report specifically related to laboratory tests.
 * <p>
 * This DTO extends {@link DiagnosticReport} and adds a list of observations,
 * which includes detailed lab test results and measurements.
 * </p>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class DiagnosticReportLab extends DiagnosticReport {

    /**
     * A list of observations from laboratory tests, including test results, measurements, and interpretations.
     */
    private List<Observation> observations;
}
