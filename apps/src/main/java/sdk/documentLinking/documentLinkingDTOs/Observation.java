package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

import javax.validation.constraints.*;

/**
 * Represents a clinical observation made during a healthcare encounter,
 * such as vital signs, lab results, or other health-related measurements.
 * <p>
 * Observations are critical for tracking patient health over time
 * and supporting clinical decision-making.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
public class Observation extends SnomedCode {

    /**
     * The current status of the observation, indicating whether it is
     * final, registered,amended, preliminary.
     * This field is required and cannot be null.
     */
    @NotNull(message = "Observation status is required")
    private DocLinkingEnums.ObservationStatus status;

    /**
     * The date and time when the observation was recorded,
     * formatted as ISO 8601 (e.g., "2023-03-31T14:30:00Z").
     */
    private String effectiveDateTime;

    /**
     * The quantitative value associated with the observation,
     * such as blood pressure readings, temperature, etc.
     */
    private ValueQuantity valueQuantity;

    /**
     * The reference range for the observation value,
     * indicating the normal or expected range for the measured parameter.
     */
    private ReferenceRange referenceRange;
}
