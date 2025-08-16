package hipravin.jarvis.github;

import com.google.common.collect.Lists;
import hipravin.jarvis.exception.BadHeaderValueException;
import hipravin.jarvis.exception.RateLimitExceedException;
import hipravin.jarvis.exception.RemoteApiException;
import hipravin.jarvis.github.jackson.JacksonGithubMapper;
import hipravin.jarvis.github.jackson.model.CodeSearchItem;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.github.jackson.model.EncodedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static hipravin.jarvis.github.GithubUtils.safeGetLogin;

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

            ensureStatusOk(request, response);

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
            ensureStatusOk(request, response);

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
                .collect(Collectors.toCollection(ArrayList::new));
        requests.add(() -> search(searchString));

        return CodeSearchResult.combine(requestConcurrently(requests));
    }

    @Override
    public String githubBrowserSearchUrl(String user, String query) {
        String specificUserSearchQuery = (StringUtils.hasText(user)) ? "user:%s %s".formatted(user, query) : query;

        return githubProperties.codeSearchBrowserUrlTemplate()
                .formatted(URLEncoder.encode(specificUserSearchQuery, StandardCharsets.UTF_8));
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

        return new CodeSearchResult(codeSearchResult.count(), codeSearchResult.incompleteResults(), List.copyOf(items));
    }

    private Supplier<CodeSearchResult> searchCodeApprovedAuthorsBatchSupplier(List<String> approvedAuthors, String searchString) {
        var orAuthors = approvedAuthors.stream()
                .map(author -> "user:" + author)
                .collect(Collectors.joining(" ", "", " "));

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
                completableFutures.forEach(cf -> {
                    if (!cf.isDone()) {
                        cf.cancel(true);
                    }
                });

                if ((e instanceof ExecutionException exec)
                        && (exec.getCause() instanceof RateLimitExceedException rle)) {
                    throw rle;
                } else {
                    throw new RuntimeException(e);
                }
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

    static <T> void logRateLimits(HttpHeaders headers) {
        try {
            long limit = getMandatoryHeaderValueAsLong(headers, "X-RateLimit-Limit");
            long used = getMandatoryHeaderValueAsLong(headers, "X-RateLimit-Used");
            long remaining = getMandatoryHeaderValueAsLong(headers, "X-RateLimit-Remaining");
            long resetEpochSeconds = getMandatoryHeaderValueAsLong(headers, "X-RateLimit-Reset");
            String resource = headers.firstValue("X-RateLimit-Resource")
                    .orElseThrow(() -> new BadHeaderValueException("X-RateLimit-Resource", "Empty value for header X-RateLimit-Resource"));

            long resetInSeconds = resetEpochSeconds - System.currentTimeMillis() / 1000;
            log.debug("Rate limits: {}/{}, remaining: {}, reset {} sec, '{}'",
                    used, limit, remaining, resetInSeconds, resource);
        } catch (BadHeaderValueException e) {
            log.error("Failed to process rate limit headers: {}", e.getMessage());
        }
    }

    static long getMandatoryHeaderValueAsLong(HttpHeaders headers, String header) throws BadHeaderValueException {
        try {
            return headers.firstValueAsLong(header)
                    .orElseThrow(() -> new BadHeaderValueException(header, "Empty value for header " + header));
        } catch (NumberFormatException e) {
            throw new BadHeaderValueException(header, "Not a number %s: %s: "
                    .formatted(header, headers.firstValue(header).orElse("")));
        }
    }

    static <T> void ensureStatusOk(HttpRequest request, HttpResponse<T> response) {
        logRateLimits(response.headers());

        if (response.statusCode() != HttpStatus.OK.value()) {

            if (response.statusCode() == 403
                    && (response.body() instanceof String body)
                    && body.contains("API rate limit exceeded")) {
                log.warn("Request failed: {}: status {}, body: {}", request.uri(), response.statusCode(), response.body());
                throw new RateLimitExceedException("Github API rate limit exceeded");
            }

            log.error("Request failed: {}: status {}, body: {}", request.uri(), response.statusCode(), response.body());
            throw new RemoteApiException("Github API call failed");
        }
    }

    @Override
    public void destroy() throws Exception {
//        httpClient.close(); //since Java 23
    }
}
