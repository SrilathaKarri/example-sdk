package sdk.facility.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FacilityTimings {

    /**
     * General description of facility operating hours in human-readable format.
     * Example: "Monday to Friday, 9 AM to 5 PM"
     */
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9\\s,:\\-()]+$",
            message = "Facility timings must contain only alphanumeric characters, spaces, commas, colons, hyphens, and parentheses")
    private String timings;

    @NotNull
    private List<Shift> shifts;

    @Data
    public static class Shift {
        private LocalDateTime start;
        private LocalDateTime end;
    }
}


