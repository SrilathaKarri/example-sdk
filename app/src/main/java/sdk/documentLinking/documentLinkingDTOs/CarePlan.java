package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

import javax.validation.constraints.*;

/**
 * Represents a care plan designed to manage a patient's health condition(s).
 * <p>
 * This DTO includes details about the care plan's category, status, intent, title, and description.
 * It is essential for coordinating healthcare activities and treatment plans.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarePlan {

    /**
     * The SNOMED code representing the category of the care plan (e.g., chronic care, rehabilitation).
     * This field must not be null.
     */
    @NotNull
    private SnomedCode category;

    /**
     * The current status of the care plan (e.g., active, draft, completed).
     * This field must not be null.
     */
    @NotNull
    private DocLinkingEnums.CarePlanStatus status;

    /**
     * The intent of the care plan (e.g., proposal, order, plan).
     * This field must not be null.
     */
    @NotNull
    private DocLinkingEnums.CarePlanIntent intent;

    /**
     * A concise title for the care plan.
     * This field must not be empty.
     */
    @NotEmpty
    private String title;

    /**
     * A detailed description of the care plan, outlining the goals, interventions, and management strategies.
     * This field must not be empty.
     */
    @NotEmpty
    private String description;
}
