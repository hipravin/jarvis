package hipravin.jarvis.github.jackson;

import hipravin.jarvis.TestUtls;
import hipravin.jarvis.github.jackson.model.CodeSearchItem;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.github.jackson.model.TextMatch;
import hipravin.jarvis.github.jackson.model.TextMatches;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JacksonGithubMapperImplTest {

    String sampleImmutableContent = TestUtls.loadFromClasspath("data/code-search-sample-immutable.json");

    @Test
    void testMapping() {
        JacksonGithubMapper mapper = new JacksonGithubMapperImpl();

        CodeSearchResult csr = mapper.readCodeSearchResult(sampleImmutableContent);

        assertEquals(14, csr.count());
        assertFalse(csr.incompleteResults());

        assertEquals(csr.count(), csr.codeSearchItems().size());

        CodeSearchItem csi0 = csr.codeSearchItems().get(0);

        assertEquals("queries.sql", csi0.name());
        assertEquals("development/playground/playground-sql/postgresql-book/queries.sql", csi0.path());
        assertEquals("faaa9e0f3bb7e8d66b955a049d1972cb82144e41", csi0.sha());
        assertEquals("https://api.github.com/repositories/453730965/contents/development/playground/playground-sql/postgresql-book/queries.sql?ref=2a9f8aafb729a0975e80eb34c9e94a6c7a00c421",
                csi0.url());
        assertEquals("https://api.github.com/repositories/453730965/git/blobs/faaa9e0f3bb7e8d66b955a049d1972cb82144e41",
                csi0.gitUrl());
        assertEquals("https://github.com/hipravin/devcompanion/blob/2a9f8aafb729a0975e80eb34c9e94a6c7a00c421/development/playground/playground-sql/postgresql-book/queries.sql",
                csi0.htmlUrl());
        assertEquals(1.0, csi0.score(), 0.0001);

        List<TextMatches> tms = csi0.textMatches();
        assertEquals(2, tms.size());

        TextMatches tm0 = tms.get(0);
        assertEquals("END; $$\n    LANGUAGE 'plpgsql' IMMUTABLE;\n", tm0.fragment());
        assertEquals("https://api.github.com/repositories/453730965/contents/development/playground/playground-sql/postgresql-book/queries.sql?ref=2a9f8aafb729a0975e80eb34c9e94a6c7a00c421",
                tm0.objectUrl());
        assertEquals("FileContent", tm0.objectType());

        List<TextMatch> tmms = tm0.matches();
        assertEquals(1, tmms.size());
        TextMatch tmm0 = tmms.get(0);
        assertEquals("IMMUTABLE", tmm0.text());
        assertEquals(31, tmm0.start());
        assertEquals(40, tmm0.end());
    }


}