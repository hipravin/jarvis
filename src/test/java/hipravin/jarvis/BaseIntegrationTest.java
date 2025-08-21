package hipravin.jarvis;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {"spring.flyway.enabled=true"})
@Testcontainers
@ActiveProfiles(profiles = {"integration"})
public abstract class BaseIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withUrlParam("currentSchema", "jarvis")
            .withUrlParam("reWriteBatchedInserts", "true")
            .withUrlParam("logServerErrorDetail", "false");
}
