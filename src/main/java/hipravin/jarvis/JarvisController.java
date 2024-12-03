package hipravin.jarvis;

import hipravin.jarvis.engine.SearchEngine;
import hipravin.jarvis.engine.model.*;
import hipravin.jarvis.github.GithubApiClient;
import hipravin.jarvis.github.GithubProperties;
import hipravin.jarvis.github.GithubUtils;
import hipravin.jarvis.github.jackson.model.CodeSearchItem;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.github.jackson.model.TextMatches;
import jakarta.validation.Valid;
import org.kohsuke.github.function.FunctionThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
    private final SearchEngine searchEngine;

    public JarvisController(GithubApiClient githubApiClient, GithubProperties githubProperties, SearchEngine searchEngine) {
        this.githubApiClient = githubApiClient;
        this.githubProperties = githubProperties;
        this.searchEngine = searchEngine;
    }

    @PostMapping(value = "/query-googlebooks")
    public ResponseEntity<JarvisResponse> query(@Valid @RequestBody JarvisRequest request) {
        JarvisResponse response = searchEngine.search(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/query")
    public ResponseEntity<JarvisResponse> queryGithub(@Valid @RequestBody JarvisRequest request) {
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
        Function<String, String> approvedAuthorOrOthers =
                (author) -> githubProperties.approvedAuthors().contains(author) ? author : AuthorResult.OTHERS;

        Map<String, Long> authorCounts = csr.codeSearchItems().stream()
                .map(GithubUtils::safeGetLogin)
                .map(approvedAuthorOrOthers)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));

        Stream<AuthorResult> results = authorCounts.entrySet().stream()
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
