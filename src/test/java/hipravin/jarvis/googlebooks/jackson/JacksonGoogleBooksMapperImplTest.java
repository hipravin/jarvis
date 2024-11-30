package hipravin.jarvis.googlebooks.jackson;

import hipravin.jarvis.TestUtls;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolume;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;
import hipravin.jarvis.googlebooks.jackson.model.SearchInfo;
import hipravin.jarvis.googlebooks.jackson.model.VolumeInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class JacksonGoogleBooksMapperImplTest {
    String sampleContent = TestUtls.loadFromClasspath("data/googlebooks/search-completable-future-allof.json");

    @Test
    void testReadSample() {
        JacksonGoogleBooksMapper mapper = new JacksonGoogleBooksMapperImpl();
        BooksVolumes bvs = mapper.readBooksVolumes(sampleContent);

        assertEquals(68, bvs.totalItems());
        assertNotNull(bvs.items());
        assertEquals(40, bvs.items().size());

        BooksVolume bv0 = bvs.items().get(0);

        VolumeInfo vi = bv0.volumeInfo(); assertNotNull(vi);
        assertEquals("Java Concurrency Patterns: Mastering Multithreading and Asynchronous Techniques", vi.title());
        assertTrue(vi.description().startsWith("Unlock the power of Java Concurrency with"));
        assertEquals("http://books.google.ru/books?id=-SorEQAAQBAJ&pg=PA29-IA45&dq=CpmletableFuture+allOf&hl=&cd=1&source=gbs_api", vi.previewLink());
        assertEquals("http://books.google.ru/books?id=-SorEQAAQBAJ&dq=CpmletableFuture+allOf&hl=&source=gbs_api", vi.infoLink());
//        assertEquals(LocalDate.of(2024, 10, 21), vi.publishedDate());

        SearchInfo si = bv0.searchInfo(); assertNotNull(si);
        assertTrue(si.textSnippet().contains("This method accepts an array of CompletableFuture objects and returns"));
    }
}