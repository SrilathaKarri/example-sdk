package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sdk.documentLinking.enums.DocLinkingEnums;

import javax.validation.constraints.*;

/**
 * Represents a service request in the healthcare domain, which defines an action or request
 * for a specific healthcare service, procedure, or intervention.
 * <p>
 * This class extends {@link SnomedCode}, inheriting SNOMED code and description attributes,
 * and adds the status and intent of the service request.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequest extends SnomedCode {

    /**
     * The current status of the service request (e.g., draft, active, completed).
     * This field must not be null.
     */
    @NotNull
    private DocLinkingEnums.ServiceRequestStatus status;

    /**
     * The intent of the service request (e.g., proposal, plan, order).
     * This field must not be null.
     */
    @NotNull
    private DocLinkingEnums.ServiceRequestIntent intent;
}
