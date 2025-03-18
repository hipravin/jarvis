package hipravin.jarvis.bookstore.dao.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "BOOK_PAGE")
public class BookPageEntity {
    @EmbeddedId
    private BookPageId bookPageId;

    @ManyToOne
    @JoinColumn(name = "BOOK_ID", insertable = false, updatable = false)
    private BookEntity book;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "PDF_CONTENT")
    private byte[] pdfContent;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getPdfContent() {
        return pdfContent;
    }

    public void setPdfContent(byte[] pdfContent) {
        this.pdfContent = pdfContent;
    }
}
