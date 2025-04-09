package sdk.patient;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import sdk.base.enums.Gender;
import sdk.base.errors.ValidationError;
import sdk.patient.DTO.PatientDTO;
import sdk.patient.DTO.UpdatePatientDTO;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


/**
 * Unit tests for the {@link Patient} service class.
 * <p>
 * This class contains both positive and negative test cases to validate the behavior of the
 * {@link Patient} class methods. It covers scenarios such as creating, updating, deleting,
 * retrieving patients, and error handling.
 * </p>
 *
 * Uses Mockito for mocking dependencies and Reactor's StepVerifier for reactive assertions.
 */
@MockitoSettings(strictness = Strictness.WARN)
public class PatientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Spy
    @InjectMocks
    private Patient patient;

    /**
     * Initializes mock objects and sets up default behaviors before each test.
     * Mocks the WebClient and defines default return values for API calls.
     */
    @BeforeEach
    public void setUp() {
        given(webClientBuilder.build()).willReturn(webClient);
        doReturn(Mono.empty()).when(patient).post(anyString(), any(), any());
        doReturn(Mono.empty()).when(patient).delete(anyString(), any());
        doReturn(Mono.empty()).when(patient).get(anyString(), any(), any());
        doReturn(Mono.empty()).when(patient).put(anyString(), any(), any());
    }

    /**
     * Tests successful retrieval of all patients.
     */
    @Test
    public void testGetAllPatients_Success() {
        String nextPage = "1";
        int pageSize = 20;

        List<Map<String, Object>> mockResponse = Collections.singletonList(new HashMap<>());

        given(patient.get(anyString(), any(), any()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(patient.findAll(pageSize, nextPage))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(patient, times(1)).get(anyString(), any(), any());
    }

    /**
     * Tests successful retrieval of a patient by ID.
     */
    @Test
    public void testGetPatientById_Success() {
        String patientId = "12345";
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("id", patientId);

        given(patient.get(anyString(), any(), any()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(patient.findById(patientId))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(patient, times(1)).get(anyString(), any(), any());
    }

    /**
     * Tests the scenario where the patient exists in the system.
     */
    @Test
    public void testPatientExists_True() {
        String patientId = "12345";
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("requestResource", new Object());
        mockResponse.put("totalNumberOfRecords", 1);

        given(patient.findById(anyString()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(patient.exists(patientId))
                .expectNext(true)
                .verifyComplete();

        verify(patient, times(1)).findById(patientId);
    }

    /**
     * Tests the scenario where the patient does not exist in the system.
     */
    @Test
    public void testPatientExists_False() {
        String patientId = "12345";
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("totalNumberOfRecords", 0);

        given(patient.findById(anyString()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(patient.exists(patientId))
                .expectNext(false)
                .verifyComplete();
    }

    /**
     * Tests successful creation of a patient.
     */
    @Test
    public void testCreatePatient_Success() {
        PatientDTO validPatient = new PatientDTO();
        validPatient.setFirstName("John");
        validPatient.setLastName("Doe");
        validPatient.setMobileNumber("1234567890");
        validPatient.setGender(Gender.FEMALE);
        validPatient.setBirthDate("2000-01-01");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("patientId", "12345");
        mockResponse.put("status", "created");

        given(patient.post(anyString(), eq(validPatient), any()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(patient.create(validPatient))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(patient, times(1)).post(anyString(), eq(validPatient), any());
    }

    /**
     * Tests successful update of patient details.
     */
    @Test
    public void testUpdatePatient_Success() {
        UpdatePatientDTO updatePatientData = new UpdatePatientDTO();
        updatePatientData.setResourceId("patient123");
        updatePatientData.setMobileNumber("9876543210");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "updated");

        given(patient.put(anyString(), any(), any()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(patient.update(updatePatientData))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(patient, times(1)).put(anyString(), eq(updatePatientData), any());
    }

    /**
     * Tests successful deletion of a patient.
     */
    @Test
    public void testDeletePatient_Success() {
        String patientId = "12345";

        given(patient.delete(anyString(), any()))
                .willReturn(Mono.just("Patient Deleted"));

        StepVerifier.create(patient.delete(patientId))
                .expectNext("Patient Deleted")
                .verifyComplete();

        verify(patient, times(1)).delete(anyString(), any());
    }

    // ---------------- Negative Test Cases ----------------

    /**
     * Tests retrieval of a patient by a null ID (invalid input).
     */
    @Test
    public void testGetPatientById_NullId() {
        StepVerifier.create(patient.findById(null))
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationError &&
                                throwable.getMessage().contains("Patient ID cannot be null or empty."))
                .verify();
    }

    /**
     * Tests retrieval of a patient with an empty ID (invalid input).
     */
    @Test
    public void testGetPatientById_InvalidId() {
        StepVerifier.create(patient.findById(""))
                .expectError(ValidationError.class)
                .verify();

        verify(patient, never()).get(anyString(), any(), any());
    }

    /**
     * Tests handling of service errors when checking patient existence.
     */
    @Test
    public void testPatientExists_ErrorHandling() {
        given(patient.findById(anyString()))
                .willReturn(Mono.error(new RuntimeException("Service Down")));

        StepVerifier.create(patient.exists("12345"))
                .expectNext(false)
                .verifyComplete();
    }

    /**
     * Tests creation of a null patient, which should trigger validation failure.
     */
    @Test
    public void testCreatePatient_NullPatient() {
        StepVerifier.create(patient.create(null))
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationError &&
                                throwable.getMessage().contains("Patient data cannot be null."))
                .verify();
    }

    /**
     * Tests updating a patient with null data (invalid input).
     */
    @Test
    public void testUpdatePatient_NullUpdateData() {
        StepVerifier.create(patient.update(null))
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationError &&
                                throwable.getMessage().contains("Update patient data cannot be null."))
                .verify();
    }

    /**
     * Tests deletion of a patient with an invalid (empty) ID.
     */
    @Test
    public void testDeletePatient_InvalidId() {
        StepVerifier.create(patient.delete(""))
                .expectError(ValidationError.class)
                .verify();

        verify(patient, never()).delete(anyString(), any());
    }

    /**
     * Tests deletion of a patient with a null ID (invalid input).
     */
    @Test
    public void testDeletePatient_NullId() {
        StepVerifier.create(patient.delete(null))
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationError &&
                                throwable.getMessage().contains("Patient ID cannot be null or empty."))
                .verify();
    }

    // ---------------- Mocks for Failed Flows ----------------

    /**
     * Tests validation failure during patient creation due to missing required fields.
     */
    @Test
    public void testCreatePatient_ValidationFailure() {
        PatientDTO invalidPatient = new PatientDTO(); // Missing required data

        StepVerifier.create(patient.create(invalidPatient))
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationError &&
                                throwable.getMessage().contains("ID Number is required."))
                .verify();
    }

    /**
     * Tests invalid data during patient update (e.g., incorrect mobile number).
     */
    @Test
    public void testUpdatePatient_InvalidData() {
        UpdatePatientDTO invalidUpdateData = new UpdatePatientDTO();
        invalidUpdateData.setResourceId("patient123");
        invalidUpdateData.setMobileNumber("invalidNumber"); // Invalid format

        StepVerifier.create(patient.update(invalidUpdateData))
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationError &&
                                throwable.getMessage().contains("Mobile Number must be exactly 10 digits."))
                .verify();
    }

    /**
     * Tests missing resource ID during patient update, which should fail validation.
     */
    @Test
    public void testUpdatePatient_EmptyResourceId() {
        UpdatePatientDTO invalidUpdateData = new UpdatePatientDTO();
        invalidUpdateData.setMobileNumber("1234567890"); // Missing resourceId

        StepVerifier.create(patient.update(invalidUpdateData))
                .expectError(ValidationError.class)
                .verify();
    }
}
