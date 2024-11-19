package hipravin.jarvis.engine.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkTest {

    @Test
    void testGithubUrlParse() {
        String htmlUrl = "https://github.com/hipravin/devcompanion/blob/2a9f8aafb729a0975e80eb34c9e94a6c7a00c421/development/playground/playground-sql/postgresql-book/queries.sql";

        Link link = Link.fromGithubHtmlUrl(htmlUrl);
        assertEquals("hipravin-devcompanion/.../queries.sql", link.title());
        assertEquals(htmlUrl, link.href());
    }
}