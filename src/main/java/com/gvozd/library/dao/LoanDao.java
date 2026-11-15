package com.gvozd.library.dao;

import com.gvozd.library.model.Loan;

import java.util.List;

public interface LoanDao {

    OrderResult placeOrder(long userId, long bookId, int maxOpenLoans);

    List<Loan> findByUser(long userId);
}