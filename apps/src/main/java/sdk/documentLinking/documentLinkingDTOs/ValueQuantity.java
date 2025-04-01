package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a quantity with its unit and code, typically used for measurements in health records.
 * <p>
 * This class allows the representation of values like blood pressure, weight, temperature, etc., along with
 * their corresponding units and codes for standardization.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueQuantity {

    /**
     * The numerical value of the measurement (e.g., 120, 98.6).
     */
    private String value;

    /**
     * The unit of measurement (e.g., "mmHg", "kg", "°C").
     */
    private String unit;

    /**
     * The code representing the unit, following a standard coding system (e.g., Snomed code).
     */
    private String code;
}
