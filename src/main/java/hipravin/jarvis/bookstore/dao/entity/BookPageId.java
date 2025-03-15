package hipravin.jarvis.bookstore.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BookPageId implements Serializable {
    @Column(name = "BOOK_ID")
    private Long bookId;

    @Column(name = "PAGE_NUM")
    private Long pageNum;

    public BookPageId() {
    }

    public BookPageId(Long bookId, Long pageNum) {
        this.bookId = bookId;
        this.pageNum = pageNum;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getPageNum() {
        return pageNum;
    }

    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookPageId that = (BookPageId) o;
        return Objects.equals(bookId, that.bookId) && Objects.equals(pageNum, that.pageNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, pageNum);
    }
}
