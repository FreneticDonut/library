package com.gvozd.library.service;

import com.gvozd.library.dao.CancelResult;
import com.gvozd.library.dao.LoanDao;
import com.gvozd.library.exception.NotFoundException;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.model.LoanStatus;
import com.gvozd.library.model.LoanView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceCancelTest {

    @Mock
    private LoanDao loanDao;

    private LoanService service;

    @BeforeEach
    void setUp() {
        service = new LoanService(loanDao, 3);
    }

    @Test
    void successfulCancelDoesNotThrow() {
        when(loanDao.cancelOrder(1L, 10L)).thenReturn(CancelResult.OK);

        assertDoesNotThrow(() -> service.cancelOrder(1L, 10L));
        verify(loanDao).cancelOrder(1L, 10L);
    }

    @Test
    void unknownLoanBecomesNotFound() {
        when(loanDao.cancelOrder(1L, 10L)).thenReturn(CancelResult.NOT_FOUND);

        assertThrows(NotFoundException.class, () -> service.cancelOrder(1L, 10L));
    }

    @Test
    void issuedLoanBecomesValidationError() {
        when(loanDao.cancelOrder(1L, 10L)).thenReturn(CancelResult.NOT_CANCELLABLE);

        assertThrows(ValidationException.class, () -> service.cancelOrder(1L, 10L));
    }

    @Test
    void userLoansAreReturnedFromDao() {
        List<LoanView> views = List.of(
                new LoanView(10L, 2L, "Книга", LoanStatus.ORDERED, null, null, null));
        when(loanDao.findViewsByUser(1L)).thenReturn(views);

        assertEquals(views, service.getUserLoans(1L));
    }
}