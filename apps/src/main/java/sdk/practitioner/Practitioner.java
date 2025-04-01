package sdk.practitioner;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import sdk.base.Base;
import sdk.base.DTO.SearchFiltersDTO;
import sdk.base.config.CacheConfig;
import sdk.base.enums.Gender;
import sdk.base.enums.ResourceType;
import sdk.base.enums.StatesAndUnionTerritories;
import sdk.base.errors.EhrApiError;
import sdk.base.errors.ValidationError;
import sdk.base.utils.Constants;
import sdk.base.utils.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <h2>Practitioner Service</h2>
 * Provides methods to perform CRUD operations on practitioners in the system.
 * This service communicates with an external EHR system using {@link WebClient}.
 */
@Service
@Validated
public class Practitioner extends Base {


    /**
     * * Constructs a new instance of {@link Practitioner}.
     *
     * @param webClient WebClient builder used to create WebClient instances.
     */

    protected Practitioner(ObjectMapper objectMapper, WebClient webClient) {
        super(objectMapper, webClient);
    }
    /**
     * <h3>
     *    Retrieve a Paginated List of Practitioners
     * </h3>
     * Fetches a list of practitioners with optional pagination parameters.
     * The results are retrieved from the practitioner resource endpoint.
     *
     * <h3>Pagination:</h3>
     * <ul>
     *     <li><b>pageSize:</b> (Optional) Specifies the number of records per page. If null, defaults to 10.</li>
     *     <li><b>nextPage:</b> (Optional) A token used to fetch the next page of results.</li>
     * </ul>
     *
     * <h3>Response Format:</h3>
     * The response is a {@link Mono<Object>} containing the list of practitioners.
     * The format of the response depends on the API but typically includes:
     * <ul>
     *     <li>A list of practitioners.</li>
     *     <li>A <code>nextPage</code> token, if more results are available.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * practitionerService.getAllPractitioners(20, null)
     *     .subscribe(response -> System.out.println("Practitioners: " + response));
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * If an error occurs during the request (e.g., network failure, API error), it will be logged,
     * and the method will return a {@link Mono#error(Throwable)}.
     *
     * @param pageSize The number of records per page (nullable, defaults to 10 if not provided).
     * @param nextPage The pagination token for fetching the next set of results (nullable).
     * @return {@link Mono<Object>} containing the paginated list of practitioners.
     */
    public Mono<Object> findAll(Integer pageSize, String nextPage) {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("_count", (pageSize != null) ? pageSize : 10);

            String encodedFilters = URLEncoder.encode(objectMapper.writeValueAsString(filters), StandardCharsets.UTF_8);

            String url = String.format("%s/%s?filters=%s%s",
                    Constants.GET_PROFILES_URL,
                    ResourceType.Practitioner,
                    encodedFilters,
                    Optional.ofNullable(nextPage)
                            .filter(s -> !s.isEmpty())
                            .map(s -> "&nextPage=" + URLEncoder.encode(s, StandardCharsets.UTF_8))
                            .orElse("")
            );

            return get(url, new ParameterizedTypeReference<>() {});

        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * <h3>Retrieve a Practitioner by ID</h3>
     * Fetches the practitioner's profile based on their unique identifier.
     * This method sends a GET request to the practitioner service and returns the profile details.
     * <h3>Cache:</h3>
     * <ul>
     *     <li>Cache Name: {@code practitionerCache}</li>
     *     <li>Cache Key: {@code practitioner-{id}}</li>
     *     <li>Expiry Time: Configured in {@link CacheConfig}</li>
     * </ul>
     *
     * <h3>Validation:</h3>
     * <ul>
     *     <li>The provided practitioner ID must not be null or empty.</li>
     * </ul>
     *
     * <h3>API Endpoint:</h3>
     * <p>{@code GET /get/Practitioner/{id}}</p>
     *
     * @param id The unique practitioner ID.
     * @return {@link Mono<Object>} containing the practitioner's profile details if found.
     *         If the ID is invalid, returns a {@link Mono(ValidationError)}.
     *         If an error occurs while fetching, logs the error and returns a {@link Mono(RuntimeException)}.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * practitionerService.getPractitionerById("12345")
     *     .subscribe(response -> System.out.println("Practitioner Details: " + response),
     *                error -> System.err.println("Error: " + error.getMessage()));
     * }</pre>
     */
    @Cacheable(value = "practitionerCache", key = "'practitioner-' + #id")
    public Mono<Object> findById(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("Practitioner ID cannot be null or empty."));
        }

        String endpoint = String.format("/get/%s/%s", ResourceType.Practitioner.name(), id);

        try {
            return get(
                    endpoint,
                    new HashMap<>(),
                    new ParameterizedTypeReference<>() {
                    });
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * <h3>Check if a Practitioner Exists</h3>
     * Determines whether a practitioner exists in the system based on their unique ID.
     * This method calls {@link #findById(String)} and checks the response to determine existence.
     *
     * <h3>Validation:</h3>
     * <ul>
     *     <li>If the ID is null or empty, returns {@code false} immediately.</li>
     * </ul>
     *
     * <h3>Logic:</h3>
     * <ul>
     *     <li>The method fetches the practitioner details using {@code getPractitionerById(id)}.</li>
     *     <li>It checks if the response contains a key "totalNumberOfRecords".</li>
     *     <li>If the value is a positive integer, it confirms the existence of the practitioner.</li>
     * </ul>
     *
     * <h3>API Endpoint:</h3>
     * <p>{@code GET /get/Practitioner/{id}}</p>
     *
     * @param id The unique identifier of the practitioner.
     * @return {@link Mono<Boolean>} that emits {@code true} if the practitioner exists, otherwise {@code false}.
     *         If an error occurs while fetching, logs the error and returns {@code false}.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * practitionerService.practitionerExists("12345")
     *     .subscribe(exists -> {
     *         if (exists) {
     *             System.out.println("Practitioner exists in the system.");
     *         } else {
     *             System.out.println("Practitioner not found.");
     *         }
     *     });
     * }</pre>
     */
    public Mono<Boolean> exists(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.just(false);
        }

        return findById(id)
                .map(response -> {
                    if (response instanceof Map) {
                        Object requestResource = ((Map<?, ?>) response).get("requestResource");
                        Object totalRecords = ((Map<?, ?>) response).get("totalNumberOfRecords");

                        return requestResource != null && totalRecords instanceof Integer && (Integer) totalRecords > 0;
                    }
                    return false;
                })
                .onErrorReturn(false);
    }

    /**
     * <h3>Create a New Practitioner</h3>
     * Adds a new practitioner to the system by sending a POST request.
     * <p>
     * Before making the request, this method validates the practitioner's data to ensure that all
     * mandatory fields are provided and formatted correctly.
     * </p>
     *
     * <h3>Validation Rules:</h3>
     * <ul>
     *     <li>All required fields (e.g., first name, last name, mobile number, etc.) must be provided.</li>
     *     <li>The email must follow a valid format.</li>
     *     <li>The mobile number must comply with the Indian phone number format.</li>
     * </ul>
     *
     * <h3>API Endpoint:</h3>
     * <p>{@code POST /add/Practitioner}</p>
     *
     * <h3>Parameters:</h3>
     * <ul>
     *     <li><b>practitioner</b> - A {@link PractitionerDTO} object containing the practitioner's details.</li>
     * </ul>
     *
     * <h3>Returns:</h3>
     * <p>
     * A {@link Mono<Object>} representing the response from the API.
     * </p>
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Validates the practitioner’s details.</li>
     *     <li>Constructs the API endpoint dynamically.</li>
     *     <li>Makes an asynchronous POST request.</li>
     * </ol>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * PractitionerDTO practitioner = PractitionerDTO.builder()
     *     .registrationId("PR12345")
     *     .department("Cardiology")
     *     .designation("Consultant")
     *     .status("Active")
     *     .joiningDate("2023-05-15")
     *     .staffType("Permanent")
     *     .firstName("shri")
     *     .lastName("ram")
     *     .birthDate("1985-08-20")
     *     .gender(Gender.MALE)
     *     .mobileNumber("9876543210")
     *     .emailId("shriram@example.com")
     *     .address("123 Street, City, State")
     *     .pincode("560001")
     *     .state(StatesAndUnionTerritories.KARNATAKA)
     *     .resourceType(ResourceType.Practitioner)
     *     .build();
     *
     * practitionerService.createPractitioner(practitioner)
     *     .subscribe(response -> System.out.println("Practitioner created: " + response));
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>If the practitioner data is null, a {@link ValidationError} is returned.</li>
     *     <li>If any required field is missing or incorrectly formatted, a validation error is thrown.</li>
     *     <li>All API-related exceptions are logged and returned as errors.</li>
     * </ul>
     *
     * @param practitioner The {@link PractitionerDTO} containing the practitioner's details.
     * @return {@link Mono<Object>} representing the response from the API.
     * @throws ValidationError if the practitioner data is null or required fields are missing.
     */
    public Mono<Object> create(@Valid PractitionerDTO practitioner) {
        if (practitioner == null) {
            return Mono.error(new ValidationError("Practitioner data cannot be null."));
        }

        try {
            validate(practitioner,false);

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
     * <h3>Update an Existing Practitioner</h3>
     * Updates the details of an existing practitioner by sending a PUT request.
     *
     * Before sending the request, this method validates the provided practitioner data to ensure all required fields are present.
     *
     *
     * <h3>Validation Rules:</h3>
     * <ul>
     *     <li>All required fields (e.g., first name, last name, mobile number, etc.) must be provided.</li>
     *     <li>The practitioner must have a valid resource ID for updating.</li>
     *     <li>The email must follow a valid format.</li>
     *     <li>The mobile number must comply with the Indian phone number format.</li>
     * </ul>
     *
     * <h3>API Endpoint:</h3>
     * <p>{@code PUT /update/Practitioner}</p>
     *
     * <h3>Parameters:</h3>
     * <ul>
     *     <li><b>updatePractitionerData</b> - A {@link PractitionerDTO} object containing the updated practitioner information.</li>
     * </ul>
     *
     * <h3>Returns:</h3>
     * A {@link Mono<Object>} representing the API response.
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Validates the updated practitioner details.</li>
     *     <li>Constructs the API endpoint dynamically.</li>
     *     <li>Makes an asynchronous PUT request.</li>
     * </ol>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * PractitionerDTO updatedPractitioner = PractitionerDTO.builder()
     *     .resourceId("abc123")
     *     .registrationId("PR12345")
     *     .department("Cardiology")
     *     .designation("Senior Consultant")
     *     .status("Active")
     *     .joiningDate("2023-05-15")
     *     .staffType("Permanent")
     *     .firstName("sri")
     *     .lastName("ram")
     *     .birthDate("1985-08-20")
     *     .gender(Gender.MALE)
     *     .mobileNumber("9876543210")
     *     .emailId("ram@example.com")
     *     .address("123 Street, City, State")
     *     .pincode("560001")
     *     .state(StatesAndUnionTerritories.KARNATAKA)
     *     .resourceType(ResourceType.Practitioner)
     *     .build();
     *
     * practitionerService.updatePractitioner(updatedPractitioner)
     *     .subscribe(response -> System.out.println("Practitioner updated: " + response));
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>If the update data is null, a {@link ValidationError} is returned.</li>
     *     <li>If required fields are missing or incorrectly formatted, validation fails.</li>
     *     <li>API-related errors are logged and returned as exceptions.</li>
     * </ul>
     *
     * @param updatePractitionerData The {@link PractitionerDTO} containing updated practitioner information.
     * @return {@link Mono<Object>} representing the API response.
     * @throws ValidationError if the update data is null or required fields are missing.
     */
    public Mono<Object> update(@Valid PractitionerDTO updatePractitionerData) {
        if (updatePractitionerData == null) {
            return Mono.error(new ValidationError("Update practitioner data cannot be null."));
        }

        try {
            validate(updatePractitionerData,false);

            String endpoint = String.format("/update/%s", ResourceType.Practitioner.name());

            return put(
                    endpoint,
                    updatePractitionerData,
                    new ParameterizedTypeReference<>() {});
        } catch (ValidationError ve) {
            return Mono.error(ve);
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * <h3>Retrieve Practitioners by Filters</h3>
     * Fetches practitioners based on specified search filters and optional pagination.
     * <p>
     * This method allows filtering practitioners by various attributes (such as gender, department, etc.),
     * and supports pagination for large datasets.
     * </p>
     *
     * <h3>Parameters:</h3>
     * <ul>
     *     <li><b>filters</b> - A {@link SearchFiltersDTO} object containing search criteria.</li>
     *     <li><b>pageSize</b> - The number of records per page (nullable, defaults to 10 if not provided).</li>
     *     <li><b>nextPage</b> - The pagination token to retrieve the next page (nullable, for continuation).</li>
     * </ul>
     *
     * <h3>Returns:</h3>
     * <p>
     * A {@link Mono<Object>} containing the filtered practitioners in JSON format.
     * </p>
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Transforms search filter keys to match the API format.</li>
     *     <li>Encodes filter parameters for safe URL transmission.</li>
     *     <li>Constructs the API endpoint dynamically, including pagination if applicable.</li>
     *     <li>Makes an asynchronous GET request to fetch the data.</li>
     * </ol>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * SearchFiltersDTO filters = new SearchFiltersDTO();
     * filters.setGender("male");
     * filters.setDepartment("Cardiology");
     *
     * practitionerService.getPractitionerByFilters(filters, 15, null)
     *     .subscribe(response -> System.out.println("Filtered Practitioners: " + response));
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>If an exception occurs, the method logs and returns a {@link Mono#error} containing the error details.</li>
     *     <li>Handles API request failures gracefully.</li>
     * </ul>
     *
     * @param filters  The search filters encapsulated in a {@link SearchFiltersDTO}.
     * @param pageSize The number of records per page (optional, defaults to 10 if null).
     * @param nextPage The pagination token for retrieving the next set of records (optional).
     * @return A {@link Mono<Object>} containing the response data.
     */
    public Mono<Object> find(SearchFiltersDTO filters,Integer pageSize, String nextPage) {
        try {
            Map<String, Object> transformedFilters = transformFilterKeys(filters);
            transformedFilters.put("_count", (pageSize != null) ? pageSize : 10);
            String encodedFilters = URLEncoder.encode(objectMapper.writeValueAsString(transformedFilters), StandardCharsets.UTF_8);

            String finalUrl = String.format("%s/%s?filters=%s%s",
                    Constants.GET_PROFILES_URL,
                    ResourceType.Practitioner,
                    encodedFilters,
                    Optional.ofNullable(nextPage)
                            .filter(s -> !s.isEmpty())
                            .map(s -> "&nextPage=" + URLEncoder.encode(s, StandardCharsets.UTF_8))
                            .orElse("")
            );

            return get(finalUrl, new ParameterizedTypeReference<>() {});

        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * <h3>Delete Practitioner by ID</h3>
     * Removes a practitioner record from the system using their unique identifier.
     * <p>
     * This method makes an asynchronous DELETE request to remove a practitioner's record from the database.
     * If the provided ID is null or empty, it returns a {@link ValidationError}.
     * </p>
     *
     * <h3>Parameters:</h3>
     * <ul>
     *     <li><b>id</b> - The unique identifier of the practitioner to be deleted.</li>
     * </ul>
     *
     * <h3>Returns:</h3>
     * <p>
     * A {@link Mono<Object>} confirming the deletion, or an error if the operation fails.
     * </p>
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Validates that the provided ID is not null or empty.</li>
     *     <li>Constructs the API endpoint dynamically using the practitioner ID.</li>
     *     <li>Makes an asynchronous DELETE request.</li>
     *     <li>Handles exceptions gracefully and logs errors.</li>
     * </ol>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * practitionerService.deletePractitioner("12345")
     *     .subscribe(response -> System.out.println("Practitioner deleted successfully."));
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>If the ID is null or empty, a {@link ValidationError} is thrown.</li>
     *     <li>If the request fails, an appropriate error is logged and returned.</li>
     * </ul>
     *
     * @param id The unique identifier of the practitioner.
     * @return A {@link Mono<Object>} confirming the deletion or returning an error.
     * @throws ValidationError if the ID is null or empty.
     */
    public Mono<Object> delete(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("Practitioner ID cannot be null or empty."));
        }

        String endpoint = String.format("/practitioners/%s", id);

        try {
            return delete(
                    endpoint,
                    new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * Validates the required fields for a {@link PractitionerDTO}.
     * <p>
     * This method ensures that all mandatory fields are provided before creating or updating a practitioner.
     * If a required field is missing, a {@link ValidationError} is thrown.
     * </p>
     *
     * <h3>Validation Rules:</h3>
     * <ul>
     *     <li><b>Resource ID</b> - Required for updates, not needed for new records.</li>
     *     <li><b>Registration ID</b> - Required.</li>
     *     <li><b>Department</b> - Required.</li>
     *     <li><b>Designation</b> - Required.</li>
     *     <li><b>Status</b> - Required.</li>
     *     <li><b>Joining Date</b> - Required.</li>
     *     <li><b>First Name</b> - Required.</li>
     *     <li><b>Last Name</b> - Required.</li>
     *     <li><b>Birth Date</b> - Required.</li>
     *     <li><b>Gender</b> - Required and must be a valid {@link Gender} enum value.</li>
     *     <li><b>Mobile Number</b> - Required.</li>
     *     <li><b>Email ID</b> - Required.</li>
     *     <li><b>Address</b> - Required.</li>
     *     <li><b>Pincode</b> - Required.</li>
     *     <li><b>State</b> - Required and must be a valid {@link StatesAndUnionTerritories} enum value.</li>
     *     <li><b>Resource Type</b> - Required and must be a valid {@link ResourceType} enum value.</li>
     * </ul>
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>If updating an existing practitioner, validates that the Resource ID is provided.</li>
     *     <li>Checks for missing mandatory fields.</li>
     *     <li>Ensures that Gender, State, and Resource Type are valid enum values.</li>
     *     <li>Throws a {@link ValidationError} if any validation fails.</li>
     * </ol>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * PractitionerDTO practitioner = new PractitionerDTO();
     * practitioner.setResourceId("12345");
     * practitioner.setFirstName("John");
     * practitioner.setLastName("Doe");
     * practitioner.setGender(Gender.MALE);
     * practitioner.setJoiningDate(LocalDate.of(2022, 5, 10));
     * practitioner.setState(StatesAndUnionTerritories.KARNATAKA);
     * practitioner.setResourceType(ResourceType.DOCTOR);
     *
     * try {
     *     validatePractitioner(practitioner, true); // For an update operation
     *     System.out.println("Practitioner data is valid.");
     * } catch (ValidationError e) {
     *     System.err.println("Validation Error: " + e.getMessage());
     * }
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Throws {@link ValidationError} with a descriptive message if any required field is missing or invalid.</li>
     *     <li>Handles invalid enum values gracefully.</li>
     * </ul>
     *
     * @param practitioner The {@link PractitionerDTO} object containing practitioner details.
     * @param isUpdate     Indicates whether this is an update operation (true) or a new practitioner creation (false).
     * @throws ValidationError If any required field is missing or contains an invalid value.
     */
    private void validate(PractitionerDTO practitioner, boolean isUpdate) {
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

    /**
     * Checks if the provided value is a valid enum constant of the given enum class.
     *
     * @param value     The enum value to validate.
     * @param enumClass The class of the enum.
     * @param <T>       The type of the enum.
     * @return {@code true} if the value is a valid enum constant, {@code false} otherwise.
     */
    private <T extends Enum<T>> boolean isValidEnum(Enum<?> value, Class<T> enumClass) {
        for (T constant : enumClass.getEnumConstants()) {
            if (constant.name().equals(value.name())) {
                return true;
            }
        }
        return false;
    }
}
