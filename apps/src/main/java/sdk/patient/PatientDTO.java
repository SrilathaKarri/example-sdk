package sdk.patient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sdk.base.enums.Gender;
import sdk.base.enums.ResourceType;
import sdk.base.enums.StatesAndUnionTerritories;
import sdk.patient.enums.PatientIdType;
import sdk.patient.enums.PatientType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {
    @NotBlank(message = "ID Number is required")
    private String idNumber;

    @NotNull(message = "ID Type is required")
    private PatientIdType idType;

    @NotBlank(message = "ABHA Address is required")
    private String abhaAddress;

    @NotNull(message = "Patient Type is required")
    private PatientType patientType;

    @NotBlank(message = "First Name is required")
    @Size(min = 3, message = "First Name must be at least 3 characters long")
    private String firstName;

    private String middleName;  // Optional

    @NotBlank(message = "Last Name is required")
    @Size(min = 3, message = "Last Name must be at least 3 characters long")
    private String lastName;

    @NotBlank(message = "Birth Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Birth Date must be in YYYY-MM-DD format")
    private String birthDate;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "Email ID is required")
    @Email(message = "Invalid email format")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Invalid email format"
    )
    private String emailId;

    @NotBlank(message = "Mobile Number is required")
    @Pattern(regexp = "^[+]{1}91[987]\\d{9}$", message = "Mobile number must start with +91 followed by 9, 8, or 7 and be exactly 10 digits")
    private String mobileNumber;

    @NotBlank(message = "Address is required")
    @Size(min = 5, message = "Address must be at least 5 characters long")
    private String address;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "\\d{6}", message = "Pincode must be 6 digits")
    private String pincode;

    @NotNull(message = "State is required")
    private StatesAndUnionTerritories state;

    private Boolean wantsToLinkWhatsapp;  // Optional

    private String photo;  // Optional

    @NotNull(message = "Resource Type is required")
    private ResourceType resourceType;

    private String resourceId;  // Optional

}
