package hipravin.jarvis.engine;

import hipravin.jarvis.engine.model.*;
import hipravin.jarvis.github.GithubApiClient;
import hipravin.jarvis.github.GithubProperties;
import hipravin.jarvis.github.GithubUtils;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.github.jackson.model.TextMatches;
import hipravin.jarvis.googlebooks.GoogleBooksApiClient;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolume;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchEngineImpl implements SearchEngine {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final GithubApiClient githubApiClient;
    private final GoogleBooksApiClient googleBooksApiClient;
    private final GithubProperties githubProperties;

    public SearchEngineImpl(GithubApiClient githubApiClient, GoogleBooksApiClient googleBooksApiClient,
                            GithubProperties githubProperties) {
        this.githubApiClient = githubApiClient;
        this.googleBooksApiClient = googleBooksApiClient;
        this.githubProperties = githubProperties;
    }

    @Override
    public JarvisResponse search(JarvisRequest request) {
        var cf1 = CompletableFuture.supplyAsync(() -> searchGithub(request.query()), executor);
        var cf2 = CompletableFuture.supplyAsync(() -> searchGoogleBooks(request.query()), executor);

        return JarvisResponse.combine(cf1.join(), cf2.join());
    }

    private JarvisResponse searchGoogleBooks(String query) {
        BooksVolumes booksVolumes = googleBooksApiClient.search(query);

        if (booksVolumes.items() == null) {
            return JarvisResponse.EMPTY_RESPONSE;
        }

        Function<BooksVolume, Link> volumeToLink = (bv) ->
                new Link(bv.volumeInfo().title() + ", " +
                        bv.yearPublished().orElse("n/a"),
                        bv.volumeInfo().previewLink());

        var codeFragments = booksVolumes.items().stream()
                .filter(BooksVolume.HAS_TEXT_SNIPPET.and(BooksVolume.HAS_PREVIEW_LINK))
                .map(bv -> new CodeFragment(bv.searchInfo().textSnippet(), volumeToLink.apply(bv),
                        bv.volumeInfo().publisher()))
                .toList();

        var authors = booksVolumes.items().stream()
                .map(bv -> new AuthorResult(bv.volumeInfo().title(), 1))
                .toList();

        return new JarvisResponse("", authors, codeFragments);
    }

    private JarvisResponse searchGithub(String query) {
        CodeSearchResult csr = githubApiClient.searchApprovedAuthors(query);

        List<CodeFragment> codeFragments = csr.codeSearchItems().stream()
                .map(csi -> new CodeFragment(joinTextMatches(csi.textMatches()), Link.fromGithubHtmlUrl(csi.htmlUrl()),
                        csi.repository().owner().login()))
                .toList();

        List<AuthorResult> authorResults = authorResultSummary(csr);

        String summary = authorResults.stream()
                .map(a -> "%s: %d".formatted(a.author(), a.count()))
                .collect(Collectors.joining(",\n"));

        return new JarvisResponse(summary, authorResults, codeFragments);
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
