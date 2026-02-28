package hipravin.jarvis.bookstore.dao;

import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.dao.entity.BookPageFtsEntity;
import hipravin.jarvis.bookstore.load.model.Book;

import java.io.OutputStream;
import java.util.List;

public interface BookstoreDao {

    BookEntity save(Book book);

    BookEntity findById(long id);

    BookEntity findByIdFetchPdf(long id);

    void deleteById(long id);

    void writePdfContentTo(long id, OutputStream outputStream); //???

    List<BookEntity> findAll();

    List<BookPageFtsEntity> search(String fullTextSearchQuery);
}
