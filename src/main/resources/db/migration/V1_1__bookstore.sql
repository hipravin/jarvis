CREATE TABLE BOOK (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    first_published INT NULL
        CHECK (first_published IS NULL OR (first_published > 1700 AND first_published < 3000)), --year
    edition_published INT NULL
        CHECK (edition_published IS NULL
                   OR (edition_published >= coalesce(first_published, 0) AND edition_published > 1700 AND edition_published < 3000)), --year
    metadata JSONB,
    pdf_content BYTEA,
--     content_hash TEXT, --TODO: think about it
    last_updated TIMESTAMP NOT NULL DEFAULT now()
);

ALTER SEQUENCE BOOK_ID_SEQ INCREMENT BY 100 RESTART 100;

CREATE TABLE BOOK_PAGE (
    page_num INT NOT NULL CHECK (page_num >= 0),
    book_id BIGINT REFERENCES BOOK(id) ON DELETE CASCADE NOT NULL,
    content TEXT,
    content_fts_en TSVECTOR GENERATED ALWAYS AS (to_tsvector('english', content)) STORED,
    pdf_content BYTEA,
    PRIMARY KEY (book_id, page_num)
);

CREATE INDEX page_book_idx on BOOK_PAGE(book_id);
CREATE UNIQUE INDEX book_title_idx on BOOK(title);
CREATE UNIQUE INDEX book_hash_idx ON BOOK (digest(pdf_content, 'sha256'));
CREATE INDEX bp_fts_en_idx ON BOOK_PAGE USING GIN (content_fts_en);