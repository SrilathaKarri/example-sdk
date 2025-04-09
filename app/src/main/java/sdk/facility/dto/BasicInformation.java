package sdk.facility.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
public class BasicInformation {

    @NotNull
    @NotBlank(message = "Facility name is required")
    @Size(min = 5, max = 100, message = "Facility name must be at least 5 characters long")
    private String facilityName;

    @NotNull(message = "Region is required")
    private String region;

    @NotNull
    @NotBlank(message = "Address is required")
    @Size(min = 5, message = "Address must be at least 5 characters long")
    @Pattern(regexp = "^[A-Za-z0-9\\s,-]+$",
            message = "Invalid address format")
    private String addressLine1;

    @NotNull
    @NotBlank(message = "Address is required")
    @Size(min = 5, message = "Address must be at least 5 characters long")
    @Pattern(regexp = "^[A-Za-z0-9\\s,-]+$",
            message = "Invalid address format")
    private String addressLine2;

    @NotNull(message = "State is required")
    private String state;

    @NotNull
    @NotBlank(message = "District is required")
    private String district;

    @NotNull
    @NotBlank(message = "Sub district is required")
    private String subDistrict;

    @NotNull
    @NotBlank(message = "City is required")
    private String city;

    @NotNull
    @NotBlank(message = "Country is required")
    private String country;

    @NotNull
    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "\\d{6}", message = "Pincode must be 6 digits")
    private String pincode;

    @NotEmpty(message = "LatLongs list cannot be empty")
    @Size(min = 2, message = "LatLongs list must contain two elements")
    private List<String> latLongs;

}

