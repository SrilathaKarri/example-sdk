package sdk.facility.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class Speciality {

    @NotNull
    @JsonProperty("systemofMedicineCode")
    private String systemOfMedicineCode;

    @NotNull
    @Size(min = 1, message = "At least one speciality must be provided")
    private List<String> specialities;

}
