package sdk.facility.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FacilityDetails {

    @NotNull
    private String ownershipType;

    @NotNull
    private String ownershipSubType;

    @NotNull
    private String status;

}

