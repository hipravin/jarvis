package hipravin.jarvis.bookstore.dao;

import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.dao.entity.BookFtsPageEntity;
import hipravin.jarvis.bookstore.load.model.Book;

import java.util.List;

public interface BookstoreDao {

    BookEntity save(Book book);

    BookEntity findById(long id);

    List<BookFtsPageEntity> search(String fullTextSearchQuery);
}
