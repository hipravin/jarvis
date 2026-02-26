package hipravin.jarvis;

import hipravin.jarvis.engine.SearchEngine;
import hipravin.jarvis.engine.model.JarvisRequest;
import hipravin.jarvis.engine.model.JarvisResponse;
import hipravin.jarvis.engine.model.SearchProviderType;
import hipravin.jarvis.openapi.TagSearch;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping(path = "/api/v1/jarvis")
public class JarvisController {
    private static final Logger log = LoggerFactory.getLogger(JarvisController.class);

    private final SearchEngine searchEngine;

    public JarvisController(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    @TagSearch
    @PostMapping("/query")
    public ResponseEntity<JarvisResponse> queryPost(@Valid @RequestBody JarvisRequest request) {
        JarvisResponse response = searchEngine.search(request);

        return ResponseEntity.ok(response);
    }

    @TagSearch
    @GetMapping("/query")
    public ResponseEntity<JarvisResponse> query(@NotBlank @RequestParam("q") String query,
                                                @RequestParam("sp") List<String> providers) {

        var searchProviders = providers.stream()
                .map(SearchProviderType::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        JarvisResponse response = searchEngine.search(new JarvisRequest(query, searchProviders));
        return ResponseEntity.ok(response);
    }
}
