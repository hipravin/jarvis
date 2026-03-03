package hipravin.jarvis.bookstore.dao.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

@MappedSuperclass
@BatchSize(size = 100)
public class BookPageBaseEntity {
    @EmbeddedId
    private BookPageId bookPageId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BOOK_ID", insertable = false, updatable = false)
//    @Fetch(FetchMode.SUBSELECT) //Association 'book' is annotated '@Fetch(SUBSELECT)' but is not many-valued
//    @Fetch(FetchMode.JOIN) //join make it implicitly EAGER ignoring FetchType setting!!!
    private BookEntity book;

    @Column(name = "CONTENT")
    private String content;

    @Basic(fetch = FetchType.LAZY)
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
