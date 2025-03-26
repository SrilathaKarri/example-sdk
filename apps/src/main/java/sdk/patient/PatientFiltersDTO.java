package sdk.patient;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for patient filtering criteria
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientFiltersDTO {

    private String firstName;

    private String lastName;

    private String gender;

    private String state;

    @Pattern(regexp = "\\d{10}", message = "Mobile Number must be exactly 10 digits")
    private String mobileNumber;

    private String count;

    private String birthDate;

    private String emailId;

    private String identifier;

    private String rawFilters;



}