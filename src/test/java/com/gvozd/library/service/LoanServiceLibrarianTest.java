package com.gvozd.library.service;

import com.gvozd.library.dao.LoanActionResult;
import com.gvozd.library.dao.LoanDao;
import com.gvozd.library.exception.NotFoundException;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.model.AdminLoanView;
import com.gvozd.library.model.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceLibrarianTest {

    @Mock
    private LoanDao loanDao;

    private LoanService service;

    @BeforeEach
    void setUp() {
        service = new LoanService(loanDao, 3);
    }

    @Test
    void issueUsesStandardLoanPeriod() {
        when(loanDao.issue(5L, LoanService.LOAN_DAYS)).thenReturn(LoanActionResult.OK);

        assertDoesNotThrow(() -> service.issueLoan(5L));
        verify(loanDao).issue(5L, 14);
    }

    @Test
    void issueOfUnknownLoanBecomesNotFound() {
        when(loanDao.issue(5L, LoanService.LOAN_DAYS)).thenReturn(LoanActionResult.NOT_FOUND);

        assertThrows(NotFoundException.class, () -> service.issueLoan(5L));
    }

    @Test
    void issueOfWrongStatusBecomesValidationError() {
        when(loanDao.issue(5L, LoanService.LOAN_DAYS)).thenReturn(LoanActionResult.WRONG_STATUS);

        assertThrows(ValidationException.class, () -> service.issueLoan(5L));
    }

    @Test
    void successfulReturnDoesNotThrow() {
        when(loanDao.returnBook(5L)).thenReturn(LoanActionResult.OK);

        assertDoesNotThrow(() -> service.acceptReturn(5L));
        verify(loanDao).returnBook(5L);
    }

    @Test
    void returnOfUnknownLoanBecomesNotFound() {
        when(loanDao.returnBook(5L)).thenReturn(LoanActionResult.NOT_FOUND);

        assertThrows(NotFoundException.class, () -> service.acceptReturn(5L));
    }

    @Test
    void returnOfWrongStatusBecomesValidationError() {
        when(loanDao.returnBook(5L)).thenReturn(LoanActionResult.WRONG_STATUS);

        assertThrows(ValidationException.class, () -> service.acceptReturn(5L));
    }

    @Test
    void ordersAreLoadedByOrderedStatus() {
        List<AdminLoanView> views = List.of(
                new AdminLoanView(1L, "Иван", "ivan@mail.ru", "Книга", LoanStatus.ORDERED, null));
        when(loanDao.findByStatus(LoanStatus.ORDERED)).thenReturn(views);

        assertEquals(views, service.getOrders());
    }

    @Test
    void activeLoansAreLoadedByActiveStatus() {
        List<AdminLoanView> views = List.of(
                new AdminLoanView(1L, "Иван", "ivan@mail.ru", "Книга", LoanStatus.ACTIVE,
                        LocalDate.now().plusDays(3)));
        when(loanDao.findByStatus(LoanStatus.ACTIVE)).thenReturn(views);

        assertEquals(views, service.getActiveLoans());
    }

    @Test
    void loanIsOverdueOnlyWhenActiveAndPastDueDate() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        assertTrue(new AdminLoanView(1L, "И", "e", "К", LoanStatus.ACTIVE, yesterday).isOverdue());
        assertFalse(new AdminLoanView(1L, "И", "e", "К", LoanStatus.ACTIVE, tomorrow).isOverdue());
        assertFalse(new AdminLoanView(1L, "И", "e", "К", LoanStatus.ORDERED, null).isOverdue());
        assertFalse(new AdminLoanView(1L, "И", "e", "К", LoanStatus.RETURNED, yesterday).isOverdue());
    }
}