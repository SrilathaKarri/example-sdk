package sdk.facility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import sdk.base.errors.EhrApiError;
import sdk.base.errors.ValidationError;
import sdk.base.utils.Constants;
import sdk.facility.dto.*;
import sdk.facility.service.FacilityDemographic;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@DisplayName("FacilityService BDD Test")
public class FacilityTest {

    @Mock
    private Facility facility;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private FacilityDemographic facilityDemographic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject mock demographic service using reflection
        try {
            java.lang.reflect.Field field = Facility.class.getDeclaredField("facilityDemographicService");
            field.setAccessible(true);
            field.set(facility, facilityDemographic);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock", e);
        }
    }

    @Test
    @DisplayName("Given a valid request, when getAllFacilities is called, then it should return a success response")
    void givenValidRequest_whenGetAllFacilities_thenReturnSuccess() {
        // Given
        Integer pageSize = 20;
        String nextPage = "abc123xyz789";
        Map<String, Object> mockResponse = Map.of("data", "facility list");

        willReturn(Mono.just(mockResponse))
                .given(facility)
                .get(any(String.class), any(Map.class), any(ParameterizedTypeReference.class));

        // When
        Mono<Object> result = facility.getAllFacilities(nextPage, pageSize);

        // Then
        then(facility).should().get(any(String.class), any(Map.class), any(ParameterizedTypeReference.class));

        StepVerifier.create(result)
                .expectNext(mockResponse)
                .verifyComplete();
    }

    @Test
    @DisplayName("Given pagination token and page size, when getAllFacilities is called, then return success response")
    void givenPaginationTokenAndPageSize_whenGetAllFacilities_thenReturnSuccess() {
        // Given
        String nextPage = "token123";
        Integer pageSize = 20;
        Map<String, Object> mockResponse = Map.of(
                "status", "Success",
                "data", List.of(Map.of("id", "facility123", "name", "Test Facility"))
        );

        willReturn(Mono.just(mockResponse))
                .given(facility)
                .get(any(String.class), any(Map.class), any(ParameterizedTypeReference.class));

        // When
        Mono<Object> result = facility.getAllFacilities(nextPage, pageSize);

        // Then
        then(facility).should().get(any(String.class), any(Map.class), any(ParameterizedTypeReference.class));

        StepVerifier.create(result)
                .expectNext(mockResponse)
                .verifyComplete();
    }

    @Test
    @DisplayName("Given a valid facility ID and type, when getFacilityById is called, then return facility data")
    void givenValidFacilityIdAndType_whenGetFacilityById_thenReturnFacilityData() {
        // Given
        String id = "IN123";
        String idType = "FACILITY_ID";
        String expectedMessage = "Facility Found !!!";

        Map<String, Object> mockResponse = Map.of(
                "message", expectedMessage,
                "data", Collections.singletonList(Map.of())
        );

        willReturn(Mono.just(mockResponse))
                .given(facility)
                .get(any(String.class), any(Map.class), any(ParameterizedTypeReference.class));

        // When
        Mono<Object> result = facility.getFacilityById(idType, id);

        // Then
        then(facility).should().get(any(String.class), any(Map.class), any(ParameterizedTypeReference.class));

        StepVerifier.create(result)
                .expectNext(mockResponse)
                .verifyComplete();
    }

    @Test
    @DisplayName("Given a valid facility ID, when facilityExists is called, then return true")
    void givenValidFacilityId_whenFacilityExists_thenReturnTrue() {
        // Given
        String id = "IN645635";
        String idType = "FACILITY_ID";
        Map<String, Object> mockResponse = Map.of("message", Constants.FACILITY_FOUND_MESSAGE);

        willReturn(Mono.just(mockResponse))
                .given(facility)
                .getFacilityById(idType, id);

        // When
        Mono<Boolean> result = facility.facilityExists(idType, id);

        // Then
        then(facility).should().getFacilityById(idType, id);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Given demographic data exists, when getDemographicData is called, then return facility demographics")
    void givenDemographicDataExists_whenGetDemographicData_thenReturnFacilityDemographics() {
        // Given
        ResponseDTOs.FacilityDemographicsDTO expectedDto = new ResponseDTOs.FacilityDemographicsDTO();
        expectedDto.setStateCode("KA");
        expectedDto.setDistrictCode("BLR");

        given(facilityDemographic.getDemographicData()).willReturn(Mono.just(expectedDto));

        // When
        Mono<ResponseDTOs.FacilityDemographicsDTO> result = facility.getDemographicData();

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto -> "KA".equals(dto.getStateCode()) && "BLR".equals(dto.getDistrictCode()))
                .verifyComplete();

        then(facilityDemographic).should().getDemographicData();
    }

    @Test
    @DisplayName("Given a valid facility, when registerFacility is called, then it should return a success message")
    void givenValidFacility_whenRegisterFacility_thenReturnSuccessMessage() {
        // Given
        FacilityDTO newFacilityDTO = createValidFacility();
        String expectedMessage = "Facility Registered Successfully";

        willReturn(Mono.just(expectedMessage))
                .given(facility)
                .post(eq(Constants.REGISTER_FACILITY_URL), any(FacilityDTO.class), any(ParameterizedTypeReference.class));

        // When
        Mono<Object> result = facility.registerFacility(newFacilityDTO);

        // Then
        then(facility).should()
                .post(eq(Constants.REGISTER_FACILITY_URL), any(FacilityDTO.class), any(ParameterizedTypeReference.class));

        StepVerifier.create(result)
                .expectNextMatches(response -> response instanceof String && expectedMessage.equals(response))
                .verifyComplete();
    }

    @Test
    @DisplayName("Given valid update data, when updateSpocForFacility is called, then it should return a success message")
    void givenValidUpdateData_whenUpdateSpocForFacility_thenReturnSuccessMessage() {
        // Given
        UpdateSpocForFacility updateData = createValidUpdateSpocData();
        String expectedMessage = "Facility Updated Successfully";

        willReturn(Mono.just(expectedMessage))
                .given(facility)
                .put(eq(Constants.UPDATE_FACILITY_URL), eq(updateData), any(ParameterizedTypeReference.class));

        // When
        Mono<Object> result = facility.updateSpocForFacility(updateData);

        // Then
        then(facility).should()
                .put(eq(Constants.UPDATE_FACILITY_URL), eq(updateData), any(ParameterizedTypeReference.class));

        StepVerifier.create(result)
                .expectNextMatches(response -> response instanceof String && expectedMessage.equals(response))
                .verifyComplete();
    }
    @Test
    @DisplayName("Given a valid facility ID, when deleteFacility is called, then it should complete without errors")
    void givenValidFacilityId_whenDeleteFacility_thenCompleteSuccessfully() {
        // Given
        String facilityId = "IN0483904";

        willReturn(Mono.empty())
                .given(facility)
                .delete(contains("/facility/" + facilityId), any(ParameterizedTypeReference.class));

        // When
        Mono<Void> result = facility.deleteFacility(facilityId);

        // Then
        then(facility).should()
                .delete(contains("/facility/" + facilityId), any(ParameterizedTypeReference.class));

        StepVerifier.create(result)
                .verifyComplete();
    }

    //Negative testCases
    @Test
    @DisplayName("Given a network error, when fetching all facilities, then an error should be thrown")
    void givenNetworkError_whenGetAllFacilities_thenThrowError() {

        willReturn(Mono.error(new RuntimeException("Network error")))
                .given(facility)
                .get(any(String.class), anyMap(), any());

        // When
        Mono<Object> result = facility.getAllFacilities(null, 10);

        // Then
        then(facility).should().get(any(String.class), anyMap(), any());

        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                        error.getMessage().contains("Error fetching facilities"))
                .verify();
    }


    @Test
    @DisplayName("Given an API failure, when fetching all facilities, then return an error")
    void givenApiFailure_whenGetAllFacilities_thenReturnError() {
        // Given
        Integer pageSize = 10;
        String nextPage = "token123";

        willReturn(Mono.error(new RuntimeException("Api error")))
                .given(facility)
                .get(any(String.class), anyMap(), any());

        // When
        Mono<Object> result = facility.getAllFacilities(nextPage, pageSize);

        // Then
        then(facility).should().get(any(String.class), anyMap(), any());

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Error fetching facilities"))
                .verify();
    }

    @Test
    @DisplayName("Given a valid facility ID, when no facility is found, then return an empty list")
    void givenValidFacilityId_whenNoFacilityFound_thenReturnEmptyList() {
        // Given
        String id = "IN123";
        String idType = "FACILITY_ID";
        String expectedMessage = "Facility Found !!!";

        Map<String, Object> expectedResponse = Map.of(
                "message", expectedMessage,
                "data", Collections.emptyList()
        );

        willReturn(Mono.just(expectedResponse))
                .given(facility)
                .get(any(String.class), any(Map.class), any(ParameterizedTypeReference.class));

        // When
        Mono<Object> result = facility.getFacilityById(idType, id);

        // Then
        then(facility).should().get(any(String.class), any(Map.class), any(ParameterizedTypeReference.class));

        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    @DisplayName("Given a facility ID, when an unexpected error occurs, then a RuntimeException should be thrown with a proper message")
    void givenFacilityId_whenUnexpectedErrorOccurs_thenThrowRuntimeException() {
        // Given
        String id = "IN123";
        String idType = "FACILITY_ID";

        willReturn(Mono.error(new RuntimeException("Unexpected error")))
                .given(facility)
                .get(any(String.class), any(Map.class), any(ParameterizedTypeReference.class));

        // When
        Mono<Object> result = facility.getFacilityById(idType, id);

        // Then
        then(facility).should().get(any(String.class), any(Map.class), any(ParameterizedTypeReference.class));

        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                "Unexpected error".equals(error.getMessage()))
                .verify();
    }

    @Test
    @DisplayName("Given a null or empty facility ID, when fetching facility, then throw a ValidationError wrapped in EhrApiError")
    void givenNullOrEmptyFacilityId_whenFetchingFacility_thenThrowEhrApiError() {
        // Given
        String idType = "FACILITY_ID";

        // When & Then
        StepVerifier.create(facility.getFacilityById(idType, null))
                .expectErrorMatches(error ->
                        error instanceof EhrApiError &&
                                "Facility ID and ID Type cannot be null or empty.".equals(error.getMessage()))
                .verify();

        StepVerifier.create(facility.getFacilityById(idType, null))
                .expectErrorMatches(error ->
                        error instanceof EhrApiError &&
                                "Facility ID and ID Type cannot be null or empty.".equals(error.getMessage()))
                .verify();

        StepVerifier.create(facility.getFacilityById(idType, null))
                .expectErrorMatches(error ->
                        error instanceof EhrApiError &&
                                "Facility ID and ID Type cannot be null or empty.".equals(error.getMessage()))
                .verify();
    }

    @Test
    @DisplayName("Given an invalid or non-existing facility, when checking existence, then return false")
    void givenInvalidOrNonExistingFacility_whenCheckingExistence_thenReturnFalse() {
        // Given
        Map<String, Object> response = Map.of("message", "Facility not found");

        // Mocking the getFacilityById method to return a response indicating facility not found
        given(facility.getFacilityById(anyString(), anyString()))
                .willReturn(Mono.just(response));

        // When & Then
        StepVerifier.create(facility.facilityExists("FACILITY_ID", "invalid"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Given a null or empty facility ID, when checking existence, then throw ValidationError")
    void givenNullOrEmptyFacilityId_whenCheckingExistence_thenThrowValidationError() {
        // Then
        StepVerifier.create(facility.facilityExists("ACCOUNT_ID", null))
                .expectErrorMatches(error ->
                        error instanceof ValidationError &&
                                "Please provide a valid ID and ID Type for checking if the facility exists.".equals(error.getMessage()))
                .verify();

        StepVerifier.create(facility.facilityExists("FACILITY_ID", ""))
                .expectErrorMatches(error ->
                        error instanceof ValidationError &&
                                "Please provide a valid ID and ID Type for checking if the facility exists.".equals(error.getMessage()))
                .verify();

        StepVerifier.create(facility.facilityExists(null, "id"))
                .expectErrorMatches(error ->
                        error instanceof ValidationError &&
                                "Please provide a valid ID and ID Type for checking if the facility exists.".equals(error.getMessage()))
                .verify();
    }

    @Test
    @DisplayName("Given an error from facilityDemographicService, when fetching demographic data, then throw RuntimeException with proper message")
    void givenErrorFromDemographicService_whenFetchingData_thenThrowRuntimeException() {
        // Given
        when(facilityDemographic.getDemographicData())
                .thenReturn(Mono.error(new RuntimeException("Failed to get demographic data")));

        // When
        Mono<ResponseDTOs.FacilityDemographicsDTO> result = facility.getDemographicData();

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                "Failed to get demographic data".equals(error.getMessage()))
                .verify();

        verify(facilityDemographic).getDemographicData();
    }

    @Test
    @DisplayName("Given an invalid facility, when registering, then throw ValidationError for missing basic information")
    void givenInvalidFacility_whenRegistering_thenThrowValidationError() {
        // Given
        FacilityDTO invalidFacilityDTO = new FacilityDTO();
        invalidFacilityDTO.setFacilityId("IN4834324"); // Missing required fields

        given(facility.registerFacility(invalidFacilityDTO))
                .willReturn(Mono.error(new ValidationError("Basic information is required")));

        // When & Then
        StepVerifier.create(facility.registerFacility(invalidFacilityDTO))
                .expectErrorMatches(error ->
                        error instanceof ValidationError &&
                                "Basic information is required".equals(error.getMessage()))
                .verify();
    }

    @Test
    @DisplayName("Given a WebClient error, when registering facility, then throw WebClientResponseException with HTTP status 400")
    void givenWebClientError_whenRegisteringFacility_thenThrowWebClientResponseException() {
        // Given
        FacilityDTO newFacilityDTO = createValidFacility();
        WebClientResponseException webError = WebClientResponseException.create(
                400, "Bad Request", null, null, null);

        given(facility.registerFacility(newFacilityDTO))
                .willReturn(Mono.error(webError));

        // When & Then
        StepVerifier.create(facility.registerFacility(newFacilityDTO))
                .expectErrorMatches(error ->
                        error instanceof WebClientResponseException &&
                                ((WebClientResponseException) error).getStatusCode() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    @DisplayName("Given a generic error, when registering facility, then throw RuntimeException")
    void givenGenericError_whenRegisteringFacility_thenThrowRuntimeException() {
        // Given
        FacilityDTO newFacilityDTO = createValidFacility();

        given(facility.registerFacility(newFacilityDTO))
                .willReturn(Mono.error(new RuntimeException("Unexpected error")));

        // When & Then
        StepVerifier.create(facility.registerFacility(newFacilityDTO))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                "Unexpected error".equals(error.getMessage()))
                .verify();
    }


    @Test
    @DisplayName("Given an invalid region, when registering facility, then throw ValidationError")
    void givenInvalidRegion_whenRegisteringFacility_thenThrowValidationError() {
        // Given
        FacilityDTO facilityDTO = createValidFacility();
        facilityDTO.getBasicInformation().setRegion("INVALID_REGION");

        given(this.facility.registerFacility(facilityDTO))
                .willReturn(Mono.error(new ValidationError("Invalid Region Type")));

        // When & Then
        StepVerifier.create(this.facility.registerFacility(facilityDTO))
                .expectErrorMatches(error ->
                        error instanceof ValidationError &&
                                "Invalid Region Type".equals(error.getMessage()))
                .verify();
    }

    @Test
    @DisplayName("Given null facility data, when registering facility, then throw ValidationError")
    void givenNullFacilityData_whenRegistering_thenThrowValidationError() {
        // When & Then
        StepVerifier.create(facility.registerFacility(null))
                .expectErrorMatches(error ->
                        error instanceof ValidationError &&
                                "Facility data cannot be null.".equals(error.getMessage()))
                .verify();
    }


    @Test
    @DisplayName("Given null update data, when updating SPOC, then throw IllegalArgumentException")
    void givenNullUpdateData_whenUpdatingSpoc_thenThrowIllegalArgumentException() {
        // Given
        given(facility.updateSpocForFacility(null))
                .willReturn(Mono.error(new IllegalArgumentException("Update facility data cannot be null.")));

        // When & Then
        StepVerifier.create(facility.updateSpocForFacility(null))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                "Update facility data cannot be null.".equals(error.getMessage()))
                .verify();
    }

    @Test
    @DisplayName("Given missing facility ID, when updating SPOC, then throw ValidationError")
    void givenMissingFacilityId_whenUpdatingSpoc_thenThrowValidationError() {
        // Given
        UpdateSpocForFacility invalidData = new UpdateSpocForFacility();
        invalidData.setSpocName("Janatha");
        invalidData.setSpocId("12345");

        // When & Then
        StepVerifier.create(facility.updateSpocForFacility(invalidData))
                .expectErrorMatches(error -> error instanceof ValidationError &&
                        "Facility ID is required for updating.".equals(error.getMessage()))
                .verify();
    }

    @Test
    @DisplayName("Given null facility ID, when deleting facility, then throw ValidationError")
    void givenNullFacilityId_whenDeletingFacility_thenThrowValidationError() {
        // When & Then
        StepVerifier.create(facility.deleteFacility(null))
                .expectErrorMatches(error -> error instanceof ValidationError &&
                        "Facility ID cannot be null or empty.".equals(error.getMessage()))
                .verify();
    }

    @Test
    @DisplayName("Given empty facility ID, when deleting facility, then throw ValidationError")
    void givenEmptyFacilityId_whenDeletingFacility_thenThrowValidationError() {
        // When & Then
        StepVerifier.create(facility.deleteFacility(""))
                .expectErrorMatches(error -> error instanceof ValidationError &&
                        "Facility ID cannot be null or empty.".equals(error.getMessage()))
                .verify();
    }

    @Test
    @DisplayName("Given an invalid facility ID, when deleteFacility is called, then it should throw EhrApiError")
    void givenInvalidFacilityId_whenDeleteFacility_thenThrowEhrApiError() {
        // Given
        String facilityId = "IN0483904";
        WebClientResponseException notFoundError = WebClientResponseException.create(
                404, "Not Found", null, null, null);

        willReturn(Mono.error(notFoundError))
                .given(facility)
                .delete(contains("/facility/" + facilityId), any(ParameterizedTypeReference.class));

        // When
        Mono<Void> result = facility.deleteFacility(facilityId);

        // Then
        then(facility).should()
                .delete(contains("/facility/" + facilityId), any(ParameterizedTypeReference.class));

        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof EhrApiError &&
                                error.getMessage().contains("Unexpected error occurred while deleting facility"))
                .verify();
    }

    // Helper methods to create test data
    private FacilityDTO createValidFacility() {
        FacilityDTO facilityDTO = new FacilityDTO();

        BasicInformation basicInfo = new BasicInformation();
        basicInfo.setFacilityName("Apollo Hospital");
        basicInfo.setRegion("URBAN");
        basicInfo.setAddressLine1("123 Main Street");
        basicInfo.setCity("Hindupur");
        basicInfo.setState("ANDHRA PRADESH");
        basicInfo.setDistrict("Ananthapuramu");
        basicInfo.setCountry("India");
        basicInfo.setPincode("110001");
        facilityDTO.setBasicInformation(basicInfo);

        ContactInformation contactInfo = new ContactInformation();
        contactInfo.setMobileNumber("+911234567890");
        contactInfo.setEmail("apollo@gmail.com");
        facilityDTO.setContactInformation(contactInfo);

        FacilityDetails facilityDetails = new FacilityDetails();
        facilityDetails.setOwnershipType("Private");
        facilityDetails.setStatus("Active");
        facilityDTO.setFacilityDetails(facilityDetails);

        facilityDTO.setFacilityId("IN4834324");
        facilityDTO.setId("12345");
        facilityDTO.setAccountId("ACC001");

        return facilityDTO;
    }

    private UpdateSpocForFacility createValidUpdateSpocData() {
        UpdateSpocForFacility updateData = new UpdateSpocForFacility();
        updateData.setId("6789035");
        updateData.setSpocName("Janatha");
        updateData.setSpocId("SPOC123");
        updateData.setConsentManagerName("Shri Ram");
        updateData.setConsentManagerId("CM456");
        return updateData;
    }
}