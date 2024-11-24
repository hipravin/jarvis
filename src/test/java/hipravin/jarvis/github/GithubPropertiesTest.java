package hipravin.jarvis.github;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles(profiles = {"test"})
class GithubPropertiesTest {
    @Autowired
    GithubProperties githubProperties;

    @Test
    void testLoadProperties() {
        assertNotNull(githubProperties);
        assertTrue(githubProperties.approvedAuthors().contains("hipravin"));
        assertTrue(githubProperties.approvedAuthors() instanceof LinkedHashSet<String>);
        assertEquals("https://api.github.com/search/code", githubProperties.codeSearchUrl());
    }
}