package hipravin.jarvis.bookstore.dao;

import hipravin.jarvis.JarvisIntegrationTest;
import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.dao.entity.BookPageFtsEntity;
import hipravin.jarvis.bookstore.load.BookReader;
import hipravin.jarvis.bookstore.load.model.Book;
import hipravin.jarvis.exception.NotFoundException;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    BookRepository bookRepository;

    BookEntity saltBook;
    BookEntity garlicBook;
    BookEntity starchBook;

    @BeforeEach
    void setUp() {
        saltBook = readAndSaveBook(sampleSaltPdf);
        garlicBook = readAndSaveBook(sampleGarlicPdf);
        starchBook = readAndSaveBook(sampleStarchPdf);
    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
    }

    @Test
    void testSaltBookSavedCorrectly() {
        assertEquals("Estimating salt intake in humans: not so easy!1", saltBook.getMetadata().get("Title"));

        BookEntity byIdEntity = bookstoreDao.findByIdFetchPdf(saltBook.getId());
        assertArrayEquals(saltBook.getPdfContent(), byIdEntity.getPdfContent(),
                "pdf contents are not equal for book " + byIdEntity.getTitle());
        assertNow(saltBook.getLastUpdated(), Duration.ofSeconds(5));
    }

    @Test
    void testDuplicateByPdfContent() {
        Book carbBook = bookLoader.read(sampleStarchPdf);
        assertThrows(DataAccessException.class,
                () -> bookstoreDao.save(bookLoader.read(carbBook.pdfContent(), "Other title"))); //duplicated binary content
    }

    @Test
    void testPdfLazyLoad() {
        //This check may fail if executed by IDEA - probably hibernate-enhance-maven-plugin is not applied
        assertThrows(LazyInitializationException.class,
                () -> bookstoreDao.findById(starchBook.getId()).getPdfContent());
    }

    @Test
    void testPdfContentStreaming() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bookstoreDao.writePdfContentTo(saltBook.getId(), bos);
        assertEquals(saltBook.getPdfContent().length, bos.size());
    }

    @Test
    void testPdfContentStreamingNotFound() {
        assertThrows(NotFoundException.class,
                () -> bookstoreDao.writePdfContentTo(-1L, new ByteArrayOutputStream()));
    }

    @Test
    void testSearch() {
        SearchSummary summary = testSearch("potato");
        assertEquals(1, summary.pageCount());
        assertEquals(Set.of(starchBook.getId()), summary.documentIds());
        assertTrue(summary.bestMatchHightlighted().contains("<b>potato</b>"));
    }

    @Test
    void testDeleteNotExistent() {
        assertThrows(NotFoundException.class, () -> {
            bookstoreDao.deleteById(-1);
        });
    }

    @Test
    void testDelete() {
        bookstoreDao.deleteById(starchBook.getId());
        assertThrows(NotFoundException.class, () -> bookstoreDao.findById(starchBook.getId()));
    }

    BookEntity readAndSaveBook(Path pdfFilePath) {
        Book book = bookLoader.read(pdfFilePath);
        BookEntity bookEntity = bookstoreDao.save(book);

        assertNotNull(bookEntity);
        assertNotNull(bookEntity.getId());

        return bookEntity;
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