package sdk.practitioner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/practitioners")
public class PractitionerController {

    @Autowired
    private PractitionerService practitionerService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> getAllPractitioners(
            @RequestParam(required = false) String nextPage) {
        return practitionerService.getAllPractitioners(nextPage)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(e.getMessage())));
    }

    //    @GetMapping("/filter")
//    public Mono<ApiResponse<Object>> getPractitionersByFilters(
//            @RequestParam String filters,
//            @RequestParam(required = false) String nextPage) {
//
//        return practitionerService.getPractitionersByFilters(filters, nextPage);
//    }

}
