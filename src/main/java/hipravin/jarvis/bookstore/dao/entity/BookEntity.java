package hipravin.jarvis.bookstore.dao.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "BOOK")
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookIdSeq")
    @SequenceGenerator(sequenceName = "BOOK_ID_SEQ", allocationSize = 100, name = "bookIdSeq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "METADATA", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> metadata;

    @Column(name = "FIRST_PUBLISHED")
    private Integer firstPublished;

    @Column(name = "EDITION_PUBLISHED")
    private Integer editionPublished;

    @Column(name = "PDF_CONTENT")
    private byte[] pdfContent;//TODO: avoid unnecessary pdf loading from DB (Projection, EntityGraph?)

    @Column(name = "LAST_UPDATED")
    @Temporal(TemporalType.TIMESTAMP)
    private Instant lastUpdated = Instant.now();

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Integer getFirstPublished() {
        return firstPublished;
    }

    public void setFirstPublished(Integer firstPublished) {
        this.firstPublished = firstPublished;
    }

    public Integer getEditionPublished() {
        return editionPublished;
    }

    public void setEditionPublished(Integer editionPublished) {
        this.editionPublished = editionPublished;
    }

    public byte[] getPdfContent() {
        return pdfContent;
    }

    public void setPdfContent(byte[] pdfContent) {
        this.pdfContent = pdfContent;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
