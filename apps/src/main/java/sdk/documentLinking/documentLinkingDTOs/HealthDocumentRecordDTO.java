package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;

/**
 * Represents a collection of health documents, such as medical reports, prescriptions,
 * diagnostic tests, or other related healthcare documents.
 * <p>
 * This DTO is used for linking multiple health documents to a patient's record.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HealthDocumentRecordDTO {

    /**
     * A list of document references representing health documents.
     * This field is required and cannot be empty.
     */
    @NotEmpty(message = "Health documents cannot be empty")
    private List<DocumentReference> healthDocuments;
}
