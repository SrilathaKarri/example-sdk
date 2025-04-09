package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

/**
 * Represents a generic coding structure used for standardized medical concepts,
 * such as SNOMED codes, LOINC codes, or other healthcare coding systems.
 * <p>
 * This class is often used to reference specific medical concepts, procedures,
 * conditions, or diagnostic codes within healthcare systems.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericCode {

    /**
     * The coding system URI or identifier.
     * This field must not be empty.
     */
    @NotEmpty
    private String system;

    /**
     * The specific code within the coding system.
     * This field must not be empty.
     */
    @NotEmpty
    private String code;

    /**
     * The human-readable text description corresponding to the code.
     * This field must not be empty.
     */
    @NotEmpty
    private String text;
}
