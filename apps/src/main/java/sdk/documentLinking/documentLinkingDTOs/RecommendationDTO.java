package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a recommendation related to vaccinations or health interventions,
 * including the vaccine code, target disease, contraindications, and forecast status.
 * <p>
 * This DTO allows the representation of clinical recommendations in a structured manner,
 * making it suitable for integration with healthcare information systems.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationDTO {

    /**
     * The code representing the vaccine being recommended.
     */
    private GenericCode vaccineCode;

    /**
     * The code representing the target disease for the vaccine recommendation.
     */
    private GenericCode targetDisease;

    /**
     * The code representing any contraindicated vaccines for the patient.
     */
    private GenericCode contraindicatedVaccineCode;

    /**
     * The code representing the forecast status of the recommendation (e.g., recommended, not recommended).
     */
    private GenericCode forecastStatus;

    /**
     * The code representing the reason for the forecast status (e.g., medical condition, age restriction).
     */
    private GenericCode forecastReason;

    /**
     * The code representing any date-related criteria for the recommendation (e.g., valid until date).
     */
    private GenericCode dateCriterion;
}
