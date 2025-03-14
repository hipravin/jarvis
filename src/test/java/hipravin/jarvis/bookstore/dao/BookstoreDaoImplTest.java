package hipravin.jarvis.bookstore.dao;

import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookstoreDaoImplTest {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookPageRepository bookPageRepository;

    @Test
    void testSave() {
        BookEntity be = new BookEntity();
        be.setTitle("Sample book 1");
        bookRepository.save(be);
    }
}