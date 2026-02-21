package hipravin.jarvis.bookstore.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record BookPageId( //JPA3: A Java record type may now be annotated @Embeddable or used as an @IdClass.
        @Column(name = "BOOK_ID") Long bookId,
        @Column(name = "PAGE_NUM") Long pageNum
) {
}
