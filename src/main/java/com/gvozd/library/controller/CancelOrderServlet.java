package com.gvozd.library.controller;

import com.gvozd.library.dao.impl.JdbcLoanDao;
import com.gvozd.library.exception.NotFoundException;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.model.SessionUser;
import com.gvozd.library.service.LoanService;
import com.gvozd.library.util.Db;
import com.gvozd.library.util.Flash;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/my/orders/cancel")
public class CancelOrderServlet extends HttpServlet {

    private static final int MAX_OPEN_LOANS = 3;

    private final LoanService loanService =
            new LoanService(new JdbcLoanDao(Db.getDataSource()), MAX_OPEN_LOANS);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser user = (SessionUser) req.getSession().getAttribute(LoginServlet.SESSION_USER);

        try {
            loanService.cancelOrder(user.getId(), parseLong(req.getParameter("loanId")));
            Flash.success(req, "Заказ отменён");
        } catch (ValidationException | NotFoundException e) {
            Flash.error(req, e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/my/books");
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