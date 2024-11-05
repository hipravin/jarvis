package hipravin.jarvis.github;

import com.google.common.collect.Lists;
import hipravin.jarvis.github.jackson.JacksonGithubMapper;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class GithubApiClientImpl implements GithubApiClient, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(GithubApiClientImpl.class);
    //apparently github doesn't allow concurrent requests
    //https://github.com/api-platform/core/issues/3205  (closed, but probably not fixed)
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
    public CodeSearchResult search(String searchString) {
        var request = githubHttpRequestBuilder.uri(URI.create(githubProperties.codeSearchUrl() + "?q="
                        + URLEncoder.encode(searchString, StandardCharsets.UTF_8)))
                .GET()
                .build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            var metadata = GithubResponseMetadata.fromHttpResponse(response);

            if (response.statusCode() != 200) {
                throw new RuntimeException("Request failed: %s".formatted(response.statusCode()));
            }

            return mapper.readCodeSearchResult(response.body());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CodeSearchResult searchApprovedAuthors(String searchString) {
        List<List<String>> approvedAuthorsBatches = Lists.partition(githubProperties.approvedAuthors(),
                githubProperties.singleRequestMaxOr());

        var requests = approvedAuthorsBatches.stream()
                .map(batch -> searchCodeApprovedAuthorsBatchSupplier(batch, searchString))
                .toList();

        return CodeSearchResult.combine(requestConcurrently(requests));
    }

    private Supplier<CodeSearchResult> searchCodeApprovedAuthorsBatchSupplier(List<String> approvedAuthors, String searchString) {
        var orAuthors = approvedAuthors.stream()
                .map(author -> "user:" + author)
                .collect(Collectors.joining(" OR "));

        return () -> search(orAuthors + " " + searchString);
    }

    private List<CodeSearchResult> requestConcurrently(List<Supplier<CodeSearchResult>> requests) {
        List<CompletableFuture<CodeSearchResult>> completableFutures = requests.stream()
                .map(r -> CompletableFuture.supplyAsync(r, executor))
                .toList();

        List<CodeSearchResult> responses = new ArrayList<>(requests.size());

        for (CompletableFuture<CodeSearchResult> completableFuture : completableFutures) {
            try {
                var response = completableFuture.get(10, TimeUnit.SECONDS);
                responses.add(response);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);//TODO: can we safely ignore other Futures after getting first exception?
            }
        }

        return responses;
    }

    @Override
    public void destroy() throws Exception {
//        httpClient.close(); //since Java 23
    }
}
