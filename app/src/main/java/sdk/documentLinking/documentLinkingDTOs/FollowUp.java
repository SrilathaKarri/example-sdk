package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

/**
 * Represents a follow-up appointment or service after a medical procedure, consultation,
 * or treatment. This class helps track the details related to post-care services.
 * <p>
 * It includes information about the service category, type, appointment reference,
 * and related medical codes.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowUp {

    /**
     * The category of the service (e.g., "Routine Check-up", "Specialist Referral").
     * This should be a valid SNOMED code.
     * This field must not be null.
     */
    @NotNull
    private SnomedCode serviceCategory;

    /**
     * The specific type of service (e.g., "Cardiology Follow-up", "Surgical Review").
     * This should be a valid SNOMED code.
     * This field must not be null.
     */
    @NotNull
    private SnomedCode serviceType;

    /**
     * The type of appointment (e.g., "In-Person", "Telehealth", "Home Visit").
     * This should be a valid SNOMED code.
     * This field must not be null.
     */
    @NotNull
    private SnomedCode appointmentType;

    /**
     * A reference ID for the follow-up appointment, used to link with appointment systems.
     * This field must not be empty.
     */
    @NotEmpty
    private String appointmentReference;
}