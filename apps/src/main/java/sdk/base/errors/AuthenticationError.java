package sdk.base.errors;

public class AuthenticationError extends EhrApiError {

    public AuthenticationError() {
        super("Authentication failed", ErrorType.AUTHENTICATION);
    }

    public AuthenticationError(String message) {
        super(message, ErrorType.AUTHENTICATION);
    }
}
