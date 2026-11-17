package com.gvozd.library.dao;

import com.gvozd.library.dao.impl.JdbcBookDao;
import com.gvozd.library.model.Book;
import com.gvozd.library.util.Db;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JdbcBookSearchTest {

    private static final AtomicLong COUNTER = new AtomicLong(System.nanoTime());

    private static BookDao bookDao;

    @BeforeAll
    static void setUp() {
        assumeTrue(System.getenv("DB_URL") != null, "DB_URL не задан, тест пропущен");
        bookDao = new JdbcBookDao(Db.getDataSource());
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        if (bookDao == null) {
            return;
        }
        try (Connection c = Db.getDataSource().getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM book_authors WHERE book_id IN "
                    + "(SELECT id FROM books WHERE title LIKE 'SearchTest %')");
            st.executeUpdate("DELETE FROM books WHERE title LIKE 'SearchTest %'");
            st.executeUpdate("DELETE FROM authors WHERE name LIKE 'SearchTest %'");
        }
    }

    private static String newToken() {
        return "tok" + COUNTER.incrementAndGet();
    }

    private static Book createBook(String title, String... authors) {
        Book book = new Book(null, "SearchTest " + title, null, 2020, 1, 1);
        return bookDao.save(book, List.of(authors));
    }

    private static boolean contains(List<Book> books, Book expected) {
        return books.stream().anyMatch(b -> b.getId().equals(expected.getId()));
    }

    @Test
    void findsBookByAuthorName() {
        String token = newToken();
        Book book = createBook("Книга " + COUNTER.incrementAndGet(), "SearchTest Автор " + token);

        List<Book> found = bookDao.findPage(token, 10, 0);

        assertEquals(1, found.size());
        assertTrue(contains(found, book));
        assertEquals(1, bookDao.count(token));
    }

    @Test
    void findsBookByTitle() {
        String token = newToken();
        Book book = createBook("Название " + token);

        List<Book> found = bookDao.findPage(token, 10, 0);

        assertEquals(1, found.size());
        assertTrue(contains(found, book));
    }

    @Test
    void authorSearchIgnoresCase() {
        String token = newToken();
        Book book = createBook("Регистр " + COUNTER.incrementAndGet(),
                "SearchTest Лев Толстой " + token);

        List<Book> found = bookDao.findPage("ЛЕВ толстой " + token.toUpperCase(), 10, 0);

        assertTrue(contains(found, book));
    }

    @Test
    void bookWithSeveralMatchingAuthorsIsListedOnce() {
        String token = newToken();
        Book book = createBook("Два автора " + COUNTER.incrementAndGet(),
                "SearchTest Первый " + token, "SearchTest Второй " + token);

        List<Book> found = bookDao.findPage(token, 10, 0);

        assertEquals(1, found.size());
        assertTrue(contains(found, book));
        assertEquals(1, bookDao.count(token));
    }

    @Test
    void titleMatchAndAuthorMatchAreBothFound() {
        String token = newToken();
        Book byTitle = createBook("Заголовок " + token);
        Book byAuthor = createBook("Другая " + COUNTER.incrementAndGet(),
                "SearchTest Автор " + token);

        List<Book> found = bookDao.findPage(token, 10, 0);

        assertEquals(2, found.size());
        assertTrue(contains(found, byTitle));
        assertTrue(contains(found, byAuthor));
        assertEquals(2, bookDao.count(token));
    }

    @Test
    void unknownQueryFindsNothing() {
        String token = newToken();

        assertTrue(bookDao.findPage(token, 10, 0).isEmpty());
        assertEquals(0, bookDao.count(token));
    }
}