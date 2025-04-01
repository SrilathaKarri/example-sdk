package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an immunization recommendation, providing guidance
 * on the administration of vaccines based on health status or schedules.
 * <p>
 * This DTO typically includes details about the recommended vaccine,
 * the rationale behind the recommendation, and any specific conditions.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImmunizationRecommendation {

    /**
     * The recommendation details, which include information such as
     * the recommended vaccine, timing, and specific guidelines.
     */
    private RecommendationDTO recommendation;
}
