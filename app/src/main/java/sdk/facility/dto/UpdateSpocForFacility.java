package sdk.facility.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSpocForFacility {

    @NotNull
    @Size(min = 3, max = 100)
    private String spocName;

    @NotNull
    @Size(min = 1, max = 50)
    private String id;

    @NotNull
    @Size(min = 1, max = 50)
    private String spocId;

    @Size(max = 100)
    private String consentManagerName;

    @Size(max = 50)
    private String consentManagerId;
}

