package hipravin.jarvis.github;

import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.SearchService;
import hipravin.jarvis.enginev2.dto.Excerpt;
import hipravin.jarvis.enginev2.dto.SearchRequest;
import hipravin.jarvis.enginev2.dto.SearchResponse;
import hipravin.jarvis.github.jackson.model.CodeSearchItem;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import org.owasp.esapi.ESAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

import static hipravin.jarvis.engine.model.InformationSource.GITHUB;

@Service
@Order(SearchService.Order.ORDER_1)
public class GithubSearchService implements SearchService {
    private static final Logger log = LoggerFactory.getLogger(GithubSearchService.class);

    private static final Pattern UNICODE_SPACES = Pattern.compile("(?U)\\s+");
    private final GithubApiClient githubApiClient;
    private final GithubProperties githubProperties;

    public GithubSearchService(GithubApiClient githubApiClient, GithubProperties githubProperties) {
        this.githubApiClient = githubApiClient;
        this.githubProperties = githubProperties;
    }

    @Override
    public SearchResponse search(SearchRequest request) {
        try {
            return SearchResponse.success(searchForCode(request.query()));
        } catch (RuntimeException e) {
            log.error("Search failed for query '{}': {}", request, e.getMessage(), e);
            return SearchResponse.failed(e);
        }
    }

    private List<Excerpt> searchForCode(String query) {
        Set<String> queryTerms = UNICODE_SPACES.splitAsStream(query)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        CodeSearchResult csr = githubApiClient.searchApprovedAuthors(query);

        Function<String, String> approvedAuthorOrOthers =
                (author) -> githubProperties.approvedAuthors().contains(author) ? author : "";

        Map<String, List<CodeSearchItem>> byAuthor = csr.codeSearchItems().stream()
                .collect(Collectors.groupingBy(csi -> approvedAuthorOrOthers.apply(GithubUtils.safeGetLogin(csi)),
                        LinkedHashMap::new, Collectors.toList()));

        return byAuthor.entrySet().stream()
                .map(e -> toExcerpt(e.getKey(), e.getValue(), query, queryTerms))
                .toList();
    }

    private Excerpt toExcerpt(String authorGroup, List<CodeSearchItem> codeSearchItems, String query, Set<String> queryTerms) {
        Map<String, Long> individualAuthorCounts =  codeSearchItems.stream()
                .collect(Collectors.groupingBy(GithubUtils::safeGetLogin, Collectors.counting()));

        return Excerpt.builder()
                .source(GITHUB)
                .title(emptyToOthers(authorGroup) + ": " + codeSearchItems.size(), githubApiClient.buildUserSearchUrl(authorGroup, query))
                .mainHtml(shortDescription(codeSearchItems, queryTerms))
                .metadata(Map.of(Excerpt.METADATA_GH_USERS, individualAuthorCounts))
                .build();
    }

    private static String emptyToOthers(String name) {
        return StringUtils.hasText(name) ? name : "Others";
    }

    private String shortDescription(List<CodeSearchItem> codeSearchItems, Set<String> queryTerms) {
        List<String> descriptionLines = stripExtraLeadingSpaces(shortDescriptionLines(codeSearchItems, queryTerms));
        String description = String.join("\n", descriptionLines);
        String sanitized = ESAPI.encoder().encodeForHTML(description);

        return queryTerms.stream()
                .reduce(sanitized, (result, elem) -> highlight(result, elem, "<b>%s</b>"::formatted));
    }

    static String highlight(String text, String term, Function<String, String> highlightTermFunction) {
        Matcher matcher = Pattern.compile("(?i)" + Pattern.quote(term)).matcher(text);
        return matcher.replaceAll((mr) -> highlightTermFunction.apply(mr.group(0)));
    }

    record Score(int distinctMatches, int allMatches) implements Comparable<Score> {
        @Override
        public int compareTo(Score o) {
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
                .mapToInt(GithubSearchService::leadingSpaceCharCount)
                .min().orElse(0);

        return original.stream()
                .map(s -> s.substring(extraSpaceCharCount))
                .toList();
    }

    @Override
    public InformationSource getSource() {
        return InformationSource.GITHUB;
    }
}
