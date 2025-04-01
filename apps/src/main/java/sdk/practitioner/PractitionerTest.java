package sdk.practitioner;

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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link Practitioner} class.
 * <p>
 * This class contains tests for all major functionalities of the Practitioner class, including
 * creating, updating, deleting, retrieving, and checking the existence of practitioners.
 * The tests use JUnit 5, Mockito, and Reactor's StepVerifier for reactive stream testing.
 * </p>
 *
 * <p>Mockito's strictness level is set to WARN to catch potential misuses while allowing some flexibility during mocking.</p>
 */
@MockitoSettings(strictness = Strictness.WARN)
public class PractitionerTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Spy
    @InjectMocks
    private Practitioner practitioner;

    /**
     * Initializes mock behaviors before each test case.
     * Configures the WebClient to return the mocked instance when the builder is used.
     */
    @BeforeEach
    public void setUp() {
        given(webClientBuilder.build()).willReturn(webClient);
    }

    /**
     * Tests the successful creation of a practitioner.
     * Verifies that the {@link Practitioner#create(PractitionerDTO)} method returns the expected response.
     */
    @Test
    public void testCreatePractitioner_Success() {
        PractitionerDTO practitionerDTO = new PractitionerDTO();
        practitionerDTO.setFirstName("Shri");
        practitionerDTO.setLastName("Ram");
        practitionerDTO.setRegistrationId("12345");
        practitionerDTO.setDepartment("Cardiology");
        practitionerDTO.setGender(Gender.MALE);
        practitionerDTO.setJoiningDate("2021-01-01");
        practitionerDTO.setMobileNumber("9876543210");
        practitionerDTO.setEmailId("shri.ram@example.com");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("practitionerId", "pract123");
        mockResponse.put("status", "created");

        given(practitioner.post(anyString(), any(), any()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.create(practitionerDTO))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(practitioner, times(1)).post(anyString(), eq(practitionerDTO), any());
    }

    /**
     * Tests the failure scenario when creating a practitioner with missing required fields.
     * Expects a {@link ValidationError} to be thrown.
     */
    @Test
    public void testCreatePractitioner_Failure_MissingData() {
        PractitionerDTO practitionerDTO = new PractitionerDTO();
        practitionerDTO.setFirstName(null);
        practitionerDTO.setLastName(null);

        StepVerifier.create(practitioner.create(practitionerDTO))
                .expectError(ValidationError.class)
                .verify();
    }

    /**
     * Tests the successful update of a practitioner's details.
     * Verifies that the {@link Practitioner#update(PractitionerDTO)} method returns the expected response.
     */
    @Test
    public void testUpdatePractitioner_Success() {
        PractitionerDTO practitionerDTO = new PractitionerDTO();
        practitionerDTO.setResourceId("1634656");
        practitionerDTO.setEmailId("Shri@example.com");
        practitionerDTO.setAddress("Ram street");
        practitionerDTO.setRegistrationId("54321");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("practitionerId", "1");
        mockResponse.put("status", "updated");

        given(practitioner.put(anyString(), eq(practitionerDTO), any()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.update(practitionerDTO))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(practitioner, times(1)).put(anyString(), eq(practitionerDTO), any());
    }

    /**
     * Tests the successful deletion of a practitioner.
     * Verifies that the {@link Practitioner#delete(String)} method returns the expected confirmation message.
     */
    @Test
    public void testDeletePractitioner_Success() {
        String practitionerId = "practitioner123";

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("message", "Practitioner deleted successfully");

        given(practitioner.delete(anyString(), any()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.delete(practitionerId))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(practitioner, times(1)).delete(anyString(), any());
    }

    /**
     * Tests the failure scenario when deleting a practitioner with a missing ID.
     * Expects a {@link ValidationError} to be thrown.
     */
    @Test
    public void testDeletePractitioner_Failure_MissingPractitionerId() {
        String practitionerId = "";

        StepVerifier.create(practitioner.delete(practitionerId))
                .expectError(ValidationError.class)
                .verify();
    }

    /**
     * Tests retrieving a practitioner by their ID successfully.
     * Verifies that the {@link Practitioner#findById(String)} method returns the expected practitioner details.
     */
    @Test
    public void testGetPractitionerById_Success() {
        String practitionerId = "practitioner123";

        Map<String, Object> mockResponse = Map.of("id", practitionerId, "name", "Dr. John");

        given(practitioner.get(anyString(), any(), any()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.findById(practitionerId))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(practitioner).get(anyString(), any(), any());
    }

    /**
     * Tests the failure scenario when retrieving a practitioner by an empty ID.
     * Expects a {@link ValidationError} to be thrown.
     */
    @Test
    public void testGetPractitionerById_Failure_MissingPractitionerId() {
        String practitionerId = "";

        StepVerifier.create(practitioner.findById(practitionerId))
                .expectError(ValidationError.class)
                .verify();
    }

    /**
     * Tests checking if a practitioner exists by their ID when the practitioner is found.
     * Expects a successful response of 'true'.
     */
    @Test
    public void testPractitionerExists_Success() {
        String practitionerId = "practitioner123";

        Map<String, Object> mockResponse = Map.of("id", practitionerId, "name", "Dr. John");
        given(practitioner.findById(anyString()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.exists(practitionerId))
                .expectNext(true)
                .verifyComplete();

        verify(practitioner).findById(practitionerId);
    }

    /**
     * Tests checking if a practitioner exists by their ID when the practitioner is not found.
     * Expects a response of 'false'.
     */
    @Test
    public void testPractitionerExists_Failure_NotFound() {
        String practitionerId = "practitioner123";

        Map<String, Object> mockResponse = Map.of("message", "Practitioner Not Found");

        given(practitioner.findById(anyString()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.exists(practitionerId))
                .expectNext(false)
                .verifyComplete();

        verify(practitioner).findById(practitionerId);
    }

    /**
     * Tests the failure scenario when an unexpected error occurs while checking for practitioner existence.
     * Expects a response of 'false'.
     */
    @Test
    public void testPractitionerExists_Failure_Error() {
        String practitionerId = "practitioner123";

        given(practitioner.findById(anyString()))
                .willReturn(Mono.error(new RuntimeException("Error occurred")));

        StepVerifier.create(practitioner.exists(practitionerId))
                .expectNext(false)
                .verifyComplete();

        verify(practitioner).findById(practitionerId);
    }

    /**
     * Tests retrieving all practitioners successfully with pagination.
     */
    @Test
    public void testGetAllPractitioners_Success() {
        Integer pageSize = 20;
        String nextPage = "2";

        Map<String, Object> mockResponse = Map.of("data", "practitioner list");

        given(practitioner.get(anyString(), any(), any()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.findAll(pageSize, nextPage))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(practitioner).get(anyString(), any(), any());
    }

    /**
     * Tests retrieving practitioners with a null nextPage and default pageSize (10).
     */
    @Test
    public void testGetAllPractitioners_WithNullPage() {
        Integer pageSize = null;
        String nextPage = null;

        Map<String, Object> mockResponse = Map.of("data", "practitioner list");

        given(practitioner.get(anyString(), any(), any()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.findAll(pageSize, nextPage))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(practitioner).get(anyString(), any(), any());
    }

    /**
     * Tests retrieving practitioners with a custom pageSize but null nextPage.
     */
    @Test
    public void testGetAllPractitioners_WithCustomPageSize() {
        Integer pageSize = 15;
        String nextPage = null;

        Map<String, Object> mockResponse = Map.of("data", "practitioner list");

        given(practitioner.get(anyString(), any(), any()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.findAll(pageSize, nextPage))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(practitioner).get(anyString(), any(), any());
    }

    /**
     * Tests retrieving practitioners with both null pageSize and nextPage, using default values.
     */
    @Test
    public void testGetAllPractitioners_WithDefaultValues() {
        Integer pageSize = null;
        String nextPage = null;

        Map<String, Object> mockResponse = Map.of("data", "practitioner list");

        given(practitioner.get(anyString(), any(), any()))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.findAll(pageSize, nextPage))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(practitioner).get(anyString(), any(), any());
    }
}
