package sdk.facility.enums;

import lombok.Getter;
import sdk.base.errors.EhrApiError;
import sdk.base.errors.ErrorType;

import java.util.Arrays;

@Getter
public enum FacilityIdType {
    ACCOUNT_ID("accountId"),
    FACILITY_ID("facilityId"),
    ID("id");

    private final String value;

    FacilityIdType(String facilityIdType) {
        this.value = facilityIdType;
    }

    /**
     * Converts a string representation of Facility ID type to the corresponding enum.
     *
     * @param idType the string representation of the facility ID type
     * @return the corresponding FacilityIdType enum
     * @throws EhrApiError if the provided idType is null, empty, or invalid
     * @note The matching is case-insensitive and works with both enum names and their string values
     * <p><b>Usage Example:</b></p>
     * <pre>
     *     FacilityIdType type = FacilityIdType.fromString("accountId");
     *     System.out.println(type); // Outputs: ACCOUNT_ID
     * </pre>
     */
    public static FacilityIdType fromString(String idType) {
        if (idType == null || idType.trim().isEmpty()) {
            throw new EhrApiError("Facility ID Type cannot be null or empty.", ErrorType.VALIDATION);
        }

        return Arrays.stream(FacilityIdType.values())
                .filter(type -> type.name().equalsIgnoreCase(idType) || type.getValue().equalsIgnoreCase(idType))
                .findFirst()
                .orElseThrow(() -> new EhrApiError(String.format("Invalid Facility ID Type: %s", idType),ErrorType.NOT_FOUND));
    }

}
