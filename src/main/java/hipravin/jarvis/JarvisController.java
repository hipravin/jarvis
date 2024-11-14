package hipravin.jarvis;

import hipravin.jarvis.github.GithubApiClient;
import hipravin.jarvis.github.engine.model.JarvisRequest;
import hipravin.jarvis.github.engine.model.JarvisResponse;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.github.jackson.model.TextMatches;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
//@Validated //TODO: add
@RequestMapping(path = "/api/v1/jarvis")
public class JarvisController {

    private final GithubApiClient githubApiClient;

    public JarvisController(GithubApiClient githubApiClient) {
        this.githubApiClient = githubApiClient;
    }

    @PostMapping(value = "/query")
    public ResponseEntity<JarvisResponse> query(@RequestBody JarvisRequest request) {
        CodeSearchResult csr = githubApiClient.searchApprovedAuthors(request.query());
        String authorsFound = csr.codeSearchItems().stream()
                .map(r -> r.repository().owner().login())
                .distinct()
                .collect(Collectors.joining(", "));

        String textFragment = csr.codeSearchItems().stream()
                .flatMap(item -> item.textMatches().stream())
                .map(TextMatches::fragment)
                .findFirst().orElse("");

        String response = "%d results, %s".formatted(csr.count(), authorsFound);

        return ResponseEntity.ok(new JarvisResponse(response, textFragment));
    }
}
