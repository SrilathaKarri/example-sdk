package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

/**
 * Represents an advisory note provided to a patient or healthcare provider.
 * <p>
 * This DTO captures the category of the note and the actual note content,
 * both represented using SNOMED codes for standardization.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdvisoryNote {

    /**
     * The SNOMED code representing the category of the advisory note (e.g., dietary advice, medication).
     * This field must not be null.
     */
    @NotNull
    private SnomedCode category;

    /**
     * The SNOMED code representing the content of the advisory note.
     * This field must not be null.
     */
    @NotNull
    private SnomedCode note;
}