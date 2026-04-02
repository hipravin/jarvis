package hipravin.jarvis.bookstore;

import hipravin.jarvis.JarvisIntegrationTest;
import hipravin.jarvis.bookstore.dao.BookstoreDao;
import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.load.*;
import jdk.jfr.Event;
import jdk.jfr.Name;
import jdk.jfr.consumer.RecordingStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.*;

@JarvisIntegrationTest
class BookstoreLoadServiceImplIT {
    Path sampleSaltPdf = Path.of("src/test/resources/data/bookstore/estimating salt intake not so easy.pdf");

    @Autowired
    BookstoreLoadService bookstoreLoadService;

    @Autowired
    BookstoreDao bookstoreDao;
    @Autowired
    BookReader bookReader;

    @AfterEach
    void tearDown() {
        bookstoreDao.findAll().forEach(b -> bookstoreDao.deleteById(b.getId()));
    }

    @Test
    void loadAll() {
        bookstoreLoadService.loadAll();

        List<BookEntity> bookEntities = bookstoreDao.findAll();
        assertEquals(3, bookEntities.size());

        assertFalse(bookstoreDao.search("salt").isEmpty());
        assertFalse(bookstoreDao.search("garlic").isEmpty());
        assertFalse(bookstoreDao.search("starch").isEmpty());
    }

    @Test
    void loadNewBooksWhenAdded() throws IOException {
        Path bookstoreTempDir = Files.createTempDirectory("bookstore");
        var tempDirProperties = new BookstoreProperties(bookstoreTempDir);
        BookstoreLoadService tempDirLoaderService = new BookstoreLoadServiceImpl(
                tempDirProperties, bookReader, bookstoreDao);
        BookstoreUpdateWatcher tempDirWatcher = new BookstoreUpdateWatcher(tempDirProperties, tempDirLoaderService);
        tempDirWatcher.startWatching();

        tempDirLoaderService.loadAll();
        assertTrue(bookstoreDao.findAll().isEmpty());

        Path newFileAdded = bookstoreTempDir.resolve(sampleSaltPdf.getFileName());
        Files.copy(sampleSaltPdf, newFileAdded);
        pause(3000);

        assertEquals(1, bookstoreDao.findAll().size());
        assertFalse(bookstoreDao.search("salt").isEmpty());

        Files.delete(newFileAdded);
    }

    @Name("TestEvent")
    class TestEvent extends Event {
    }

    @Test
    void testJfrBookLoadEvents() throws InterruptedException {
        Set<String> paths = new ConcurrentSkipListSet<>();
        try (RecordingStream rs = new RecordingStream()) {
            rs.onEvent("TestEvent", e -> rs.close());
            rs.onEvent("BookLoad", e -> paths.add(e.getString("path")));
            rs.startAsync();
            TestEvent testEvent = new TestEvent();

            bookstoreLoadService.loadAll();

            testEvent.commit();
            rs.awaitTermination();
        }

        assertEquals(3, paths.size());

    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}