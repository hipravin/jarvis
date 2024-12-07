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
        assertEquals("https://stub-github/search/code", githubProperties.codeSearchUrl());
        assertEquals("https://github.com/search?q=%s&type=code", githubProperties.codeSearchBrowserUrlTemplate());
    }
}