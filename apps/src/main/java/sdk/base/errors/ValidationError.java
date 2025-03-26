package sdk.base.errors;

public class ValidationError extends EhrApiError {
    public ValidationError(String message) {
        super(message, ErrorType.VALIDATION);
    }
}
