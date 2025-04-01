package sdk.facility.validators;

import sdk.facility.dto.Document;

public class FileValidator {
    private static final long MAX_FILE_SIZE = 1048576; // 1MB

    public static void validateDocument(Document document, String fieldName) {
        if (document == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private static boolean isValidMimeType(String contentType) {
        return "image/jpeg".equalsIgnoreCase(contentType) || "image/png".equalsIgnoreCase(contentType);
    }

}

