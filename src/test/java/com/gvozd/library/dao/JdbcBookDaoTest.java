package com.gvozd.library.dao;

import com.gvozd.library.dao.impl.JdbcBookDao;
import com.gvozd.library.model.Book;
import com.gvozd.library.util.Db;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JdbcBookDaoTest {

    private static BookDao dao;

    @BeforeAll
    static void setUp() {
        assumeTrue(System.getenv("DB_URL") != null, "DB_URL не задан, тест пропущен");
        dao = new JdbcBookDao(Db.getDataSource());
    }

    @Test
    void savesAndFindsBook() {
        String title = "Test Book " + System.nanoTime();
        Book saved = dao.save(new Book(null, title, null, 2024, 1, 1));

        assertNotNull(saved.getId());
        assertEquals(title, dao.findById(saved.getId()).orElseThrow().getTitle());
    }

    @Test
    void searchIsCaseInsensitive() {
        List<Book> found = dao.findPage("clean", 10, 0);

        assertTrue(found.stream().anyMatch(b -> b.getTitle().equals("Clean Code")));
    }
}