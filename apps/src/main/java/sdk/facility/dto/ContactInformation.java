package sdk.facility.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class ContactInformation {

    @NotBlank(message = "Mobile Number is required")
    @Pattern(regexp = "[987]\\d{9}",
            message = "Mobile number must start with 9, 8, or 7 and be exactly 10 digits")
    private String mobileNumber;

    @NotBlank(message = "Email ID is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "landline is required")
    @Pattern(regexp = "^[0-9]{6,12}$", message = "Please enter a valid landline number")
    private String landline;

    @NotBlank(message = "stdCode is required")
    @Pattern(regexp = "^[0-9]{2,5}$", message = "Please enter a valid STD code")
    private String stdcode;

    @NotBlank(message = "website link is required")
    @Pattern(
            regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
            message = "Please enter a valid website URL"
    )
    private String websiteLink;
}

