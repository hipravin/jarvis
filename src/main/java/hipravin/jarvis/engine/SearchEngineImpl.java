package hipravin.jarvis.engine;

import hipravin.jarvis.bookstore.dao.BookstoreDao;
import hipravin.jarvis.bookstore.dao.entity.BookPageFtsEntity;
import hipravin.jarvis.engine.model.*;
import hipravin.jarvis.github.GithubApiClient;
import hipravin.jarvis.github.GithubProperties;
import hipravin.jarvis.github.GithubUtils;
import hipravin.jarvis.github.jackson.model.CodeSearchItem;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.googlebooks.GoogleBooksApiClient;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolume;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;
import hipravin.jarvis.googlebooks.jackson.model.SearchInfo;
import org.owasp.esapi.ESAPI;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

import static hipravin.jarvis.engine.model.SearchProviderType.*;

@Service
public class SearchEngineImpl implements SearchEngine {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private static final Pattern UNICODE_SPACES = Pattern.compile("(?U)\\s+");

    private final GithubApiClient githubApiClient;
    private final GoogleBooksApiClient googleBooksApiClient;
    private final GithubProperties githubProperties;
    private final BookstoreDao bookstoreDao;

    public SearchEngineImpl(GithubApiClient githubApiClient, GoogleBooksApiClient googleBooksApiClient,
                            GithubProperties githubProperties, BookstoreDao bookstoreDao) {
        this.githubApiClient = githubApiClient;
        this.googleBooksApiClient = googleBooksApiClient;
        this.githubProperties = githubProperties;
        this.bookstoreDao = bookstoreDao;
    }

    @Override
    public JarvisResponse search(JarvisRequest request) {
        Set<SearchProviderType> providers = request.searchProviders();
        var cfGithub = searchAsync(providers, GITHUB, () -> searchGithub(request.query()));
        var cfBookstore = searchAsync(providers, BOOKSTORE, () -> searchBookstore(request.query()));
        var cfGoogleBooks = searchAsync(providers, GOOGLE_BOOKS, () -> searchGoogleBooks(request.query()));

        JarvisResponse response = CompletableFuture.allOf(cfGithub, cfBookstore, cfGoogleBooks)
                .thenApply(_ -> JarvisResponse.combine(cfGithub.join(), cfBookstore.join(), cfGoogleBooks.join()))
                .join();

        return response;
    }

    private CompletableFuture<JarvisResponse> searchAsync(Set<SearchProviderType> searchProviders,
                                                          SearchProviderType provider,
                                                          Supplier<JarvisResponse> searchSupplier) {
        if ((searchProviders != null) && searchProviders.contains(provider)) {
            return CompletableFuture.supplyAsync(searchSupplier, executor)
                    .thenApply(r -> r.orElse("No results in %s matching your query".formatted(provider.alias())))
                    .exceptionally(e -> JarvisResponse.ofMessage("%s: %s".formatted(provider.alias(), e.getMessage())));
        } else {
            return CompletableFuture.completedFuture(JarvisResponse.ofMessage("%s search disabled".formatted(provider.alias())));
        }
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

        var responseItems = booksVolumes.items().stream()
                .map(bv -> new ResponseItem(volumeToLink.apply(bv),
                        GOOGLE_BOOKS,
                        Optional.ofNullable(bv.searchInfo()).map(SearchInfo::textSnippet).orElse("n/a")))
                .toList();

        return new JarvisResponse("", responseItems);
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
                .map(e -> new ResponseItem(new Link(emptyToOthers(e.getKey()) + ": " + e.getValue().size(),
                        githubApiClient.githubBrowserSearchUrl(e.getKey(), query)),
                        GITHUB,
                        shortDescription(e.getValue(), queryTerms)))
                .toList();

        return new JarvisResponse("Github result count: " + csr.count(), responseItems);
    }

    private JarvisResponse searchBookstore(String query) {
        List<BookPageFtsEntity> matchedBookPages = bookstoreDao.search(query);

        List<ResponseItem> responseItems = matchedBookPages.stream()
                .map(this::fromBookPage)
                .toList();

        return new JarvisResponse("", responseItems);
    }

    private ResponseItem fromBookPage(BookPageFtsEntity bp) {
        String title = bp.getBook().getTitle();
        Long bookId = Objects.requireNonNull(bp.getBookPageId().bookId());
        Long pageNum = Objects.requireNonNull(bp.getBookPageId().pageNum());

        String linkText = "%s, p.%d".formatted(title, pageNum);
        String linkHref = "/api/v1/bookstore/book/%d/rawpdf#page=%d".formatted(
                bookId, pageNum);
        Link bookPageLink = new Link(linkText, linkHref);

        return new ResponseItem(bookPageLink, BOOKSTORE, bp.getContentHighlighted());
    }

    public static String emptyToOthers(String name) {
        return StringUtils.hasText(name) ? name : "Others";
    }

    private String shortDescription(List<CodeSearchItem> codeSearchItems, Set<String> queryTerms) {
        List<String> descriptionLines = stripExtraLeadingSpaces(shortDescriptionLines(codeSearchItems, queryTerms));
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

    record Score(int distinctMatches, int allMatches) implements Comparable<Score> {
        @Override
        public int compareTo(SearchEngineImpl.Score o) {
            int result = Integer.compare(distinctMatches, o.distinctMatches);
            if (result == 0) {
                result = Integer.compare(allMatches, o.allMatches);
            }
            return result;
        }

        public static Score calculateMatchScore(List<String> lines, Set<String> queryTerms) {
            Set<String> distinctMatchTerms = new HashSet<>();

            int matchCount = 0;
            for (String line : lines) {
                String lineLc = line.toLowerCase();
                for (String term : queryTerms) {
                    if (lineLc.contains(term.toLowerCase())) {
                        distinctMatchTerms.add(term);
                        matchCount++;
                    }
                }
            }
            return new Score(distinctMatchTerms.size(), matchCount);
        }
    }

    private List<String> shortDescriptionLines(List<CodeSearchItem> codeSearchItems, Set<String> queryTerms) {
        int maxLines = 7;
        List<String> blankLines = Stream.generate(() -> "").limit(maxLines).toList();

        List<String> bestScored = codeSearchItems.stream()
                .flatMap(csi -> csi.textMatches().stream())
                .flatMap(tm -> Stream.concat(
                        tm.fragment().lines()
                                .filter(l -> !l.isBlank()),
                        blankLines.stream()))
                .gather(Gatherers.windowSliding(maxLines))
                .max(Comparator.comparing(lines -> Score.calculateMatchScore(lines, queryTerms)))
                .orElse(List.of());

        return bestScored.stream()
                .filter(l -> !l.isBlank()).toList();//remove synthetic empty lines
    }

    private static final Pattern LEADING_SPACES = Pattern.compile("^(\\s+).*$");

    private static int leadingSpaceCharCount(String s) {
        var m = LEADING_SPACES.matcher(s);
        if (m.find()) {
            return m.group(1).length();
        } else {
            return 0;
        }
    }

    private static List<String> stripExtraLeadingSpaces(List<String> original) {
        if (original.isEmpty()) {
            return original;
        }
        if (original.size() == 1) {
            return List.of(original.getFirst().stripLeading());
        }

        int extraSpaceCharCount = original.stream()
                .mapToInt(SearchEngineImpl::leadingSpaceCharCount)
                .min().orElse(0);

        return original.stream()
                .map(s -> s.substring(extraSpaceCharCount))
                .toList();
    }

//    private List<AuthorResult> authorResultSummary(CodeSearchResult csr) {
//        Function<String, String> approvedAuthorOrOthers =
//                (author) -> githubProperties.approvedAuthors().contains(author) ? author : AuthorResult.OTHERS;
//
//        Map<String, Long> authorCounts = csr.codeSearchItems().stream()
//                .map(GithubUtils::safeGetLogin)
//                .map(approvedAuthorOrOthers)
//                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
//
//        Stream<AuthorResult> results = authorCounts.entrySet().stream()
//                .map(e -> new AuthorResult(e.getKey(), e.getValue()));
//
//        return Stream.concat(Stream.of(new AuthorResult(AuthorResult.TOTAL, csr.count())), results)
//                .toList();
//    }

//    private static class SearchThreadFactory implements ThreadFactory {
//        private final AtomicInteger threadNumber = new AtomicInteger(0);
//
//        @Override
//        public Thread newThread(Runnable r) {
//            Thread thread = new Thread(r);
//            thread.setDaemon(true);
//            thread.setName("Search engine worker #%d".formatted(this.threadNumber.getAndIncrement()));
//            return thread;
//        }
//    }
}
