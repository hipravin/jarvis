package hipravin.jarvis.stackexchange.client;

import hipravin.jarvis.stackexchange.client.dto.ResponseItems;
import hipravin.jarvis.stackexchange.client.dto.SearchExcerpt;
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
    private final StackExchangeMapper mapper;

    public StackExchangeApiClientImpl(StackExchangeProperties stackExchangeProperties,
                                      @Qualifier("stackExchangeHttpClient") HttpClient httpClient,
                                      @Qualifier("stackExchangeHttpRequestBuilder") HttpRequest.Builder requestBuilder,
                                      StackExchangeMapper mapper) {
        this.props = stackExchangeProperties;
        this.httpClient = httpClient;
        this.requestBuilder = requestBuilder;
        this.mapper = mapper;
    }

    @Override
    public ResponseItems<SearchExcerpt> searchExcerpts(String query) {
        var uri = UriComponentsBuilder.fromUriString(
                        props.apiBaseUrl() + props.excerptUrl())
                .queryParams(MultiValueMap.fromSingleValue(props.searchExcerptsParams()))
                .queryParam("q", query)
                .build().toUri();

        var request = requestBuilder.uri(uri)
                .GET()
                .build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ensureStatusOk(request, response);

            return mapper.readSearchExcerpts(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> void ensureStatusOk(HttpRequest request, HttpResponse<T> response) {
        if(response.statusCode() != HttpStatus.OK.value()) {
            log.warn("Request failed: {}: status {}, body: {}", request.uri(), response.statusCode(), response.body());
            throw new RuntimeException("Request Failed");
        }
    }
}
