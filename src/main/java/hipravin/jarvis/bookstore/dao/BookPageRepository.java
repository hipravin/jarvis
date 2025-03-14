package hipravin.jarvis.bookstore.dao;

import hipravin.jarvis.bookstore.dao.entity.BookPage;
import hipravin.jarvis.bookstore.dao.entity.BookPageId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookPageRepository extends JpaRepository<BookPage, BookPageId> {
}
