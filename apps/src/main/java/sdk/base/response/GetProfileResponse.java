package sdk.base.response;

import lombok.Data;

@Data
public class GetProfileResponse {
    private String type;
    private String message;
    private Object requestResource;
    private Integer totalNumberOfRecords;
    private String nextPageLink;
}
