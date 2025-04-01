package sdk.facility.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class ResponseDTOs {

    @Data
    public static class StateDTO {
        private String code;
        private String name;
        private List<DistrictDTO> districts;
    }

    @Data
    public static class DistrictDTO {
        private String code;
        private String name;
    }

    @Data
    public static class TypeDataResponseDTO {
        private String type;
        private List<CodeValueDTO> data;
    }

    @Data
    public static class CodeValueDTO {
        private String code;
        private String value;
    }

    @Data
    public static class LocationResponseDTO {
        private List<LocationResultDTO> results;
        private String status;
    }

    @Data
    public static class LocationResultDTO {
        private GeometryDTO geometry;
    }

    @Data
    public static class GeometryDTO {
        private LocationDTO location;
    }

    @Data
    public static class LocationDTO {
        private double lat;
        private double lng;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacilityDemographicsDTO {
        private String stateCode;
        private String districtCode;
        private String subDistrictCode;
        private String regionCode;
        private String latitude;
        private String longitude;
        private String addressProofCode;
        private String ownershipTypeCode;
        private String ownershipSubTypeCode;
        private String facilityStatusCode;
        private String systemOfMedicineCode;
        private List<String> specialityCodes;
        private String facilityTypeCode;
        private String facilitySubTypeCode;
        private String typeServiceCode;
        private String hasDialysisCenterCode;
        private String hasPharmacyCode;
        private String hasBloodBankCode;
        private String hasCathLabCode;
        private String hasDiagnosticLabCode;
        private String hasImagingCenterCode;
        private String serviceByImagingCenterCode;

        @Override
        public String toString() {
            return "stateCode: " + stateCode + "\n" +
                    "districtCode: " + districtCode + "\n" +
                    "subDistrictCode: " + subDistrictCode + "\n" +
                    "regionCode: " + regionCode + "\n" +
                    "latitude: " + latitude + "\n" +
                    "longitude: " + longitude + "\n"+
                    "addressProofCode: " + addressProofCode + "\n" +
                    "ownershipTypeCode: " + ownershipTypeCode + "\n" +
                    "ownershipSubTypeCode: " + ownershipSubTypeCode + "\n" +
                    "facilityStatusCode: " + facilityStatusCode + "\n" +
                    "systemOfMedicineCode: " + systemOfMedicineCode + "\n" +
                    "specialityCodes: " + specialityCodes + "\n" +
                    "facilityTypeCode: " + facilityTypeCode + "\n" +
                    "facilitySubTypeCode: " + facilitySubTypeCode + "\n" +
                    "typeServiceCode: " + typeServiceCode + "\n" +
                    "hasDialysisCenterCode: " + hasDialysisCenterCode + "\n" +
                    "hasPharmacyCode: " + hasPharmacyCode + "\n" +
                    "hasBloodBankCode: " + hasBloodBankCode + "\n" +
                    "hasCathLabCode: " + hasCathLabCode + "\n" +
                    "hasDiagnosticLabCode: " + hasDiagnosticLabCode + "\n" +
                    "hasImagingCenterCode: " + hasImagingCenterCode + "\n" +
                    "serviceByImagingCenterCode: " + serviceByImagingCenterCode + "\n";
        }

    }

}
