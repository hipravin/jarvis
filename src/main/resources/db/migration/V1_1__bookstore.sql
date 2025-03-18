CREATE TABLE BOOK (
    id BIGSERIAL PRIMARY KEY,
    source TEXT,
    metadata JSONB,
    pdf_content BYTEA
);

ALTER SEQUENCE BOOK_ID_SEQ INCREMENT BY 100 RESTART 100;

CREATE TABLE BOOK_PAGE (
    page_num INT NOT NULL CHECK (page_num >= 0),
    book_id BIGINT REFERENCES BOOK(id) ON DELETE CASCADE NOT NULL,
    content TEXT,
    pdf_content BYTEA,
    PRIMARY KEY (book_id, page_num)
);

CREATE INDEX page_book_idx on BOOK_PAGE(book_id);
CREATE INDEX pgweb_idx ON BOOK_PAGE USING GIN (to_tsvector('english', content));