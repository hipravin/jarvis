package hipravin.jarvis.bookstore.dao;


import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.dao.entity.BookPageEntity;
import hipravin.jarvis.bookstore.dao.entity.BookPageFtsEntity;
import hipravin.jarvis.bookstore.dao.entity.BookPageId;
import hipravin.jarvis.bookstore.load.model.Book;
import hipravin.jarvis.bookstore.load.model.BookPage;
import hipravin.jarvis.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class BookstoreDaoImpl implements BookstoreDao {
    private static final String BOOK_FTS_NATIVE_QUERY = """
            with pages_ranked as
                     (select *, ts_rank_cd(content_fts_en, query) as rank
                      from book_page, websearch_to_tsquery(:query) query
                      where content_fts_en @@ query)
            select *,
                   ts_headline('english', content, websearch_to_tsquery('english', :query),
                               'MaxFragments=5, MaxWords=15, MinWords=3, StartSel=<b>, StopSel=</b>') as content_highlighted
            from (select *,
                         row_number() over (partition by book_id order by rank desc) as rownum_per_book,
                         max(rank) over (partition by book_id)                       as max_rank_per_book
                  from pages_ranked) as x
            where rownum_per_book <= :max_per_book
            order by max_rank_per_book desc, book_id, rownum_per_book limit :max_total""";

    @PersistenceContext
    private EntityManager entityManager;
    private final JdbcClient jdbcClient;
    private final BookRepository bookRepository;

    public BookstoreDaoImpl(JdbcClient jdbcClient, BookRepository bookRepository) {
        this.jdbcClient = jdbcClient;
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
                () -> new NotFoundException("Book '%d'".formatted(id)));
    }

    @Override
    public BookEntity findByIdFetchPdf(long id) {
        var be = findById(id);
        Hibernate.initialize(be.getPdfContent());
        return be;
    }

    @Override
    public void writePdfContentTo(long id, OutputStream outputStream) {
        jdbcClient.sql("select id, pdf_content from book where id = ?")
                .param(1, id)
                .query(rs -> {
                    if (rs.next()) {
                        try {
                            return rs.getBinaryStream("pdf_content").transferTo(outputStream);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    } else {
                        throw new NotFoundException("Book '%d'".formatted(id));
                    }
                });
    }

    @Override
    public List<BookEntity> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public List<BookPageFtsEntity> search(String fullTextSearchQuery) {
        var query = entityManager.createNativeQuery(BOOK_FTS_NATIVE_QUERY, BookPageFtsEntity.class)
                .setParameter("query", fullTextSearchQuery)
                .setParameter("max_per_book", 3)
                .setParameter("max_total", 20);

        @SuppressWarnings("unchecked")
        List<BookPageFtsEntity> pages = (List<BookPageFtsEntity>) query.getResultList();
        for (BookPageFtsEntity page : pages) {
            Hibernate.initialize(page.getBook());
        }

        return pages;
    }

    static BookPageEntity mapToEntity(long bookId, BookPage page) {
        BookPageEntity bpe = new BookPageEntity();
        bpe.setBookPageId(new BookPageId(bookId, page.pageNum()));
        bpe.setContent(page.content());
        bpe.setPdfContent(page.pdfContent());

        return bpe;
    }
}
