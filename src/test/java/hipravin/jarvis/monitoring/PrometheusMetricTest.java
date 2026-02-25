package hipravin.jarvis.monitoring;

import hipravin.jarvis.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka
@ActiveProfiles(profiles = {"test"})
public class PrometheusMetricTest {
    @LocalManagementPort
    int managementPort;

    HttpClient httpClient;

    @BeforeAll
    void beforeAll() {
        httpClient = HttpClient.newBuilder()
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("admin", "aadmin".toCharArray());
                    }
                })
                .build();
    }

    @Test
    void testBuildInfo() throws IOException, InterruptedException {
        var promText = TestUtils.httpGetAsStringEnsureOkNotNull(httpClient, managementPort, "actuator/prometheus");

        var buildInfoLine = promText.lines()
                .filter(l -> l.startsWith("app_build"))
                .findAny();

        assertTrue(buildInfoLine.isPresent(), "Build info not found in actuator prometheus block");

        String[] expectedLabels = {
                "app_name",
                "app_version",
                "build_time",
                "ci_build_number",
                "commit_id",
                "short_commit_id"};

        Arrays.stream(expectedLabels)
                .forEach(prop -> {
                    assertTrue(buildInfoLine.get().contains(prop),
                            "Expected contain property: %s, actual: %s".formatted(prop, buildInfoLine));
                });
    }
}
