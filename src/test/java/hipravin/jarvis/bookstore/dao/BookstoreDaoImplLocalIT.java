package hipravin.jarvis.bookstore.dao;

import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.dao.entity.BookFtsPageEntity;
import hipravin.jarvis.bookstore.dao.entity.BookPageEntity;
import hipravin.jarvis.bookstore.dao.entity.BookPageId;
import hipravin.jarvis.bookstore.load.BookLoader;
import hipravin.jarvis.bookstore.load.PdfBookLoader;
import hipravin.jarvis.bookstore.load.model.Book;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@Disabled
@ActiveProfiles({"itlocal"})
class BookstoreDaoImplLocalIT {
    static Path sampleGarlicPdf = Path.of("src/test/resources/data/bookstore/garlic-onion-15.JChromat.A2006.pdf");
    static Path sampleSaltPdf = Path.of("src/test/resources/data/bookstore/estimating salt intake not so easy.pdf");

    @Autowired
    private BookLoader bookLoader;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookPageRepository bookPageRepository;

    @Autowired
    private BookstoreDao bookstoreDao;

    @Test
    void testSave() {
        BookEntity be = new BookEntity();
        be.setTitle("Sample book 1");
        bookRepository.save(be);
    }

    @Test
    void testSaveBatch() {
        List<BookEntity> books = IntStream.range(0, 200)
                .mapToObj(i -> newBookEntity("Sample Batch Book Title %d".formatted(i)))
                .toList();

        bookRepository.saveAll(books);
        List<BookPageEntity> pages = books.stream()
                .flatMap(book -> newPages(book).stream())
                .toList();

        bookPageRepository.saveAll(pages);
    }

    @Test
    void testSearch() {
        List<BookFtsPageEntity> pages = bookstoreDao.search("transaction serializable");

        pages.forEach(p -> {
            assertTrue(p.getContent().toLowerCase().contains("transact"));
            assertTrue(p.getContent().toLowerCase().contains("serial"));
        });

    }

    @Test
    void testSaveSampleParsed() {
        PdfBookLoader loader = new PdfBookLoader();

        Book garlicOnion = loader.load(sampleGarlicPdf);
        bookstoreDao.save(garlicOnion);
    }

    @Test
    void testSaveExt() {
        var paths = List.of(Path.of("C:/Users/Alex/YandexDisk/books/developer/designing-data-intensive-applications.pdf"),
                Path.of("C:/Users/Alex/YandexDisk/books/developer/Docker.Deep.Dive.2024.pdf"),
                Path.of("C:/Users/Alex/YandexDisk/books/developer/Learning PostgreSQL.pdf"),
                Path.of("C:/Users/Alex/YandexDisk/books/developer/high-performance-java-persistence-vlad-mihalcea.pdf")
        );

        paths.forEach(p -> {
            Book book = bookLoader.load(p);
            System.out.printf("Parsed: %s %s, pages: %d%n", book.title(), book.metadata().title(), book.pages().size());

            bookstoreDao.save(book);
        });
    }

    static BookEntity newBookEntity(String title) {
        BookEntity be = new BookEntity();
        be.setTitle(title);

        return be;
    }

    static BookPageEntity newBookPageEntity(long bookId, long pageNum, String pageContent) {
        BookPageEntity bpe = new BookPageEntity();
        bpe.setBookPageId(new BookPageId(bookId, pageNum));

        return bpe;
    }

    List<BookPageEntity> newPages(BookEntity book) {
        return LongStream.range(0, 20)
                .mapToObj(i -> newBookPageEntity(book.getId(), i, "book %d page %d".formatted(book.getId(), i)))
                .toList();
    }
}