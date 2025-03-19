package hipravin.jarvis.bookstore.dao;

import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.dao.entity.BookPageEntity;
import hipravin.jarvis.bookstore.load.model.Book;

import java.util.List;

public interface BookstoreDao {

    BookEntity save(Book book);

    BookEntity findById(long id);

    List<BookPageEntity> search(String query);
}
