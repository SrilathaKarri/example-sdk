package sdk.practitioner;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sdk.base.enums.Gender;
import sdk.base.enums.StatesAndUnionTerritories;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PractitionerFiltersDTO {

    @Pattern(regexp = "^[a-zA-Z.]+$", message = "First name can only contain letters and periods.")
    @NotEmpty(message = "First name cannot be empty.")
    @Size(min = 3, max = 20, message = "First name must be between 3 and 20 characters.")
    private String firstName;

    @Pattern(regexp = "^[a-zA-Z.]+$", message = "Last name can only contain letters and periods.")
    @Size(min = 3, max = 20, message = "Last name must be between 3 and 20 characters.")
    private String lastName;

    private Gender gender;

    @Pattern(regexp = "^[+\\d]*$", message = "Phone number is not valid.")
    private String phone;

    private StatesAndUnionTerritories state;

    private String organization;

    @Size(min = 1, message = "Count must be at least 1.")
    private String count;

    private String identifier;
}
