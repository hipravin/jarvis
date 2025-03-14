package hipravin.jarvis.bookstore.dao;

import hipravin.jarvis.bookstore.dao.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<BookEntity, Long> {
}
