package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

/**
 * Represents a SNOMED code, which is a standardized clinical terminology used for health information exchange.
 * <p>
 * This class encapsulates the code and its corresponding text description.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SnomedCode {

    /**
     * The SNOMED  code representing a specific clinical concept.
     * This field must not be empty.
     */
    @NotEmpty
    private String code;

    /**
     * The human-readable text description of the SNOMED code.
     * This field must not be empty.
     */
    @NotEmpty
    private String text;
}
