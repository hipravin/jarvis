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

    private final BookRepository bookRepository;

    public BookstoreDaoImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public BookEntity save(Book book) {
        BookEntity be = new BookEntity();
        be.setTitle(book.title());
        be.setPdfContent(book.pdfContent());
        if (book.metadata().metadata() != null) {
            be.setMetadata(Map.copyOf(book.metadata().metadata()));
        }

        entityManager.persist(be);

        List<BookPageEntity> pageEntities = book.pages().stream()
                .map(bp -> mapToEntity(be.getId(), bp))
                .toList();

        pageEntities.forEach(entityManager::persist);

        return be;
    }

    @Override
    public BookEntity findById(long id) {
        return bookRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Book with '%d' not found".formatted(id)));
    }

    @Override
    public List<BookPageEntity> search(String keywords) {
        String searchNativeQuery = """
                with pages as (
                    select * from {h-schema}book_page where to_tsvector('english', content) @@ plainto_tsquery(:query))
                select *
                    from (
                         select *, row_number() over (partition by book_id order by page_num) as n
                         from pages
                     ) as x
                where n <= :max_per_book""";
        var query = entityManager.createNativeQuery(searchNativeQuery, BookPageEntity.class)
                .setParameter("query", keywords)
                .setParameter("max_per_book", 10);

        @SuppressWarnings("unchecked")
        List<BookPageEntity> pages = (List<BookPageEntity>) query.getResultList();

        return pages;
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
