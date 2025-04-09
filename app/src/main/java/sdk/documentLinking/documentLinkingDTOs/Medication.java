package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a collection of medication-related information,
 * including both medication statements and medication requests.
 * <p>
 * This DTO captures information about prescribed medications
 * as well as statements related to medication usage or history.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Medication {

    /**
     * A list of medication statements that describe the patient's
     * current or past medication usage, including prescribed and over-the-counter drugs.
     */
    private List<MedicationStatement> statement;

    /**
     * A list of medication requests that represent prescriptions
     * or requests for specific medications to be administered or provided.
     */
    private List<MedicationRequest> request;
}
