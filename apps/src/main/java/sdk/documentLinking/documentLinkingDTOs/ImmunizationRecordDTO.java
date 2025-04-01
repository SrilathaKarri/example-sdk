package sdk.documentLinking.documentLinkingDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a record of immunizations administered to a patient.
 * <p>
 * This DTO holds a list of immunization records, which can include
 * various details such as vaccine types, dates of administration, and more.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImmunizationRecordDTO {

    /**
     * A list of immunization records, where each record can contain details
     * like vaccine code, administration date, lot number, etc.
     * This field can hold generic objects representing different immunization formats.
     */
    private List<Object> immunizations;
}
