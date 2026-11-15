package com.gvozd.library.service;

import com.gvozd.library.dao.LoanDao;
import com.gvozd.library.dao.OrderResult;
import com.gvozd.library.exception.NotFoundException;
import com.gvozd.library.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    private static final int MAX_LOANS = 3;

    @Mock
    private LoanDao loanDao;

    private LoanService service;

    @BeforeEach
    void setUp() {
        service = new LoanService(loanDao, MAX_LOANS);
    }

    @Test
    void successfulOrderDoesNotThrow() {
        when(loanDao.placeOrder(1L, 2L, MAX_LOANS)).thenReturn(OrderResult.OK);

        assertDoesNotThrow(() -> service.orderBook(1L, 2L));
    }

    @Test
    void passesUserBookAndLimitToDao() {
        when(loanDao.placeOrder(5L, 7L, MAX_LOANS)).thenReturn(OrderResult.OK);

        service.orderBook(5L, 7L);

        verify(loanDao).placeOrder(5L, 7L, MAX_LOANS);
    }

    @Test
    void noCopiesBecomesValidationError() {
        when(loanDao.placeOrder(1L, 2L, MAX_LOANS)).thenReturn(OrderResult.NO_COPIES);

        assertThrows(ValidationException.class, () -> service.orderBook(1L, 2L));
    }

    @Test
    void alreadyOrderedBecomesValidationError() {
        when(loanDao.placeOrder(1L, 2L, MAX_LOANS)).thenReturn(OrderResult.ALREADY_ORDERED);

        assertThrows(ValidationException.class, () -> service.orderBook(1L, 2L));
    }

    @Test
    void limitReachedMentionsTheLimit() {
        when(loanDao.placeOrder(1L, 2L, MAX_LOANS)).thenReturn(OrderResult.LIMIT_REACHED);

        ValidationException e = assertThrows(ValidationException.class,
                () -> service.orderBook(1L, 2L));

        assertTrue(e.getMessage().contains(String.valueOf(MAX_LOANS)));
    }

    @Test
    void unknownBookBecomesNotFound() {
        when(loanDao.placeOrder(1L, 2L, MAX_LOANS)).thenReturn(OrderResult.BOOK_NOT_FOUND);

        assertThrows(NotFoundException.class, () -> service.orderBook(1L, 2L));
    }
}