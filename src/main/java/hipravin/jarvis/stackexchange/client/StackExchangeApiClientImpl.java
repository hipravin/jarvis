package hipravin.jarvis.stackexchange.client;

import hipravin.jarvis.stackexchange.client.dto.ResponseItems;
import hipravin.jarvis.stackexchange.client.dto.SearchExcerpt;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class StackExchangeApiClientImpl implements StackExchangeApiClient {
    private static final Logger log = LoggerFactory.getLogger(StackExchangeApiClientImpl.class);

    private final StackExchangeProperties props;
    private final HttpClient httpClient;
    private final HttpRequest.Builder requestBuilder;
    private final JsonReader mapper;

    public StackExchangeApiClientImpl(StackExchangeProperties stackExchangeProperties,
                                      @Qualifier("stackExchangeHttpClient") HttpClient httpClient,
                                      @Qualifier("stackExchangeHttpRequestBuilder") HttpRequest.Builder requestBuilder,
                                      JsonReader mapper) {
        this.props = stackExchangeProperties;
        this.httpClient = httpClient;
        this.requestBuilder = requestBuilder;
        this.mapper = mapper;
    }

    @Retry(name = "stackExchangeClient")
    @Override
    public ResponseItems<SearchExcerpt> searchExcerpts(String query) {
        var uri = UriComponentsBuilder.fromUriString(props.apiBaseUrl() + props.excerptUrl())
                .queryParams(MultiValueMap.fromSingleValue(props.searchExcerptsParams()))
                .queryParam("q", query)
                .build().toUri();

        var request = requestBuilder.uri(uri).GET().build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ensureStatusOk(request, response);

            var result = mapper.readSearchExcerpts(response.body());
            logQuotaRemaining(result);

            return result;
        } catch (IOException | InterruptedException e) {
            var msg = (e.getMessage() != null) ? e.getMessage() : "";
            throw new RuntimeException(
                    "SE error: '%s' for '%s'".formatted(msg, query));
        }
    }

    static <T> void ensureStatusOk(HttpRequest request, HttpResponse<T> response) {
        if (response.statusCode() != HttpStatus.OK.value()) {
            log.warn("Request failed: {}: status {}, body: {}", request.uri(), response.statusCode(), response.body());
            throw new RuntimeException("Request Failed");
        }
    }

    static <T> void logQuotaRemaining(ResponseItems<T> items) {
        if(log.isDebugEnabled()) {
            log.debug("Quota max/remaining: {}/{}", items.quotaMax(), items.quotaRemaining());
        }
    }
}
