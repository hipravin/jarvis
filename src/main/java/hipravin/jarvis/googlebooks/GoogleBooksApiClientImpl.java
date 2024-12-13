package hipravin.jarvis.googlebooks;

import hipravin.jarvis.googlebooks.jackson.JacksonGoogleBooksMapper;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <a href="https://developers.google.com/books/docs/v1/using">Google Books API</a>
 */
@Component
public class GoogleBooksApiClientImpl implements GoogleBooksApiClient {

    private final GoogleBooksProperties googleBooksProperties;
    private final JacksonGoogleBooksMapper mapper;
    private final HttpClient httpClient;
    private final HttpRequest.Builder googlebooksHttpRequestBuilder;

    private static final Pattern UNICODE_SPACES = Pattern.compile("(?U)\\s+");

    public GoogleBooksApiClientImpl(GoogleBooksProperties googleBooksProperties,
                                    JacksonGoogleBooksMapper mapper,
                                    HttpClient.Builder googlebooksHttpClientBuilder,
                                    HttpRequest.Builder googlebooksHttpRequestBuilder) {
        this.googleBooksProperties = googleBooksProperties;
        this.mapper = mapper;
        this.httpClient = googlebooksHttpClientBuilder.build();
        this.googlebooksHttpRequestBuilder = googlebooksHttpRequestBuilder;
    }

    @Override
    public BooksVolumes search(String searchString) {
        //https://www.googleapis.com/books/v1/volumes?q=CpmletableFuture+allOf&langRestrict=en&maxResults=40
        var request = googlebooksHttpRequestBuilder.uri(URI.create(
                        "%s?q=%s&key=%s&maxResults=40".formatted(//&langRestrict=en //TODO: work with language
                                googleBooksProperties.booksSearchUrl(),
                                URLEncoder.encode(enquoteEveryTerm(searchString), StandardCharsets.UTF_8),
                                googleBooksProperties.apiKey())))
                .GET()
                .build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Request failed: %s: status %d, body: %s".formatted(
                        request.uri(), response.statusCode(), response.body()));
            }

            return mapper.readBooksVolumes(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static String enquoteEveryTerm(String searchString) {
        return UNICODE_SPACES.splitAsStream(searchString)
                .filter(term -> !term.isEmpty())
                .map(term -> "\"" + term + "\"")
                .collect(Collectors.joining(" "));
    }
}
