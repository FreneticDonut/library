package com.gvozd.library.dao;

import com.gvozd.library.dao.impl.JdbcBookDao;
import com.gvozd.library.dao.impl.JdbcLoanDao;
import com.gvozd.library.dao.impl.JdbcUserDao;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JdbcLoanDaoTest {

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
                    + "(SELECT id FROM users WHERE email LIKE 'loan-test-%@example.com')");
            st.executeUpdate("DELETE FROM loans WHERE book_id IN "
                    + "(SELECT id FROM books WHERE title LIKE 'LoanTest %')");
            st.executeUpdate("DELETE FROM users WHERE email LIKE 'loan-test-%@example.com'");
            st.executeUpdate("DELETE FROM books WHERE title LIKE 'LoanTest %'");
        }
    }

    private static User createUser() {
        String email = "loan-test-" + COUNTER.incrementAndGet() + "@example.com";
        return userDao.save(new User(null, email, "hash", "Тест", Role.READER));
    }

    private static Book createBook(int copies) {
        String title = "LoanTest " + COUNTER.incrementAndGet();
        return bookDao.save(new Book(null, title, null, 2020, copies, copies));
    }

    private static int available(Book book) {
        return bookDao.findById(book.getId()).orElseThrow().getAvailableCopies();
    }

    @Test
    void orderCreatesLoanAndReservesCopy() {
        User user = createUser();
        Book book = createBook(2);

        OrderResult result = loanDao.placeOrder(user.getId(), book.getId(), 3);

        assertEquals(OrderResult.OK, result);
        assertEquals(1, available(book));

        List<Loan> loans = loanDao.findByUser(user.getId());
        assertEquals(1, loans.size());
        assertEquals(LoanStatus.ORDERED, loans.get(0).getStatus());
        assertEquals(book.getId(), loans.get(0).getBookId());
    }

    @Test
    void orderFailsWhenNoCopiesLeft() {
        User first = createUser();
        User second = createUser();
        Book book = createBook(1);

        assertEquals(OrderResult.OK, loanDao.placeOrder(first.getId(), book.getId(), 3));
        assertEquals(OrderResult.NO_COPIES, loanDao.placeOrder(second.getId(), book.getId(), 3));

        assertEquals(0, available(book));
        assertEquals(0, loanDao.findByUser(second.getId()).size());
    }

    @Test
    void sameBookCannotBeOrderedTwice() {
        User user = createUser();
        Book book = createBook(5);

        assertEquals(OrderResult.OK, loanDao.placeOrder(user.getId(), book.getId(), 3));
        assertEquals(OrderResult.ALREADY_ORDERED, loanDao.placeOrder(user.getId(), book.getId(), 3));

        assertEquals(4, available(book));
        assertEquals(1, loanDao.findByUser(user.getId()).size());
    }

    @Test
    void limitOfOpenLoansIsEnforced() {
        User user = createUser();
        Book first = createBook(1);
        Book second = createBook(1);
        Book third = createBook(1);

        assertEquals(OrderResult.OK, loanDao.placeOrder(user.getId(), first.getId(), 2));
        assertEquals(OrderResult.OK, loanDao.placeOrder(user.getId(), second.getId(), 2));
        assertEquals(OrderResult.LIMIT_REACHED, loanDao.placeOrder(user.getId(), third.getId(), 2));

        assertEquals(1, available(third));
        assertEquals(2, loanDao.findByUser(user.getId()).size());
    }

    @Test
    void unknownBookIsReported() {
        User user = createUser();

        assertEquals(OrderResult.BOOK_NOT_FOUND, loanDao.placeOrder(user.getId(), -1L, 3));
    }

    @Test
    void onlyOneUserGetsTheLastCopyWhenOrderingConcurrently() throws Exception {
        Book book = createBook(1);
        int usersCount = 4;
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < usersCount; i++) {
            userIds.add(createUser().getId());
        }

        ExecutorService pool = Executors.newFixedThreadPool(usersCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<OrderResult>> futures = new ArrayList<>();
        for (long userId : userIds) {
            futures.add(pool.submit(() -> {
                start.await();
                return loanDao.placeOrder(userId, book.getId(), 3);
            }));
        }
        start.countDown();

        int successes = 0;
        for (Future<OrderResult> future : futures) {
            if (future.get(30, TimeUnit.SECONDS) == OrderResult.OK) {
                successes++;
            }
        }
        pool.shutdown();

        assertEquals(1, successes);
        assertEquals(0, available(book));
    }
}