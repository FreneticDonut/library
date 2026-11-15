package com.gvozd.library.service;

import com.gvozd.library.dao.LoanDao;
import com.gvozd.library.dao.OrderResult;
import com.gvozd.library.exception.NotFoundException;
import com.gvozd.library.exception.ValidationException;

public class LoanService {

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
}