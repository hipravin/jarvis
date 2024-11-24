package hipravin.jarvis;

import hipravin.jarvis.engine.model.CodeFragment;
import hipravin.jarvis.engine.model.Link;
import hipravin.jarvis.github.GithubApiClient;
import hipravin.jarvis.engine.model.JarvisRequest;
import hipravin.jarvis.engine.model.JarvisResponse;
import hipravin.jarvis.github.GithubProperties;
import hipravin.jarvis.github.jackson.model.CodeSearchItem;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.github.jackson.model.TextMatches;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

import static hipravin.jarvis.github.GithubUtils.safeGetLogin;

@RestController
@Validated
@RequestMapping(path = "/api/v1/jarvis")
public class JarvisController {

    private final GithubApiClient githubApiClient;
    private final GithubProperties githubProperties;

    public JarvisController(GithubApiClient githubApiClient, GithubProperties githubProperties) {
        this.githubApiClient = githubApiClient;
        this.githubProperties = githubProperties;
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

//        String response = "%d results, %s".formatted(csr.count(), authorsFound);

        List<CodeFragment> codeFragments = csr.codeSearchItems().stream()
                .map(csi -> new CodeFragment(joinTextMatches(csi.textMatches()), Link.fromGithubHtmlUrl(csi.htmlUrl()),
                        csi.repository().owner().login()))
                .toList();

        return ResponseEntity.ok(new JarvisResponse(resultSummary(csr), codeFragments));
    }

    private String resultSummary(CodeSearchResult csr) {
        Map<String, Long> authorToResultCount = new LinkedHashMap<>();
        String other = "others";

        for (CodeSearchItem csi : csr.codeSearchItems()) { //TODO: just for fun - try to refactor it with streams
            String author = safeGetLogin(csi);
            if (!githubProperties.approvedAuthors().contains(author)) {
                author = other;
            }
            authorToResultCount.merge(author, 1L, Long::sum);
        }
        String summary = authorToResultCount.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(",\n"));

        return "Total: " + csr.count() + "\n" + summary;
    }

    private static String joinTextMatches(List<TextMatches> textMatches) {
        return textMatches.stream()
                .map(TextMatches::fragment)
                .collect(Collectors.joining("\n ... \n"));
    }
}
