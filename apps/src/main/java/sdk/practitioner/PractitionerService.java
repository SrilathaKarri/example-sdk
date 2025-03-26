package sdk.practitioner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.validation.Valid;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling practitioner-related operations such as retrieving, creating, updating, and deleting practitioners.
 */
@Service
@Validated
public class PractitionerService extends BaseService {

    private EhrApiError ehrApiError;

    /**
     * Constructor initializes ObjectMapper.
     *
     * @param webClientBuilder WebClient builder used to create WebClient instances.
     */
    public PractitionerService(WebClient.Builder webClientBuilder) {
        super(webClientBuilder);
    }

    /**
     * Retrieves a list of all practitioners.
     *
     * @return {@link ApiResponse} containing a list of practitioner profiles.
     * @throws RuntimeException if the request fails.
     */
    public Mono<Object> getAllPractitioners(String nextPage) {
        Map<String, String> queryParams = new HashMap<>();
        if (!StringUtils.isNullOrEmpty(nextPage)) {
            queryParams.put("nextPage", nextPage);
        }

        String endpoint = String.format("/get/%s", ResourceType.Practitioner.name());

        try {
            return get(
                    endpoint,
                    queryParams,
                    new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * Retrieves a practitioner's profile by their unique ID.
     *
     * @param id The unique identifier of the practitioner.
     * @return {@link ApiResponse} containing the practitioner's profile.
     * @throws ValidationError  if the ID is null or empty.
     * @throws RuntimeException if the request fails.
     */
    public Mono<Object> getPractitionerById(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("Patient ID cannot be null or empty."));
        }

        String endpoint = String.format("/get/%s/%s", ResourceType.Practitioner.name(), id);

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
     * Checks if a practitioner exists based on the provided ID.
     *
     * @param id The unique identifier of the practitioner.
     * @return {@code true} if the practitioner exists, otherwise {@code false}.
     */
    public Mono<Boolean> practitionerExists(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.just(false);
        }

        return getPractitionerById(id)
                .map(response -> {
                    if (response instanceof Map) {
                        Object message = ((Map<?, ?>) response).get("message");
                        return message != null && "Practitioner Found !!!".equals(message.toString());
                    }
                    return false;
                })
                .onErrorReturn(false);
    }

    /**
     * Creates a new practitioner.
     *
     * @param practitioner The {@link PractitionerDTO} object containing the practitioner's details.
     * @return {@link ApiResponse} containing the response after creation.
     * @throws ValidationError  if the practitioner data is null or required fields are missing.
     * @throws RuntimeException if the request fails.
     */
    public Mono<Object> createPractitioner(@Valid PractitionerDTO practitioner) {
        if (practitioner == null) {
            return Mono.error(new ValidationError("Practitioner data cannot be null."));
        }

        try {
            validatePractitioner(practitioner,false);

            String endpoint = String.format("/add/%s", ResourceType.Practitioner.name());

            return post(
                    endpoint,
                    practitioner,
                    new ParameterizedTypeReference<>() {});
        } catch (ValidationError ve) {
            return Mono.error(ve);
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * Updates an existing practitioner's details.
     *
     * @param updatePractitionerData The {@link PractitionerDTO} object containing updated practitioner information.
     * @return {@link ApiResponse} containing the response after the update.
     * @throws ValidationError  if the update data is null or required fields are missing.
     * @throws RuntimeException if the request fails.
     */
    public Mono<ApiResponse<Object>> updatePractitioner(@Valid PractitionerDTO updatePractitionerData) {
        if (updatePractitionerData == null) {
            return Mono.error(new ValidationError("Update practitioner data cannot be null."));
        }

        try {
            validatePractitioner(updatePractitionerData,false);

            String endpoint = String.format("/update/%s", ResourceType.Practitioner.name());

            return sendPutRequest(
                    endpoint,
                    updatePractitionerData,
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
     * Deletes a practitioner based on their unique ID.
     *
     * @param id The unique identifier of the practitioner to be deleted.
     * @return {@link ApiResponse} confirming the deletion.
     * @throws ValidationError  if the ID is null or empty.
     * @throws RuntimeException if the request fails.
     */
    public Mono<ApiResponse<Object>> deletePractitioner(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("Practitioner ID cannot be null or empty."));
        }

        String endpoint = String.format("/practitioners/%s", id);

        try {
            return sendDeleteRequest(
                    endpoint,
                    new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * Validates required fields for PractitionerDTO.
     *
     * @param practitioner The practitioner data to validate.
     * @param isUpdate     Whether this is an update operation.
     * @throws ValidationError if any required field is missing.
     */
    private void validatePractitioner(PractitionerDTO practitioner, boolean isUpdate) {
        if (isUpdate && StringUtils.isNullOrEmpty(practitioner.getResourceId())) {
            throw new ValidationError("Resource ID is required for updating a practitioner.");
        }

        if (StringUtils.isNullOrEmpty(practitioner.getRegistrationId())) {
            throw new ValidationError("Registration ID is required.");
        }
        if (StringUtils.isNullOrEmpty(practitioner.getDepartment())) {
            throw new ValidationError("Department is required.");
        }
        if (StringUtils.isNullOrEmpty(practitioner.getDesignation())) {
            throw new ValidationError("Designation is required.");
        }
        if (StringUtils.isNullOrEmpty(practitioner.getStatus())) {
            throw new ValidationError("Status is required.");
        }
        if (practitioner.getJoiningDate() == null) {
            throw new ValidationError("Joining Date is required.");
        }
        if (StringUtils.isNullOrEmpty(practitioner.getFirstName())) {
            throw new ValidationError("First Name is required.");
        }
        if (StringUtils.isNullOrEmpty(practitioner.getLastName())) {
            throw new ValidationError("Last Name is required.");
        }
        if (practitioner.getBirthDate() == null) {
            throw new ValidationError("Birth Date is required.");
        }
        if (practitioner.getGender() == null) {
            throw new ValidationError("Gender is required.");
        }
        if (!isValidEnum(practitioner.getGender(), Gender.class)) {
            throw new ValidationError("Invalid Gender.");
        }
        if (StringUtils.isNullOrEmpty(practitioner.getMobileNumber())) {
            throw new ValidationError("Mobile Number is required.");
        }
        if (StringUtils.isNullOrEmpty(practitioner.getEmailId())) {
            throw new ValidationError("Email ID is required.");
        }
        if (StringUtils.isNullOrEmpty(practitioner.getAddress())) {
            throw new ValidationError("Address is required.");
        }
        if (StringUtils.isNullOrEmpty(practitioner.getPincode())) {
            throw new ValidationError("Pincode is required.");
        }
        if (practitioner.getState() == null) {
            throw new ValidationError("State is required.");
        }
        if (!isValidEnum(practitioner.getState(), StatesAndUnionTerritories.class)) {
            throw new ValidationError("Invalid State.");
        }
        if (practitioner.getResourceType() == null) {
            throw new ValidationError("Resource Type is required.");
        }
        if (!isValidEnum(practitioner.getResourceType(), ResourceType.class)) {
            throw new ValidationError("Invalid Resource Type.");
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

    //    public Mono<ApiResponse<Object>> getPractitionersByFilters(String filtersJson, String nextPage) {
//        try {
//            // Parse the JSON filters string
//            Map<String, Object> filters = parseFilters(filtersJson);
//
//            // Create the query parameters mapl
//            Map<String, String> queryParams = new HashMap<>();
//
//            // Add the filters as a JSON string
//            queryParams.put("filters", filtersJson);
//
//            // Add nextPage parameter if provided
//            if (!StringUtils.isNullOrEmpty(nextPage)) {
//                queryParams.put("nextPage", nextPage);
//            }
//
//            String endpoint = "/health-lake/get-profiles/" + ResourceType.Practitioner.name();
//
//            return sendGetRequest1(endpoint, queryParams, new ParameterizedTypeReference<ApiResponse<Object>>() {});
//        } catch (Exception e) {
//            LogUtil.logger.error("Error processing practitioner filters: {}", e.getMessage(), e);
//            return Mono.error(EhrApiError.handleAndLogApiError(e));
//        }
//    }

    /**
     * Parses the filters JSON string and validates the content.
     *
     * @param filtersJson The JSON string containing filter criteria
     * @return A map of validated filter criteria
     * @throws ValidationError if the filters are invalid
     */
    private Map<String, Object> parseFilters(String filtersJson) {
        try {
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
            Map<String, Object> filters = objectMapper.readValue(filtersJson, typeRef);

            // Validate the filters
            validateFilters(filters);

            return filters;
        } catch (JsonProcessingException e) {
            throw new ValidationError("Invalid JSON format for filters: " + e.getMessage());
        }
    }

    /**
     * Validates the provided filters.
     *
     * @param filters The filters map to validate
     * @throws ValidationError if the filters are invalid
     */
    private void validateFilters(Map<String, Object> filters) {
        if (filters == null) {
            throw new ValidationError("Filters cannot be null.");
        }

        // Validate name if present
        if (filters.containsKey("name") && !(filters.get("name") instanceof String)) {
            throw new ValidationError("Name must be a string.");
        }

        // Validate family if present
        if (filters.containsKey("family") && !(filters.get("family") instanceof String)) {
            throw new ValidationError("Family must be a string.");
        }

        // Additional validation can be added here for other fields
    }
}

