package hipravin.jarvis;

import hipravin.jarvis.engine.model.*;
import hipravin.jarvis.github.GithubApiClient;
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
import java.util.stream.Stream;

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

//        String response = "%d results, %s".formatted(csr.count(), authorsFound);

        List<CodeFragment> codeFragments = csr.codeSearchItems().stream()
                .map(csi -> new CodeFragment(joinTextMatches(csi.textMatches()), Link.fromGithubHtmlUrl(csi.htmlUrl()),
                        csi.repository().owner().login()))
                .toList();

        List<AuthorResult> authorResults = authorResultSummary(csr);

        String summary = authorResults.stream()
                .map(a -> "%s: %d".formatted(a.author(), a.count()))
                .collect(Collectors.joining(",\n"));

        return ResponseEntity.ok(new JarvisResponse(summary, authorResults, codeFragments));
    }

    private List<AuthorResult> authorResultSummary(CodeSearchResult csr) {
        Map<String, Integer> authorToResultCount = new LinkedHashMap<>();

        for (CodeSearchItem csi : csr.codeSearchItems()) { //TODO: just for fun - try to refactor it with streams
            String author = safeGetLogin(csi);
            if (!githubProperties.approvedAuthors().contains(author)) {
                author = AuthorResult.OTHERS;
            }
            authorToResultCount.merge(author, 1, Integer::sum);
        }

        Stream<AuthorResult> results = authorToResultCount.entrySet().stream()
                .map(e -> new AuthorResult(e.getKey(), e.getValue()));

        return Stream.concat(Stream.of(new AuthorResult(AuthorResult.TOTAL, csr.count())), results)
                .toList();
    }

    private static String joinTextMatches(List<TextMatches> textMatches) {
        return textMatches.stream()
                .map(TextMatches::fragment)
                .collect(Collectors.joining("\n ... \n"));
    }
}
