package sdk.base.response;

import lombok.Data;
import java.util.List;

@Data
public class UpdateProfileResponse {
    private String type;
    private String message;
    private String resourceId;
    private List<Object> validationErrors;
    private Object resource;
}
