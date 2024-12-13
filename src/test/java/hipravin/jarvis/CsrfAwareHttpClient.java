package hipravin.jarvis;

import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class CsrfAwareHttpClient {
    public static final String CSRF_COOKIE = "XSRF-TOKEN";
    public static final String CSRF_HEADER = "X-XSRF-TOKEN";

    private final String baseUrl;
    private HttpClient httpClient;

    public CsrfAwareHttpClient(String baseUrl) {
        this.baseUrl = baseUrl;

        var cookieHandler = new CookieManager();
        httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieHandler)
                .build();
    }

    public HttpResponse<String> get(String subUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + subUrl))
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> post(String subUrl, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + subUrl))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header(CSRF_HEADER, extractCsrfFromCookie().orElse(""))
                .header(HttpHeaders.ACCEPT, "application/json")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private Optional<String> extractCsrfFromCookie() {
        CookieStore cookieStore = httpClient.cookieHandler()
                .map(ch -> ((CookieManager) ch).getCookieStore()).orElse(null);
        if (cookieStore == null) {
            return Optional.empty();
        } else {
            return cookieStore.get(URI.create(baseUrl)).stream()
                    .filter(c -> CSRF_COOKIE.equalsIgnoreCase(c.getName()))
                    .map(HttpCookie::getValue)
                    .findFirst();
        }
    }
}
