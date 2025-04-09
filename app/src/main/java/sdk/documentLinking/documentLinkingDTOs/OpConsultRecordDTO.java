package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;

/**
 * Represents an outpatient (OP) consultation record, capturing details of the patient's consultation,
 * including medications, conditions, advisory notes, procedures, and follow-up recommendations.
 * <p>
 * This DTO also supports the inclusion of medical history, allergies, and relevant documents from the consultation.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpConsultRecordDTO {

    /**
     * A list of medications prescribed during the outpatient consultation.
     */
    private List<MedicationRequest> medications;

    /**
     * A list of conditions identified during the consultation.
     */
    private List<Condition> conditions;

    /**
     * A list of advisory notes provided to the patient.
     */
    private List<AdvisoryNote> advisoryNotes;

    /**
     * A list of medical history conditions relevant to the outpatient consultation.
     */
    private List<Condition> medicalHistory;

    /**
     * Family medical history relevant to the consultation.
     */
    private MedicalHistory familyHistory;

    /**
     * A list of procedures performed during the consultation.
     */
    private List<Procedure> procedures;

    /**
     * A list of allergies or intolerances identified during the consultation.
     */
    private List<AllergyIntolerance> allergies;

    /**
     * A list of physical examination observations made during the consultation.
     */
    private List<Observation> physicalExamination;

    /**
     * A list of follow-up recommendations for the patient after the consultation.
     */
    private List<FollowUp> followUps;

    /**
     * A list of documents related to the outpatient consultation (e.g., reports, prescriptions).
     * This field must not be empty.
     */
    @NotEmpty
    private List<DocumentReference> opConsultDocuments;

    /**
     * A list of service requests for investigations or additional diagnostic tests.
     */
    private List<ServiceRequest> investigationAdvice;
}
