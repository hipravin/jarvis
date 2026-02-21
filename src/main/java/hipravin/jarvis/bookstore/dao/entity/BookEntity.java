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
    @GeneratedValue //jpa 3: When @GeneratedValue does not explicitly specify a generator name, the provider automatically picks the “closest” matching sequence or table generator defined in the same entity class or package.
    @SequenceGenerator(sequenceName = "BOOK_ID_SEQ", allocationSize = 100)
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

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "PDF_CONTENT")
    private byte[] pdfContent;

    @Column(name = "LAST_UPDATED")
    private Instant lastUpdated = Instant.now();

//    @OneToMany( // @OneToMany is practical only when many means few (c) vladmihalcea
//            mappedBy = BookPageBaseEntity_.BOOK,
//            cascade = CascadeType.ALL,
//            orphanRemoval = true
//    )
//    private List<BookPageEntity> bookPages = new ArrayList<>();

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
