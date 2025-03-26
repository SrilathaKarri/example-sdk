package sdk.base.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sdk.base.errors.ErrorType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private String type;
    private String message;
    private List<T> data;
    private int totalRecords;
    private int status;
    private int page;
    private int size;
    private int totalPages;

    public ApiResponse(List<T> data, String message, String type, int page, int size, int totalPages) {
        this.data = (data != null) ? data : List.of();
        this.message = message;
        this.type = type;
        this.totalRecords = (data != null) ? data.size() : 0;
        this.status = this.totalRecords > 0 ?
                ErrorType.SUCCESS.getStatusCode() :
                ErrorType.NO_CONTENT.getStatusCode();
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
    }

    // Constructor for handling errors with specific error type
    public ApiResponse(String message, String type, ErrorType errorType) {
        this(null, message, type, 0, 0, 0);
        this.status = errorType.getStatusCode();
    }

    public ApiResponse(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public ApiResponse(String message, String type, int statusCode) {
        this.message = message;
        this.type = type;
        this.status = statusCode;
    }
}
