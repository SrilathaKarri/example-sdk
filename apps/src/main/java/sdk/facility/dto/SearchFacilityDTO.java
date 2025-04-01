package sdk.facility.dto;

import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class SearchFacilityDTO {

    @Size(max = 50)
    private String ownershipCode;

    @Size(max = 50)
    private String stateLGDCode;

    @Size(max = 50)
    private String districtLGDCode;

    @Size(max = 50)
    private String subDistrictLGDCode;

    @Size(max = 10)
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be a 6-digit number")
    private String pincode;

    @Size(min = 3, max = 255, message = "Facility name must be between 3 and 255 characters")
    private String facilityName;

    @Size(max = 50)
    private String facilityId;

    @NotNull(message = "Page number is required")
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer page;

    @NotNull(message = "Results per page is required")
    @Min(value = 1, message = "Results per page must be at least 1")
    @Max(value = 100, message = "Results per page cannot exceed 100")
    private Integer resultsPerPage;
}

