package hipravin.jarvis.googlebooks;

import hipravin.jarvis.BaseIntegrationTest;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles(profiles = {"integration"})
class GoogleBooksApiClientImplIT extends BaseIntegrationTest {
    @Autowired
    GoogleBooksApiClient client;

    @Test
    void testSampleSearch() {
        BooksVolumes bvs = client.search("atomic");

        assertNotNull(bvs);
        assertEquals(20, bvs.items().size());

        bvs.items().stream()
                .peek(bv -> System.out.println("\n" + bv.volumeInfo().title()))
                .map(bv -> bv.searchInfo().textSnippet())
                .forEach(System.out::println);
    }
}