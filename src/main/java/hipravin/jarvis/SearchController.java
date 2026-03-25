package hipravin.jarvis;

import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.SearchService;
import hipravin.jarvis.enginev2.dto.SearchRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/search")
public class SearchController {
    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<Object> search(@NotBlank @RequestParam("q") String query,
                                         @RequestParam(value = "is", required = false) Set<String> informationSources) {
        Set<InformationSource> sources = (informationSources == null || informationSources.isEmpty())
                ? EnumSet.allOf(InformationSource.class)
                : informationSources.stream().map(InformationSource::fromAlias).collect(Collectors.toSet());
        return ResponseEntity.ok(searchService.search(new SearchRequest(query, sources)));
    }

    @PostMapping
    public ResponseEntity<Object> search(@Valid @RequestBody SearchRequest request) {
        return ResponseEntity.ok(searchService.search(request));
    }
}
