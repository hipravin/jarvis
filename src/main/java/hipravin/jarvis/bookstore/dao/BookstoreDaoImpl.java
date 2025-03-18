package hipravin.jarvis.bookstore.dao;


import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.dao.entity.BookPageEntity;
import hipravin.jarvis.bookstore.dao.entity.BookPageId;
import hipravin.jarvis.bookstore.load.model.Book;
import hipravin.jarvis.bookstore.load.model.BookPage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class BookstoreDaoImpl implements BookstoreDao {
    @PersistenceContext
    private EntityManager entityManager;

    public BookstoreDaoImpl() {
    }

    @Override
    public BookEntity save(Book book) {
        BookEntity be = new BookEntity();
        be.setSource(book.source());
        be.setPdfContent(book.pdfContent());
        if(book.metadata().metadata() != null) {
            be.setMetadata(Map.copyOf(book.metadata().metadata()));
        }

        entityManager.persist(be);

        List<BookPageEntity> pageEntities = book.pages().stream()
                .map(bp -> mapToEntity(be.getId(), bp))
                .toList();

        pageEntities.forEach(entityManager::persist);

        return be;
    }

    static BookPageEntity mapToEntity(long bookId, BookPage page) {
        BookPageEntity bpe = new BookPageEntity();
        bpe.setBookPageId(new BookPageId(bookId, page.pageNum()));
        bpe.setContent(page.content());
        bpe.setPdfContent(page.pdfContent());

        return bpe;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
