package hipravin.jarvis.bookstore.dao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = { "spring.flyway.enabled=true" })
@Testcontainers
@ActiveProfiles({"test"})
//@SqlGroup({
//        @Sql("/db/migration/V1_0__init_jarvis_schema.sql"),
//        @Sql("/db/migration/V1_1__bookstore.sql")})
class BookstoreDaoImplIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @Test
    void testNoOp() {
        System.out.println("done");
    }
}