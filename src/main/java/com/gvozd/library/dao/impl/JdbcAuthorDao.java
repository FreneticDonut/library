package com.gvozd.library.dao.impl;

import com.gvozd.library.dao.AuthorDao;
import com.gvozd.library.model.Author;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcAuthorDao implements AuthorDao {

    private final DataSource ds;

    public JdbcAuthorDao(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Author findOrCreate(String name) {
        try (Connection c = ds.getConnection()) {
            return findOrCreate(c, name);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения автора", e);
        }
    }

    @Override
    public List<Author> findByBookId(long bookId) {
        String sql = """
                SELECT a.id, a.name
                FROM authors a
                JOIN book_authors ba ON ba.author_id = a.id
                WHERE ba.book_id = ?
                ORDER BY a.name
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Author> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(new Author(rs.getLong("id"), rs.getString("name")));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения авторов книги", e);
        }
    }

    @Override
    public Map<Long, List<Author>> findByBookIds(Collection<Long> bookIds) {
        Map<Long, List<Author>> result = new HashMap<>();
        if (bookIds.isEmpty()) {
            return result;
        }

        String sql = """
                SELECT ba.book_id, a.id, a.name
                FROM book_authors ba
                JOIN authors a ON a.id = ba.author_id
                WHERE ba.book_id = ANY (?)
                ORDER BY a.name
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            Array ids = c.createArrayOf("bigint", bookIds.toArray(new Long[0]));
            try {
                ps.setArray(1, ids);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.computeIfAbsent(rs.getLong("book_id"), k -> new ArrayList<>())
                                .add(new Author(rs.getLong("id"), rs.getString("name")));
                    }
                }
            } finally {
                ids.free();
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения авторов книг", e);
        }
    }

    /**
     * Находит автора по имени (без учёта регистра) или создаёт нового.
     * Работает на переданном соединении, поэтому может быть частью чужой транзакции.
     */
    public static Author findOrCreate(Connection c, String name) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя автора не может быть пустым");
        }
        String trimmed = name.trim();

        String insert = "INSERT INTO authors (name) VALUES (?) ON CONFLICT ((LOWER(name))) DO NOTHING";
        try (PreparedStatement ps = c.prepareStatement(insert)) {
            ps.setString(1, trimmed);
            ps.executeUpdate();
        }

        String select = "SELECT id, name FROM authors WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = c.prepareStatement(select)) {
            ps.setString(1, trimmed);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Автор не найден после создания: " + trimmed);
                }
                return new Author(rs.getLong("id"), rs.getString("name"));
            }
        }
    }

    /**
     * Заменяет авторов книги на переданный список имён.
     * Работает на переданном соединении, чтобы вызываться внутри транзакции сохранения книги.
     */
    public static void replaceBookAuthors(Connection c, long bookId, List<String> names)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM book_authors WHERE book_id = ?")) {
            ps.setLong(1, bookId);
            ps.executeUpdate();
        }

        String link = "INSERT INTO book_authors (book_id, author_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = c.prepareStatement(link)) {
            for (String name : names) {
                Author author = findOrCreate(c, name);
                ps.setLong(1, bookId);
                ps.setLong(2, author.getId());
                ps.executeUpdate();
            }
        }
    }
}