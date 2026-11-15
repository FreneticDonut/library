package com.gvozd.library.dao;

import com.gvozd.library.model.Loan;
import com.gvozd.library.model.LoanView;

import java.util.List;

public interface LoanDao {

    OrderResult placeOrder(long userId, long bookId, int maxOpenLoans);

    CancelResult cancelOrder(long userId, long loanId);

    List<Loan> findByUser(long userId);

    List<LoanView> findViewsByUser(long userId);
}