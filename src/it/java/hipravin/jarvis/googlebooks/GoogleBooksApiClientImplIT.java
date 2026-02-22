package hipravin.jarvis.googlebooks;

import hipravin.jarvis.JarvisIntegrationTest;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JarvisIntegrationTest
class GoogleBooksApiClientImplIT {
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