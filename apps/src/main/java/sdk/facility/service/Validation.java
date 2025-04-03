package sdk.facility.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple3;
import sdk.base.Base;
import sdk.base.errors.EhrApiError;
import sdk.base.errors.ErrorType;
import sdk.base.utils.Constants;
import sdk.facility.dto.ResponseDTOs;
import sdk.facility.enums.Region;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

import static sdk.base.errors.EhrApiError.mapHttpStatusToErrorType;

@Service
public class Validation extends Base {

    private static final Scanner scanner = new Scanner(System.in);

    @Value("${api.url}")
    private String apiURL;

    @Value("${api.key}")
    private String apiKey;

    @Value("${google.api.key}")
    private String googleApiKey;

    @Autowired
    private WebClient webClient;

    protected Validation(ObjectMapper objectMapper, WebClient webClient) {
        super(objectMapper, webClient);
    }

    public <T> Mono<T> fetchData(String endpoint, ParameterizedTypeReference<T> typeReference) {
        return webClient.get()
                .uri(apiURL + endpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("Client error occurred")
                                .flatMap(body -> Mono.error(new EhrApiError("Client error: " + body, mapHttpStatusToErrorType(response.statusCode()))))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("Server error occurred")
                                .flatMap(body -> Mono.error(new EhrApiError("Server error: " + body, mapHttpStatusToErrorType(response.statusCode()))))
                )
                .bodyToMono(typeReference);
    }

    public <T, R> Mono<R> makePostRequestForData(String endpoint, T requestBody, ParameterizedTypeReference<R> responseType) {
        return webClient.post()
                .uri(apiURL + endpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("Client error occurred")
                                .flatMap(body -> Mono.error(new EhrApiError("Client error: " + body, mapHttpStatusToErrorType(response.statusCode()))))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("Server error occurred")
                                .flatMap(body -> Mono.error(new EhrApiError("Server error: " + body, mapHttpStatusToErrorType(response.statusCode()))))
                )
                .bodyToMono(responseType)
                .timeout(Duration.ofSeconds(5));
    }


    public Mono<Map<String, String>> fetchCodeValuePairs(String url) {
        return fetchData(url, new ParameterizedTypeReference<ResponseDTOs.TypeDataResponseDTO>() {})
                .map(response -> response.getData().stream()
                        .collect(Collectors.toMap(ResponseDTOs.CodeValueDTO::getCode, ResponseDTOs.CodeValueDTO::getValue))
                );
    }

    public Mono<Map<String, String>> fetchCodeValuePairs(String url, Map<String, String> requestBody) {
        return makePostRequestForData(url, requestBody, new ParameterizedTypeReference<ResponseDTOs.TypeDataResponseDTO>() {})
                .map(response -> response.getData().stream()
                        .collect(Collectors.toMap(ResponseDTOs.CodeValueDTO::getCode, ResponseDTOs.CodeValueDTO::getValue))
                );
    }


    public Mono<List<ResponseDTOs.StateDTO>> fetchStates() {
        return get(Constants.FETCH_STATES_URL, Map.of(), new ParameterizedTypeReference<>() {
        });
    }


    public Mono<List<ResponseDTOs.DistrictDTO>> fetchSubDistricts(Integer districtCode) {
        if (districtCode == null) {
            return Mono.error(new IllegalArgumentException("District code cannot be null"));
        }
        String url = String.format("%s%s", Constants.FETCH_SUB_DISTRICTS_URL,
                String.format(Constants.DISTRICT_CODE_PARAM, districtCode));

        return fetchData(url, new ParameterizedTypeReference<>() {});
    }

    public Mono<Tuple3<String, String, String>> fetchAndPromptLocation() {
        return promptForValidLocation()
                .flatMap(this::fetchLatitudeLongitude)
                .doOnSuccess(latLong -> System.out.printf(
                        "Latitude: %s,\nLongitude: %s\nFor location: %s%n",
                        latLong.getT1(), latLong.getT2(), latLong.getT3()))
                .onErrorResume(ex -> {
                    System.out.println(ex.getMessage());
                    return fetchAndPromptLocation();
                });
    }

    private Mono<Tuple3<String, String, String>> fetchLatitudeLongitude(String location) {
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String fullUrl = Constants.GOOGLE_LOCATION_URL + "?address=" + encodedLocation + "&key=" + googleApiKey;

        return webClient.get()
                .uri(fullUrl)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseDTOs.LocationResponseDTO>() {
                })
                .flatMap(response -> {
                    if (response == null || response.getResults().isEmpty()) {
                        return Mono.error(new EhrApiError(
                                "No data found for this location. Please enter a valid location.",
                                ErrorType.NOT_FOUND));
                    }

                    var locationData = response.getResults().get(0).getGeometry().getLocation();
                    return Mono.just(reactor.util.function.Tuples.of(
                            String.valueOf(locationData.getLat()),
                            String.valueOf(locationData.getLng()),
                            location
                    ));
                })
                .onErrorResume(WebClientResponseException.class, err -> {
                    HttpStatus status = HttpStatus.resolve(err.getStatusCode().value());
                    if (status == HttpStatus.NOT_FOUND) {
                        return Mono.error(new EhrApiError("Google API returned 404 - Location not found", ErrorType.NOT_FOUND));
                    } else if (status != null && status.is4xxClientError()) {
                        return Mono.error(new EhrApiError("Google API client error: " + err.getMessage(), ErrorType.VALIDATION));
                    } else if (status != null && status.is5xxServerError()) {
                        return Mono.error(new EhrApiError("Google API server error: " + err.getMessage(), ErrorType.INTERNAL_SERVER_ERROR));
                    } else {
                        return Mono.error(new EhrApiError("Google API error: " + err.getMessage(), ErrorType.CONNECTION));
                    }
                });
    }

    public Mono<Map<String, String>> fetchValidAddressProofTypes() {
        return fetchCodeValuePairs(Constants.FETCH_ADDRESS_PROOF_URL);
    }

    public Mono<Map<String, String>> fetchValidOwnershipTypes() {
        return fetchCodeValuePairs(Constants.FETCH_OWNERSHIP_TYPE_URL);
    }

    public Mono<Map<String, String>> fetchValidOwnershipSubTypes(String ownershipTypeCode) {
        return fetchCodeValuePairs(Constants.FETCH_OWNERSHIP_SUBTYPE_URL, Map.of("ownershipCode", ownershipTypeCode));
    }

    public Mono<Map<String, String>> fetchValidFacilityStatusTypes() {
        return fetchCodeValuePairs(Constants.FETCH_FACILITY_STATUS_URL);
    }

    public Mono<Map<String, String>> fetchValidSystemOfMedicineCodes() {
        return fetchCodeValuePairs(Constants.FETCH_SYSTEM_OF_MEDICINE_URL);
    }

    public Mono<Map<String, String>> fetchSpecialitiesForMedicine(String systemOfMedicineCode) {
        return fetchCodeValuePairs(Constants.FETCH_SPECIALITIES_URL, Map.of("systemOfMedicineCode", systemOfMedicineCode));
    }

    public Mono<Map<String, String>> fetchValidFacilityTypes(String ownershipCode) {
        return fetchCodeValuePairs(Constants.FETCH_FACILITY_TYPE_URL, Map.of("ownershipCode", ownershipCode));
    }

    public Mono<Map<String, String>> fetchValidFacilitySubTypes(String facilityTypeCode) {
        return fetchCodeValuePairs(Constants.FETCH_FACILITY_SUBTYPE_URL, Map.of("facilityTypeCode", facilityTypeCode));
    }

    public Mono<Map<String, String>> fetchValidServiceTypes() {
        return fetchCodeValuePairs(Constants.FETCH_SERVICE_TYPES_URL);
    }

    public Mono<Map<String, String>> fetchValidGeneralInfoOptions() {
        return fetchCodeValuePairs(Constants.FETCH_GENERAL_INFO_OPTIONS_URL)
                .map(data -> data.entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().trim().toLowerCase(),
                                entry -> entry.getValue().trim().toLowerCase()
                        )));
    }

    public Mono<Map<String, String>> fetchValidImagingCenterServices() {
        return fetchCodeValuePairs(Constants.FETCH_IMAGING_CENTER_SERVICES_URL);
    }


    public Mono<String> promptUser(String message) {
        return Mono.fromCallable(() -> {
                    System.out.println(message);
                    synchronized (scanner) {
                        return scanner.nextLine();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<String>> promptMultipleInputs(String message) {
        return Mono.fromCallable(() -> {
                    System.out.println(message);
                    synchronized (scanner) {
                        return List.of(scanner.nextLine().split(","));
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    // Generic method to prompt for user input and validate it against a list
    private <T> Mono<String> promptForValidOption(
            String message,
            List<T> options,
            Function<T, String> nameExtractor,
            Function<T, String> codeExtractor,
            String errorMessage) {

        return promptUser(message)
                .map(input -> validateOption(input, options, nameExtractor, codeExtractor, errorMessage))
                .onErrorResume(ex -> {
                    System.out.println(ex.getMessage());
                    return promptForValidOption(message, options, nameExtractor, codeExtractor, errorMessage);
                });
    }

    // Generic method to prompt for user input and validate it against a map
    private Mono<String> promptForValidOptionFromMap(
            String message,
            Map<String, String> options,
            String errorMessage) {

        return promptUser(message)
                .map(input -> validateOption(input, options, errorMessage))
                .onErrorResume(ex -> {
                    System.out.println(ex.getMessage());
                    return promptForValidOptionFromMap(message, options, errorMessage);
                });
    }

    public Mono<String> promptForValidState(List<ResponseDTOs.StateDTO> states) {
        return promptForValidOption(
                "Please enter a state from the list above:",
                states,
                ResponseDTOs.StateDTO::getName,
                ResponseDTOs.StateDTO::getCode,
                "Invalid state selection"
        );
    }

    public Mono<String> promptForValidDistrict(List<ResponseDTOs.DistrictDTO> districts) {
        return promptForValidOption(
                "Please enter a district from the list above:",
                districts,
                ResponseDTOs.DistrictDTO::getName,
                ResponseDTOs.DistrictDTO::getCode,
                "Invalid district selection"
        );
    }

    public Mono<String> promptForValidSubDistrict(List<ResponseDTOs.DistrictDTO> subDistricts) {
        return promptForValidOption(
                "Please enter a sub district from the list above:",
                subDistricts,
                ResponseDTOs.DistrictDTO::getName,
                ResponseDTOs.DistrictDTO::getCode,
                "Invalid sub-district selection"
        );
    }

    public Mono<String> promptForValidRegion() {
        return promptUser("Please select a region (URBAN/RURAL):")
                .map(regionInput -> {
                    try {
                        return Region.fromString(regionInput) == Region.URBAN ? "U" : "R";
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid region selection. Please enter URBAN or RURAL.");
                    }
                })
                .onErrorResume(ex -> {
                    System.out.println(ex.getMessage());
                    return promptForValidRegion();
                });
    }

    public Mono<String> promptForValidLocation() {
        return promptUser("Enter the Location: ")
                .map(String::trim)
                .filter(location -> !location.isEmpty())
                .switchIfEmpty(Mono.defer(() -> {
                    System.out.println("Location cannot be empty. Please enter a valid location.");
                    return promptForValidLocation();
                }));
    }

    public Mono<String> promptForValidAddressProof(Map<String, String> validProofs) {
        return promptForValidOptionFromMap(
                "Please enter an Address Proof from the list above:",
                validProofs,
                "Invalid address proof selection"
        );
    }

    public Mono<String> promptForValidOwnershipType(Map<String, String> validOwnerships) {
        return promptForValidOptionFromMap(
                "Please enter an Ownership Type from the list above:",
                validOwnerships,
                "Invalid ownership type selection"
        );
    }

    public Mono<String> promptForValidOwnershipSubType(Map<String, String> ownershipSubTypes) {
        return promptForValidOptionFromMap(
                "Please enter an Ownership Sub Type from the list above:",
                ownershipSubTypes,
                "Invalid ownership sub type selection"
        );
    }

    public Mono<String> promptForValidFacilityStatusType(Map<String, String> facilityStatusTypes) {
        return promptForValidOptionFromMap(
                "Please enter a Facility Status Type from the list above:",
                facilityStatusTypes,
                "Invalid Facility status type selection"
        );
    }

    public Mono<String> promptForValidFacilityType(Map<String, String> facilityTypes) {
        return promptForValidOptionFromMap(
                "Please enter a facility type from the list above:",
                facilityTypes,
                "Invalid Facility Type"
        );
    }

    public Mono<String> promptForValidFacilitySubType(Map<String, String> facilitySubTypes) {
        return promptForValidOptionFromMap(
                "Please enter a facility sub type from the list above:",
                facilitySubTypes,
                "Invalid Facility Sub Type"
        );
    }

    public Mono<String> promptForValidSystemOfMedicine(Map<String, String> systemOfMedicines) {
        return promptForValidOptionFromMap(
                "Please enter a System of Medicine from the list above:",
                systemOfMedicines,
                "Invalid system of medicine type selection"
        );
    }

    public Mono<List<String>> promptForValidSpecialities(Map<String, String> specialities) {
        return promptMultipleInputs("Please select specialities from the list above (comma-separated):")
                .map(selectedSpecialities -> selectedSpecialities.stream()
                        .map(String::trim)
                        .map(speciality -> validateOption(speciality, specialities, "Speciality"))
                        .collect(Collectors.toList()))
                .onErrorResume(ex -> {
                    System.out.println(ex.getMessage());
                    return promptForValidSpecialities(specialities);
                });
    }

    public Mono<String> promptForValidServiceType(Map<String, String> serviceTypes) {
        return promptForValidOptionFromMap(
                "Please enter a type of service from the list above:",
                serviceTypes,
                "Invalid Type of Service"
        );
    }

    public Mono<String> promptForValidImagingService(Map<String, String> imagingOptions) {
        return promptForValidOptionFromMap(
                "Please select imaging service from the list above:",
                imagingOptions,
                "Invalid Imaging Service"
        );
    }

    public Mono<String> promptForValidGeneralOption(String question, Map<String, String> generalOptions) {
        return promptForValidOptionFromMap(
                String.format("%s (Enter Code or Name)", question),
                generalOptions,
                "Invalid input"
        );
    }

    private String normalizeString(String input) {
        return input == null ? "" : input.replaceAll("\\s+", "").toLowerCase();
    }

    private String validateOption(String input, Map<String, String> options, String errorMessage) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty. Please provide a valid option.");
        }

        String cleanedInput = normalizeString(input);

        if (options.containsKey(cleanedInput)) {
            return cleanedInput;
        }

        return options.entrySet().stream()
                .filter(entry -> normalizeString(entry.getValue()).equalsIgnoreCase(cleanedInput))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("%s '%s' does not exist.", errorMessage, input)));
    }

    private <T> String validateOption(String input, List<T> options, Function<T, String> nameExtractor, Function<T, String> codeExtractor, String errorMessage) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty. Please provide a valid option.");
        }

        String cleanedInput = normalizeString(input);

        return options.stream()
                .filter(option -> normalizeString(nameExtractor.apply(option)).equalsIgnoreCase(cleanedInput))
                .map(codeExtractor)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("%s '%s' does not exist.", errorMessage, input)));
    }


    public Mono<List<ResponseDTOs.DistrictDTO>> getDistrictsFromState(String stateCode, List<ResponseDTOs.StateDTO> states) {
        return Mono.fromCallable(() ->
                states.stream()
                        .filter(state -> state.getCode().equals(stateCode))
                        .findFirst()
                        .map(ResponseDTOs.StateDTO::getDistricts)
                        .orElse(Collections.emptyList())
        );
    }
}
