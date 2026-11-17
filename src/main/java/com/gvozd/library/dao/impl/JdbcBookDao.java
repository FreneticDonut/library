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

        private static final String MATCH =
            "(LOWER(b.title) LIKE ? OR EXISTS (SELECT 1 FROM book_authors ba "
            + "JOIN authors a ON a.id = ba.author_id "
            + "WHERE ba.book_id = b.id AND LOWER(a.name) LIKE ?))";

    public JdbcBookDao(DataSource ds) {
        this.ds = ds;
    }

       @Override
    public List<Book> findPage(String query, int limit, int offset) {
        String sql = "SELECT b.id, b.title, b.isbn, b.publish_year, b.total_copies, "
                + "b.available_copies FROM books b WHERE " + MATCH
                + " ORDER BY b.title LIMIT ? OFFSET ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String pattern = like(query);
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setInt(3, limit);
            ps.setInt(4, offset);
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
    public long count(String query) {
        String sql = "SELECT COUNT(*) FROM books b WHERE " + MATCH;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String pattern = like(query);
            ps.setString(1, pattern);
            ps.setString(2, pattern);
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

    @Override
    public boolean hasLoans(long bookId) {
        String sql = "SELECT 1 FROM loans WHERE book_id = ? LIMIT 1";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки выдач книги", e);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления книги", e);
        }
    }

    @Override
    public Book save(Book book, List<String> authorNames) {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                insertRow(c, book);
                JdbcAuthorDao.replaceBookAuthors(c, book.getId(), authorNames);
                c.commit();
                return book;
            } catch (SQLException | RuntimeException e) {
                book.setId(null);
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения книги", e);
        }
    }

    @Override
    public void update(Book book, List<String> authorNames) {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                updateRow(c, book);
                JdbcAuthorDao.replaceBookAuthors(c, book.getId(), authorNames);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления книги", e);
        }
    }

    private static void insertRow(Connection c, Book book) throws SQLException {
        String sql = """
                INSERT INTO books (title, isbn, publish_year, total_copies, available_copies)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        }
    }

    private static void updateRow(Connection c, Book book) throws SQLException {
        String sql = """
                UPDATE books
                SET title = ?, isbn = ?, publish_year = ?, total_copies = ?, available_copies = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
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