package hipravin.jarvis;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
@EmbeddedKafka
public class IntegrationTestConfiguration {

    @Bean(destroyMethod = "close")
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:18-alpine")
                .withUrlParam("currentSchema", "jarvis")
                .withUrlParam("reWriteBatchedInserts", "true")
                .withUrlParam("logServerErrorDetail", "false");
    }
}
