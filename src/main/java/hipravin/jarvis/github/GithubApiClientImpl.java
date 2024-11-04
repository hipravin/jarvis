package hipravin.jarvis.github;

import hipravin.jarvis.github.jackson.GithubResponseMetadata;
import hipravin.jarvis.github.jackson.JacksonGithubMapper;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class GithubApiClientImpl implements GithubApiClient, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(GithubApiClientImpl.class);

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
        var orAuthors = githubProperties.approvedAuthors().stream()
                .limit(5)
                .map(author -> "user:" + author)
                .collect(Collectors.joining(" OR "));
        return search(orAuthors + " " + searchString);
    }



    @Override
    public void destroy() throws Exception {
//        httpClient.close(); //since Java 23
    }
}
