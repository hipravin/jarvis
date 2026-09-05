package hipravin.jarvis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EmbeddedKafka
@ActiveProfiles(profiles = {"test"})
class JarvisApplicationTests {
    @Test
    void contextLoads() {
    }
}
