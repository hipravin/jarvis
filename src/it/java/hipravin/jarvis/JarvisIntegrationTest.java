package hipravin.jarvis;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootTest
@EmbeddedKafka
@Import(IntegrationTestConfiguration.class)
@ActiveProfiles(profiles = {"integration"})
public @interface JarvisIntegrationTest {
}
