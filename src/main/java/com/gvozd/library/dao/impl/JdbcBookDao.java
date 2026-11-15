package com.gvozd.library.dao.impl;

import com.gvozd.library.dao.BookDao;
import com.gvozd.library.model.Book;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBookDao implements BookDao {

    private final DataSource ds;

    public JdbcBookDao(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public List<Book> findPage(String titleQuery, int limit, int offset) {
        String sql = """
                SELECT id, title, isbn, publish_year, total_copies, available_copies
                FROM books
                WHERE LOWER(title) LIKE ?
                ORDER BY title
                LIMIT ? OFFSET ?
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like(titleQuery));
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Book> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска книг", e);
        }
    }

    @Override
    public long count(String titleQuery) {
        String sql = "SELECT COUNT(*) FROM books WHERE LOWER(title) LIKE ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like(titleQuery));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка подсчёта книг", e);
        }
    }

    @Override
    public Optional<Book> findById(long id) {
        String sql = """
                SELECT id, title, isbn, publish_year, total_copies, available_copies
                FROM books
                WHERE id = ?
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения книги", e);
        }
    }

    @Override
    public Book save(Book book) {
        String sql = """
                INSERT INTO books (title, isbn, publish_year, total_copies, available_copies)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getIsbn());
            if (book.getPublishYear() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, book.getPublishYear());
            }
            ps.setInt(4, book.getTotalCopies());
            ps.setInt(5, book.getAvailableCopies());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setId(keys.getLong("id"));
                }
            }
            return book;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения книги", e);
        }
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        String sql = "SELECT 1 FROM books WHERE isbn = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки ISBN", e);
        }
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        String sql = """
                SELECT id, title, isbn, publish_year, total_copies, available_copies
                FROM books
                WHERE isbn = ?
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска книги по ISBN", e);
        }
    }

    @Override
    public void update(Book book) {
        String sql = """
                UPDATE books
                SET title = ?, isbn = ?, publish_year = ?, total_copies = ?, available_copies = ?
                WHERE id = ?
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getIsbn());
            if (book.getPublishYear() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, book.getPublishYear());
            }
            ps.setInt(4, book.getTotalCopies());
            ps.setInt(5, book.getAvailableCopies());
            ps.setLong(6, book.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления книги", e);
        }
    }

    private static String like(String query) {
        String s = (query == null) ? "" : query.trim().toLowerCase();
        return "%" + s + "%";
    }

    private static Book map(ResultSet rs) throws SQLException {
        int year = rs.getInt("publish_year");
        Integer publishYear = rs.wasNull() ? null : year;
        return new Book(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("isbn"),
                publishYear,
                rs.getInt("total_copies"),
                rs.getInt("available_copies"));
    }
}