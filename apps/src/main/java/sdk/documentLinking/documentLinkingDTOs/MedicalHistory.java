package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;

/**
 * Represents the medical history of a patient, including conditions
 * and procedures that are significant to the patient's health status.
 * <p>
 * This DTO helps track a patient's historical health data, aiding
 * in diagnosis, treatment planning, and care continuity.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalHistory {

    /**
     * A list of conditions (e.g., diseases, disorders) from the patient's
     * past and present medical history.
     * This field is required and cannot be null.
     */
    @NotNull(message = "Conditions cannot be null")
    private List<Condition> conditions;

    /**
     * A list of procedures (e.g., surgeries, diagnostic tests)
     * that the patient has undergone in the past or present.
     * This field is required and cannot be null.
     */
    @NotNull(message = "Procedures cannot be null")
    private List<Procedure> procedures;
}
