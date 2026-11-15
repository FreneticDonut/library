package com.gvozd.library.dao;

import com.gvozd.library.dao.impl.JdbcBookDao;
import com.gvozd.library.dao.impl.JdbcLoanDao;
import com.gvozd.library.dao.impl.JdbcUserDao;
import com.gvozd.library.model.Book;
import com.gvozd.library.model.LoanStatus;
import com.gvozd.library.model.LoanView;
import com.gvozd.library.model.Role;
import com.gvozd.library.model.User;
import com.gvozd.library.util.Db;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JdbcLoanCancelTest {

    private static final AtomicLong COUNTER = new AtomicLong(System.nanoTime());

    private static BookDao bookDao;
    private static UserDao userDao;
    private static LoanDao loanDao;

    @BeforeAll
    static void setUp() {
        assumeTrue(System.getenv("DB_URL") != null, "DB_URL не задан, тест пропущен");
        bookDao = new JdbcBookDao(Db.getDataSource());
        userDao = new JdbcUserDao(Db.getDataSource());
        loanDao = new JdbcLoanDao(Db.getDataSource());
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        if (loanDao == null) {
            return;
        }
        try (Connection c = Db.getDataSource().getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM loans WHERE user_id IN "
                    + "(SELECT id FROM users WHERE email LIKE 'cancel-test-%@example.com')");
            st.executeUpdate("DELETE FROM loans WHERE book_id IN "
                    + "(SELECT id FROM books WHERE title LIKE 'CancelTest %')");
            st.executeUpdate("DELETE FROM users WHERE email LIKE 'cancel-test-%@example.com'");
            st.executeUpdate("DELETE FROM books WHERE title LIKE 'CancelTest %'");
        }
    }

    private static User createUser() {
        String email = "cancel-test-" + COUNTER.incrementAndGet() + "@example.com";
        return userDao.save(new User(null, email, "hash", "Тест", Role.READER));
    }

    private static Book createBook(int copies) {
        String title = "CancelTest " + COUNTER.incrementAndGet();
        return bookDao.save(new Book(null, title, null, 2020, copies, copies));
    }

    private static int available(Book book) {
        return bookDao.findById(book.getId()).orElseThrow().getAvailableCopies();
    }

    private static long orderAndGetLoanId(User user, Book book) {
        loanDao.placeOrder(user.getId(), book.getId(), 3);
        return loanDao.findByUser(user.getId()).get(0).getId();
    }

    private static void markAsIssued(long loanId) throws SQLException {
        String sql = "UPDATE loans SET status = 'ACTIVE', loaned_at = now(), "
                + "due_date = current_date + 14 WHERE id = ?";
        try (Connection c = Db.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, loanId);
            ps.executeUpdate();
        }
    }

    @Test
    void cancelRemovesLoanAndReturnsCopy() {
        User user = createUser();
        Book book = createBook(2);
        long loanId = orderAndGetLoanId(user, book);
        assertEquals(1, available(book));

        CancelResult result = loanDao.cancelOrder(user.getId(), loanId);

        assertEquals(CancelResult.OK, result);
        assertEquals(2, available(book));
        assertTrue(loanDao.findByUser(user.getId()).isEmpty());
    }

    @Test
    void issuedLoanCannotBeCancelled() throws SQLException {
        User user = createUser();
        Book book = createBook(2);
        long loanId = orderAndGetLoanId(user, book);
        markAsIssued(loanId);

        CancelResult result = loanDao.cancelOrder(user.getId(), loanId);

        assertEquals(CancelResult.NOT_CANCELLABLE, result);
        assertEquals(1, available(book));
        assertEquals(1, loanDao.findByUser(user.getId()).size());
    }

    @Test
    void someoneElsesOrderCannotBeCancelled() {
        User owner = createUser();
        User stranger = createUser();
        Book book = createBook(2);
        long loanId = orderAndGetLoanId(owner, book);

        CancelResult result = loanDao.cancelOrder(stranger.getId(), loanId);

        assertEquals(CancelResult.NOT_FOUND, result);
        assertEquals(1, available(book));
        assertEquals(1, loanDao.findByUser(owner.getId()).size());
    }

    @Test
    void unknownLoanIsReported() {
        User user = createUser();

        assertEquals(CancelResult.NOT_FOUND, loanDao.cancelOrder(user.getId(), -1L));
    }

    @Test
    void cancellingTwiceDoesNotReturnCopyTwice() {
        User user = createUser();
        Book book = createBook(2);
        long loanId = orderAndGetLoanId(user, book);

        assertEquals(CancelResult.OK, loanDao.cancelOrder(user.getId(), loanId));
        assertEquals(CancelResult.NOT_FOUND, loanDao.cancelOrder(user.getId(), loanId));

        assertEquals(2, available(book));
    }

    @Test
    void viewsContainBookTitleAndStatus() {
        User user = createUser();
        Book book = createBook(1);
        orderAndGetLoanId(user, book);

        List<LoanView> views = loanDao.findViewsByUser(user.getId());

        assertEquals(1, views.size());
        assertEquals(book.getTitle(), views.get(0).getBookTitle());
        assertEquals(LoanStatus.ORDERED, views.get(0).getStatus());
    }
}