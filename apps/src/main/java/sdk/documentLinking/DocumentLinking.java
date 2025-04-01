package sdk.documentLinking;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import sdk.base.Base;
import sdk.base.errors.EhrApiError;
import sdk.base.errors.ErrorType;
import sdk.base.errors.ValidationError;
import sdk.base.utils.LogUtil;
import sdk.documentLinking.documentLinkingDTOs.*;


/**
 * Service responsible for linking health documents through a multi-step process.
 * This service handles the workflow of document linking in a healthcare information system.
 * It involves creating appointments, care contexts, updating visit records, and linking care contexts.
 */
@Service
public class DocumentLinking extends Base {

    @Autowired
    private Mapper mapper;

    /**
     * Constructs a DocumentLinkingService instance with the provided ObjectMapper and WebClient.
     *
     * @param objectMapper the ObjectMapper for JSON processing
     * @param webClient    the WebClient for making HTTP requests
     */
    protected DocumentLinking(ObjectMapper objectMapper, WebClient webClient) {
        super(objectMapper, webClient);
    }

    /**
     * Initiates the health document linking process by performing multiple steps:
     * - Validating the provided DTO
     * - Creating an appointment
     * - Creating a care context
     * - Updating visit records
     * - Linking the care context to the health document
     *
     * @param dto DTO containing health document linking information
     * @return Mono<Boolean> indicating successful document linking
     */

    public Mono<Boolean> linkHealthDocument(@Valid HealthDocumentLinkingDTO dto) {
        final TransactionState transactionState = new TransactionState();

        return Mono.fromCallable(() -> {
                    validateDTO(dto);
                    return dto;
                })
                .subscribeOn(Schedulers.boundedElastic())
                // Step 1: Create Appointment
                .flatMap(this::createAppointmentData)
                .flatMap(this::sendAppointmentRequest)
                .flatMap(appointmentResponse -> {
                    String appointmentRef = extractAppointmentReference(appointmentResponse);
                    transactionState.setAppointmentReference(appointmentRef);
                    transactionState.setAppointmentCreated(true);
                    LogUtil.logger.info("Appointment created with reference: {}", appointmentRef);

                    // Step 2: Create Care Context
                    return createCareContextData(dto, appointmentRef)
                            .flatMap(this::sendCareContextRequest)
                            .doOnNext(careContextResponse -> {
                                transactionState.setCareContextReference(careContextResponse.getCareContextReference());
                                transactionState.setRequestId(careContextResponse.getRequestId());
                                transactionState.setCareContextCreated(true);
                                LogUtil.logger.info("Care context created with reference: {}",
                                        careContextResponse.getCareContextReference());
                            })
                            .flatMap(careContextResponse -> {
                                // Step 3: Update Visit Records
                                return updateVisitRecords(dto, appointmentRef,
                                        careContextResponse.getCareContextReference(),
                                        careContextResponse.getRequestId())
                                        .doOnNext(visitRecordResponse -> {
                                            transactionState.setVisitRecordsUpdated(true);
                                            LogUtil.logger.info("Visit records updated successfully");
                                        })
                                        .flatMap(visitRecordResponse -> {
                                            // Step 4: Link Care Context
                                            return linkCareContext(dto,
                                                    careContextResponse.getCareContextReference(),
                                                    appointmentRef,
                                                    careContextResponse.getRequestId())
                                                    .doOnNext(response -> {
                                                        transactionState.setCareContextLinked(true);
                                                        LogUtil.logger.info("Care context linked successfully");
                                                    });
                                        });
                            });
                })
                .map(response -> true)
                .doOnSuccess(result -> LogUtil.logger.info("Health document successfully linked"))
                .onErrorResume(error -> {
                    if (error instanceof ValidationError) {
                        LogUtil.logger.error("Validation error: {}", error.getMessage());
                        return Mono.error(new EhrApiError("Validation failed: " + error.getMessage(),
                                HttpStatusCode.valueOf(400)));
                    }
                    LogUtil.logger.error("Error in linking health document. Transaction state: {}",
                            transactionState, error);
                    return Mono.error(new EhrApiError("Failed to link health document. Transaction state: " +
                            transactionState, ErrorType.INTERNAL_SERVER_ERROR));
                });
    }

    /**
     * Extracts the appointment reference from the appointment response.
     *
     * @param response the appointment response DTO
     * @return the appointment reference as a String
     * @throws ValidationError if the appointment reference is missing or invalid
     */
    private String extractAppointmentReference(@Valid AppointmentResponseDTO response) {
        String appointmentRef = response.getResourceId();
        if ((appointmentRef == null || appointmentRef.isEmpty()) && response.getAppointmentDTO() != null) {
            appointmentRef = response.getAppointmentDTO().getReference();
        }

        if (appointmentRef == null || appointmentRef.isEmpty()) {
            LogUtil.logger.error("Failed to get appointment reference from response");
            throw new ValidationError("Failed to get appointment reference from response");
        }
        return appointmentRef;
    }

    /**
     * Creates appointment data from the provided health document linking DTO.
     *
     * @param dto the health document linking DTO
     * @return a Mono containing the AppointmentDTO
     */
    Mono<AppointmentDTO> createAppointmentData(@Valid HealthDocumentLinkingDTO dto) {
        return Mono.fromCallable(() -> {
            AppointmentDTO appointmentData = mapper.mapToAppointmentDTO(dto);
            validateDTO(appointmentData);
            return appointmentData;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Sends a request to create an appointment in the system.
     *
     * @param appointmentData the appointment data to be sent
     * @return a Mono containing the AppointmentResponseDTO
     */
    Mono<AppointmentResponseDTO> sendAppointmentRequest(@Valid AppointmentDTO appointmentData) {
        return post("/add/Appointment",
                appointmentData,
                new ParameterizedTypeReference<AppointmentResponseDTO>() {})
                .doOnNext(appointmentResponse -> {
                    LogUtil.logger.debug("Raw Appointment Response: {}", appointmentResponse);
                    if (appointmentResponse == null) {
                        LogUtil.logger.error("Appointment API response is null.");
                        throw new ValidationError("Failed to create appointment, response is null.");
                    }
                })
                .onErrorResume(error -> {
                    LogUtil.logger.error("Error while sending appointment request", error);
                    return Mono.error(new EhrApiError("Failed to send appointment request",
                            ErrorType.INTERNAL_SERVER_ERROR));
                });
    }

    /**
     * Creates care context data from the health document linking DTO.
     *
     * @param dto               the health document linking DTO
     * @param appointmentReference the appointment reference
     * @return a Mono containing the CreateCareContextDTO
     */
    Mono<CreateCareContextDTO> createCareContextData(@Valid HealthDocumentLinkingDTO dto, String appointmentReference) {
        return Mono.fromCallable(() -> {
            String appointmentStartDate = String.valueOf(dto.getAppointmentStartDate());
            String appointmentEndDate = String.valueOf(dto.getAppointmentEndDate());
            CreateCareContextDTO careContextData = mapper.mapToCareContextDTO(dto,
                    appointmentReference,
                    appointmentStartDate,
                    appointmentEndDate);
            validateDTO(careContextData);
            return careContextData;
        });
    }

    /**
     * Sends a request to create a care context in the system.
     *
     * @param careContextData the care context data to be sent
     * @return a Mono containing the CreateCareContextResponseDTO
     */
    Mono<CreateCareContextResponseDTO> sendCareContextRequest(@Valid CreateCareContextDTO careContextData) {
        return post("/abdm-flows/create-carecontext",
                careContextData,
                new ParameterizedTypeReference<>() {});
    }

    /**
     * Updates visit records with consultation data.
     *
     * @param dto                  the health document linking DTO
     * @param appointmentReference the appointment reference
     * @param careContextReference the care context reference
     * @param requestId            the request ID
     * @return a Mono containing the ConsultationDTO
     */
    Mono<ConsultationDTO> updateVisitRecords(@Valid HealthDocumentLinkingDTO dto,
                                             String appointmentReference,
                                             String careContextReference,
                                             String requestId) {
        return Mono.fromCallable(() -> {
                    ConsultationDTO consultationData = mapper.mapToConsultationDTO(dto,
                            appointmentReference,
                            careContextReference,
                            requestId);
                    validateDTO(consultationData);
                    return consultationData;
                })
                .flatMap(consultationData ->
                        post("/abdm-flows/update-visit-records",
                                consultationData,
                                new ParameterizedTypeReference<>() {}));
    }

    /**
     * Links the care context to the existing health document.
     *
     * @param dto                  the health document linking DTO
     * @param careContextReference the care context reference
     * @param appointmentReference the appointment reference
     * @param requestId            the request ID
     * @return a Mono<Boolean> indicating success
     */
    Mono<Boolean> linkCareContext(@Valid HealthDocumentLinkingDTO dto,
                                  String careContextReference,
                                  String appointmentReference,
                                  String requestId) {
        return Mono.fromCallable(() -> {
                    LinkCareContextDTO linkData = mapper.mapToLinkCareContextDTO(dto,
                            careContextReference,
                            appointmentReference,
                            requestId);
                    validateDTO(linkData);
                    return linkData;
                })
                .flatMap(linkData ->
                        post("/abdm-flows/link-carecontext",
                                linkData,
                                new ParameterizedTypeReference<LinkCareContextDTO>() {}))
                .doOnNext(response -> LogUtil.logger.info("LinkCareContext API response: {}", response))
                .map(response -> true)
                .onErrorReturn(false);
    }

    /**
     * Validates input data to ensure it is not null and meets required conditions.
     *
     * @param data the data object to be validated
     * @throws ValidationError if validation fails
     */
    private void validateDTO(Object data) {
        if (data == null) {
            throw new ValidationError("Input data cannot be null");
        }
        if (data instanceof HealthDocumentLinkingDTO) {
            validateHealthDocumentLinkingDTO((HealthDocumentLinkingDTO) data);
        } else if (data instanceof AppointmentDTO) {
            validateAppointmentDTO((AppointmentDTO) data);
        } else if (data instanceof CreateCareContextDTO) {
            validateCreateCareContextDTO((CreateCareContextDTO) data);
        } else if (data instanceof ConsultationDTO) {
            validateConsultationDTO((ConsultationDTO) data);
        } else if (data instanceof LinkCareContextDTO) {
            validateLinkCareContextDTO((LinkCareContextDTO) data);
        }
    }

    private void validateHealthDocumentLinkingDTO(@Valid HealthDocumentLinkingDTO dto) {
        validateNotNullOrEmpty(dto.getPatientReference(), "patientReference");
        validateNotNullOrEmpty(dto.getPractitionerReference(), "practitionerReference");

        if (!dto.getPatientReference().matches("^[a-fA-F0-9]{32}$|^[a-fA-F0-9-]{36}$")) {
            throw new ValidationError("Patient reference must be a valid 32 or 36 character UUID");
        }

        validateNotNullOrEmpty(String.valueOf(dto.getAppointmentStartDate()), "appointmentStartDate");
        validateNotNullOrEmpty(String.valueOf(dto.getAppointmentEndDate()), "appointmentEndDate");

        if (dto.getAppointmentPriority() != null && dto.getAppointmentPriority().isEmpty()) {
            throw new ValidationError("Appointment priority cannot be empty");
        }

        validateNotNullOrEmpty(dto.getOrganizationId(), "organizationID");

        if (dto.getHiType() == null) {
            throw new ValidationError("Health information type is required");
        }

        validateNotNullOrEmpty(dto.getMobileNumber(), "mobileNumber");
    }

    private void validateAppointmentDTO(@Valid AppointmentDTO dto) {
        validateNotNullOrEmpty(dto.getPractitionerReference(), "practitionerReference");
        validateNotNullOrEmpty(dto.getPatientReference(), "patientReference");
        validateNotNullOrEmpty(dto.getStart().toString(), "start");
        validateNotNullOrEmpty(dto.getEnd().toString(), "end");
    }

    private void validateCreateCareContextDTO(@Valid CreateCareContextDTO dto) {
        validateNotNullOrEmpty(dto.getPatientReference(), "patientReference");
        validateNotNullOrEmpty(dto.getPractitionerReference(), "practitionerReference");
        validateNotNullOrEmpty(dto.getAppointmentReference(), "appointmentReference");
        validateNotNullOrEmpty(dto.getAppointmentDate(), "appointmentDate");

        // Validate UUID format
        String uuidPattern = "^[a-fA-F0-9-]{36}$";
        if (!dto.getPatientReference().matches(uuidPattern)) {
            throw new ValidationError("Patient reference must be a valid 36-character UUID");
        }
        if (!dto.getPractitionerReference().matches(uuidPattern)) {
            throw new ValidationError("Practitioner reference must be a valid 36-character UUID");
        }
        if (!dto.getAppointmentReference().matches(uuidPattern)) {
            throw new ValidationError("Appointment reference must be a valid 36-character UUID");
        }

        if (dto.getHiType() == null) {
            throw new ValidationError("Health information type is required");
        }

        if (dto.getResendOtp() == null) {
            throw new ValidationError("Resend OTP flag is required");
        }
    }

    private void validateConsultationDTO(@Valid ConsultationDTO dto) {
        validateNotNullOrEmpty(dto.getCareContextReference(), "careContextReference");
        validateNotNullOrEmpty(dto.getPatientReference(), "patientReference");
        validateNotNullOrEmpty(dto.getPractitionerReference(), "practitionerReference");
        validateNotNullOrEmpty(dto.getAppointmentReference(), "appointmentReference");
    }

    private void validateLinkCareContextDTO(@Valid LinkCareContextDTO dto) {
        validateNotNullOrEmpty(dto.getRequestId(), "requestId");
        validateNotNullOrEmpty(dto.getAppointmentReference(), "appointmentReference");
        validateNotNullOrEmpty(dto.getPatientAddress(), "patientAddress");
        validateNotNullOrEmpty(dto.getPatientReference(), "patientReference");
        validateNotNullOrEmpty(dto.getCareContextReference(), "careContextReference");
        validateNotNullOrEmpty(dto.getAuthMode(), "authMode");
    }

    private void validateNotNullOrEmpty(String value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new ValidationError(fieldName + " cannot be null or empty");
        }
    }
}
