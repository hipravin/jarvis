package hipravin.jarvis.github;

import com.google.common.collect.Lists;
import hipravin.jarvis.github.jackson.JacksonGithubMapper;
import hipravin.jarvis.github.jackson.model.CodeSearchItem;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.github.jackson.model.EncodedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static hipravin.jarvis.github.GithubUtils.safeGetLogin;
import static java.util.Optional.ofNullable;

/**
 * <a href="https://docs.github.com/en/search-github/searching-on-github/searching-code">Github docs</a>
 */
@Component
public class GithubApiClientImpl implements GithubApiClient, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(GithubApiClientImpl.class);
    //apparently github doesn't allow concurrent requests
    //https://github.com/api-platform/core/issues/3205  (closed, but probably not fixed)
    //anyway documentation explicitly recommends to avoid concurrent requests
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    private final GithubProperties githubProperties;
    private final JacksonGithubMapper mapper;
    private final HttpClient httpClient;
    private final HttpRequest.Builder githubHttpRequestBuilder;

    public GithubApiClientImpl(GithubProperties githubProperties,
                               JacksonGithubMapper mapper,
                               HttpClient.Builder githubHttpClientBuilder,
                               HttpRequest.Builder githubHttpRequestBuilder) {
        this.githubProperties = githubProperties;
        this.mapper = mapper;
        this.httpClient = githubHttpClientBuilder.build();
        this.githubHttpRequestBuilder = githubHttpRequestBuilder;
    }

    @Override
    public String getContent(String uri) {
        var request = githubHttpRequestBuilder.uri(URI.create(uri))
                .GET()
                .build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Request failed: %s: status %d, body: %s".formatted(
                        request.uri(), response.statusCode(), response.body()));
            }

            EncodedContent encodedContent = mapper.readContent(response.body());
            return decode(encodedContent.content(), encodedContent.encoding());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CodeSearchResult search(String searchString) {
        var request = githubHttpRequestBuilder.uri(URI.create(
                        "%s?per_page=%d&q=%s".formatted(githubProperties.codeSearchUrl(),
                                githubProperties.codeSearchPerPage(),
                                URLEncoder.encode(searchString, StandardCharsets.UTF_8))))
                .GET()
                .build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            var metadata = GithubResponseMetadata.fromHttpResponse(response);

            if (response.statusCode() != 200) {
                throw new RuntimeException("Request failed: %s: status %d, body: %s".formatted(
                        request.uri(), response.statusCode(), response.body()));
            }

            return mapper.readCodeSearchResult(response.body());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CodeSearchResult searchApprovedAuthors(String searchString) {
        List<List<String>> approvedAuthorsBatches = Lists.partition(List.copyOf(githubProperties.approvedAuthors()),
                githubProperties.singleRequestMaxOr());

        var requests = approvedAuthorsBatches.stream()
                .map(batch -> searchCodeApprovedAuthorsBatchSupplier(batch, searchString))
                .toList();

        var approvedAuthorsResult = CodeSearchResult.combine(requestConcurrently(requests));
        var genericSearchResult = search(searchString);

        return CodeSearchResult.combine(sortByApprovedAuthor(approvedAuthorsResult), genericSearchResult);
    }

    private CodeSearchResult sortByApprovedAuthor(CodeSearchResult codeSearchResult) {
        List<CodeSearchItem> items = new ArrayList<>(codeSearchResult.codeSearchItems());
        Map<String, Integer> approvedAuthorsPositions = new HashMap<>();
        int pos = 0;
        for (String author : githubProperties.approvedAuthors()) {
            approvedAuthorsPositions.put(author, pos);
            pos++;
        }

        Comparator<CodeSearchItem> byApprovedAuthorPositions = Comparator.comparingInt(
                it -> approvedAuthorsPositions.getOrDefault(safeGetLogin(it), Integer.MAX_VALUE));
        items.sort(byApprovedAuthorPositions);
//        //*.md, *.log last
//        List<CodeSearchItem> mdFiles = items.stream()
//                .filter(item -> item.path().toLowerCase().endsWith(".md"))
//                .toList();
//        List<CodeSearchItem> logFiles = items.stream()
//                .filter(item -> item.path().toLowerCase().endsWith(".log"))
//                .toList();
//        items.removeIf(item -> item.path().toLowerCase().endsWith(".md") || item.path().toLowerCase().endsWith(".log"));
//        items.addAll(mdFiles);
//        items.addAll(logFiles);

        return new CodeSearchResult(codeSearchResult.count(), codeSearchResult.incompleteResults(), List.copyOf(items));
    }

    private Supplier<CodeSearchResult> searchCodeApprovedAuthorsBatchSupplier(List<String> approvedAuthors, String searchString) {
        var orAuthors = approvedAuthors.stream()
                .map(author -> "user:" + author)
                .collect(Collectors.joining(" ", "", " "));
//                .collect(Collectors.joining(" OR ", "", " ")); //OR/AND seems to be not supported, rather 'OR' is included to search terms

        return () -> search(orAuthors + searchString);
    }

    private List<CodeSearchResult> requestConcurrently(List<Supplier<CodeSearchResult>> requests) {
        List<CompletableFuture<CodeSearchResult>> completableFutures = requests.stream()
                .map(r -> CompletableFuture.supplyAsync(r, executor))
                .toList();

        List<CodeSearchResult> responses = new ArrayList<>(requests.size());

        for (CompletableFuture<CodeSearchResult> completableFuture : completableFutures) {
            try {
                var response = completableFuture.get(20, TimeUnit.SECONDS);
                responses.add(response);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);//TODO: can we safely ignore other Futures after getting first exception?
            }
        }

        return responses;
    }

    private static String decode(String content, String encoding) {
        return switch (encoding) {
            case "base64" -> decodeBase64Multiline(content);
            default -> throw new IllegalArgumentException("Unknown encoding: " + encoding);
        };
    }

    private static String decodeBase64Multiline(String content) {
        return content.lines()
                .map(GithubApiClientImpl::decodeBase64)
                .collect(Collectors.joining());
    }

    private static String decodeBase64(String encoded) {
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }

    @Override
    public void destroy() throws Exception {
//        httpClient.close(); //since Java 23
    }
}
