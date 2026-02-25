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

    public static String httpGetAsStringEnsureOkNotNull(HttpClient httpClient, int port, String url) throws IOException, InterruptedException {
        var promRequest = HttpRequest.newBuilder(URI.create("http://localhost:%d/%s".formatted(
                        port, url)))
                .GET()
                .build();

        var response = httpClient.send(promRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNotNull(response.body());

        return response.body();
    }
}
