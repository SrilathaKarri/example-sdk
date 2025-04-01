package sdk.patient;

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
import sdk.base.enums.Gender;
import sdk.base.enums.ResourceType;
import sdk.base.enums.StatesAndUnionTerritories;
import sdk.base.errors.EhrApiError;
import sdk.base.errors.ValidationError;
import sdk.base.utils.Constants;
import sdk.base.utils.StringUtils;
import sdk.patient.DTO.PatientDTO;
import sdk.patient.DTO.UpdatePatientDTO;
import sdk.patient.enums.PatientIdType;
import sdk.patient.enums.PatientType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for handling patient-related operations such as retrieving, creating, updating, and deleting patients.
 * This service interacts with an external EHR system via REST API calls using WebClient.
 * <p>
 * The service provides methods for:
 * <ul>
 *     <li>Retrieving all patients</li>
 *     <li>Fetching a patient by ID</li>
 *     <li>Checking patient existence</li>
 *     <li>Creating a new patient</li>
 *     <li>Updating an existing patient</li>
 *     <li>Searching for patients using filters</li>
 *     <li>Deleting a patient</li>
 * </ul>
 * </p>
 */
@Service
@Validated
public class Patient extends Base {

    protected Patient(ObjectMapper objectMapper, WebClient webClient) {
        super(objectMapper, webClient);
    }

    /**
     * <h3>Retrieve a Paginated List of Patients</h3>
     * Fetches a list of patient records from the EHR system, with optional pagination parameters.
     *
     * <h3>Pagination:</h3>
     * <ul>
     *     <li><b>pageSize:</b> (Optional) Specifies the number of records per page. Defaults to 10 if not provided.</li>
     *     <li><b>nextPage:</b> (Optional) A token used to fetch the next page of results.</li>
     * </ul>
     *
     * <h3>Response Format:</h3>
     * The response is a {@link Mono<Object>} containing the list of patients.
     * The format of the response depends on the API but typically includes:
     * <ul>
     *     <li>A list of patient records.</li>
     *     <li>A <code>nextPage</code> token, if more results are available.</li>
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * If an error occurs during the request (e.g., network failure, API error), it will be logged,
     * and the method will return a {@link Mono#error(Throwable)}.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * patientService.getAllPatients(20, null)
     *     .subscribe(response -> System.out.println("Patients: " + response));
     * }</pre>
     *
     * @param pageSize The number of records per page (nullable, defaults to 10 if not provided).
     * @param nextPage The pagination token for fetching the next set of results (nullable).
     * @return {@link Mono<Object>} containing the paginated list of patients.
     */
    public Mono<Object> findAll(Integer pageSize, String nextPage) {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("_count", (pageSize != null) ? pageSize : 10);

            String encodedFilters = URLEncoder.encode(objectMapper.writeValueAsString(filters), StandardCharsets.UTF_8);

            String url = String.format("%s/%s?filters=%s%s",
                    Constants.GET_PROFILES_URL,
                    ResourceType.Patient,
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
     * <h3>Retrieve a Patient's Profile by ID</h3>
     * Fetches the details of a patient from the EHR system using their unique identifier.
     * The response includes the patient's profile information.
     *
     * <h3>Caching Mechanism:</h3>
     * This method is cached using {@link Cacheable} to improve performance and reduce unnecessary API calls.
     * The cache key follows the format: <code>patient-{id}</code>.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li><b>ValidationError:</b> If the provided patient ID is null or empty.</li>
     *     <li><b>RuntimeException:</b> If an error occurs while making the API request.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * patientService.getPatientById("12345")
     *     .subscribe(response -> System.out.println("Patient Details: " + response));
     * }</pre>
     *
     * @param id The unique identifier of the patient. Must not be null or empty.
     * @return {@link Mono} containing the patient's profile.
     */
    @Cacheable(value = "patientCache", key = "'patient-' + #id")
    public Mono<Object> findById(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("Patient ID cannot be null or empty."));
        }

        String endpoint = String.format("/get/%s/%s", ResourceType.Patient.name(), id);

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
     * <h3>Check if a Patient Exists</h3>
     * Determines whether a patient exists in the EHR system based on their unique identifier.
     * This method calls {@link #findById(String)} and checks if valid patient data is returned.
     *
     * <h3>Validation:</h3>
     * If the provided ID is null or empty, the method immediately returns {@code Mono.just(false)}.
     *
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>If any error occurs while fetching the patient data, it gracefully returns {@code false}.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * patientService.patientExists("12345")
     *     .subscribe(exists -> System.out.println("Patient exists: " + exists));
     * }</pre>
     *
     * @param id The unique identifier of the patient. Must not be null or empty.
     * @return {@code Mono<Boolean>} indicating whether the patient exists.
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
     * Creates a new patient in the system.
     * <p>
     * This method validates the provided {@link PatientDTO} object and sends a request to the EHR system to create a new patient record.
     * </p>
     *
     * <h3>Validation Rules:</h3>
     * <ul>
     *     <li><b>ID Number:</b> Required, must be non-blank.</li>
     *     <li><b>ID Type:</b> Required, must be a valid {@link PatientIdType}.</li>
     *     <li><b>ABHA Address:</b> Required, must be non-blank.</li>
     *     <li><b>Patient Type:</b> Required, must be a valid {@link PatientType}.</li>
     *     <li><b>First & Last Name:</b> Required, at least 3 characters long.</li>
     *     <li><b>Birth Date:</b> Required, must follow YYYY-MM-DD format.</li>
     *     <li><b>Gender:</b> Required, must be a valid {@link Gender}.</li>
     *     <li><b>Email ID:</b> Required, must be in a valid email format.</li>
     *     <li><b>Mobile Number:</b> Required, must be a valid 10-digit or +91 format.</li>
     *     <li><b>Address:</b> Required, at least 5 characters long.</li>
     *     <li><b>Pincode:</b> Required, must be a 6-digit number.</li>
     *     <li><b>State:</b> Required, must be a valid {@link StatesAndUnionTerritories}.</li>
     *     <li><b>Resource Type:</b> Required, must be a valid {@link ResourceType}.</li>
     * </ul>
     *
     * @param patient The {@link PatientDTO} object containing the patient's details.
     * @return {@link Mono<Object>} containing the API response after the patient is created.
     *         The response structure depends on the EHR system's API.
     * @throws ValidationError if the patient data is null or fails validation.
     * @throws RuntimeException if the request to the EHR system fails.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * PatientDTO patient = new PatientDTO(
     *     "1234567890", // ID Number
     *     PatientIdType.AADHAAR, // ID Type
     *     "ram@abha.com", // ABHA Address
     *     PatientType.REGULAR, // Patient Type
     *     "Shri", "S.", "Ram", // Name
     *     "1990-05-15", // Birth Date
     *     Gender.MALE, // Gender
     *     "shri.ram@example.com", // Email
     *     "9876543210", // Mobile Number
     *     "123 Main Street", // Address
     *     "560001", // Pincode
     *     StatesAndUnionTerritories.KARNATAKA, // State
     *     true, // Wants to link WhatsApp (Optional)
     *     null, // Photo (Optional)
     *     ResourceType.Patient, // Resource Type
     *     null // Resource ID (Optional)
     * );
     *
     * patientService.createPatient(patient)
     *     .subscribe(response -> System.out.println("Patient created: " + response));
     * }</pre>
     */
    public Mono<Object> create(@Valid PatientDTO patient) {
        if (patient == null) {
            return Mono.error(new ValidationError("Patient data cannot be null."));
        }

        try {
            validate(patient);

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
     * <h3>Update an Existing Patient's Details</h3>
     * Modifies the details of a patient using the provided update data.
     * The method first validates the input fields before making an API call to update the patient record.
     *
     * <h3>Validation:</h3>
     * <ul>
     *     <li>If the {@code updatePatientData} is {@code null}, a {@link ValidationError} is thrown.</li>
     *     <li>Additional validation is performed via {@code validateUpdatePatientFields(updatePatientData)}.</li>
     * </ul>
     *
     * <h3>API Request:</h3>
     * <ul>
     *     <li>Makes a <b>PUT</b> request to the endpoint: {@code /update/Patient}.</li>
     *     <li>The request body contains the serialized {@link UpdatePatientDTO} object.</li>
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Throws {@link ValidationError} if validation fails.</li>
     *     <li>Handles all other exceptions using {@link EhrApiError(Exception)}.</li>
     * </ul>
     *
     * @param updatePatientData The {@link UpdatePatientDTO} object containing updated patient information.
     *                          This object must not be {@code null} and should contain valid fields.
     * @return {@link Mono<Object>} containing the response from the EHR system.
     *         The response typically includes confirmation of the update operation.
     *
     * @throws ValidationError  if the update data is null or fails validation.
     * @throws RuntimeException if the API request encounters an unexpected error.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * UpdatePatientDTO updateData = new UpdatePatientDTO("12345", "new_email@example.com","9876543210",ResourceType.Patient);
     * patientService.updatePatient(updateData)
     *     .subscribe(response -> System.out.println("Update Response: " + response));
     * }</pre>
     */
    public Mono<Object> update(UpdatePatientDTO updatePatientData) {
        if (updatePatientData == null) {
            return Mono.error(new ValidationError("Update patient data cannot be null."));
        }

        try {
            validateUpdatePatientFields(updatePatientData);

            String endpoint = String.format("/update/%s", ResourceType.Patient.name());

            return put(
                    endpoint,
                    updatePatientData,
                    new ParameterizedTypeReference<>() {});
        } catch (ValidationError ve) {
            return Mono.error(ve);
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * Retrieves a list of patients based on the specified search filters.
     * <p>
     * This method constructs a query using the provided {@link SearchFiltersDTO} object,
     * applies necessary transformations, and makes an HTTP GET request to fetch patient records.
     * </p>
     *
     * <h3>Search Filters:</h3>
     * The following filters can be applied to refine the patient search:
     * <ul>
     *     <li><b>First Name:</b> (Optional) Must contain only letters (a-z, A-Z) and dots (.), between 3-20 characters.</li>
     *     <li><b>Last Name:</b> (Optional) Must contain only letters (a-z, A-Z) and dots (.), between 3-20 characters.</li>
     *     <li><b>Birth Date:</b> (Optional) Must follow the YYYY-MM-DD format.</li>
     *     <li><b>Gender:</b> (Optional) Must be a valid {@link Gender} enum value.</li>
     *     <li><b>Phone:</b> (Optional) Can contain only digits and an optional leading '+'.</li>
     *     <li><b>State:</b> (Optional) Must be a valid {@link StatesAndUnionTerritories} enum value.</li>
     *     <li><b>Pincode:</b> (Optional) Must be a valid 6-digit Indian pincode (starting with 1-9).</li>
     *     <li><b>Email ID:</b> (Optional) Must be in a valid email format.</li>
     *     <li><b>Organization ID:</b> (Optional) Identifier for filtering patients by organization.</li>
     *     <li><b>Registration ID:</b> (Optional) Unique registration ID of the patient.</li>
     *     <li><b>Count:</b> (Optional) Must be a positive integer specifying the number of records to fetch.</li>
     *     <li><b>Identifier:</b> (Optional) Unique identifier for advanced filtering.</li>
     *     <li><b>From Date:</b> (Optional) Must follow the YYYY-MM-DD format and cannot be in the future.</li>
     *     <li><b>To Date:</b> (Optional) Must follow the YYYY-MM-DD format, cannot be in the future, and should be after or equal to {@code fromDate}.</li>
     *     <li><b>Next Page:</b> (Optional) Used for pagination to fetch the next set of results.</li>
     * </ul>
     *
     * <h3>Parameters:</h3>
     * @param filters   The {@link SearchFiltersDTO} object containing search criteria.
     * @param pageSize  (Optional) The number of records to fetch. Defaults to 10 if not provided.
     * @param nextPage  (Optional) The pagination token for fetching the next page of results.
     * @return {@link Mono<Object>} containing the list of patients matching the filters.
     *         The response structure depends on the EHR system's API.
     * @throws RuntimeException if the request to the EHR system fails.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * SearchFiltersDTO filters = new SearchFiltersDTO();
     * filters.setBirthDate("1990-05-15");
     * filters.setGender(Gender.MALE);
     * filters.setState(StatesAndUnionTerritories.KARNATAKA);
     *
     * patientService.getPatientByFilters(filters, 10, null)
     *     .subscribe(response -> System.out.println("Search results: " + response));
     * }</pre>
     */
    public Mono<Object> find(SearchFiltersDTO filters, Integer pageSize, String nextPage) {
        try {
            Map<String, Object> transformedFilters = transformFilterKeys(filters);
            transformedFilters.put("_count", (pageSize != null) ? pageSize : 10);
            String encodedFilters = URLEncoder.encode(objectMapper.writeValueAsString(transformedFilters), StandardCharsets.UTF_8);

            String finalUrl = String.format("%s/%s?filters=%s%s",
                    Constants.GET_PROFILES_URL,
                    ResourceType.Patient,
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
     * <h3>Delete a Patient Record</h3>
     * Removes a patient from the system based on their unique ID.
     * This method sends a DELETE request to the EHR system to remove the specified patient.
     *
     * <h3>Validation:</h3>
     * <ul>
     *     <li>Throws a {@link ValidationError} if the provided {@code id} is null or empty.</li>
     * </ul>
     *
     * <h3>API Request:</h3>
     * <ul>
     *     <li>Makes a <b>DELETE</b> request to the endpoint: {@code /patients/{id}}.</li>
     *     <li>The patient ID is dynamically inserted into the URL.</li>
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Throws {@link ValidationError} if the provided ID is invalid.</li>
     *     <li>Handles other exceptions using {@link EhrApiError(Exception)}.</li>
     * </ul>
     *
     * @param id The unique identifier of the patient to be deleted.
     *           This value must not be null or empty.
     * @return {@link Mono<Object>} containing the response from the EHR system,
     *         typically confirming the deletion operation.
     *
     * @throws ValidationError   if the ID is null or empty.
     * @throws RuntimeException if an unexpected error occurs during the request.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * patientService.deletePatient("12345")
     *     .subscribe(response -> System.out.println("Delete Response: " + response));
     * }</pre>
     */
    public Mono<Object> delete(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("Patient ID cannot be null or empty."));
        }

        String endpoint = String.format("/patients/%s", id);

        try {
            return delete(
                    endpoint,
                    new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * Validates the required fields for a new patient.
     * <p>
     * Ensures that essential patient details such as ID, name, birth date,
     * gender, mobile number, and address are provided and valid.
     * </p>
     *
     * @param patient The {@link PatientDTO} object containing patient details.
     * @throws ValidationError If any required field is missing or invalid.
     */
    private void validate(PatientDTO patient) {
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

    /**
     * Validates the fields for updating an existing patient.
     * <p>
     * Ensures that a resource ID is provided for the update and that optional fields,
     * such as mobile number and email, follow the correct format.
     * </p>
     *
     * @param updatePatientData The {@link UpdatePatientDTO} containing updated patient data.
     * @throws ValidationError If any required field is missing or an optional field is in an incorrect format.
     */
    private void validateUpdatePatientFields(@Valid UpdatePatientDTO updatePatientData) {
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

    /**
     * Validates if a given value is a valid enum constant.
     *
     * @param value     The enum value to check.
     * @param enumClass The class of the enum type.
     * @param <T>       The type parameter for the enum class.
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
