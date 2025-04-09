package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;

/**
 * Represents a prescription record that includes information about medications,
 * associated conditions, and binary documents related to the prescription.
 * <p>
 * This DTO is used to capture details of prescriptions in electronic health records (EHR).
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionRecordDTO {

    /**
     * A list of medication requests included in the prescription.
     */
    private List<MedicationRequest> medicationRequests;

    /**
     * A list of document references representing prescription binaries.
     * This field must not be empty.
     */
    @NotEmpty
    private List<DocumentReference> prescriptionBinaries;

    /**
     * A list of conditions related to the prescription.
     */
    private List<Condition> conditions;

    /**
     * A list of physical examination observations associated with the prescription.
     */
    private List<Observation> physicalExamination;
}
