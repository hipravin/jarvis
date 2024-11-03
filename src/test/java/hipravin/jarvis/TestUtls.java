package hipravin.jarvis;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public abstract class TestUtls {
    private TestUtls() {
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
}
