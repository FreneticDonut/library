package com.gvozd.library.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Loan {
    private Long id;
    private long userId;
    private long bookId;
    private LoanStatus status;
    private LocalDateTime loanedAt;
    private LocalDate dueDate;
    private LocalDateTime returnedAt;

    public Loan() {}

    public Loan(Long id, long userId, long bookId, LoanStatus status,
                LocalDateTime loanedAt, LocalDate dueDate, LocalDateTime returnedAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.status = status;
        this.loanedAt = loanedAt;
        this.dueDate = dueDate;
        this.returnedAt = returnedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getBookId() { return bookId; }
    public void setBookId(long bookId) { this.bookId = bookId; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    public LocalDateTime getLoanedAt() { return loanedAt; }
    public void setLoanedAt(LocalDateTime loanedAt) { this.loanedAt = loanedAt; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }

    @Override
    public String toString() {
        return "Loan{id=" + id + ", userId=" + userId + ", bookId=" + bookId + ", status=" + status + "}";
    }
}