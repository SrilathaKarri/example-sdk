package sdk.facility;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import sdk.base.Base;
import sdk.base.errors.EhrApiError;
import sdk.base.errors.ValidationError;
import sdk.base.utils.Constants;
import sdk.base.utils.LogUtil;
import sdk.base.utils.StringUtils;
import sdk.facility.dto.FacilityDTO;
import sdk.facility.dto.ResponseDTOs;
import sdk.facility.dto.SearchFacilityDTO;
import sdk.facility.dto.UpdateSpocForFacility;
import sdk.facility.enums.FacilityIdType;
import sdk.facility.enums.Region;
import sdk.facility.service.FacilityDemographic;

import java.util.HashMap;
import java.util.Map;

/**
 * Service class that handles operations related to facilities.
 * This class provides methods for retrieving, creating, updating, and deleting facilities.
 */
@Service
public class Facility extends Base {

    /**
     * Dependency for demographic-related facility operations.
     */
    @Autowired
    private FacilityDemographic facilityDemographic;

    protected Facility(ObjectMapper objectMapper, WebClient webClient) {
        super(objectMapper, webClient);
    }

    /**
     * Constructs a new FacilityService with the given WebClient builder.
     *
     * @param webClientBuilder The WebClient builder to be used for making HTTP requests
     */


    /**
     * Retrieves a paginated list of all facilities from the external API.
     * <br>
     * This method allows pagination by accepting a {@code nextPage} token to fetch
     * subsequent pages of results. If no {@code pageSize} is provided, the default value of 10 is used.
     *
     * <h3>Request Parameters:</h3>
     * <ul>
     *     <li><b>nextPage</b> (optional) - A token for fetching the next page of results.</li>
     *     <li><b>pageSize</b> (optional, default: 10) - The number of results per page (min: 1, max: 100).</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Fetch first page with default size
     * facilityService.getAllFacilities(null, 10)
     *     .subscribe(response -> {
     *         if ("Error".equals(response.getStatus())) {
     *             System.out.println("Error fetching facilities: " + response.getMessage());
     *         } else {
     *             System.out.println("Retrieved facilities: " + response.getRequestResource());
     *         }
     *     });
     * }</pre>
     *
     * <h3>Example API Response:</h3>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * Content-Type: application/json
     *
     * {
     *   "message": "Success",
     *   "totalNumberOfRecords": 4,
     *   "nextPageLink": "abc123"
     *   "data": [....]
     *
     * }
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>If the external API returns an error, it is wrapped in an {@code EhrApiError} and logged.</li>
     *     <li>Unexpected exceptions result in a {@code RuntimeException} with an appropriate message.</li>
     * </ul>
     *
     * @param nextPage A token for retrieving the next page of results (optional). Pass {@code null} for the first request.
     * @param pageSize The number of results per page (must be between 1 and 100, default is 10).
     * @return A {@code Mono<ApiResponse<FacilityResponseDTO>>} containing the paginated list of facilities or an error.
     */
    public Mono<Object> getAllFacilities(
            @RequestParam(required = false) String nextPage,
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) Integer pageSize) {

        Map<String, String> queryParams = new HashMap<>();
        if (nextPage != null && !nextPage.trim().isEmpty()) {
            queryParams.put("nextPage", nextPage);
        }
        queryParams.put("pageSize", String.valueOf(pageSize));

        return get(Constants.GET_FACILITIES_URL, queryParams, new ParameterizedTypeReference<>() {
        })
                .onErrorResume(EhrApiError.class, e -> Mono.error(EhrApiError.handleAndLogApiError(e)))
                .onErrorResume(Exception.class, e -> Mono.error(new RuntimeException("Error fetching facilities", e)));
    }

    /**
     * Retrieves a facility's details using its unique identifier type and ID.
     * <br>
     * This method dynamically constructs the API endpoint based on the identifier type
     * and facility ID, then performs an HTTP GET request to retrieve facility details.
     * The response is cached using Spring's {@link Cacheable} annotation.
     *
     * <h3>Request Parameters:</h3>
     * <ul>
     *     <li><b>idType</b> - The type of facility identifier (e.g., "FACILITY_ID", "ACCOUNT_ID", "ID").</li>
     *     <li><b>id</b> - The unique facility identifier.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * facilityService.getFacilityById("FACILITY_ID", "IN13243539043")
     *     .subscribe(response -> System.out.println("Facility Details: " + response));
     * }</pre>
     *
     * <h3>Example API Response:</h3>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * Content-Type: application/json
     *
     * {
     *   "message": "Facility Found !!!",
     *   "data": { ... facility details ... }
     *   "nextPageLink": "",
     *   "totalNumberOfRecords": 1
     * }
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>If {@code idType} or {@code id} is null/empty, a {@link ValidationError} is thrown.</li>
     *     <li>If an invalid identifier type is provided, an exception is thrown.</li>
     *     <li>Errors from the API call are handled using {@link EhrApiError}.</li>
     * </ul>
     *
     * @param idType The type of facility identifier (e.g., "facilityId", "FACILITY_ID","accountId","ACCOUNT_ID","id","ID").
     * @param id     The unique facility identifier.
     * @return A {@link Mono<Object>} containing the facility's details or an error.
     * @throws ValidationError if the facility ID or identifier type is null or empty.
     */
    @Cacheable(value = "facilityCache", key = "#idType + '-' + #id")
    public Mono<Object> getFacilityById(String idType, String id) {
        if (StringUtils.isNullOrEmpty(id) || StringUtils.isNullOrEmpty(idType)) {
            return Mono.error(new ValidationError("Facility ID and ID Type cannot be null or empty."));
        }

        try {
            FacilityIdType facilityIdType = FacilityIdType.fromString(idType);
            String endPoint = String.format("%s/%s/%s", Constants.GET_FACILITIES_URL, facilityIdType.getValue(), id);

            return get(endPoint, new HashMap<>(), new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * Checks whether a facility exists based on the given facility ID and identifier type.
     * <br>
     * This method retrieves facility details using {@link #getFacilityById(String, String)} and examines the
     * response to determine if the facility record exists.
     *
     * <h3>Behavior:</h3>
     * <ul>
     *   <li>If either {@code idType} or {@code id} is null or empty, the method immediately returns a {@code ValidationError}.</li>
     *   <li>Calls {@link #getFacilityById(String, String)} to fetch facility details.</li>
     *   <li>Parses the response as a {@code Map} and checks if the "message" field matches {@link Constants#FACILITY_FOUND_MESSAGE}.</li>
     *   <li>If an error occurs, logs the error and returns {@code false}.</li>
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *   <li>If an invalid {@code idType} or {@code id} is provided, a {@link ValidationError} is returned.</li>
     *   <li>If {@link #getFacilityById(String, String)} throws an exception, the error is logged and {@code false} is returned.</li>
     * </ul>
     *
     * <h3>Usage Example:</h3>
     * <pre>{@code
     * // Asynchronously check if a facility exists:
     * Mono<Boolean> existsMono = facilityService.facilityExists("facilityId", "IN13243539043");
     * existsMono.subscribe(exists -> System.out.println("Facility exists: " + exists));
     * }</pre>
     *
     * <h3>Example API Response (Facility Found):</h3>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * Content-Type: application/json
     *
     * {
     *   "message": "Facility Found !!!",
     *   "data": [{ ... facility details ... }]
     * }
     * }</pre>
     *
     * <h3>Example API Response (Facility Not Found):</h3>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * Content-Type: application/json
     *
     * {
     *   "message": "Facility Not Found",
     *   "data": []
     * }
     * }</pre>
     *
     * <h3>Example API Response (Error Case):</h3>
     * <pre>{@code
     * HTTP/1.1 400 Bad Request
     * Content-Type: application/json
     *
     * {
     *   "error": "Invalid ID format"
     * }
     * }</pre>
     *
     * @param idType The type of facility identifier (e.g., "facilityId", "FACILITY_ID","accountId","ACCOUNT_ID","id","ID").
     *               Must match one of the values defined in {@link FacilityIdType}.
     * @param id     The unique facility identifier (e.g., "IN13243539043").
     * @return A {@code Mono<Boolean>} that emits {@code true} if the facility exists, otherwise {@code false}.
     */
    public Mono<Boolean> facilityExists(String idType, String id) {
        if (idType == null || id == null || id.trim().isEmpty()) {
            return Mono.error(new ValidationError("Please provide a valid ID and ID Type for checking if the facility exists."));
        }

        return getFacilityById(idType, id)
                .map(response -> {
                    if (response instanceof Map<?, ?> responseMap) {
                        Object messageObj = responseMap.get("message");

                        if (messageObj instanceof String message) {
                            return Constants.FACILITY_FOUND_MESSAGE.equalsIgnoreCase(message);
                        }
                    }
                    return false;
                })
                .onErrorResume(e -> {
                    LogUtil.logger.error("Error checking facility existence: {}", e.getMessage());
                    return Mono.just(false);
                });
    }
    /**
     * Searches for facilities based on the provided search criteria.
     * <br>
     * This method sends a POST request to the external EHR system using the provided
     * {@link SearchFacilityDTO} object. The response is returned as a generic {@code Object},
     * allowing flexibility in handling different response structures.
     *
     * <h3>Behavior:</h3>
     * <ul>
     *   <li>Sends a POST request to the {@code /search-facility} endpoint with the given criteria.</li>
     *   <li>Returns a {@link Mono} containing the API response.</li>
     *   <li>If an error occurs, logs the error and returns a {@code Mono.error}.</li>
     * </ul>
     *
     * @param searchFacilityData The {@link SearchFacilityDTO} object containing search criteria.
     * @return A {@link Mono} containing the search results as an {@code Object}.
     * @throws RuntimeException if an error occurs during the API call.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * SearchFacilityDTO searchCriteria = new SearchFacilityDTO();
     * searchCriteria.setFacilityName("Apollo");
     * searchCriteria.setPage(1);
     * searchCriteria.setResultsPerPage(20);
     *
     * facilityService.searchFacility(searchCriteria)
     *     .subscribe(response -> System.out.println("Search results: " + response),
     *                error -> System.err.println("Error: " + error.getMessage()));
     * }</pre>
     *
     * <h3>Example API Request:</h3>
     * <pre>{@code
     * POST /search-facility
     * Content-Type: application/json
     *
     * {
     *   "facilityName": "Apollo",
     *   "stateLGDCode": "28",
     *   "districtLGDCode": "745",
     *   "subDistrictLGDCode": "4887",
     *   "pincode": "520010",
     *   "page": 1,
     *   "resultsPerPage": 20
     * }
     * }</pre>
     *
     * <h3>Example API Response (Success):</h3>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * Content-Type: application/json
     *
     * {
     *   "facilities": [
     *     {
     *       "facilityId": "IN123456",
     *       "facilityName": "Apollo Hospital",
     *       "facilityStatus": "Active",
     *       "ownership": "Private",
     *       "address": "Vijayawada, Andhra Pradesh",
     *       "pincode": "520010",
     *       "latitude": "16.5061743",
     *       "longitude": "80.6480153"
     *     }
     *   ],
     *   "message": "Request processed successfully",
     *   "totalFacilities": 1,
     *   "numberOfPages": 1
     * }
     * }</pre>
     *
     * <h3>Example API Response (Error):</h3>
     * <pre>{@code
     * HTTP/1.1 400 Bad Request
     * Content-Type: application/json
     *
     * "Error processing request: Invalid search criteria"
     * }</pre>
     */
    public Mono<Object> searchFacility(@Valid SearchFacilityDTO searchFacilityData) {
        return post(Constants.SEARCH_FACILITY_URL, searchFacilityData,
                new ParameterizedTypeReference<>() {})
                .onErrorResume(error -> {
                    LogUtil.logger.error("Error while searching for Facility: {}", error.getMessage());
                    return Mono.error(error);
                });
    }

    /**
     * Retrieves comprehensive demographic data for facilities.
     * <br>
     * This method gathers various demographic details by sequentially calling multiple data retrieval methods.
     * It combines information from different sources such as state, district, sub-district, region,
     * address proof, ownership details, facility type, and medical specialities.
     *
     * <h3>Workflow:</h3>
     * <ul>
     *     <li>Fetches state and district information.</li>
     *     <li>Retrieves sub-district and region details based on the district.</li>
     *     <li>Gets address proof and ownership type information.</li>
     *     <li>Obtains the system of medicine and speciality details.</li>
     *     <li>Determines facility type and subtype based on ownership type.</li>
     *     <li>Fetches additional service and general information.</li>
     *     <li>Combines all retrieved data into a {@link ResponseDTOs.FacilityDemographicsDTO} object.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>
     * Mono&lt;ResponseDTOs.FacilityDemographicsDTO&gt; facilityData = facilityDemographicService.getDemographicData();
     * facilityData.subscribe(data -> {
     *     System.out.println("State Code: " + data.getStateCode());
     *     System.out.println("District Code: " + data.getDistrictCode());
     * });
     * </pre>
     *
     * <h3>Example Response:</h3>
     * <pre>
     * {
     *   "stateCode": "12",
     *   "districtCode": "21",
     *   "subDistrictCode": "234",
     *   "regionCode": "U",
     *   "addressProofCode": "TD",
     *   "ownershipTypeCode": "P",
     *   "ownershipSubTypeCode": "PP",
     *   "facilityStatusTypeCode": "UC",
     *   "systemOfMedicineCode": "A",
     *   "specialityCodes": ["A-S2", "A-R"],
     *   "facilityTypeCode": "H",
     *   "facilitySubTypeCode": "GH",
     *   "serviceGeneralInfo": { YALL }
     * }
     * </pre>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>If any step fails (e.g., state or district lookup fails), the Mono will return an error.</li>
     *     <li>Logged errors help in debugging the failure points in data retrieval.</li>
     * </ul>
     *
     * @return A Mono containing a {@link ResponseDTOs.FacilityDemographicsDTO} with aggregated facility demographic details.
     */
    public Mono<ResponseDTOs.FacilityDemographicsDTO> getDemographicData() {
        return facilityDemographic.getDemographicData();
    }

    /**
     * Registers a new facility in the system.
     * <br>
     * This method validates the provided facility details before sending a registration request.
     * It ensures that required fields such as facility name, demographic codes, and other attributes are correctly populated.
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Validates the facility data using {@link #validateFacility(FacilityDTO)}.</li>
     *     <li>If validation passes, performs a POST request to register the facility.</li>
     *     <li>Handles and logs any potential errors.</li>
     * </ol>
     *
     * <h3>Pre-requisite:</h3>
     * <br>
     * Users <b>must</b> ensure that all required demographic data codes are valid.
     * This can be done by calling {@link #getDemographicData()} before registering a facility.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * Facility facility = new Facility();
     * facility.setName("New Facility");
     * facility.setRegion("U");
     *
     * facilityService.registerFacility(facility)
     *         .subscribe(response -> System.out.println("Registration Response: " + response));
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Throws {@link ValidationError} if facility data is missing or invalid.</li>
     *     <li>Handles API errors and wraps them in {@link EhrApiError}.</li>
     * </ul>
     *
     * @param facilityDTO The facility details to be registered.
     * @return A {@code Mono<Object>} containing the registration response.
     * @throws ValidationError If facility validation fails.
     */
    public Mono<Object> registerFacility(@Valid FacilityDTO facilityDTO) {
        if (facilityDTO == null) {
            return Mono.error(new ValidationError("Facility data cannot be null."));
        }

        try {
            validateFacility(facilityDTO);
            String endpoint = Constants.REGISTER_FACILITY_URL;

            return post(endpoint, facilityDTO, new ParameterizedTypeReference<>() {});
        } catch (ValidationError ve) {
            return Mono.error(ve);
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }
    /**
     * Updates the Single Point of Contact (SPOC) information for a facility.
     * <br>
     * This method updates the SPOC details of a facility, ensuring that the required data is provided before making the request.
     *
     * <h3>Validation Rules:</h3>
     * <ul>
     *     <li><b>Facility ID</b> - Required and cannot be empty.</li>
     *     <li><b>SPOC Name</b> - Required and cannot be empty.</li>
     *     <li><b>SPOC ID</b> - Required and cannot be empty.</li>
     * </ul>
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Validates the provided SPOC details.</li>
     *     <li>Performs an HTTP PUT request to update the facility SPOC information.</li>
     *     <li>Returns the API response wrapped in a reactive Mono.</li>
     * </ol>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * UpdateSpocForFacility spocUpdate = new UpdateSpocForFacility();
     * spocUpdate.setId("facility123");
     * spocUpdate.setSpocName("David Hill");
     * spocUpdate.setSpocId("spoc001");
     *
     * facilityService.updateSpocForFacility(spocUpdate)
     *         .subscribe(response -> System.out.println("SPOC Update Response: " + response));
     * }</pre>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Throws {@link ValidationError} if required data is missing.</li>
     *     <li>Handles API errors reactively.</li>
     * </ul>
     *
     * @param updateFacilityData The SPOC update details.
     * @return A {@code Mono<Object>} containing the update result.
     * @throws ValidationError If validation fails for the update data.
     */
    public Mono<Object> updateSpocForFacility(UpdateSpocForFacility updateFacilityData) {
        if (updateFacilityData == null) {
            return Mono.error(new IllegalArgumentException("Update facility data cannot be null."));
        }
        if (updateFacilityData.getId() == null || updateFacilityData.getId().trim().isEmpty()) {
            return Mono.error(new ValidationError("Facility ID is required for updating."));
        }
        if (updateFacilityData.getSpocName() == null || updateFacilityData.getSpocName().trim().isEmpty()) {
            return Mono.error(new ValidationError("SPOC Name is required."));
        }
        if (updateFacilityData.getSpocId() == null || updateFacilityData.getSpocId().trim().isEmpty()) {
            return Mono.error(new ValidationError("SPOC ID is required."));
        }

        return put(Constants.UPDATE_FACILITY_URL, updateFacilityData, new ParameterizedTypeReference<>() {});
    }

    /**
     * Deletes a facility by its ID.
     *
     * <br>This method removes a facility from the system based on the provided facility ID.
     * It performs input validation to ensure the ID is not null or empty before sending the delete request.
     *
     * <h2>Workflow:</h2>
     * <ol>
     *     <li>Validates the facility ID.</li>
     *     <li>Performs an HTTP DELETE request to remove the facility.</li>
     *     <li>Returns a {@code Mono<Void>} indicating success or failure.</li>
     * </ol>
     *
     * <h2>Example Usage:</h2>
     * <pre>{@code
     * facilityService.deleteFacility("facility123")
     *         .subscribe(
     *             success -> System.out.println("Facility deleted successfully."),
     *             error -> System.err.println("Error deleting facility: " + error.getMessage())
     *         );
     * }</pre>
     *
     * <h2>Error Handling:</h2>
     * <ul>
     *     <li>Throws {@link ValidationError} if the facility ID is null or empty.</li>
     *     <li>Logs detailed error messages for debugging purposes.</li>
     * </ul>
     *
     * @param id The facility identifier.
     * @return A {@code Mono<Void>} indicating success or failure.
     * @throws ValidationError If the facility ID is null or empty.
     */
    public Mono<Void> deleteFacility(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Mono.error(new ValidationError("Facility ID cannot be null or empty."));
        }

        String deleteEndpoint = String.format("%s/%s", Constants.DELETE_FACILITY_URL, id);

        return delete(deleteEndpoint, new ParameterizedTypeReference<Void>() {
        })
                .doOnSuccess(response -> LogUtil.logger.info("Facility deleted successfully. ID: {}", id))
                .doOnError(e -> LogUtil.logger.error("Error deleting facility ID {}: {}", id, e.getMessage()))
                .onErrorMap(WebClientResponseException.class, e ->
                        new EhrApiError(e.getResponseBodyAsString(), e.getStatusCode()))
                .onErrorMap(Exception.class, e -> new EhrApiError(
                        "Unexpected error occurred while deleting facility.", HttpStatus.INTERNAL_SERVER_ERROR
                ));
    }

    /**
     * Validates a {@link FacilityDTO} object to ensure all required fields are present and valid.
     *
     * <br>This method performs a series of checks on a facility object before it is processed,
     * ensuring that all necessary attributes are properly set.
     *
     * <h2>Validation Rules:</h2>
     * <ul>
     *     <li><b>Basic Information</b> - Must be present.</li>
     *     <li><b>Contact Information</b> - Must be present.</li>
     *     <li><b>Facility Details</b> - Must be present.</li>
     *     <li><b>Account ID</b> - Cannot be null or empty.</li>
     *     <li><b>Region</b> - Must be a valid enum value of {@link Region}.</li>
     * </ul>
     *
     * <h2>Workflow:</h2>
     * <ol>
     *     <li>Checks if the facility object is null; if so, returns an error.</li>
     *     <li>Validates that all required fields are populated.</li>
     *     <li>Verifies that the region type corresponds to a valid {@link Region} enum value.</li>
     *     <li>Returns the validated facility wrapped in a {@code Mono}.</li>
     * </ol>
     *
     * <h2>Example Usage:</h2>
     * <pre>{@code
     * Facility facility = new Facility();
     * facility.setBasicInformation(new BasicInformation("regionCode"));
     * facility.setContactInformation(new ContactInformation("email@example.com"));
     * facility.setFacilityDetails(new FacilityDetails("Hospital"));
     * facility.setAccountId("account123");
     *
     * facilityService.validateFacility(facility)
     *         .subscribe(validatedFacility -> System.out.println("Facility is valid!"));
     * }</pre>
     *
     * <h2>Error Handling:</h2>
     * <ul>
     *     <li>Throws a {@link ValidationError} if any required field is missing or invalid.</li>
     *     <li>Ensures the provided region type is a valid {@link Region} value.</li>
     * </ul>
     *
     * @param facilityDTO The facility object to validate.
     * @return A {@code Mono<Facility>} containing the validated facility object.
     * @throws ValidationError If any validation check fails.
     */
    private Mono<FacilityDTO> validateFacility(FacilityDTO facilityDTO) {
        return Mono.justOrEmpty(facilityDTO)
                .switchIfEmpty(Mono.error(new ValidationError("Facility data cannot be null.")))
                .flatMap(fac -> {
                    if (fac.getBasicInformation() == null) return Mono.error(new ValidationError("Basic information is required."));
                    if (fac.getContactInformation() == null) return Mono.error(new ValidationError("Contact information is required."));
                    if (fac.getFacilityDetails() == null) return Mono.error(new ValidationError("Facility details are required."));
                    if (fac.getAccountId() == null || fac.getAccountId().trim().isEmpty()) return Mono.error(new ValidationError("Account ID is required."));
                    return isValidEnum(fac.getBasicInformation().getRegion(), Region.class)
                            ? Mono.just(fac)
                            : Mono.error(new ValidationError("Invalid Region Type."));
                });
    }
}