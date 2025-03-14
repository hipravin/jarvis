package hipravin.jarvis.bookstore;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainersExperimentIT {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }


    @Test
    void testSample() {
        System.out.println(postgres.getJdbcUrl());
        System.out.println(postgres.getUsername());
        System.out.println(postgres.getPassword());
    }
}
