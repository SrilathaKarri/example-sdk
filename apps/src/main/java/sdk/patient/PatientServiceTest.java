//package sdk;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//import sdk.base.enums.Gender;
//import sdk.base.enums.ResourceType;
//import sdk.base.enums.StatesAndUnionTerritories;
//import sdk.base.errors.ValidationError;
//import sdk.base.response.ApiResponse;
//import sdk.base.response.GetProfileResponse;
//import sdk.base.response.UpdateProfileResponse;
//import sdk.patient.PatientDTO;
//import sdk.patient.Patient;
//import sdk.patient.UpdatePatientDTO;
//import sdk.patient.enums.PatientIdType;
//import sdk.patient.enums.PatientType;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//public class PatientServiceTest {
//
//    @Mock
//    private WebClient.Builder webClientBuilder;
//
//    private Patient patientService;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//        patientService = spy(new Patient(webClientBuilder));
//    }
//
//    @Test
//    public void testGetAllPatients() {
//        ApiResponse<GetProfileResponse> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Success");
//
//        String endpoint = "/get/" + ResourceType.Patient.name();
//        doReturn(expectedResponse)
//                .when(patientService)
//                .sendGetRequest(eq(endpoint), any());
//
//        ApiResponse<GetProfileResponse> actualResponse = patientService.findAll("1");
//        assertThat(actualResponse)
//                .isNotNull();
//        assertThat(actualResponse.getData())
//                .as("The returned patients list should match the expected data")
//                .isEqualTo(patientList);
//        assertThat(actualResponse.getMessage())
//                .as("The response message should be 'Success'")
//                .isEqualTo("Success");
//        assertThat(actualResponse.getStatus())
//                .as("The status should be 200")
//                .isEqualTo(200);
//    }
//
//    @Test
//    public void testGetPatientById_ValidId() {
//        String id = "123";
//        ApiResponse<GetProfileResponse> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Patient Found !!!");
//        String endpoint = String.format("/get/%s/%s", ResourceType.Patient.name(), id);
//        doReturn(expectedResponse)
//                .when(patientService)
//                .sendGetRequest(eq(endpoint), any());
//
//        ApiResponse<GetProfileResponse> actualResponse = patientService.findById(id);
//
//        assertThat(actualResponse).isNotNull();
//        assertThat(actualResponse.getMessage()).isEqualTo("Patient Found !!!");
//    }
//
//    @Test
//    public void testGetPatientById_NullOrEmptyId_ShouldThrowValidationError() {
//        assertThrows(ValidationError.class, () -> patientService.findById(null));
//        assertThrows(ValidationError.class, () -> patientService.findById(""));
//        }
//
//    @Test
//    public void testPatientExists_ValidPatient() {
//        String id = "123";
//        ApiResponse<GetProfileResponse> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Patient Found !!!");
//        doReturn(expectedResponse).when(patientService).findById(eq(id));
//        assertTrue(patientService.checkPatient(id));
//    }
//
//    @Test
//    public void testPatientExists_InvalidOrNonExistingPatient() {
//        assertFalse(patientService.checkPatient(null));
//        assertFalse(patientService.checkPatient(""));
//    }
//
//    @Test
//    public void testPatientExistsReturnsFalse() {
//        String id = "123";
//        ApiResponse<GetProfileResponse> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Patient Not Found");
//
//        // The endpoint is built as: "/get/Patient/{id}"
//        String endpoint = String.format("/get/%s/%s", ResourceType.Patient, id);
//
//        //Mock the sendGetRequest method
//        when(patientService.sendGetRequest(
//                eq(endpoint),
//                any(ParameterizedTypeReference.class)))
//                .thenReturn(Mono.just(expectedResponse));
//        boolean exists = patientService.checkPatient(id);
//
//        assertFalse(exists, "Expected checkPatient() to return false when message is not 'Patient Found !!!'");
//    }
//
//    @Test
//    public void testCreatePatient_Success() {
//        PatientDTO newPatient = new PatientDTO();
//        newPatient.setIdNumber("ID-987654");
//        newPatient.setIdType(PatientIdType.AADHAAR);
//        newPatient.setAbhaAddress("test@abdm");
//        newPatient.setPatientType(PatientType.NEW);
//        newPatient.setFirstName("Sai");
//        newPatient.setMiddleName("P.");
//        newPatient.setLastName("Raina");
//        newPatient.setBirthDate("1945-05-15");
//        newPatient.setGender(Gender.MALE);
//        newPatient.setEmailId("sandy.raina@gmail.com");
//        newPatient.setMobileNumber("9876543210");
//        newPatient.setAddress("123, Main Street, Hindupur");
//        newPatient.setPincode("510001");
//        newPatient.setState(StatesAndUnionTerritories.ANDHRAPRADESH);
//        newPatient.setWantsToLinkWhatsapp(true);
//        newPatient.setPhoto("photo-url");
//        newPatient.setResourceType(ResourceType.Patient);
//        newPatient.setResourceId("res-123");
//
//
//
//        ApiResponse<UpdateProfileResponse> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Patient Created Successfully");
//
//        String endpoint = "/add/" + ResourceType.Patient.name();
//        doReturn(expectedResponse)
//                .when(patientService)
//                .sendPostRequest(eq(endpoint), eq(newPatient), any());
//
//        ApiResponse<UpdateProfileResponse> actualResponse = patientService.createPatient(newPatient);
//
//        assertThat(actualResponse).isNotNull();
//        assertThat(actualResponse.getMessage()).isEqualTo("Patient Created Successfully");
//    }
//
//    @Test
//    public void testCreatePatient_NullPatient_ShouldThrowValidationError() {
//        assertThrows(ValidationError.class, () -> patientService.createPatient(null));
//    }
//
//    @Test
//    public void testCreatePatient_MissingIdNumber_ShouldThrowValidationError() {
//        PatientDTO patient = new PatientDTO();
//        patient.setIdType(PatientIdType.AADHAAR);
//        patient.setFirstName("Ravi");
//        patient.setLastName("Kumar");
//        patient.setBirthDate("1990-01-01");
//        patient.setGender(Gender.MALE);
//        patient.setMobileNumber("9876543210");
//        patient.setAddress("xy56 Street");
//        patient.setPincode("123456");
//        patient.setState(StatesAndUnionTerritories.KARNATAKA);
//        patient.setResourceType(ResourceType.Patient);
//
//        ValidationError exception = assertThrows(ValidationError.class, () -> patientService.createPatient(patient));
//        assertThat(exception.getMessage()).isEqualTo("ID Number is required.");
//    }
//
//    @Test
//    public void testCreatePatient_MissingFirstName_ShouldThrowValidationError() {
//        PatientDTO patient = new PatientDTO();
//        patient.setIdNumber("ID-12345");
//        patient.setIdType(PatientIdType.AADHAAR);
//        patient.setLastName("kiran");
//        patient.setBirthDate("1990-01-01");
//        patient.setGender(Gender.MALE);
//        patient.setMobileNumber("9876543210");
//        patient.setAddress("3425 Street");
//        patient.setPincode("123456");
//        patient.setState(StatesAndUnionTerritories.KARNATAKA);
//        patient.setResourceType(ResourceType.Patient);
//
//        ValidationError exception = assertThrows(ValidationError.class, () -> patientService.createPatient(patient));
//        assertThat(exception.getMessage()).isEqualTo("First Name is required.");
//    }
//
//    @Test
//    public void testCreatePatient_MissingGender_ShouldThrowValidationError() {
//        PatientDTO patient = new PatientDTO();
//        patient.setIdNumber("ID-12345");
//        patient.setIdType(PatientIdType.AADHAAR);
//        patient.setFirstName("Roman");
//        patient.setLastName("Reigns");
//        patient.setBirthDate("1990-01-01");
//        patient.setMobileNumber("9876543210");
//        patient.setAddress("123 Street");
//        patient.setPincode("123456");
//        patient.setState(StatesAndUnionTerritories.KARNATAKA);
//        patient.setResourceType(ResourceType.Patient);
//        patient.setPatientType(PatientType.NEW);
//
//        ValidationError exception = assertThrows(ValidationError.class, () -> patientService.createPatient(patient));
//        assertThat(exception.getMessage()).isEqualTo("Gender is required.");
//    }
//
//    @Test
//    public void testUpdatePatient_Success() {
//        UpdatePatientDTO updatePatientData = new UpdatePatientDTO();
//        updatePatientData.setResourceId("res-13423");
//        updatePatientData.setEmailId("updated_email@gmail.com");
//        updatePatientData.setMobileNumber("9876543210");
//        updatePatientData.setResourceType(ResourceType.Patient);
//
//        ApiResponse<UpdateProfileResponse> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Patient Updated Successfully");
//
//        String endpoint = "/update/" + ResourceType.Patient.name();
//        doReturn(expectedResponse)
//                .when(patientService)
//                .sendPutRequest(eq(endpoint), eq(updatePatientData), any());
//
//        ApiResponse<UpdateProfileResponse> actualResponse = patientService.updatePatient(updatePatientData);
//
//        assertThat(actualResponse).isNotNull();
//        assertThat(actualResponse.getMessage()).isEqualTo("Patient Updated Successfully");
//    }
//
//    @Test
//    public void testUpdatePatient_NullPatient_ShouldThrowValidationError() {
//        assertThrows(ValidationError.class, () -> patientService.updatePatient(null));
//    }
//    @Test
//    public void testUpdatePatient_MissingResourceId_ShouldThrowValidationError() {
//        UpdatePatientDTO updatePatient = new UpdatePatientDTO();
//        updatePatient.setMobileNumber("9876543210");
//        updatePatient.setEmailId("test@gmail.com");
//
//        ValidationError exception = assertThrows(ValidationError.class, () -> patientService.updatePatient(updatePatient));
//        assertThat(exception.getMessage()).isEqualTo("Resource ID is required for updating patient.");
//    }
//
//    @Test
//    public void testUpdatePatient_InvalidEmailFormat_ShouldThrowValidationError() {
//        UpdatePatientDTO updatePatient = new UpdatePatientDTO();
//        updatePatient.setResourceId("res-123");
//        updatePatient.setEmailId("invalid-email");
//
//        ValidationError exception = assertThrows(ValidationError.class, () -> patientService.updatePatient(updatePatient));
//        assertThat(exception.getMessage()).isEqualTo("Invalid Email format.");
//    }
//
//    @Test
//    public void testDeletePatient_Success() {
//        String id = "123";
//        ApiResponse<Void> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Patient Deleted Successfully");
//        String endpoint = String.format("/patients/%s", id);
//        doReturn(expectedResponse)
//                .when(patientService)
//                .sendDeleteRequest(eq(endpoint), any());
//
//        ApiResponse<Void> actualResponse = patientService.deletePatient(id);
//
//        assertThat(actualResponse).isNotNull();
//        assertThat(actualResponse.getMessage()).isEqualTo("Patient Deleted Successfully");
//    }
//
//    @Test
//    public void testDeletePatient_NullOrEmptyId_ShouldThrowValidationError() {
//        assertThrows(ValidationError.class, () -> patientService.deletePatient(null));
//        assertThrows(ValidationError.class, () -> patientService.deletePatient(""));
//    }
//}