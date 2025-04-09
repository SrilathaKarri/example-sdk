package sdk.documentLinking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import sdk.documentLinking.documentLinkingDTOs.HealthDocumentLinkingDTO;

@RestController
public class DocumentLinkingController {

    @Autowired
    DocumentLinking documentLinkingService;

    @PostMapping("/link")
    public Mono<ResponseEntity<Boolean>> linkDoc(@RequestBody HealthDocumentLinkingDTO dto) {
        return documentLinkingService.linkHealthDocument(dto)
                .map(ResponseEntity::ok)
                .onErrorResume(error ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false)));
    }
}
