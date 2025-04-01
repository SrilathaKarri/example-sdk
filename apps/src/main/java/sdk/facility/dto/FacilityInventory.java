package sdk.facility.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FacilityInventory {

    @NotNull
    @Min(value = 0, message = "Total number of ventilators cannot be negative")
    private int totalNumberOfVentilators;

    @NotNull
    @Min(value = 0, message = "Total number of beds cannot be negative")
    private int totalNumberOfBeds;

    @NotNull
    private String hasDialysisCenter;

    @NotNull
    private String hasPharmacy;

    @NotNull
    private String hasBloodBank;

    @NotNull
    private String hasCathLab;

    @NotNull
    private String hasDiagnosticLab;

    @NotNull
    private List<ImagingCenterServiceType> servicesByImagingCenter;

    @NotNull
    private String nhrrid;

    @NotNull
    private String nin;

    @NotNull
    private String abpmjayid;

    @NotNull
    private String rohiniId;

    @NotNull
    private String echsId;

    @NotNull
    private String cghsId;

    @NotNull
    private String ceaRegistration;

    @NotNull
    private String stateInsuranceSchemeId;

}

