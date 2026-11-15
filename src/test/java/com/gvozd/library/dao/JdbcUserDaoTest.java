package com.gvozd.library.dao;

import com.gvozd.library.dao.impl.JdbcUserDao;
import com.gvozd.library.model.Role;
import com.gvozd.library.model.User;
import com.gvozd.library.util.Db;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JdbcUserDaoTest {

    private static final String EMAIL_PREFIX = "dao-test-";
    private static final String EMAIL_DOMAIN = "@example.com";

    private static UserDao dao;

    @BeforeAll
    static void setUp() {
        assumeTrue(System.getenv("DB_URL") != null, "DB_URL не задан, тест пропущен");
        dao = new JdbcUserDao(Db.getDataSource());
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        if (dao == null) {
            return;
        }
        String sql = "DELETE FROM users WHERE email LIKE ?";
        try (Connection c = Db.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, EMAIL_PREFIX + "%" + EMAIL_DOMAIN);
            ps.executeUpdate();
        }
    }

    private static String uniqueEmail() {
        return EMAIL_PREFIX + System.nanoTime() + EMAIL_DOMAIN;
    }

    @Test
    void savesAndFindsByEmail() {
        String email = uniqueEmail();
        User saved = dao.save(new User(null, email, "hash", "Иван", Role.READER));

        assertNotNull(saved.getId());

        User found = dao.findByEmail(email).orElseThrow();
        assertEquals(saved.getId(), found.getId());
        assertEquals("Иван", found.getName());
        assertEquals("hash", found.getPasswordHash());
    }

    @Test
    void roleIsStoredAndRestored() {
        String email = uniqueEmail();
        dao.save(new User(null, email, "hash", "Мария", Role.LIBRARIAN));

        assertEquals(Role.LIBRARIAN, dao.findByEmail(email).orElseThrow().getRole());
    }

    @Test
    void findsById() {
        User saved = dao.save(new User(null, uniqueEmail(), "hash", "Пётр", Role.READER));

        assertEquals(saved.getEmail(), dao.findById(saved.getId()).orElseThrow().getEmail());
    }

    @Test
    void unknownEmailIsNotFound() {
        assertTrue(dao.findByEmail("nobody-" + System.nanoTime() + EMAIL_DOMAIN).isEmpty());
    }

    @Test
    void existsByEmailWorks() {
        String email = uniqueEmail();
        assertFalse(dao.existsByEmail(email));

        dao.save(new User(null, email, "hash", "Анна", Role.READER));

        assertTrue(dao.existsByEmail(email));
    }

    @Test
    void duplicateEmailIsRejected() {
        String email = uniqueEmail();
        dao.save(new User(null, email, "hash", "Олег", Role.READER));

        assertThrows(RuntimeException.class,
                () -> dao.save(new User(null, email, "hash2", "Двойник", Role.READER)));
    }
}