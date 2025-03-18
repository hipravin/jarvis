package hipravin.jarvis.bookstore.dao.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "BOOK")
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookIdSeq")
    @SequenceGenerator(sequenceName = "BOOK_ID_SEQ", allocationSize = 100, name = "bookIdSeq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "SOURCE")
    private String source;

    @Column(name = "METADATA", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> metadata;

    @Column(name = "PDF_CONTENT")
    private byte[] pdfContent;

//    @OneToMany( // @OneToMany is practical only when many means few (c) vladmihalcea
//            mappedBy = "book",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true
//    )
//    private List<BookPage> bookPages = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public byte[] getPdfContent() {
        return pdfContent;
    }

    public void setPdfContent(byte[] pdfContent) {
        this.pdfContent = pdfContent;
    }
}
