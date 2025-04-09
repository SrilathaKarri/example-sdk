package sdk.facility.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple6;
import reactor.util.function.Tuples;
import sdk.base.errors.EhrApiError;
import sdk.base.errors.ErrorType;
import sdk.base.utils.LogUtil;
import sdk.facility.dto.ResponseDTOs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FacilityDemographic {

    @Autowired
    private Validation validation;

    public Mono<ResponseDTOs.FacilityDemographicsDTO> getDemographicData() {
        return getStateAndDistrictInfo()
                .flatMap(stateDistrictInfo -> {
                    String stateCode = stateDistrictInfo.getT1();
                    String districtCode = stateDistrictInfo.getT2();

                    return getSubDistrictAndRegionInfo(districtCode)
                            .flatMap(subDistrictRegionInfo -> {
                                String subDistrictCode = subDistrictRegionInfo.getT1();
                                String regionCode = subDistrictRegionInfo.getT2();

                                return validation.fetchAndPromptLocation()
                                        .flatMap(latLong -> {
                                            String latitude = latLong.getT1();
                                            String longitude = latLong.getT2();

                                            return getAddressAndOwnershipInfo()
                                                    .flatMap(addressOwnershipInfo -> {
                                                        String addressProofCode = addressOwnershipInfo.getT1();
                                                        String ownershipTypeCode = addressOwnershipInfo.getT2();
                                                        String ownershipSubTypeCode = addressOwnershipInfo.getT3();
                                                        String facilityStatusTypeCode = addressOwnershipInfo.getT4();

                                                        return getMedicineAndSpecialityInfo()
                                                                .flatMap(medicineSpecialityInfo -> {
                                                                    String systemOfMedicineCode = medicineSpecialityInfo.getT1();
                                                                    List<String> specialityCodes = medicineSpecialityInfo.getT2();

                                                                    return getFacilityTypeInfo(ownershipTypeCode)
                                                                            .flatMap(facilityTypeInfo -> {
                                                                                String facilityTypeCode = facilityTypeInfo.getT1();
                                                                                String facilitySubTypeCode = facilityTypeInfo.getT2();

                                                                                return getServiceAndGeneralInfo()
                                                                                        .map(serviceGeneralInfo -> buildFacilityDemographicsDTO(
                                                                                                stateCode, districtCode, subDistrictCode, regionCode,latitude, longitude,
                                                                                                addressProofCode, ownershipTypeCode, ownershipSubTypeCode,
                                                                                                facilityStatusTypeCode, systemOfMedicineCode, specialityCodes,
                                                                                                facilityTypeCode, facilitySubTypeCode, serviceGeneralInfo
                                                                                        ));
                                                                            });
                                                                });
                                                    });
                                        });
                            });
                });
    }



    private Mono<reactor.util.function.Tuple2<String, String>> getStateAndDistrictInfo() {
        return fetchAndPromptState()
                .flatMap(stateCode -> fetchAndPromptDistrict(stateCode)
                        .map(districtCode -> Tuples.of(stateCode, districtCode))
                );
    }

    private Mono<String> fetchAndPromptState() {
        return validation.fetchStates()
                .filter(states -> !states.isEmpty())
                .switchIfEmpty(Mono.error(new EhrApiError("No states found.", ErrorType.NOT_FOUND)))
                .doOnNext(states -> {
                    String stateList = states.stream()
                            .map(ResponseDTOs.StateDTO::getName)
                            .collect(Collectors.joining("\n"));
                    LogUtil.logger.info("\nAvailable States:\n{}", stateList);
                })
                .flatMap(validation::promptForValidState);
    }


    private Mono<String> fetchAndPromptDistrict(String stateCode) {
        return validation.fetchStates()
                .flatMap(states -> validation.getDistrictsFromState(stateCode, states))
                .filter(districts -> !districts.isEmpty())
                .switchIfEmpty(Mono.error(new EhrApiError("No districts found for the selected state.",ErrorType.NOT_FOUND)))
                .doOnNext(districts -> {
                    String districtList = districts.stream()
                            .map(ResponseDTOs.DistrictDTO::getName)
                            .collect(Collectors.joining("\n"));
                    LogUtil.logger.info("\nAvailable Districts:\n{}", districtList);
                })
                .flatMap(validation::promptForValidDistrict);
    }

    private Mono<reactor.util.function.Tuple2<String, String>> getSubDistrictAndRegionInfo(String districtCode) {
        if (!districtCode.matches("\\d+")) {
            return Mono.error(new EhrApiError("Invalid district code: must be a numeric value.",ErrorType.VALIDATION));
        }

        int districtId = Integer.parseInt(districtCode);
        return validation.fetchSubDistricts(districtId)
                .flatMap(subDistricts -> {
                    if (subDistricts.isEmpty()) {
                        return Mono.error(new EhrApiError("No sub-districts found for the selected district.", ErrorType.NOT_FOUND));
                    }

                    String subDistrictList = subDistricts.stream()
                            .map(ResponseDTOs.DistrictDTO::getName)
                            .collect(Collectors.joining("\n"));

                    LogUtil.logger.info("\nAvailable Sub-Districts:\n{}", subDistrictList);

                    return validation.promptForValidSubDistrict(subDistricts)
                            .flatMap(subDistrictCode -> validation.promptForValidRegion()
                                    .map(regionCode -> Tuples.of(subDistrictCode, regionCode))
                            );
                });
    }

    private Mono<reactor.util.function.Tuple4<String, String, String, String>> getAddressAndOwnershipInfo() {
        return fetchAndPromptAddressProof()
                .flatMap(addressProofCode ->
                        fetchAndPromptOwnershipType()
                                .flatMap(ownershipTypeCode ->
                                        fetchAndPromptOwnershipSubType(ownershipTypeCode)
                                                .flatMap(ownershipSubTypeCode ->
                                                        fetchAndPromptFacilityStatusType()
                                                                .map(facilityStatusTypeCode ->
                                                                        Tuples.of(
                                                                                addressProofCode, ownershipTypeCode,
                                                                                ownershipSubTypeCode, facilityStatusTypeCode))
                                                )
                                )
                );
    }

    private Mono<String> fetchAndPromptAddressProof() {
        return validation.fetchValidAddressProofTypes()
                .doOnNext(addressProofs -> {
                    String formattedAddressProofs = String.join("\n", addressProofs.values());

                    LogUtil.logger.info("\nAvailable Address Proof Types:\n{}", formattedAddressProofs);
                })
                .flatMap(validation::promptForValidAddressProof);
    }


    private Mono<String> fetchAndPromptOwnershipType() {
        return validation.fetchValidOwnershipTypes()
                .doOnNext(ownershipTypes -> {
                    String formattedOwnershipTypes = String.join("\n", ownershipTypes.values());
                    LogUtil.logger.info("\nAvailable Ownership Types:\n{}", formattedOwnershipTypes);
                })
                .flatMap(validation::promptForValidOwnershipType);
    }

    private Mono<String> fetchAndPromptOwnershipSubType(String ownershipTypeCode) {
        return validation.fetchValidOwnershipSubTypes(ownershipTypeCode)
                .doOnNext(ownershipSubTypes -> {
                    String formattedOwnershipSubTypes = String.join("\n", ownershipSubTypes.values());
                    LogUtil.logger.info("\nAvailable Ownership Sub Types:\n{}", formattedOwnershipSubTypes);
                })
                .flatMap(validation::promptForValidOwnershipSubType);
    }

    private Mono<String> fetchAndPromptFacilityStatusType() {
        return validation.fetchValidFacilityStatusTypes()
                .doOnNext(facilityStatusTypes -> {
                    String formattedFacilityStatusTypes = String.join("\n", facilityStatusTypes.values());
                    LogUtil.logger.info("\nAvailable Facility Status Types:\n{}", formattedFacilityStatusTypes);
                })
                .flatMap(validation::promptForValidFacilityStatusType);
    }

    private Mono<reactor.util.function.Tuple2<String, List<String>>> getMedicineAndSpecialityInfo() {
        return validation.fetchValidSystemOfMedicineCodes()
                .flatMap(systemOfMedicines -> {
                    String formattedSystemOfMedicineValues = String.join("\n", systemOfMedicines.values());
                    LogUtil.logger.info("\nSystem Of Medicines:\n{}", formattedSystemOfMedicineValues);
                    return validation.promptForValidSystemOfMedicine(systemOfMedicines);
                })
                .flatMap(systemOfMedicineCode -> validation.fetchSpecialitiesForMedicine(systemOfMedicineCode)
                        .flatMap(specialities -> {
                            List<String> specialityNames = new ArrayList<>(specialities.values());
                            LogUtil.logger.info("\nSpecialities: {}", String.join(", ", specialityNames));
                            return validation.promptForValidSpecialities(specialities)
                                    .map(specialityCodes -> Tuples.of(systemOfMedicineCode, specialityCodes));
                        })
                );
    }

    private Mono<reactor.util.function.Tuple2<String, String>> getFacilityTypeInfo(String ownershipTypeCode) {
        return validation.fetchValidFacilityTypes(ownershipTypeCode)
                .flatMap(facilityTypes -> {
                    String formattedFacilityTypes = String.join("\n", facilityTypes.values());
                    LogUtil.logger.info("\nFacility Types:\n{}", formattedFacilityTypes);
                    return validation.promptForValidFacilityType(facilityTypes);
                })
                .flatMap(facilityTypeCode -> validation.fetchValidFacilitySubTypes(facilityTypeCode)
                        .flatMap(facilitySubTypes -> {
                            String formattedFacilitySubTypes = String.join("\n", facilitySubTypes.values());
                            LogUtil.logger.info("\nFacility Sub Types:\n{}", formattedFacilitySubTypes);
                            return validation.promptForValidFacilitySubType(facilitySubTypes)
                                    .map(facilitySubTypeCode -> Tuples.of(facilityTypeCode, facilitySubTypeCode));
                        })
                );
    }


    private Mono<reactor.util.function.Tuple8<String, String, String, String, String, String, String, String>> getServiceAndGeneralInfo() {
        return validation.fetchValidServiceTypes()
                .flatMap(serviceTypes -> {
                    String formattedServiceTypes = String.join("\n", serviceTypes.values());
                    LogUtil.logger.info("\nType Services:\n{}", formattedServiceTypes);
                    return validation.promptForValidServiceType(serviceTypes);
                })
                .flatMap(typeServiceCode -> validation.fetchValidGeneralInfoOptions()
                        .flatMap(generalOptions -> collectGeneralInformation(generalOptions)
                                .flatMap(generalInfo -> handleImagingCenterSelection(typeServiceCode, generalInfo)))
                );
    }

    private Mono<reactor.util.function.Tuple8<String, String, String, String, String, String, String, String>> handleImagingCenterSelection(
            String typeServiceCode, Tuple6<String, String, String, String, String, String> generalInfo) {

        String hasImagingCenterCode = generalInfo.getT6();

        if ("N".equalsIgnoreCase(Objects.toString(hasImagingCenterCode, ""))) {
            return Mono.just(Tuples.of(
                    typeServiceCode,
                    generalInfo.getT1(), // hasDialysisCenterCode
                    generalInfo.getT2(), // hasPharmacyCode
                    generalInfo.getT3(), // hasBloodBankCode
                    generalInfo.getT4(), // hasCathLabCode
                    generalInfo.getT5(), // hasDiagnosticLabCode
                    hasImagingCenterCode, // hasImagingCenterCode (null-safe)
                    ""                // serviceByImagingCenterCode
            ));
        } else {
            return validation.fetchValidImagingCenterServices()
                    .flatMap(imagingOptions -> {
                        String formattedImagingServices = String.join("\n", imagingOptions.values());
                        LogUtil.logger.info("\nImaging Services:\n{}", formattedImagingServices);
                        return validation.promptForValidImagingService(imagingOptions);
                    })
                    .map(serviceByImagingCenterCode -> Tuples.of(
                            typeServiceCode,
                            generalInfo.getT1(), // hasDialysisCenterCode
                            generalInfo.getT2(), // hasPharmacyCode
                            generalInfo.getT3(), // hasBloodBankCode
                            generalInfo.getT4(), // hasCathLabCode
                            generalInfo.getT5(), // hasDiagnosticLabCode
                            Objects.toString(hasImagingCenterCode, ""),  // hasImagingCenterCode
                            serviceByImagingCenterCode
                    ));
        }
    }

    private Mono<Tuple6<String, String, String, String, String, String>> collectGeneralInformation(Map<String, String> generalOptions) {
        LogUtil.logger.info("\nAvailable Options:\n{}", generalOptions.entrySet().stream()
                .map(entry -> entry.getKey() + " → " + entry.getValue())
                .collect(Collectors.joining("\n")));
        return validation.promptForValidGeneralOption("Do you have Dialysis Center?", generalOptions)
                .flatMap(dialysis -> validation.promptForValidGeneralOption("Do you have Pharmacy?", generalOptions)
                        .flatMap(pharmacy -> validation.promptForValidGeneralOption("Do you have Blood bank?", generalOptions)
                                .flatMap(bloodBank -> validation.promptForValidGeneralOption("Do you have Cath lab?", generalOptions)
                                        .flatMap(cathLab -> validation.promptForValidGeneralOption("Do you have Diagnostic lab?", generalOptions)
                                                .flatMap(diagnosticLab -> validation.promptForValidGeneralOption("Do you have Imaging center?", generalOptions)
                                                        .map(imaging -> Tuples.of(dialysis, pharmacy, bloodBank, cathLab, diagnosticLab, imaging))
                                                )))));
    }

    /**
     * Builds the FacilityDemographicsDTO from all collected information.
     *
     * @param stateCode State code
     * @param districtCode District code
     * @param subDistrictCode Sub-district code
     * @param regionCode Region code
     * @param addressProofCode Address proof code
     * @param ownershipTypeCode Ownership type code
     * @param ownershipSubTypeCode Ownership subtype code
     * @param facilityStatusTypeCode Facility status code
     * @param systemOfMedicineCode System of medicine code
     * @param specialityCodes List of speciality codes
     * @param facilityTypeCode Facility type code
     * @param facilitySubTypeCode Facility subtype code
     * @param serviceInfo Tuple containing service type and general information
     * @return The built FacilityDemographicsDTO
     */
    private ResponseDTOs.FacilityDemographicsDTO buildFacilityDemographicsDTO(
            String stateCode, String districtCode, String subDistrictCode, String regionCode,
            String latitude, String longitude, String addressProofCode, String ownershipTypeCode,
            String ownershipSubTypeCode, String facilityStatusTypeCode, String systemOfMedicineCode,
            List<String> specialityCodes, String facilityTypeCode, String facilitySubTypeCode,
            reactor.util.function.Tuple8<String, String, String, String, String, String, String, String> serviceInfo) {

        ResponseDTOs.FacilityDemographicsDTO dto = ResponseDTOs.FacilityDemographicsDTO.builder()
                .stateCode(stateCode)
                .districtCode(districtCode)
                .subDistrictCode(subDistrictCode)
                .regionCode(regionCode)
                .latitude(latitude)
                .longitude(longitude)
                .addressProofCode(addressProofCode)
                .ownershipTypeCode(ownershipTypeCode)
                .ownershipSubTypeCode(ownershipSubTypeCode)
                .facilityStatusCode(facilityStatusTypeCode)
                .systemOfMedicineCode(systemOfMedicineCode)
                .specialityCodes(specialityCodes)
                .facilityTypeCode(facilityTypeCode)
                .facilitySubTypeCode(facilitySubTypeCode)
                .typeServiceCode(serviceInfo.getT1())
                .hasDialysisCenterCode(serviceInfo.getT2())
                .hasPharmacyCode(serviceInfo.getT3())
                .hasBloodBankCode(serviceInfo.getT4())
                .hasCathLabCode(serviceInfo.getT5())
                .hasDiagnosticLabCode(serviceInfo.getT6())
                .hasImagingCenterCode(serviceInfo.getT7())
                .serviceByImagingCenterCode(serviceInfo.getT8())
                .build();

        LogUtil.logger.info("\nFacilityDemographicsDTO Response:\n{}", dto);
        return dto;
    }

}
