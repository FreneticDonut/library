package com.gvozd.library.dao.impl;

import com.gvozd.library.dao.LoanDao;
import com.gvozd.library.dao.OrderResult;
import com.gvozd.library.model.Loan;
import com.gvozd.library.model.LoanStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import com.gvozd.library.dao.CancelResult;
import com.gvozd.library.model.LoanView;

public class JdbcLoanDao implements LoanDao {

    private final DataSource ds;

    public JdbcLoanDao(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public OrderResult placeOrder(long userId, long bookId, int maxOpenLoans) {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                OrderResult result = tryPlaceOrder(c, userId, bookId, maxOpenLoans);
                if (result == OrderResult.OK) {
                    c.commit();
                } else {
                    c.rollback();
                }
                return result;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка оформления заказа", e);
        }
    }

    private OrderResult tryPlaceOrder(Connection c, long userId, long bookId, int maxOpenLoans)
            throws SQLException {
        lockUser(c, userId);

        if (hasOpenLoan(c, userId, bookId)) {
            return OrderResult.ALREADY_ORDERED;
        }
        if (countOpenLoans(c, userId) >= maxOpenLoans) {
            return OrderResult.LIMIT_REACHED;
        }
        if (!reserveCopy(c, bookId)) {
            return bookExists(c, bookId) ? OrderResult.NO_COPIES : OrderResult.BOOK_NOT_FOUND;
        }

        String sql = "INSERT INTO loans (user_id, book_id, status) VALUES (?, ?, 'ORDERED')";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, bookId);
            ps.executeUpdate();
        }
        return OrderResult.OK;
    }

    private void lockUser(Connection c, long userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM users WHERE id = ? FOR UPDATE")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Пользователь не найден: " + userId);
                }
            }
        }
    }

    private boolean hasOpenLoan(Connection c, long userId, long bookId) throws SQLException {
        String sql = """
                SELECT 1 FROM loans
                WHERE user_id = ? AND book_id = ? AND status IN ('ORDERED', 'ACTIVE')
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int countOpenLoans(Connection c, long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM loans WHERE user_id = ? AND status IN ('ORDERED', 'ACTIVE')";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private boolean reserveCopy(Connection c, long bookId) throws SQLException {
        String sql = """
                UPDATE books SET available_copies = available_copies - 1
                WHERE id = ? AND available_copies > 0
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            return ps.executeUpdate() == 1;
        }
    }

    private boolean bookExists(Connection c, long bookId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM books WHERE id = ?")) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public List<Loan> findByUser(long userId) {
        String sql = """
                SELECT id, user_id, book_id, status, loaned_at, due_date, returned_at
                FROM loans
                WHERE user_id = ?
                ORDER BY id DESC
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Loan> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения выдач", e);
        }
    }

    @Override
    public CancelResult cancelOrder(long userId, long loanId) {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                CancelResult result = tryCancel(c, userId, loanId);
                if (result == CancelResult.OK) {
                    c.commit();
                } else {
                    c.rollback();
                }
                return result;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка отмены заказа", e);
        }
    }

    private CancelResult tryCancel(Connection c, long userId, long loanId) throws SQLException {
        long bookId;
        String status;

        String select = "SELECT book_id, status FROM loans WHERE id = ? AND user_id = ? FOR UPDATE";
        try (PreparedStatement ps = c.prepareStatement(select)) {
            ps.setLong(1, loanId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return CancelResult.NOT_FOUND;
                }
                bookId = rs.getLong("book_id");
                status = rs.getString("status");
            }
        }

        if (!LoanStatus.ORDERED.name().equals(status)) {
            return CancelResult.NOT_CANCELLABLE;
        }

        try (PreparedStatement ps = c.prepareStatement("DELETE FROM loans WHERE id = ?")) {
            ps.setLong(1, loanId);
            ps.executeUpdate();
        }

        String restore = "UPDATE books SET available_copies = available_copies + 1 WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(restore)) {
            ps.setLong(1, bookId);
            ps.executeUpdate();
        }
        return CancelResult.OK;
    }

    @Override
    public List<LoanView> findViewsByUser(long userId) {
        String sql = """
                SELECT l.id, l.book_id, b.title, l.status, l.loaned_at, l.due_date, l.returned_at
                FROM loans l
                JOIN books b ON b.id = l.book_id
                WHERE l.user_id = ?
                ORDER BY l.id DESC
                """;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<LoanView> result = new ArrayList<>();
                while (rs.next()) {
                    Timestamp loanedAt = rs.getTimestamp("loaned_at");
                    Date dueDate = rs.getDate("due_date");
                    Timestamp returnedAt = rs.getTimestamp("returned_at");
                    result.add(new LoanView(
                            rs.getLong("id"),
                            rs.getLong("book_id"),
                            rs.getString("title"),
                            LoanStatus.valueOf(rs.getString("status")),
                            loanedAt == null ? null : loanedAt.toLocalDateTime(),
                            dueDate == null ? null : dueDate.toLocalDate(),
                            returnedAt == null ? null : returnedAt.toLocalDateTime()));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения выдач пользователя", e);
        }
    }

    private static Loan map(ResultSet rs) throws SQLException {
        Timestamp loanedAt = rs.getTimestamp("loaned_at");
        Date dueDate = rs.getDate("due_date");
        Timestamp returnedAt = rs.getTimestamp("returned_at");
        return new Loan(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getLong("book_id"),
                LoanStatus.valueOf(rs.getString("status")),
                loanedAt == null ? null : loanedAt.toLocalDateTime(),
                dueDate == null ? null : dueDate.toLocalDate(),
                returnedAt == null ? null : returnedAt.toLocalDateTime());
    }
}