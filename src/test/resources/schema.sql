--DAO layer to be tested with integration test powered by testcontainers
--Embedded HSQL database is used to occasionally simplify context initialization in unit tests
--and possibly test some trivial database usage scenarios
CREATE SEQUENCE book_id_seq START WITH 100 INCREMENT BY 100;

CREATE TABLE BOOK (
    id BIGSERIAL PRIMARY KEY,
    source TEXT,
    metadata TEXT,--JSONB
    pdf_content BYTEA
);

CREATE TABLE BOOK_PAGE (
    page_num INT NOT NULL,
    book_id BIGINT NOT NULL  REFERENCES BOOK(id) ON DELETE CASCADE ,
    content TEXT,
    pdf_content BYTEA,
    PRIMARY KEY (book_id, page_num)
);

CREATE INDEX page_book_idx on BOOK_PAGE(book_id);
CREATE INDEX pgweb_idx ON BOOK_PAGE (content);