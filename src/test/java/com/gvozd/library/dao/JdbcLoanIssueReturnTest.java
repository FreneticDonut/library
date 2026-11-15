package com.gvozd.library.dao;

import com.gvozd.library.dao.impl.JdbcBookDao;
import com.gvozd.library.dao.impl.JdbcLoanDao;
import com.gvozd.library.dao.impl.JdbcUserDao;
import com.gvozd.library.model.AdminLoanView;
import com.gvozd.library.model.Book;
import com.gvozd.library.model.Loan;
import com.gvozd.library.model.LoanStatus;
import com.gvozd.library.model.Role;
import com.gvozd.library.model.User;
import com.gvozd.library.util.Db;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JdbcLoanIssueReturnTest {

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
                    + "(SELECT id FROM users WHERE email LIKE 'issue-test-%@example.com')");
            st.executeUpdate("DELETE FROM loans WHERE book_id IN "
                    + "(SELECT id FROM books WHERE title LIKE 'IssueTest %')");
            st.executeUpdate("DELETE FROM users WHERE email LIKE 'issue-test-%@example.com'");
            st.executeUpdate("DELETE FROM books WHERE title LIKE 'IssueTest %'");
        }
    }

    private static User createUser() {
        String email = "issue-test-" + COUNTER.incrementAndGet() + "@example.com";
        return userDao.save(new User(null, email, "hash", "Тест", Role.READER));
    }

    private static Book createBook(int copies) {
        String title = "IssueTest " + COUNTER.incrementAndGet();
        return bookDao.save(new Book(null, title, null, 2020, copies, copies));
    }

    private static int available(Book book) {
        return bookDao.findById(book.getId()).orElseThrow().getAvailableCopies();
    }

    private static Loan loanOf(User user) {
        return loanDao.findByUser(user.getId()).get(0);
    }

    private static long order(User user, Book book) {
        loanDao.placeOrder(user.getId(), book.getId(), 3);
        return loanOf(user).getId();
    }

    @Test
    void issueMakesLoanActiveWithDueDate() {
        User user = createUser();
        Book book = createBook(2);
        long loanId = order(user, book);

        LoanActionResult result = loanDao.issue(loanId, 14);

        assertEquals(LoanActionResult.OK, result);
        Loan loan = loanOf(user);
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        assertNotNull(loan.getLoanedAt());

        // дата считается на стороне БД, а её часовой пояс может отличаться от нашего,
        // поэтому допускаем отклонение в сутки
        LocalDate expected = LocalDate.now().plusDays(14);
        assertFalse(loan.getDueDate().isBefore(expected.minusDays(1)));
        assertFalse(loan.getDueDate().isAfter(expected.plusDays(1)));
    }

    @Test
    void issueDoesNotChangeAvailableCopies() {
        User user = createUser();
        Book book = createBook(2);
        long loanId = order(user, book);
        assertEquals(1, available(book));

        loanDao.issue(loanId, 14);

        assertEquals(1, available(book));
    }

    @Test
    void issueTwiceIsRejected() {
        User user = createUser();
        long loanId = order(user, createBook(2));

        assertEquals(LoanActionResult.OK, loanDao.issue(loanId, 14));
        assertEquals(LoanActionResult.WRONG_STATUS, loanDao.issue(loanId, 14));
    }

    @Test
    void issueOfUnknownLoanIsReported() {
        assertEquals(LoanActionResult.NOT_FOUND, loanDao.issue(-1L, 14));
    }

    @Test
    void returnRestoresCopyAndClosesLoan() {
        User user = createUser();
        Book book = createBook(2);
        long loanId = order(user, book);
        loanDao.issue(loanId, 14);
        assertEquals(1, available(book));

        LoanActionResult result = loanDao.returnBook(loanId);

        assertEquals(LoanActionResult.OK, result);
        assertEquals(2, available(book));
        Loan loan = loanOf(user);
        assertEquals(LoanStatus.RETURNED, loan.getStatus());
        assertNotNull(loan.getReturnedAt());
    }

    @Test
    void returnOfNotIssuedLoanIsRejectedAndCopyStaysReserved() {
        User user = createUser();
        Book book = createBook(2);
        long loanId = order(user, book);

        LoanActionResult result = loanDao.returnBook(loanId);

        assertEquals(LoanActionResult.WRONG_STATUS, result);
        assertEquals(1, available(book));
        assertEquals(LoanStatus.ORDERED, loanOf(user).getStatus());
    }

    @Test
    void returnTwiceDoesNotRestoreCopyTwice() {
        User user = createUser();
        Book book = createBook(2);
        long loanId = order(user, book);
        loanDao.issue(loanId, 14);

        assertEquals(LoanActionResult.OK, loanDao.returnBook(loanId));
        assertEquals(LoanActionResult.WRONG_STATUS, loanDao.returnBook(loanId));

        assertEquals(2, available(book));
    }

    @Test
    void returnOfUnknownLoanIsReported() {
        assertEquals(LoanActionResult.NOT_FOUND, loanDao.returnBook(-1L));
    }

    @Test
    void readerCanOrderAgainAfterReturn() {
        User user = createUser();
        Book book = createBook(1);
        long loanId = order(user, book);
        loanDao.issue(loanId, 14);
        loanDao.returnBook(loanId);

        assertEquals(OrderResult.OK, loanDao.placeOrder(user.getId(), book.getId(), 3));
    }

    @Test
    void listsContainUserAndBookNames() {
        User user = createUser();
        Book book = createBook(2);
        long loanId = order(user, book);

        List<AdminLoanView> orders = loanDao.findByStatus(LoanStatus.ORDERED);
        assertTrue(orders.stream().anyMatch(v ->
                v.getLoanId() == loanId
                        && v.getUserEmail().equals(user.getEmail())
                        && v.getBookTitle().equals(book.getTitle())));

        loanDao.issue(loanId, 14);

        List<AdminLoanView> active = loanDao.findByStatus(LoanStatus.ACTIVE);
        assertTrue(active.stream().anyMatch(v -> v.getLoanId() == loanId && v.getDueDate() != null));
        assertFalse(loanDao.findByStatus(LoanStatus.ORDERED).stream()
                .anyMatch(v -> v.getLoanId() == loanId));
    }
}