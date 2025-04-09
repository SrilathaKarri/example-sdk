package sdk.facility.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ImagingCenterServiceType {

    @NotNull
    @Size(min = 2, max = 100)
    private String service;

    @Min(value = 0, message = "Count must be a non-negative number")
    private int count;
}

