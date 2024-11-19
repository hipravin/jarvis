package hipravin.jarvis;

import hipravin.jarvis.engine.model.CodeFragment;
import hipravin.jarvis.engine.model.Link;
import hipravin.jarvis.github.GithubApiClient;
import hipravin.jarvis.engine.model.JarvisRequest;
import hipravin.jarvis.engine.model.JarvisResponse;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.github.jackson.model.TextMatches;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;
import java.util.List;

@RestController
@Validated
@RequestMapping(path = "/api/v1/jarvis")
public class JarvisController {

    private final GithubApiClient githubApiClient;

    public JarvisController(GithubApiClient githubApiClient) {
        this.githubApiClient = githubApiClient;
    }

    @PostMapping(value = "/query")
    public ResponseEntity<JarvisResponse> query(@Valid @RequestBody JarvisRequest request) {
        CodeSearchResult csr = githubApiClient.searchApprovedAuthors(request.query());
        String authorsFound = csr.codeSearchItems().stream()
                .map(r -> r.repository().owner().login())
                .distinct()
                .collect(Collectors.joining(", "));

        String briefResult = csr.codeSearchItems().stream()
                .map(csi -> csi.htmlUrl() + "\n" + joinTextMatches(csi.textMatches()))
                .collect(Collectors.joining("\n=========================\n"));

        String response = "%d results, %s".formatted(csr.count(), authorsFound);

        List<CodeFragment> codeFragments = csr.codeSearchItems().stream()
                .map(csi -> new CodeFragment(joinTextMatches(csi.textMatches()), Link.fromGithubHtmlUrl(csi.htmlUrl()),
                        csi.repository().owner().login()))
                .toList();

        return ResponseEntity.ok(new JarvisResponse(response, briefResult, codeFragments));
    }

    private static String joinTextMatches(List<TextMatches> textMatches) {
        return textMatches.stream()
                .map(TextMatches::fragment)
                .collect(Collectors.joining("\n ... \n"));
    }
}
