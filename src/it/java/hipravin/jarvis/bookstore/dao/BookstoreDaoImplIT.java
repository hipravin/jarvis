package hipravin.jarvis.bookstore.dao;

import hipravin.jarvis.JarvisIntegrationTest;
import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.dao.entity.BookPageFtsEntity;
import hipravin.jarvis.bookstore.load.BookReader;
import hipravin.jarvis.bookstore.load.model.Book;
import hipravin.jarvis.exception.NotFoundException;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@JarvisIntegrationTest
class BookstoreDaoImplIT {
    static Path sampleSaltPdf = Path.of("src/test/resources/data/bookstore/estimating salt intake not so easy.pdf");
    static Path sampleGarlicPdf = Path.of("src/test/resources/data/bookstore/garlic-onion-15.JChromat.A2006.pdf");
    static Path sampleStarchPdf = Path.of("src/test/resources/data/bookstore/Hardy_QRB15_starch.pdf");

    @Autowired
    BookReader bookLoader;

    @Autowired
    BookstoreDao bookstoreDao;

    @Test
    void testSaveThenSearch() {
        Book saltBook = bookLoader.read(sampleSaltPdf);
        BookEntity bookEntity = bookstoreDao.save(saltBook);

        assertNotNull(bookEntity);
        assertNotNull(bookEntity.getId());

        assertEquals("Estimating salt intake in humans: not so easy!1", bookEntity.getMetadata().get("Title"));

        BookEntity byIdEntity = bookstoreDao.findByIdFetchPdf(bookEntity.getId());
        assertArrayEquals(bookEntity.getPdfContent(), byIdEntity.getPdfContent(),
                "pdf contents are not equal for book " + byIdEntity.getTitle());
        assertNow(bookEntity.getLastUpdated(), Duration.ofSeconds(5));

        Book carbBook = bookLoader.read(sampleStarchPdf);
        bookstoreDao.save(bookLoader.read(sampleGarlicPdf));
        var carb = bookstoreDao.save(carbBook);

        SearchSummary search1 = testSearch("potato");
        assertEquals(1, search1.pageCount());
        assertEquals(Set.of(carb.getId()), search1.documentIds());
        assertTrue(search1.bestMatchHightlighted().contains("<b>potato</b>"));

        assertThrows(DataAccessException.class, () -> {
            bookstoreDao.save(bookLoader.read(carbBook.pdfContent(), "Other title")); //duplicated binary content
        });

        //This check may fail if executed by IDEA - probably hibernate-enhance-maven-plugin is not applied (IDEA 2022)
        assertThrows(LazyInitializationException.class, () -> {
            bookstoreDao.findById(carb.getId()).getPdfContent();
        });

        //get all
        List<BookEntity> bookEntities = bookstoreDao.findAll();
        assertEquals(3, bookEntities.size());
        assertThrows(LazyInitializationException.class, () -> {
            bookEntities.getFirst().getPdfContent();
        });
        //rawpdf
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bookstoreDao.writePdfContentTo(bookEntities.getFirst().getId(), bos);
        assertEquals(saltBook.pdfContent().length, bos.size());

        assertThrows(NotFoundException.class, () -> bookstoreDao.writePdfContentTo(-1L, new ByteArrayOutputStream()));
    }

    record SearchSummary(int pageCount, Set<Long> documentIds, String bestMatchHightlighted) {
    }

    SearchSummary testSearch(String... queryTerms) {
        List<BookPageFtsEntity> pages = bookstoreDao.search(String.join(" ", queryTerms));
        for (BookPageFtsEntity page : pages) {
            for (String term : queryTerms) {
                assertTrue(page.getContent().contains(term), page.getContent() + ", terms:" + Arrays.toString(queryTerms));
            }
            assertNotNull(page.getBook());
        }
        return new SearchSummary(pages.size(),
                pages.stream().map(p -> p.getBookPageId().bookId()).collect(Collectors.toSet()),
                pages.stream().map(BookPageFtsEntity::getContentHighlighted).findFirst().orElse("no pages matched"));
    }

    static void assertNow(Instant actual, TemporalAmount delta) {
        assertNotNull(actual);

        Instant now = Instant.now();
        Instant nowMinus = now.minus(delta);
        Instant nowPlus = now.plus(delta);

        String message = "time %s is not in range %s-%s".formatted(now, nowMinus, nowPlus);

        assertTrue(actual.isAfter(nowMinus), message);
        assertTrue(actual.isBefore(nowPlus), message);
    }
}