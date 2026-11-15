package com.gvozd.library.controller;

import com.gvozd.library.dao.impl.JdbcLoanDao;
import com.gvozd.library.exception.NotFoundException;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.service.LoanService;
import com.gvozd.library.util.Db;
import com.gvozd.library.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/loans")
public class AdminLoansServlet extends HttpServlet {

    private static final int MAX_OPEN_LOANS = 3;

    private final LoanService loanService =
            new LoanService(new JdbcLoanDao(Db.getDataSource()), MAX_OPEN_LOANS);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Flash.moveToRequest(req);

        req.setAttribute("orders", loanService.getOrders());
        req.setAttribute("active", loanService.getActiveLoans());
        req.getRequestDispatcher("/WEB-INF/views/admin-loans.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        long loanId = parseLong(req.getParameter("loanId"));

        try {
            if ("issue".equals(action)) {
                loanService.issueLoan(loanId);
                Flash.success(req, "Книга выдана. Срок возврата: " + LoanService.LOAN_DAYS + " дней");
            } else if ("return".equals(action)) {
                loanService.acceptReturn(loanId);
                Flash.success(req, "Возврат принят");
            } else {
                Flash.error(req, "Неизвестное действие");
            }
        } catch (ValidationException | NotFoundException e) {
            Flash.error(req, e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/loans");
    }

    private static long parseLong(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}