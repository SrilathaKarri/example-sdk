package sdk.facility.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SystemOfMedicine {

    @NotNull
    private List<Speciality> specialities;

    @NotNull
    private String facilityType;

    @NotNull
    private String facilitySubType;

    @NotNull
    private String serviceType;

}

