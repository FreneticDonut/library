package com.gvozd.library.service;

import com.gvozd.library.dao.CancelResult;
import com.gvozd.library.dao.LoanActionResult;
import com.gvozd.library.dao.LoanDao;
import com.gvozd.library.dao.OrderResult;
import com.gvozd.library.exception.NotFoundException;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.model.AdminLoanView;
import com.gvozd.library.model.LoanStatus;
import com.gvozd.library.model.LoanView;

import java.util.List;

public class LoanService {

    public static final int LOAN_DAYS = 14;

    private final LoanDao loanDao;
    private final int maxOpenLoans;

    public LoanService(LoanDao loanDao, int maxOpenLoans) {
        this.loanDao = loanDao;
        this.maxOpenLoans = maxOpenLoans;
    }

    public void orderBook(long userId, long bookId) {
        OrderResult result = loanDao.placeOrder(userId, bookId, maxOpenLoans);

        switch (result) {
            case OK -> { }
            case BOOK_NOT_FOUND -> throw new NotFoundException("Книга не найдена");
            case NO_COPIES -> throw new ValidationException("Свободных экземпляров этой книги нет");
            case ALREADY_ORDERED -> throw new ValidationException("Вы уже заказали эту книгу");
            case LIMIT_REACHED -> throw new ValidationException(
                    "Нельзя иметь больше " + maxOpenLoans + " книг одновременно");
        }
    }

    public void cancelOrder(long userId, long loanId) {
        CancelResult result = loanDao.cancelOrder(userId, loanId);

        switch (result) {
            case OK -> { }
            case NOT_FOUND -> throw new NotFoundException("Заказ не найден");
            case NOT_CANCELLABLE -> throw new ValidationException(
                    "Отменить можно только заказ, который ещё не выдан");
        }
    }

    public void issueLoan(long loanId) {
        LoanActionResult result = loanDao.issue(loanId, LOAN_DAYS);

        switch (result) {
            case OK -> { }
            case NOT_FOUND -> throw new NotFoundException("Заказ не найден. Возможно, читатель его отменил");
            case WRONG_STATUS -> throw new ValidationException("Выдать можно только заказанную книгу");
        }
    }

    public void acceptReturn(long loanId) {
        LoanActionResult result = loanDao.returnBook(loanId);

        switch (result) {
            case OK -> { }
            case NOT_FOUND -> throw new NotFoundException("Выдача не найдена");
            case WRONG_STATUS -> throw new ValidationException("Принять можно только книгу, которая на руках");
        }
    }

    public List<LoanView> getUserLoans(long userId) {
        return loanDao.findViewsByUser(userId);
    }

    public List<AdminLoanView> getOrders() {
        return loanDao.findByStatus(LoanStatus.ORDERED);
    }

    public List<AdminLoanView> getActiveLoans() {
        return loanDao.findByStatus(LoanStatus.ACTIVE);
    }
}