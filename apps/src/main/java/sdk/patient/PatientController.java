package sdk.patient;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import sdk.base.LogUtil;
import sdk.base.response.ApiResponse;

import java.util.Optional;


/**
 * Controller for testing the Patient functionality.
 * Provides endpoints to test all the methods in the Patient.
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private Patient patient;


    /**
     * Test endpoint for getting all patients.
     *
     * @param nextPage Pagination token (optional)
     * @return API response with patient data
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> getAllPatients(
            @RequestParam(required = false) String nextPage) {
        return patient.findAll(nextPage)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }

    /**
     * Test endpoint for getting a patient by ID.
     *
     * @param id Patient ID
     * @return API response with patient data
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> findById(@PathVariable String id) {
        return patient.getPatientById(id)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }

    /**
     * Test endpoint for checking if a patient exists.
     *
     * @param id Patient ID
     * @return Boolean indicating if patient exists
     */
    @GetMapping(value = "/exists/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Boolean>> checkPatient(@PathVariable String id) {
        return patient.patientExists(id)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(false)));
    }


    /**
     * Test endpoint for updating an existing patient.
     *
     * @param updatePatientDTO Updated patient data
     * @return API response with updated patient data
     */
    @PutMapping("/update")
    public Mono<ApiResponse<Object>> updatePatient(@RequestBody UpdatePatientDTO updatePatientDTO) {
        return patient.updatePatient(updatePatientDTO);
    }

    /**
     * Test endpoint for creating a new patient.
     *
     * @param patientDTO Patient data
     * @return API response with created patient data
     */
    @PostMapping("/create")
    public Mono<ResponseEntity<Object>> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        return patient.createPatient(patientDTO)
                .map(response -> ResponseEntity.ok().body(response))
                .onErrorResume(e -> {
                    LogUtil.logger.error("Error creating patient", e);
                    return Mono.just(ResponseEntity.badRequest().body(new ApiResponse<>(e.getMessage(), e.toString())));
                });
    }

    /**
     * Test endpoint for deleting a patient.
     *
     * @param id Patient ID
     * @return API response confirming deletion
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ApiResponse<Object>>> deletePatient(@PathVariable String id) {
        return patient.deletePatient(id)
                .map(response -> ResponseEntity.ok().body(response))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(new ApiResponse<>(e.getMessage(),e.toString()))));
    }



    }
