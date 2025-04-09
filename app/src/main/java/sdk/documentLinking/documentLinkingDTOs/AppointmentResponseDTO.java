package sdk.documentLinking.documentLinkingDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response for an appointment-related API request.
 * <p>
 * This DTO includes metadata about the response, such as a message, type, and resource identifiers,
 * along with the appointment details encapsulated in {@link AppointmentDTO}.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponseDTO {

    /**
     * A message providing additional context or information about the response.
     */
    private String message;

    /**
     * The type of the response, which could indicate success, error, or specific status codes.
     */
    private String type;

    /**
     * The unique identifier for the related resource (e.g., appointment ID).
     */
    private String resourceId;

    /**
     * The FHIR profile ID associated with the appointment.
     */
    private String fhirProfileId;

    /**
     * The appointment details, represented by {@link AppointmentDTO}.
     * This field is mapped to the JSON property "resource" during serialization/deserialization.
     */
    @JsonProperty("resource")
    private AppointmentDTO appointmentDTO;
}
