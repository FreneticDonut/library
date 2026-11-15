package com.gvozd.library.model;

import java.time.LocalDate;

public class AdminLoanView {
    private final long loanId;
    private final String userName;
    private final String userEmail;
    private final String bookTitle;
    private final LoanStatus status;
    private final LocalDate dueDate;

    public AdminLoanView(long loanId, String userName, String userEmail, String bookTitle,
                         LoanStatus status, LocalDate dueDate) {
        this.loanId = loanId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.bookTitle = bookTitle;
        this.status = status;
        this.dueDate = dueDate;
    }

    public long getLoanId() { return loanId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getBookTitle() { return bookTitle; }
    public LoanStatus getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }

    public boolean isOverdue() {
        return status == LoanStatus.ACTIVE
                && dueDate != null
                && dueDate.isBefore(LocalDate.now());
    }
}