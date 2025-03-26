//package sdk;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import sdk.base.enums.Gender;
//import sdk.base.enums.ResourceType;
//import sdk.base.enums.StatesAndUnionTerritories;
//import sdk.base.errors.EhrApiError;
//import sdk.base.errors.ValidationError;
//import sdk.base.response.ApiResponse;
//import sdk.base.response.GetProfileResponse;
//import sdk.base.response.UpdateProfileResponse;
//import sdk.practitioner.PractitionerDTO;
//import sdk.practitioner.PractitionerService;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.spy;
//
//public class PractitionerServiceTest {
//
//    private PractitionerService practitionerService;
//
//    @BeforeEach
//    public void setUp() {
//        practitionerService = spy(new PractitionerService());
//    }
//
//    @Test
//    public void testGetAllPractitioners() {
//        ApiResponse<GetProfileResponse> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Success");
//
//        String endpoint = "/get/" + ResourceType.Practitioner.name();
//        doReturn(expectedResponse)
//                .when(practitionerService)
//                .sendGetRequest(eq(endpoint), any());
//
//        ApiResponse<GetProfileResponse> actualResponse = practitionerService.getAllPractitioners("");
//
//        assertThat(actualResponse)
//                .isNotNull()
//                .extracting(ApiResponse::getMessage)
//                .isEqualTo("Success");
//    }
//
//    @Test
//    public void testGetPractitionerById_ValidId() {
//        String id = "456";
//        ApiResponse<GetProfileResponse> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Practitioner Found !!!");
//
//        doReturn(expectedResponse)
//                .when(practitionerService)
//                .sendGetRequest(eq(String.format("/get/%s/%s", ResourceType.Practitioner.name(), id)), any());
//
//        ApiResponse<GetProfileResponse> actualResponse = practitionerService.getPractitionerById(id);
//
//        assertThat(actualResponse).isNotNull();
//        assertThat(actualResponse.getMessage()).isEqualTo("Practitioner Found !!!");
//    }
//
//    @Test
//    public void testGetPractitionerById_NullOrEmptyId() {
//        assertThatThrownBy(() -> practitionerService.getPractitionerById(null))
//                .isInstanceOf(ValidationError.class)
//                .hasMessage("Practitioner ID cannot be null or empty.");
//    }
//
//    @Test
//    public void testPractitionerExists_True() {
//        String id = "456";
//        ApiResponse<GetProfileResponse> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Practitioner Found !!!");
//
//        doReturn(expectedResponse).when(practitionerService).getPractitionerById(id);
//
//        boolean exists = practitionerService.practitionerExists(id);
//        assertThat(exists).isTrue();
//    }
//
//    @Test
//    public void testPractitionerExists_False() {
//        String id = "456";
//        ApiResponse<GetProfileResponse> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Practitioner Not Found");
//
//        doReturn(expectedResponse).when(practitionerService).getPractitionerById(id);
//
//        boolean exists = practitionerService.practitionerExists(id);
//        assertThat(exists).isFalse();
//    }
//
//    @Test
//    public void testCreatePractitioner() {
//        PractitionerDTO newPractitioner = new PractitionerDTO();
//        newPractitioner.setRegistrationId("REG-12345");
//        newPractitioner.setDepartment("Cardiology");
//        newPractitioner.setDesignation("Senior Consultant");
//        newPractitioner.setStatus("Active");
//        newPractitioner.setJoiningDate("2022-01-10");
//        newPractitioner.setStaffType("Full-Time");
//        newPractitioner.setFirstName("Sandy");
//        newPractitioner.setMiddleName("A.");
//        newPractitioner.setLastName("Raina");
//        newPractitioner.setBirthDate("2000-05-15");
//        newPractitioner.setGender(Gender.MALE);
//        newPractitioner.setMobileNumber("9876543210");
//        newPractitioner.setAddress("123, Main Street,Hindupur");
//        newPractitioner.setPincode("510001");
//        newPractitioner.setState(StatesAndUnionTerritories.ANDHRAPRADESH);
//        newPractitioner.setResourceType(ResourceType.Practitioner);
//        newPractitioner.setEmailId("sandy.raina@example.com");
//        newPractitioner.setWantsToLinkWhatsapp(true);
//        newPractitioner.setPhoto("photo-url");
//        newPractitioner.setResourceId("prac-789");
//
//        ApiResponse<UpdateProfileResponse> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Practitioner Created Successfully");
//
//        String endpoint = "/add/" + ResourceType.Practitioner.name();
//        doReturn(expectedResponse)
//                .when(practitionerService)
//                .sendPostRequest(eq(endpoint), eq(newPractitioner), any());
//
//        ApiResponse<UpdateProfileResponse> actualResponse = practitionerService.createPractitioner(newPractitioner);
//
//        assertThat(actualResponse)
//                .isNotNull()
//                .extracting(ApiResponse::getMessage)
//                .isEqualTo("Practitioner Created Successfully");
//    }
//
//    @Test
//    public void testCreatePractitioner_NullPractitioner_ShouldThrowValidationError(){
//        assertThrows(ValidationError.class, () -> practitionerService.createPractitioner(null));
//    }
//    @Test
//    public void testCreatePractitioner_MissingMandatoryFields_ShouldThrowValidationError() {
//        PractitionerDTO invalidPractitioner = new PractitionerDTO();
//
//        assertThrows(ValidationError.class, () -> practitionerService.createPractitioner(invalidPractitioner));
//    }
//
//    @Test
//    public void testCreatePractitioner_MissingRegistrationId_ShouldThrowValidationError() {
//        PractitionerDTO newPractitioner = new PractitionerDTO();
//        newPractitioner.setFirstName("abhi");
//        newPractitioner.setLastName("ram");
//        newPractitioner.setMobileNumber("9876543210");
//
//        assertThatThrownBy(() -> practitionerService.createPractitioner(newPractitioner))
//                .isInstanceOf(EhrApiError.class)
//                .hasMessage("Registration ID is required.");
//    }
//
//    @Test
//    public void testCreatePractitioner_InvalidPhoneNumber_ShouldThrowValidationError() {
//        PractitionerDTO newPractitioner = new PractitionerDTO();
//        newPractitioner.setRegistrationId("REG-12345");
//        newPractitioner.setFirstName("John");
//        newPractitioner.setLastName("Doe");
//        newPractitioner.setMobileNumber("123"); // Invalid phone number
//
//        assertThrows(ValidationError.class, () -> practitionerService.createPractitioner(newPractitioner));
//    }
//
//    @Test
//    public void testCreatePractitioner_MissingDepartment_ShouldThrowValidationError() {
//        PractitionerDTO newPractitioner = new PractitionerDTO();
//        newPractitioner.setRegistrationId("REG-12345");
//
//        assertThatThrownBy(() -> practitionerService.createPractitioner(newPractitioner))
//                .isInstanceOf(EhrApiError.class)
//                .hasMessage("Department is required.");
//    }
//
//    @Test
//    public void testUpdatePractitioner_MissingResourceId_ShouldThrowValidationError() {
//        PractitionerDTO updatePractitionerData = new PractitionerDTO();
//        updatePractitionerData.setEmailId("updated.xyz@gmail.com");
//
//        assertThrows(ValidationError.class, () -> practitionerService.updatePractitioner(updatePractitionerData));
//    }
//
//    @Test
//    public void testUpdatePractitioner_BackendFailure_ShouldHandleError() {
//        PractitionerDTO updatePractitionerData = new PractitionerDTO();
//        updatePractitionerData.setResourceId("MIC-789");
//        updatePractitionerData.setRegistrationId("374904"); // Required field
//        updatePractitionerData.setDepartment("Cardiology"); // Required field
//        updatePractitionerData.setDesignation("Senior Doctor");
//        updatePractitionerData.setStatus("Active");
//        updatePractitionerData.setJoiningDate("2014-01-24");
//        updatePractitionerData.setFirstName("Sai");
//        updatePractitionerData.setMiddleName("A.");
//        updatePractitionerData.setLastName("Ram");
//        updatePractitionerData.setBirthDate("2000-05-15");
//        updatePractitionerData.setGender(Gender.MALE);
//        updatePractitionerData.setMobileNumber("9876543210");
//        updatePractitionerData.setEmailId("updated_practitioner@xyz.com");
//        updatePractitionerData.setAddress("123, Main Street, Coorg");
//        updatePractitionerData.setPincode("510001");
//        updatePractitionerData.setState(StatesAndUnionTerritories.KARNATAKA);
//        updatePractitionerData.setStaffType("Full-Time");
//        updatePractitionerData.setResourceType(ResourceType.Practitioner);
//        updatePractitionerData.setWantsToLinkWhatsapp(true);
//        updatePractitionerData.setPhoto("photo-url");
//
//        String endpoint = "/update/" + ResourceType.Practitioner.name();
//
//        // Simulate backend failure (null response)
//        doReturn(null).when(practitionerService).sendPutRequest(eq(endpoint), eq(updatePractitionerData), any());
//
//        ApiResponse<UpdateProfileResponse> response = practitionerService.updatePractitioner(updatePractitionerData);
//
//        // Ensuring the response is null when the backend fails
//        assertThat(response).isNull();
//    }
//
//
//    @Test
//    public void testUpdatePractitioner() {
//        PractitionerDTO updatePractitionerData = new PractitionerDTO();
//        updatePractitionerData.setResourceId("prac-789");
//        updatePractitionerData.setRegistrationId("374904"); // Required field
//        updatePractitionerData.setDepartment("Cardiology"); // Required field
//        updatePractitionerData.setDesignation("Senior Doctor");
//        updatePractitionerData.setStatus("Active");
//        updatePractitionerData.setJoiningDate("2014-01-24"); // Ensured proper date format (YYYY-MM-DD)
//        updatePractitionerData.setFirstName("Sai");
//        updatePractitionerData.setMiddleName("P.");
//        updatePractitionerData.setLastName("Sandeep");
//        updatePractitionerData.setBirthDate("2000-05-15");
//        updatePractitionerData.setGender(Gender.MALE);
//        updatePractitionerData.setMobileNumber("9876543210");
//        updatePractitionerData.setEmailId("updated_practitioner@xyz.com");
//        updatePractitionerData.setAddress("123, Main Street, Coorg");
//        updatePractitionerData.setPincode("510001");
//        updatePractitionerData.setState(StatesAndUnionTerritories.KARNATAKA);
//        updatePractitionerData.setStaffType("Full-Time");
//        updatePractitionerData.setResourceType(ResourceType.Practitioner);
//        updatePractitionerData.setWantsToLinkWhatsapp(true);
//        updatePractitionerData.setPhoto("photo-url");
//
//        ApiResponse<UpdateProfileResponse> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Practitioner Updated Successfully");
//
//        String endpoint = "/update/" + ResourceType.Practitioner.name();
//        doReturn(expectedResponse)
//                .when(practitionerService)
//                .sendPutRequest(eq(endpoint), eq(updatePractitionerData), any());
//
//        ApiResponse<UpdateProfileResponse> actualResponse = practitionerService.updatePractitioner(updatePractitionerData);
//
//        assertThat(actualResponse)
//                .isNotNull()
//                .extracting(ApiResponse::getMessage)
//                .isEqualTo("Practitioner Updated Successfully");
//    }
//
//    @Test
//    public void testUpdatePractitioner_MissingRegistrationId_ShouldThrowValidationError() {
//        PractitionerDTO updatePractitionerData = new PractitionerDTO();
//        updatePractitionerData.setDepartment("Neurology");
//        updatePractitionerData.setResourceId("7439");
//
//        assertThatThrownBy(() -> practitionerService.updatePractitioner(updatePractitionerData))
//                .isInstanceOf(EhrApiError.class)
//                .hasMessage("Registration ID is required.");
//    }
//
//    @Test
//    public void testUpdatePractitioner_NullPractitioner_ShouldThrowValidationError() {
//        assertThrows(ValidationError.class, () -> practitionerService.updatePractitioner(null));
//    }
//
//    @Test
//    public void testDeletePractitioner_ValidId() {
//        String id = "456";
//        ApiResponse<Void> expectedResponse = new ApiResponse<>();
//        expectedResponse.setMessage("Practitioner Deleted Successfully");
//
//        doReturn(expectedResponse)
//                .when(practitionerService)
//                .sendDeleteRequest(eq(String.format("/practitioner/%s", id)), any());
//
//        ApiResponse<Void> actualResponse = practitionerService.deletePractitioner(id);
//
//        assertThat(actualResponse).isNotNull();
//        assertThat(actualResponse.getMessage()).isEqualTo("Practitioner Deleted Successfully");
//    }
//
//    @Test
//    public void testDeletePractitioner_InvalidId_ShouldThrowValidationError() {
//        assertThatThrownBy(() -> practitionerService.deletePractitioner(""))
//                .isInstanceOf(ValidationError.class)
//                .hasMessage("Practitioner ID cannot be null or empty.");
//    }
//
//}
