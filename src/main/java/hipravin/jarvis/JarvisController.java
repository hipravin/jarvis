package hipravin.jarvis;

import hipravin.jarvis.engine.SearchEngine;
import hipravin.jarvis.engine.model.JarvisRequest;
import hipravin.jarvis.engine.model.JarvisResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(path = "/api/v1/jarvis")
public class JarvisController {

    private final SearchEngine searchEngine;

    public JarvisController(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    @PostMapping("/query")
    public ResponseEntity<JarvisResponse> query(@Valid @RequestBody JarvisRequest request) {
        JarvisResponse response = searchEngine.search(request);

        return ResponseEntity.ok(response);
    }
}
