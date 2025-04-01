package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

/**
 * Represents a reference to a document, typically used to reference healthcare-related files such as
 * medical records, imaging reports, discharge summaries, etc.
 * <p>
 * This DTO includes details about the content type and the actual data (e.g., base64 encoded content or a URL).
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentReference {

    /**
     * The MIME type of the document (e.g., application/pdf, image/jpeg).
     * This field must not be empty.
     */
    @NotEmpty
    private String contentType;

    /**
     * The actual data of the document, which could be base64-encoded content, a URL, or a reference
     * to an external document repository.
     * This field must not be empty.
     */
    @NotEmpty
    private String data;
}