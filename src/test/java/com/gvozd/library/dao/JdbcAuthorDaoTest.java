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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JdbcAuthorDaoTest {

    private static final AtomicLong COUNTER = new AtomicLong(System.nanoTime());

    private static AuthorDao authorDao;
    private static BookDao bookDao;

    @BeforeAll
    static void setUp() {
        assumeTrue(System.getenv("DB_URL") != null, "DB_URL не задан, тест пропущен");
        authorDao = new JdbcAuthorDao(Db.getDataSource());
        bookDao = new JdbcBookDao(Db.getDataSource());
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        if (authorDao == null) {
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

    private static String uniqueName(String base) {
        return "AuthorTest " + base + " " + COUNTER.incrementAndGet();
    }

    private static long createBook() {
        String title = "AuthorTest book " + COUNTER.incrementAndGet();
        return bookDao.save(new Book(null, title, null, 2020, 1, 1)).getId();
    }

    private static void setAuthors(long bookId, String... names) throws SQLException {
        try (Connection c = Db.getDataSource().getConnection()) {
            JdbcAuthorDao.replaceBookAuthors(c, bookId, List.of(names));
        }
    }

    @Test
    void findOrCreateCreatesNewAuthor() {
        String name = uniqueName("Новый");

        Author author = authorDao.findOrCreate(name);

        assertNotNull(author.getId());
        assertEquals(name, author.getName());
    }

    @Test
    void sameAuthorIsFoundIgnoringCaseAndSpaces() {
        String name = uniqueName("Толстой");

        Author first = authorDao.findOrCreate(name);
        Author second = authorDao.findOrCreate("  " + name.toUpperCase() + "  ");

        assertEquals(first.getId(), second.getId());
        assertEquals(name, second.getName());
    }

    @Test
    void parallelCreationGivesOneAuthor() throws Exception {
        String name = uniqueName("Parallel");
        int threads = 4;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Long>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                return authorDao.findOrCreate(name).getId();
            }));
        }
        start.countDown();

        Set<Long> ids = new HashSet<>();
        for (Future<Long> future : futures) {
            ids.add(future.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();

        assertEquals(1, ids.size());
    }

    @Test
    void blankNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> authorDao.findOrCreate("   "));
        assertThrows(IllegalArgumentException.class, () -> authorDao.findOrCreate(null));
    }

    @Test
    void replaceSetsAndThenReplacesAuthors() throws SQLException {
        long bookId = createBook();
        String first = uniqueName("A");
        String second = uniqueName("B");

        setAuthors(bookId, first, second);
        List<Author> both = authorDao.findByBookId(bookId);

        assertEquals(2, both.size());
        assertEquals(first, both.get(0).getName());
        assertEquals(second, both.get(1).getName());

        setAuthors(bookId, second);
        List<Author> onlySecond = authorDao.findByBookId(bookId);

        assertEquals(1, onlySecond.size());
        assertEquals(second, onlySecond.get(0).getName());
    }

    @Test
    void replaceWithEmptyListRemovesAllAuthors() throws SQLException {
        long bookId = createBook();
        setAuthors(bookId, uniqueName("C"));

        setAuthors(bookId);

        assertTrue(authorDao.findByBookId(bookId).isEmpty());
    }

    @Test
    void sameAuthorInDifferentCaseIsLinkedOnce() throws SQLException {
        long bookId = createBook();
        String name = uniqueName("Достоевский");

        setAuthors(bookId, name, name.toUpperCase());

        assertEquals(1, authorDao.findByBookId(bookId).size());
    }

    @Test
    void findByBookIdsGroupsAuthorsByBook() throws SQLException {
        long book1 = createBook();
        long book2 = createBook();
        long book3 = createBook();
        String a = uniqueName("A");
        String b = uniqueName("B");

        setAuthors(book1, a, b);
        setAuthors(book2, b);

        Map<Long, List<Author>> result = authorDao.findByBookIds(List.of(book1, book2, book3));

        assertEquals(2, result.get(book1).size());
        assertEquals(1, result.get(book2).size());
        assertFalse(result.containsKey(book3));
    }

    @Test
    void findByBookIdsWithEmptyCollectionReturnsEmptyMap() {
        assertTrue(authorDao.findByBookIds(List.of()).isEmpty());
    }
}