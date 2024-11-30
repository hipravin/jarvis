package hipravin.jarvis.googlebooks;

import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(profiles = {"itlocal"})
class GoogleBooksApiClientImplIT {
    @Autowired
    GoogleBooksApiClient client;

    @Test
    void testSampleSearch() {
        BooksVolumes bvs = client.search("ofVirtual runnable");

        bvs.items().stream()
                .peek(bv -> System.out.println("\n" + bv.volumeInfo().title()))
                .map(bv -> bv.searchInfo().textSnippet())
                .forEach(System.out::println);
    }
}