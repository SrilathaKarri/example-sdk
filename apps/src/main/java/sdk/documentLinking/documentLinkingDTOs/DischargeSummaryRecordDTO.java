package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;

/**
 * Represents a discharge summary record that provides a comprehensive overview of a patient's
 * hospitalization, including conditions, medications, procedures, and follow-up plans.
 * <p>
 * This DTO aggregates various healthcare records relevant to the patient's discharge process.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DischargeSummaryRecordDTO {

    /**
     * A list of conditions diagnosed during hospitalization.
     */
    private List<Condition> conditions;

    /**
     * A list of discharge summary documents, which provide detailed information about the patient's discharge.
     * This field must not be empty.
     */
    @NotEmpty
    private List<DocumentReference> dischargeSummaryDocuments;

    /**
     * A list of medical conditions from the patient's past medical history.
     */
    private List<Condition> medicalHistory;

    /**
     * Family medical history, providing insights into hereditary health conditions.
     */
    private MedicalHistory familyHistory;

    /**
     * A list of laboratory investigations conducted during the hospital stay.
     */
    private List<DiagnosticReportLab> investigations;

    /**
     * A list of procedures performed during hospitalization.
     */
    private List<Procedure> procedures;

    /**
     * A list of medications prescribed at discharge.
     */
    private List<MedicationRequest> medications;

    /**
     * A list of care plans recommended post-discharge.
     */
    private List<CarePlan> carePlan;

    /**
     * A list of physical examinations conducted during the hospital stay.
     */
    private List<Observation> physicalExamination;

    /**
     * A list of allergies identified during the hospitalization.
     */
    private List<AllergyIntolerance> allergies;

    /**
     * A list of medication statements detailing the patient's medication history.
     */
    private List<MedicationStatement> medicationStatements;
}
