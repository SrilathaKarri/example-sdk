package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO (Data Transfer Object) that represents the response for creating a care context.
 * This includes the reference to the care context, the request ID, and the authorization modes associated with the request.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCareContextResponseDTO {

    /**
     * The reference identifier for the created care context.
     * This reference is used to uniquely identify the care context.
     */
    private String careContextReference;

    /**
     * The unique identifier for the request that initiated the creation of the care context.
     */
    private String requestId;

    /**
     * Authentication modes associated with this care context.
     */
    private List<String> authModes;
}