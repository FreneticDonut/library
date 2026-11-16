CREATE TABLE IF NOT EXISTS users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'READER'
                  CHECK (role IN ('READER', 'LIBRARIAN'))
);

CREATE TABLE IF NOT EXISTS authors (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(200) NOT NULL
);

CREATE TABLE IF NOT EXISTS books (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title            VARCHAR(300) NOT NULL,
    isbn             VARCHAR(20)  UNIQUE,
    publish_year     INT,
    total_copies     INT NOT NULL CHECK (total_copies >= 0),
    available_copies INT NOT NULL CHECK (available_copies >= 0),
    CHECK (available_copies <= total_copies)
);

CREATE TABLE IF NOT EXISTS book_authors (
    book_id   BIGINT NOT NULL REFERENCES books(id)   ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES authors(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);

CREATE TABLE IF NOT EXISTS loans (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    book_id     BIGINT NOT NULL REFERENCES books(id),
    status      VARCHAR(20) NOT NULL DEFAULT 'ORDERED'
                CHECK (status IN ('ORDERED', 'ACTIVE', 'RETURNED')),
    loaned_at   TIMESTAMP,
    due_date    DATE,
    returned_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_books_title ON books (LOWER(title));
CREATE INDEX IF NOT EXISTS idx_loans_user  ON loans (user_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_loans_open_user_book
    ON loans (user_id, book_id)
    WHERE status IN ('ORDERED', 'ACTIVE');

CREATE INDEX IF NOT EXISTS idx_loans_book ON loans (book_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_authors_name ON authors (LOWER(name));
CREATE INDEX IF NOT EXISTS idx_book_authors_author ON book_authors (author_id);