package com.gvozd.library.dao;

import com.gvozd.library.dao.impl.JdbcAuthorDao;
import com.gvozd.library.dao.impl.JdbcBookDao;
import com.gvozd.library.model.Author;
import com.gvozd.library.model.Book;
import com.gvozd.library.util.Db;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JdbcBookAuthorsTest {

    private static final AtomicLong COUNTER = new AtomicLong(System.nanoTime());

    private static BookDao bookDao;
    private static AuthorDao authorDao;

    @BeforeAll
    static void setUp() {
        assumeTrue(System.getenv("DB_URL") != null, "DB_URL не задан, тест пропущен");
        bookDao = new JdbcBookDao(Db.getDataSource());
        authorDao = new JdbcAuthorDao(Db.getDataSource());
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        if (bookDao == null) {
            return;
        }
        try (Connection c = Db.getDataSource().getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM book_authors WHERE book_id IN "
                    + "(SELECT id FROM books WHERE title LIKE 'AuthorTest %')");
            st.executeUpdate("DELETE FROM books WHERE title LIKE 'AuthorTest %'");
            st.executeUpdate("DELETE FROM authors WHERE name LIKE 'AuthorTest %'");
        }
    }

    private static String unique(String base) {
        return "AuthorTest " + base + " " + COUNTER.incrementAndGet();
    }

    private static Book newBook(String title) {
        return new Book(null, title, null, 2020, 1, 1);
    }

    private static long count(String sql, String value) throws SQLException {
        try (Connection c = Db.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    @Test
    void bookIsSavedTogetherWithAuthors() {
        String first = unique("A");
        String second = unique("B");

        Book saved = bookDao.save(newBook(unique("book")), List.of(first, second));

        assertNotNull(saved.getId());
        List<Author> authors = authorDao.findByBookId(saved.getId());
        assertEquals(2, authors.size());
        assertEquals(first, authors.get(0).getName());
        assertEquals(second, authors.get(1).getName());
    }

    @Test
    void bookWithoutAuthorsIsSaved() {
        Book saved = bookDao.save(newBook(unique("no authors")), List.of());

        assertNotNull(saved.getId());
        assertEquals(0, authorDao.findByBookId(saved.getId()).size());
    }

    @Test
    void failureWhileSavingAuthorsRollsBackTheBook() throws SQLException {
        String title = unique("rollback");
        String goodAuthor = unique("Good");
        String tooLong = "AuthorTest " + "x".repeat(200);
        Book book = newBook(title);

        assertThrows(RuntimeException.class,
                () -> bookDao.save(book, List.of(goodAuthor, tooLong)));

        assertNull(book.getId());
        assertEquals(0, count("SELECT count(*) FROM books WHERE title = ?", title));
        assertEquals(0, count("SELECT count(*) FROM authors WHERE name = ?", goodAuthor));
    }

    @Test
    void updateReplacesAuthors() {
        Book saved = bookDao.save(newBook(unique("update")), List.of(unique("Old")));
        String newAuthor = unique("New");

        saved.setTitle(saved.getTitle() + " edited");
        bookDao.update(saved, List.of(newAuthor));

        assertEquals(saved.getTitle(), bookDao.findById(saved.getId()).orElseThrow().getTitle());
        List<Author> authors = authorDao.findByBookId(saved.getId());
        assertEquals(1, authors.size());
        assertEquals(newAuthor, authors.get(0).getName());
    }

    @Test
    void failedUpdateKeepsOldTitleAndAuthors() {
        String oldAuthor = unique("Keep");
        Book saved = bookDao.save(newBook(unique("keep")), List.of(oldAuthor));
        String oldTitle = saved.getTitle();

        Book changed = bookDao.findById(saved.getId()).orElseThrow();
        changed.setTitle(oldTitle + " changed");
        String tooLong = "AuthorTest " + "y".repeat(200);

        assertThrows(RuntimeException.class,
                () -> bookDao.update(changed, List.of(unique("Other"), tooLong)));

        assertEquals(oldTitle, bookDao.findById(saved.getId()).orElseThrow().getTitle());
        List<Author> authors = authorDao.findByBookId(saved.getId());
        assertEquals(1, authors.size());
        assertEquals(oldAuthor, authors.get(0).getName());
    }

    @Test
    void emptyAuthorListRemovesAllAuthors() {
        Book saved = bookDao.save(newBook(unique("clear")), List.of(unique("Gone")));

        bookDao.update(saved, List.of());

        assertEquals(0, authorDao.findByBookId(saved.getId()).size());
    }
}