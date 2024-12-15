package hipravin.jarvis.engine;

import hipravin.jarvis.engine.model.*;
import hipravin.jarvis.github.GithubApiClient;
import hipravin.jarvis.github.GithubProperties;
import hipravin.jarvis.github.GithubUtils;
import hipravin.jarvis.github.jackson.model.CodeSearchItem;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.github.jackson.model.TextMatches;
import hipravin.jarvis.googlebooks.GoogleBooksApiClient;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolume;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;
import hipravin.jarvis.googlebooks.jackson.model.SearchInfo;
import org.owasp.esapi.ESAPI;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchEngineImpl implements SearchEngine {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Pattern UNICODE_SPACES = Pattern.compile("(?U)\\s+");

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

        if (booksVolumes.items() == null || booksVolumes.items().isEmpty()) {
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

        var responseItems = booksVolumes.items().stream()
                .map(bv -> new ResponseItem(volumeToLink.apply(bv),
                        Optional.ofNullable(bv.searchInfo()).map(SearchInfo::textSnippet).orElse("n/a")))
                .toList();

        return new JarvisResponse("", authors, responseItems, codeFragments);
    }

    private JarvisResponse searchGithub(String query) {
        Set<String> queryTerms = UNICODE_SPACES.splitAsStream(query)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        CodeSearchResult csr = githubApiClient.searchApprovedAuthors(query);

        Function<String, String> approvedAuthorOrOthers =
                (author) -> githubProperties.approvedAuthors().contains(author) ? author : AuthorResult.OTHERS;

        Map<String, List<CodeSearchItem>> byAuthor = csr.codeSearchItems().stream()
                .collect(Collectors.groupingBy(csi -> approvedAuthorOrOthers.apply(GithubUtils.safeGetLogin(csi)),
                        LinkedHashMap::new, Collectors.toList()));

        List<ResponseItem> responseItems = byAuthor.entrySet().stream()
                .map(e -> new ResponseItem(new Link(e.getKey() + ": " + e.getValue().size(),
                        githubApiClient.githubBrowserSearchUrl(e.getKey(), query)), shortDescription(e.getValue(), query, queryTerms)))
                .toList();

        List<CodeFragment> codeFragments = csr.codeSearchItems().stream()
                .map(csi -> new CodeFragment(joinTextMatches(csi.textMatches()), Link.fromGithubHtmlUrl(csi.htmlUrl()),
                        csi.repository().owner().login()))
                .toList();

        List<AuthorResult> authorResults = authorResultSummary(csr);

        String summary = authorResults.stream()
                .map(a -> "%s: %d".formatted(a.author(), a.count()))
                .collect(Collectors.joining(",\n"));

        return new JarvisResponse(summary, authorResults, responseItems, codeFragments);
    }

    private String shortDescription(List<CodeSearchItem> codeSearchItems, String query, Set<String> queryTerms) {
        List<String> descriptionLines = removeCommonLeadingSpaces(shortDescriptionLines(codeSearchItems, query, queryTerms));
        String description = String.join("\n", descriptionLines);
        String sanitized = ESAPI.encoder().encodeForHTML(description);

        String highlighted = queryTerms.stream()
                .reduce(sanitized, (result, elem) -> highlight(result, elem, "<b>%s</b>"::formatted));

        return highlighted;
    }

    static String highlight(String text, String term, Function<String, String> highlightTermFunction) {
        Matcher matcher = Pattern.compile("(?i)" + Pattern.quote(term)).matcher(text);
        return matcher.replaceAll((mr) -> highlightTermFunction.apply(mr.group(0)));
    }

    private List<String> shortDescriptionLines(List<CodeSearchItem> codeSearchItems, String query, Set<String> queryTerms) {
        int maxLines = 5;

        List<String> bestMatch = new ArrayList<>();

        for (CodeSearchItem codeSearchItem : codeSearchItems) {
            for (TextMatches textMatches : codeSearchItem.textMatches()) {
                Iterable<String> textMatchesLines = () -> textMatches.fragment().lines().iterator();
                List<String> sequentialLinesWithTerms = new ArrayList<>();
                for (String textMatchesLine : textMatchesLines) {
                    boolean containAnyTerm = queryTerms.stream().allMatch(term -> textMatchesLine.toLowerCase().contains(term.toLowerCase()));
                    if(containAnyTerm) {
                        sequentialLinesWithTerms.add(textMatchesLine);
                        if(bestMatch.size() < sequentialLinesWithTerms.size()) {
                            bestMatch = List.copyOf(sequentialLinesWithTerms);
                        }
                    } else {
                        sequentialLinesWithTerms.clear();
                    }
                    if(sequentialLinesWithTerms.size() >= maxLines) {
                        return sequentialLinesWithTerms;
                    }
                }
            }
        }
        return bestMatch;
    }

    private static List<String> removeCommonLeadingSpaces(List<String> original) {
        if(original.isEmpty()) {
            return original;
        }
        if(original.size() == 1) {
            return List.of(original.get(0).stripLeading());
        }

        return original.stream()
                .map(String::stripLeading)
                .toList();
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
