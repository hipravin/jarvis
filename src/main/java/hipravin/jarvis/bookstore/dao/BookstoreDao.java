package hipravin.jarvis.bookstore.dao;

import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import hipravin.jarvis.bookstore.load.model.Book;

public interface BookstoreDao {

    BookEntity save(Book book);

}
