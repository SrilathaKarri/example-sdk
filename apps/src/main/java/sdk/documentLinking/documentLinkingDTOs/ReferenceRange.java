package sdk.documentLinking.documentLinkingDTOs;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a reference range for a specific measurement or observation,
 * typically used in clinical assessments to indicate normal value ranges.
 * <p>
 * This class includes validation to ensure that the upper bound is greater than the lower bound
 * when both are provided.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceRange {

    /**
     * The lower bound of the reference range.
     */
    private ValueQuantity low;

    /**
     * The upper bound of the reference range.
     */
    private ValueQuantity high;

    /**
     * Validates that when both the low and high values are provided,
     * the high value is greater than the low value.
     *
     * @return true if the range is valid, false otherwise
     */
    @AssertTrue(message = "High value must be greater than low value")
    private boolean isValidRange() {
        if (low != null && high != null
                && low.getValue() != null
                && high.getValue() != null) {
            return high.getValue().compareTo(low.getValue()) > 0;
        }
        return true;
    }
}
