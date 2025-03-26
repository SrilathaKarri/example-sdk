package sdk.patient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sdk.base.enums.ResourceType;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePatientDTO {
        @NotBlank(message = "Resource ID is required")
        private String resourceId;

        @Email(message = "Invalid email format")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "Invalid email format"
        )
        private String emailId;  // Optional

        @Pattern(regexp = "\\d{10}", message = "Mobile number must be 10 digits")
        private String mobileNumber;  // Optional

        @NotNull(message = "Resource Type is required")
        private ResourceType resourceType;
}
