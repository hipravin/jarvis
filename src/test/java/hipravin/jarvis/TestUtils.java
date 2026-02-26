package hipravin.jarvis;

import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class TestUtils {
    private TestUtils() {
    }

    public static String loadFromClasspath(String fileName) {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(fileName)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                throw new RuntimeException("Not found in classpath: " + fileName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static HttpResponse<String> httpGet(int port, String url) throws IOException, InterruptedException {
        try (var httpClient = HttpClient.newBuilder().build()) {
            return httpGet(httpClient, port, url);
        }
    }

    public static HttpResponse<String> httpGet(HttpClient httpClient, int port, String url) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:%d/%s".formatted(
                        port, removeLeadingSlash(url))))
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static String httpGetAsStringEnsureOkNotNull(HttpClient httpClient, int port, String url) throws IOException, InterruptedException {
        var response = httpGet(httpClient, port, url);

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNotNull(response.body());

        return response.body();
    }

    private static String removeLeadingSlash(String url) {
        return url.startsWith("/") ? url.substring(1) : url;
    }
}
