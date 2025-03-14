package hipravin.jarvis.bookstore.dao.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "BOOK_PAGE")
public class BookPage {
    @EmbeddedId
    private BookPageId bookPageId;

    @ManyToOne
    @JoinColumn(name = "BOOK_ID", insertable = false, updatable = false)
    private BookEntity book;

    public BookPageId getBookPageId() {
        return bookPageId;
    }

    public void setBookPageId(BookPageId bookPageId) {
        this.bookPageId = bookPageId;
    }

    public BookEntity getBook() {
        return book;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }
}
