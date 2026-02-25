package hipravin.jarvis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.GitProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(profiles = {"test"})
class JarvisApplicationTests {
    @Autowired
    GitProperties gitProperties;

    @Test
    void contextLoads() {
        System.out.println(gitProperties);
    }
}
