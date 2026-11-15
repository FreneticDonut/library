package com.gvozd.library.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoanView {
    private final long loanId;
    private final long bookId;
    private final String bookTitle;
    private final LoanStatus status;
    private final LocalDateTime loanedAt;
    private final LocalDate dueDate;
    private final LocalDateTime returnedAt;

    public LoanView(long loanId, long bookId, String bookTitle, LoanStatus status,
                    LocalDateTime loanedAt, LocalDate dueDate, LocalDateTime returnedAt) {
        this.loanId = loanId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.status = status;
        this.loanedAt = loanedAt;
        this.dueDate = dueDate;
        this.returnedAt = returnedAt;
    }

    public long getLoanId() { return loanId; }
    public long getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public LoanStatus getStatus() { return status; }
    public LocalDateTime getLoanedAt() { return loanedAt; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
}