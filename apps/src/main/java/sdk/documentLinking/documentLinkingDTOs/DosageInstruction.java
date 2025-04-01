package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

/**
 * Represents dosage instructions for a medication, including details about
 * frequency, route of administration, and duration.
 * <p>
 * This DTO is used to capture prescription details such as how often to take
 * the medication, the method of administration, and the prescribed duration.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DosageInstruction {

    /**
     * The duration of the medication regimen, typically measured in days, weeks,
     * or months. This field can be null if not specified.
     */
    private Integer duration;

    /**
     * The frequency with which the medication should be administered (e.g., "Once", "Twice").
     * This is an optional field.
     */
    private DocLinkingEnums.DosageFrequency frequency;

    /**
     * The route of administration for the medication (e.g., "Oral", "Intravenous").
     * This is an optional field.
     */
    private DocLinkingEnums.MedicationRoute route;

    /**
     * The method of administration (e.g., "Swallow").
     * This is an optional field.
     */
    private DocLinkingEnums.MedicationMethod method;
}