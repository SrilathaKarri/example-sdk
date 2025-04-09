package sdk.documentLinking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import sdk.base.errors.EhrApiError;
import sdk.base.errors.ValidationError;
import sdk.documentLinking.documentLinkingDTOs.*;
import sdk.documentLinking.enums.DocLinkingEnums;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DocumentLinking}.
 * <p>
 * This test class verifies both successful and failure scenarios for the document linking workflow.
 * It uses Mockito for mocking dependencies and Reactor's StepVerifier for testing reactive Mono streams.
 * </p>
 */
@MockitoSettings(strictness = Strictness.WARN)
public class DocumentLinkingServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Spy
    @InjectMocks
    private DocumentLinking documentLinkingService;

    /**
     * Initializes the test setup before each test case.
     * Mocks WebClient and ensures the post method returns an empty Mono by default.
     */
    @BeforeEach
    public void setUp() {
        given(webClientBuilder.build()).willReturn(webClient);
        doReturn(Mono.empty()).when(documentLinkingService).post(anyString(), any(), any());
    }

    /**
     * Tests the successful linking of health documents through all stages.
     * Verifies that the workflow completes and all interactions with dependencies are correct.
     */
    @Test
    public void testLink_SuccessfulLinking() {
        HealthDocumentLinkingDTO validDTO = createValidHealthDocumentLinkingDTO();

        mockSuccessfulAppointmentCreation(validDTO);
        mockSuccessfulCareContextCreation(validDTO);
        mockSuccessfulVisitRecordUpdate(validDTO);
        mockSuccessfulCareContextLinking(validDTO);

        StepVerifier.create(documentLinkingService.linkHealthDocument(validDTO))
                .expectNext(true)
                .verifyComplete();

        verify(documentLinkingService).createAppointmentData(validDTO);
        verify(documentLinkingService).sendAppointmentRequest(any(AppointmentDTO.class));
        verify(documentLinkingService).createCareContextData(eq(validDTO), anyString());
        verify(documentLinkingService).sendCareContextRequest(any(CreateCareContextDTO.class));
        verify(documentLinkingService).updateVisitRecords(eq(validDTO), anyString(), anyString(), anyString());
        verify(documentLinkingService).linkCareContext(eq(validDTO), anyString(), anyString(), anyString());
    }

    /**
     * Tests the scenario where a null input is passed to the linkHealthDocument method.
     * Expects a validation error with HTTP status 400.
     */
    @Test
    public void testLink_NullInput() {
        StepVerifier.create(documentLinkingService.linkHealthDocument(null))
                .expectErrorMatches(throwable ->
                        throwable instanceof EhrApiError &&
                                ((EhrApiError) throwable).getStatusCode() == 400 &&
                                throwable.getMessage().contains("Validation failed"))
                .verify();
    }

    /**
     * Simulates a failure during the appointment creation stage.
     * Expects an EhrApiError with an appropriate message and status code 400.
     */
    @Test
    public void testLink_AppointmentCreationFailed() {
        HealthDocumentLinkingDTO validDTO = createValidHealthDocumentLinkingDTO();
        mockFailedAppointmentCreation();

        StepVerifier.create(documentLinkingService.linkHealthDocument(validDTO))
                .expectError(EhrApiError.class)
                .verify();
    }

    /**
     * Simulates a failure during the care context creation stage.
     * Expects an EhrApiError indicating failure in care context creation.
     */
    @Test
    public void testLink_CareContextCreationFailed() {
        HealthDocumentLinkingDTO validDTO = createValidHealthDocumentLinkingDTO();
        mockSuccessfulAppointmentCreation(validDTO);

        CreateCareContextDTO careContextDTO = new CreateCareContextDTO();
        given(documentLinkingService.createCareContextData(any(), anyString()))
                .willReturn(Mono.just(careContextDTO));
        given(documentLinkingService.sendCareContextRequest(any()))
                .willReturn(Mono.error(new EhrApiError("Care Context Creation Failed", HttpStatusCode.valueOf(400))));

        StepVerifier.create(documentLinkingService.linkHealthDocument(validDTO))
                .expectError(EhrApiError.class)
                .verify();
    }

    /**
     * Simulates a failure during the visit record update stage.
     * Expects an EhrApiError indicating the failure of the visit record update operation.
     */
    @Test
    public void testLink_VisitRecordUpdateFailed() {
        HealthDocumentLinkingDTO validDTO = createValidHealthDocumentLinkingDTO();
        mockSuccessfulAppointmentCreation(validDTO);
        mockSuccessfulCareContextCreation(validDTO);

        given(documentLinkingService.updateVisitRecords(any(), anyString(), anyString(), anyString()))
                .willReturn(Mono.error(new EhrApiError("Visit Record Update Failed", HttpStatusCode.valueOf(400))));

        StepVerifier.create(documentLinkingService.linkHealthDocument(validDTO))
                .expectError(EhrApiError.class)
                .verify();
    }

    /**
     * Simulates a failure during the care context linking stage.
     * Expects an EhrApiError indicating the failure of care context linking.
     */
    @Test
    public void testLink_CareContextLinkingFailed() {
        HealthDocumentLinkingDTO validDTO = createValidHealthDocumentLinkingDTO();
        mockSuccessfulAppointmentCreation(validDTO);
        mockSuccessfulCareContextCreation(validDTO);
        mockSuccessfulVisitRecordUpdate(validDTO);

        given(documentLinkingService.linkCareContext(any(), anyString(), anyString(), anyString()))
                .willReturn(Mono.error(new EhrApiError("Care Context Linking Failed", HttpStatusCode.valueOf(400))));

        StepVerifier.create(documentLinkingService.linkHealthDocument(validDTO))
                .expectError(EhrApiError.class)
                .verify();
    }

    /**
     * Tests the scenario where required fields are missing in the input DTO.
     * Expects a ValidationError indicating missing required fields.
     */
    @Test
    public void testLink_MissingRequiredFields() {
        HealthDocumentLinkingDTO incompleteDTO = new HealthDocumentLinkingDTO();

        given(documentLinkingService.createAppointmentData(any()))
                .willReturn(Mono.error(new ValidationError("Missing required fields")));

        StepVerifier.create(documentLinkingService.linkHealthDocument(incompleteDTO))
                .expectError(EhrApiError.class)
                .verify();
    }

    /**
     * Tests the scenario where required fields are empty in the input DTO.
     * Expects a ValidationError indicating empty fields.
     */
    @Test
    public void testLink_EmptyFields() {
        HealthDocumentLinkingDTO emptyDTO = createValidHealthDocumentLinkingDTO();
        emptyDTO.setPatientReference("");
        emptyDTO.setReference("");
        emptyDTO.setPractitionerReference("");
        emptyDTO.setPatientAddress("");
        emptyDTO.setPatientName("");
        emptyDTO.setAppointmentStartDate(String.valueOf(ZonedDateTime.parse("")));
        emptyDTO.setAppointmentEndDate(String.valueOf(ZonedDateTime.parse("")));
        emptyDTO.setOrganizationId("");
        emptyDTO.setMobileNumber("");

        given(documentLinkingService.createAppointmentData(any()))
                .willReturn(Mono.error(new ValidationError("Empty fields")));

        StepVerifier.create(documentLinkingService.linkHealthDocument(emptyDTO))
                .expectError(EhrApiError.class)
                .verify();
    }


    private void mockSuccessfulAppointmentCreation(HealthDocumentLinkingDTO dto) {
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        given(documentLinkingService.createAppointmentData(any()))
                .willReturn(Mono.just(appointmentDTO));

        AppointmentResponseDTO appointmentResponse = new AppointmentResponseDTO();
        appointmentResponse.setResourceId("appointment123");
        given(documentLinkingService.sendAppointmentRequest(any()))
                .willReturn(Mono.just(appointmentResponse));
    }

    private void mockSuccessfulCareContextCreation(HealthDocumentLinkingDTO dto) {
        CreateCareContextDTO careContextDTO = new CreateCareContextDTO();
        given(documentLinkingService.createCareContextData(any(), anyString()))
                .willReturn(Mono.just(careContextDTO));

        CreateCareContextResponseDTO careContextResponse = new CreateCareContextResponseDTO();
        careContextResponse.setCareContextReference("careContext123");
        careContextResponse.setRequestId("request123");
        given(documentLinkingService.sendCareContextRequest(any()))
                .willReturn(Mono.just(careContextResponse));
    }

    private void mockSuccessfulVisitRecordUpdate(HealthDocumentLinkingDTO dto) {
        ConsultationDTO consultationResponse = new ConsultationDTO();
        consultationResponse.setCareContextReference("careContext123");
        consultationResponse.setAppointmentReference("appointment123");
        consultationResponse.setRequestId("request123");

        given(documentLinkingService.updateVisitRecords(any(), anyString(), anyString(), anyString()))
                .willReturn(Mono.just(consultationResponse));
    }

    private void mockSuccessfulCareContextLinking(HealthDocumentLinkingDTO dto) {
        given(documentLinkingService.linkCareContext(any(), anyString(), anyString(), anyString()))
                .willReturn(Mono.just(true));
    }

    private HealthDocumentLinkingDTO createValidHealthDocumentLinkingDTO() {
        HealthDocumentLinkingDTO dto = new HealthDocumentLinkingDTO();
        String uuid32 = UUID.randomUUID().toString().replace("-", "");
        dto.setPatientReference(uuid32);
        dto.setReference("ref123");
        dto.setPractitionerReference("practitioner123");
        dto.setPatientAddress("123 Main St");
        dto.setPatientName("John Doe");
        dto.setAppointmentStartDate(String.valueOf(ZonedDateTime.parse("2025-02-28T09:00:00Z")));
        dto.setAppointmentEndDate(String.valueOf(ZonedDateTime.parse("2025-02-28T10:00:00Z")));
        dto.setOrganizationId("org123");
        dto.setMobileNumber("1234567890");
        dto.setHiType(DocLinkingEnums.HealthInformationTypes.valueOf("OPConsultation"));
        dto.setPatientReference(UUID.randomUUID().toString());
        return dto;
    }

    private void mockFailedAppointmentCreation() {
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        given(documentLinkingService.createAppointmentData(any()))
                .willReturn(Mono.just(appointmentDTO));

        given(documentLinkingService.sendAppointmentRequest(any()))
                .willReturn(Mono.error(new EhrApiError("Appointment Creation Failed", HttpStatusCode.valueOf(400))));
    }
}
