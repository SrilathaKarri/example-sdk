package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;

/**
 * Data Transfer Object (DTO) representing a wellness record, which includes various health observations
 * and related documents for comprehensive health tracking.
 * <p>
 * This DTO is designed to capture different aspects of wellness, such as vital signs, physical activities,
 * lifestyle assessments, and associated health documents.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WellnessRecordDTO {

    /**
     * List of vital signs observed (e.g., heart rate, blood pressure).
     */
    private List<Observation> vitalSigns;

    /**
     * List of body measurements (e.g., height, weight, BMI).
     */
    private List<Observation> bodyMeasurements;

    /**
     * List of physical activities.
     */
    private List<Observation> physicalActivities;

    /**
     * List of general health assessments.
     */
    private List<Observation> generalAssessments;

    /**
     * List of women’s health observations.
     */
    private List<Observation> womenHealth;

    /**
     * List of lifestyle-related observations.
     */
    private List<Observation> lifeStyle;

    /**
     * List of other miscellaneous health observations not covered in other categories.
     */
    private List<Observation> others;

    /**
     * List of wellness-related documents.
     * This field must not be empty.
     */
    @NotEmpty
    private List<DocumentReference> wellnessDocuments;
}
