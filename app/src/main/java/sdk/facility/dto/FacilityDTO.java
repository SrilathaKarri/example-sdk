package sdk.facility.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityDTO {

    @NotNull(message = "Basic information is required.")
    @Valid
    private BasicInformation basicInformation;

    @NotNull(message = "Contact information is required.")
    @Valid
    private ContactInformation contactInformation;

    @NotNull(message = "Upload documents are required.")
    @Valid
    private UploadDocuments uploadDocuments;

    @NotNull(message = "At least one address proof is required.")
    @Valid
    @JsonProperty("addAddressProof")
    private List<AddressProof> addressProof;

    @NotNull(message = "Facility timings are required.")
    @Valid
    private List<FacilityTimings> facilityTimings;

    @NotNull(message = "Facility details are required.")
    @Valid
    private FacilityDetails facilityDetails;

    @NotNull(message = "System of medicine is required.")
    @Valid
    private SystemOfMedicine systemOfMedicine;

    @NotNull(message = "Facility inventory details are required.")
    @Valid
    private FacilityInventory facilityInventory;

    @NotNull(message = "Account ID is required.")
    private String accountId;

    private String facilityId;

    private String id;


}
