package hipravin.jarvis.bookstore.dao;

import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.dao.entity.BookPageEntity;
import hipravin.jarvis.bookstore.load.BookLoader;
import hipravin.jarvis.bookstore.load.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = { "spring.flyway.enabled=true" })
@Testcontainers
@ActiveProfiles({"integration"})
class BookstoreDaoImplIT {
    static Path sampleSaltPdf = Path.of("src/test/resources/data/bookstore/estimating salt intake not so easy.pdf");
    static Path sampleGarlicPdf = Path.of("src/test/resources/data/bookstore/garlic-onion-15.JChromat.A2006.pdf");
    static Path sampleStarchPdf = Path.of("src/test/resources/data/bookstore/Hardy_QRB15_starch.pdf");

    @Autowired
    BookLoader bookLoader;

    @Autowired
    BookstoreDao bookstoreDao;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @Test
    void testSaveThenSearch() {
        Book book = bookLoader.load(sampleSaltPdf);
        BookEntity bookEntity = bookstoreDao.save(book);

        assertNotNull(bookEntity);
        assertNotNull(bookEntity.getId());

        assertEquals("Estimating salt intake in humans: not so easy!1", bookEntity.getMetadata().get("Title"));

        BookEntity byIdEntity = bookstoreDao.findById(bookEntity.getId());
        assertArrayEquals(bookEntity.getPdfContent(), byIdEntity.getPdfContent());

        var garlic = bookstoreDao.save(bookLoader.load(sampleGarlicPdf));
        var carb = bookstoreDao.save(bookLoader.load(sampleStarchPdf));

        SearchSummary search1 = testSearch("potato");
        assertEquals(1, search1.pageCount());
        assertEquals(Set.of(carb.getId()), search1.documentIds());

        System.out.println("done");
    }

    record SearchSummary(int pageCount, Set<Long> documentIds) {}

    SearchSummary testSearch(String... queryTerms) {
        List<BookPageEntity> pages = bookstoreDao.search(String.join(" ", queryTerms));
        for (BookPageEntity page : pages) {
            for (String term : queryTerms) {
                assertTrue(page.getContent().contains(term), page.getContent() + ", terms:" + Arrays.toString(queryTerms));
            }
        }
        return new SearchSummary(pages.size(),
                pages.stream().map(p -> p.getBookPageId().getBookId()).collect(Collectors.toSet()));
    }
}