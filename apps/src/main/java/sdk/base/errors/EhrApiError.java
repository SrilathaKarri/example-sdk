package sdk.base.errors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import sdk.base.LogUtil;
import sdk.base.response.ApiResponse;

@Getter
@ToString
public class EhrApiError extends RuntimeException {
    private final int statusCode;
    private final ErrorType errorType;

    public EhrApiError(String message, ErrorType errorType) {
        super(message);
        this.statusCode = errorType.getStatusCode();
        this.errorType = errorType;
    }

    public EhrApiError(String message, HttpStatusCode httpStatusCode) {
        super(message);
        HttpStatus httpStatus = HttpStatus.valueOf(httpStatusCode.value()); // Ensure compatibility
        this.statusCode = httpStatus.value();
        this.errorType = mapHttpStatusToErrorType(httpStatus);
    }

    public static RuntimeException handleAndLogApiError(Throwable error) {
        if (error instanceof EhrApiError ehrApiError) {
            LogUtil.logger.error(String.format("EhrApiError occurred: %s", error.getMessage()));
            return ehrApiError;
        } else if (error instanceof WebClientResponseException e) {
            LogUtil.logger.error(String.format("WebClientResponseException: Status - %s, Body - %s",
                    e.getStatusCode(), e.getResponseBodyAsString()));

            return new EhrApiError(e.getResponseBodyAsString(), mapHttpStatusToErrorType(e.getStatusCode()));
        } else {
            LogUtil.logger.error(String.format("Unexpected Error: %s", error.getMessage()));
            return new EhrApiError(String.format("Unexpected Error: %s", error.getMessage()), ErrorType.INTERNAL_SERVER_ERROR);
        }
    }

    public static ErrorType mapHttpStatusToErrorType(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            return ErrorType.INTERNAL_SERVER_ERROR;
        }
        return switch (status) {
            case BAD_REQUEST -> ErrorType.VALIDATION;
            case UNAUTHORIZED -> ErrorType.AUTHENTICATION;
            case FORBIDDEN -> ErrorType.AUTHORIZATION;
            case NOT_FOUND -> ErrorType.NOT_FOUND;
            case CONFLICT -> ErrorType.CONFLICT;
            default -> ErrorType.INTERNAL_SERVER_ERROR;
        };
    }

    public static <T> Mono<ResponseEntity<ApiResponse<T>>> handleWebClientException(WebClientResponseException e) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(e.getResponseBodyAsString());

            String message = jsonNode.has("message") ?
                    jsonNode.get("message").asText() : "An unexpected error occurred";
            int statusCode = e.getStatusCode().value();
            HttpStatus httpStatus = HttpStatus.valueOf(e.getStatusCode().value());
            ErrorType errorType = mapHttpStatusToErrorType(httpStatus);

            ApiResponse<T> apiErrorResponse = new ApiResponse<>(errorType.getStatusMessage(), message, statusCode);
            return Mono.just(ResponseEntity.status(errorType.getStatusCode()).body(apiErrorResponse));
        } catch (Exception ex) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(ErrorType.INTERNAL_SERVER_ERROR.getStatusMessage(), "Error processing response", ErrorType.INTERNAL_SERVER_ERROR)));
        }
    }

    public static <T> Mono<ResponseEntity<ApiResponse<T>>> handleGenericException(Exception ex) {
        String errorMessage = (ex.getMessage() != null) ? ex.getMessage() : "An unexpected error occurred";

        int statusCode;
        String statusMessage;

        if (ex instanceof EhrApiError ehrApiError) {
            statusCode = ehrApiError.getStatusCode();
            statusMessage = ehrApiError.getErrorType().getStatusMessage();
        } else {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
            statusMessage = HttpStatus.INTERNAL_SERVER_ERROR.name();
        }

        ApiResponse<T> apiErrorResponse = new ApiResponse<>(statusMessage, errorMessage, statusCode);
        return Mono.just(ResponseEntity.status(statusCode).body(apiErrorResponse));
    }
}
