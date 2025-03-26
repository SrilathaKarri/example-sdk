package sdk.patient;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import sdk.base.BaseService;
import sdk.base.StringUtils;
import sdk.base.enums.Gender;
import sdk.base.enums.ResourceType;
import sdk.base.enums.StatesAndUnionTerritories;
import sdk.base.errors.EhrApiError;
import sdk.base.errors.ValidationError;
import sdk.base.response.ApiResponse;
import sdk.patient.enums.PatientIdType;
import sdk.patient.enums.PatientType;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for handling patient-related operations such as retrieving, creating, updating, and deleting patients.
 */
@Service
@Validated
public class Patient extends BaseService {

    @Autowired
    public Patient(WebClient.Builder webClientBuilder) {
        super(webClientBuilder);
    }

    private EhrApiError ehrApiError;

    /**
     * Retrieves a list of all patients.
     *
     * @return {@link ApiResponse} containing a list of patient profiles.
     * @throws RuntimeException if the request fails.
     */
    public Mono<Object> findAll(String nextPage) {
        Map<String, String> queryParams = new HashMap<>();
        if (!StringUtils.isNullOrEmpty(nextPage)) {
            queryParams.put("nextPage", nextPage);
        }

        String endpoint = String.format("/get/%s", ResourceType.Patient.name());

        try {
            return get(
                    endpoint,
                    queryParams,
                    new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

//    public Mono<Object> getAllPatients(Optional<String> pageSize, Optional<String> nextPage) {
//        Map<String, String> queryParams = new HashMap<>();
//
//        String countValue = pageSize.orElse("10");
//        String filtersJson = String.format("{\"_count\":%s}", countValue);
//        queryParams.put("filters", filtersJson);
//
//        nextPage.ifPresent(np -> queryParams.put("nextPage", np));
//
//        String endpoint = String.format("/health-lake/get-profiles/%s", ResourceType.Patient.name());
//
//        try {
//            return sendGetRequest1(
//                    endpoint,
//                    queryParams,
//                    new ParameterizedTypeReference<Object>() {}
//            );
//        } catch (Exception e) {
//            return Mono.error(EhrApiError.handleAndLogApiError(e));
//        }
//    }

    /**
     * Retrieves a patient's profile by their unique ID.
     *
     * @param id The unique identifier of the patient.
     * @return {@link ApiResponse} containing the patient's profile.
     * @throws ValidationError  if the ID is null or empty.
     * @throws RuntimeException if the request fails.
     */
    public Mono<Object> getPatientById(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("Patient ID cannot be null or empty."));
        }

        String endpoint = String.format("/get/%s/%s", ResourceType.Patient.name(), id);

        try {
            return get(
                    endpoint,
                    new HashMap<>(),
                    new ParameterizedTypeReference<Object>() {});
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * Checks if a patient exists based on the provided ID.
     *
     * @param id The unique identifier of the patient.
     * @return {@code true} if the patient exists, otherwise {@code false}.
     */
    public Mono<Boolean> patientExists(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.just(false);
        }

        return getPatientById(id)
                .map(response -> {
                    if (response instanceof Map) {
                        Object message = ((Map<?, ?>) response).get("message");
                        return message != null && "Patient Found !!!".equals(message.toString());
                    }
                    return false;
                })
                .onErrorReturn(false);
    }


    /**
     * Creates a new patient.
     *
     * @param patient The {@link PatientDTO} object containing the patient's details.
     * @return {@link ApiResponse} containing the response after creation.
     * @throws ValidationError  if the patient data is null.
     * @throws RuntimeException if the request fails.
     */
    public Mono<Object> createPatient(@Valid PatientDTO patient) {
        if (patient == null) {
            return Mono.error(new ValidationError("Patient data cannot be null."));
        }

        try {
            validatePatientFields(patient);

            String endpoint = String.format("/add/%s", ResourceType.Patient.name());

            return post(
                    endpoint,
                    patient,
                    new ParameterizedTypeReference<>() {});
        } catch (ValidationError ve) {
            return Mono.error(ve);
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }


    /**
     * Updates an existing patient's details.
     *
     * @param updatePatientData The {@link UpdatePatientDTO} object containing updated patient information.
     * @return {@link ApiResponse} containing the response after the update.
     * @throws ValidationError  if the update data is null.
     * @throws RuntimeException if the request fails.
     */
    public Mono<ApiResponse<Object>> updatePatient(@Valid UpdatePatientDTO updatePatientData) {
        if (updatePatientData == null) {
            return Mono.error(new ValidationError("Update patient data cannot be null."));
        }

        try {
            validateUpdatePatientFields(updatePatientData);

            String endpoint = String.format("/update/%s", ResourceType.Patient.name());

            return sendPutRequest(
                    endpoint,
                    updatePatientData,
                    new ParameterizedTypeReference<>() {}).map(apiResponse -> {
                if (apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                    apiResponse.setData(apiResponse.getData());
                } else {
                    apiResponse.setData(List.of());
                }
                return apiResponse;
            });
        } catch (ValidationError ve) {
            return Mono.error(ve);
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * Deletes a patient based on their unique ID.
     *
     * @param id The unique identifier of the patient to be deleted.
     * @return {@link ApiResponse} confirming the deletion.
     * @throws ValidationError  if the ID is null or empty.
     * @throws RuntimeException if the request fails.
     */
    public Mono<ApiResponse<Object>> deletePatient(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("Patient ID cannot be null or empty."));
        }

        String endpoint = String.format("/patients/%s", id);

        try {
            return sendDeleteRequest(
                    endpoint,
                    new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }


//    public Mono<ApiResponse<Object>> getPatientByFilters(PatientFiltersDTO filters, String nextPage) {
//        try {
//            validateFilters(filters);
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//
//            String filtersJson = objectMapper.writeValueAsString(transformFilterKeys(filters));
//
//
//            StringBuilder finalUrl = new StringBuilder("/health-lake/get-profiles/Patient");
//
//            if (nextPage != null && !nextPage.isEmpty()) {
//                finalUrl.append("&nextPage=").append(nextPage);
//            }
//
//            System.out.println("Final Request URL: " + finalUrl);
//
//            return sendGetRequest1(finalUrl.toString(), filtersJson, new ParameterizedTypeReference<ApiResponse<Object>>() {});
//        } catch (Exception e) {
//            return Mono.error(EhrApiError.handleAndLogApiError(e));
//        }
//    }


    /**
         * Transforms filter keys to match the API's expected format.
         *
         * @param filters The filters to transform.
         * @return A map with transformed filter keys.
         */
        private Map<String, String> transformFilterKeys(PatientFiltersDTO filters) {
            Map<String, String> updatedFilters = new HashMap<>();

            if (filters.getFirstName() != null) {
                updatedFilters.put("name", filters.getFirstName());
            }
            if (filters.getLastName() != null) {
                updatedFilters.put("family", filters.getLastName());
            }
            if (filters.getState() != null) {
                updatedFilters.put("address-state", filters.getState());
            }
            if (filters.getCount() != null) {
                updatedFilters.put("_count", filters.getCount().toString());
            }

            return updatedFilters;
        }

        /**
         * Validates the provided filters.
         *
         * @param filters The filter object to validate.
         */
        private void validateFilters(PatientFiltersDTO filters) {
            if (filters == null) {
                throw new ValidationError("Filters cannot be null.");
            }
        }



    private void validatePatientFields(PatientDTO patient) {
        if (StringUtils.isNullOrEmpty(patient.getIdNumber())) {
            throw new ValidationError("ID Number is required.");
        }
        if (patient.getIdType() == null) {
            throw new ValidationError("ID Type is required.");
        }
        if (!isValidEnum(patient.getIdType(), PatientIdType.class)) {
            throw new ValidationError("Invalid ID Type.");
        }
        if (StringUtils.isNullOrEmpty(patient.getFirstName())) {
            throw new ValidationError("First Name is required.");
        }
        if (StringUtils.isNullOrEmpty(patient.getLastName())) {
            throw new ValidationError("Last Name is required.");
        }
        if (StringUtils.isNullOrEmpty(patient.getBirthDate())) {
            throw new ValidationError("Birth Date is required.");
        }
        if (patient.getGender() == null) {
            throw new ValidationError("Gender is required.");
        }
        if (!isValidEnum(patient.getGender(), Gender.class)) {
            throw new ValidationError("Invalid Gender.");
        }
        if (StringUtils.isNullOrEmpty(patient.getMobileNumber())) {
            throw new ValidationError("Mobile Number is required.");
        }
        if (StringUtils.isNullOrEmpty(patient.getAddress())) {
            throw new ValidationError("Address is required.");
        }
        if (StringUtils.isNullOrEmpty(patient.getPincode())) {
            throw new ValidationError("Pincode is required.");
        }
        if (patient.getState() == null) {
            throw new ValidationError("State is required.");
        }
        if (!isValidEnum(patient.getState(), StatesAndUnionTerritories.class)) {
            throw new ValidationError("Invalid State.");
        }
        if (patient.getPatientType() == null) {
            throw new ValidationError("Patient Type is required.");
        }
        if (!isValidEnum(patient.getPatientType(), PatientType.class)) {
            throw new ValidationError("Invalid Patient Type.");
        }
    }

    private void validateUpdatePatientFields(UpdatePatientDTO updatePatientData) {
        if (updatePatientData.getResourceId() == null || updatePatientData.getResourceId().isEmpty()) {
            throw new ValidationError("Resource ID is required for updating patient.");
        }
        if (updatePatientData.getMobileNumber() != null && !updatePatientData.getMobileNumber().matches("\\d{10}")) {
            throw new ValidationError("Mobile Number must be exactly 10 digits.");
        }
        if (updatePatientData.getEmailId() != null && !updatePatientData.getEmailId().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationError("Invalid Email format.");
        }
    }

    private <T extends Enum<T>> boolean isValidEnum(Enum<?> value, Class<T> enumClass) {
        for (T constant : enumClass.getEnumConstants()) {
            if (constant.name().equals(value.name())) {
                return true;
            }
        }
        return false;
    }
}
