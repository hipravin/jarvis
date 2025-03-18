package hipravin.jarvis.bookstore.dao;

import hipravin.jarvis.bookstore.dao.entity.BookEntity;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = { "spring.flyway.enabled=true" })
@Testcontainers
@ActiveProfiles({"test"})
class BookstoreDaoImplIT {
    static Path sampleSaltPdf = Path.of("src/test/resources/data/bookstore/estimating salt intake not so easy.pdf");

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
    void testSave() {
        Book book = bookLoader.load(sampleSaltPdf);
        BookEntity bookEntity = bookstoreDao.save(book);

        assertNotNull(bookEntity);
        assertNotNull(bookEntity.getId());

        assertEquals("Estimating salt intake in humans: not so easy!1", bookEntity.getMetadata().get("Title"));

        System.out.println("done");
    }
}