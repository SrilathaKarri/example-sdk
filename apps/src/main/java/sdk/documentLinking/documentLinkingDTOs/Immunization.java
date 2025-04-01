package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

/**
 * Represents a specific immunization event administered to a patient.
 * <p>
 * This DTO captures essential details about the immunization, including
 * its status, vaccine information, lot number, expiration date, and
 * the date/time of occurrence.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Immunization {

    /**
     * The status of the immunization (e.g., completed,not-done).
     * This field is required and cannot be null.
     */
    private DocLinkingEnums.ImmunizationStatusEnum status;

    /**
     * The brand name of the vaccine administered.
     * This field is optional and can be null if not provided.
     */
    private String brandName;

    /**
     * The vaccine code, representing the specific vaccine administered.
     * This field can include details like the vaccine's standard code or identifier.
     */
    private GenericCode vaccineCode;

    /**
     * The date and time when the immunization was administered,
     * in ISO 8601 format (e.g., "2023-03-31T10:00:00Z").
     * This field is optional but should be provided if available.
     */
    private String occurrenceDateTime;

    /**
     * The lot number of the vaccine, useful for tracking and recall purposes.
     * This field is optional.
     */
    private String lotNumber;

    /**
     * The expiration date of the vaccine.
     * This field is optional but important for vaccine safety tracking.
     */
    private String expirationDate;
}
