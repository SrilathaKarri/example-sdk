package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

import javax.validation.constraints.*;

/**
 * Represents a health information record, which can be in raw FHIR format
 * or as a structured DTO.
 * <p>
 * This DTO is used to encapsulate various types of health-related data,
 * including FHIR documents, health information types, and related metadata.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HealthInformationDTO {

    /**
     * Indicates whether the health information is in raw FHIR format.
     * This field can be null if not specified.
     */
    private Boolean rawFhir;

    /**
     * The FHIR document if the data is in raw FHIR format.
     * This can be a JSON or XML representation of the FHIR resource.
     */
    private Object fhirDocument;

    /**
     * The type of health information (e.g., Prescription, Immunization Record).
     * This field is required and cannot be null.
     */
    @NotNull(message = "Health information type is required")
    private DocLinkingEnums.HealthInformationTypes informationType;

    /**
     * A custom DTO representing structured health information.
     * This field can be null if not applicable.
     */
    private Object dto;
}
