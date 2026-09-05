package hipravin.jarvis.github;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka
@ActiveProfiles(profiles = {"test"})
class GithubPropertiesTest {
    @Autowired
    GithubProperties githubProperties;

    @Test
    void testLoadProperties() {
        assertNotNull(githubProperties);
        assertTrue(githubProperties.approvedAuthors().contains("hipravin"));
        assertInstanceOf(LinkedHashSet.class, githubProperties.approvedAuthors());
        assertEquals("https://stub-github/search/code", githubProperties.codeSearchUrl());
        assertEquals("https://github.com/search?q=%s&type=code", githubProperties.codeSearchBrowserUrlTemplate());
    }
}