package hipravin.jarvis.bookstore.dao.entity;

import jakarta.persistence.*;

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

//    @OneToMany(
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
}
