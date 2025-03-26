package sdk.base.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    SUCCESS(HttpStatus.OK),
    NO_CONTENT(HttpStatus.NO_CONTENT),
    VALIDATION(HttpStatus.BAD_REQUEST),
    AUTHENTICATION(HttpStatus.UNAUTHORIZED),
    AUTHORIZATION(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    CONFLICT(HttpStatus.CONFLICT),
    CONNECTION(HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    public int getStatusCode() {
        return httpStatus.value();
    }

    public String getStatusMessage() {
        return httpStatus.getReasonPhrase();
    }
}
