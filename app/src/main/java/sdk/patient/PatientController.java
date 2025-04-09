package sdk.patient;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import sdk.base.utils.LogUtil;
import sdk.patient.DTO.PatientDTO;
import sdk.patient.DTO.UpdatePatientDTO;


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
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) String nextPage) {
        return patient.findAll(pageNo,nextPage)
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
        return patient.findById(id)
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
        return patient.exists(id)
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
    public Mono<Object> updatePatient(@RequestBody UpdatePatientDTO updatePatientDTO) {
        return patient.update(updatePatientDTO);
    }

    /**
     * Test endpoint for creating a new patient.
     *
     * @param patientDTO Patient data
     * @return API response with created patient data
     */
    @PostMapping("/create")
    public Mono<ResponseEntity<Object>> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        return patient.create(patientDTO)
                .map(response -> ResponseEntity.ok().body(response))
                .onErrorResume(e -> {
                    LogUtil.logger.error("Error creating patient", e);
                    return Mono.just(ResponseEntity.badRequest().body(e.getMessage()));
                });
    }

    /**
     * Test endpoint for deleting a patient.
     *
     * @param id Patient ID
     * @return API response confirming deletion
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> deletePatient(@PathVariable String id) {
        return patient.delete(id)
                .map(response -> ResponseEntity.ok().body(response))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }



}
