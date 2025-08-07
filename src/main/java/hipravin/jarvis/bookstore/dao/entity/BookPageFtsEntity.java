package hipravin.jarvis.bookstore.dao.entity;

import jakarta.persistence.*;

/**
 * Entity intended for mapping from result of book full text search
 */
@Entity
public class BookPageFtsEntity extends BookPageBaseEntity {
    @Column(name = "RANK")
    private Double rank;

    @Column(name= "ROWNUM_PER_BOOK")
    private Long rownumPerBook;

    @Column(name = "CONTENT_HIGHLIGHTED")
    private String contentHighlighted;

    public Double getRank() {
        return rank;
    }

    public void setRank(Double rank) {
        this.rank = rank;
    }

    public Long getRownumPerBook() {
        return rownumPerBook;
    }

    public void setRownumPerBook(Long rownumPerBook) {
        this.rownumPerBook = rownumPerBook;
    }

    public String getContentHighlighted() {
        return contentHighlighted;
    }

    public void setContentHighlighted(String contentHighlighted) {
        this.contentHighlighted = contentHighlighted;
    }
}
