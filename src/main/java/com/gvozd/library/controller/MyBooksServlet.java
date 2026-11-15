package com.gvozd.library.controller;

import com.gvozd.library.dao.impl.JdbcLoanDao;
import com.gvozd.library.model.SessionUser;
import com.gvozd.library.service.LoanService;
import com.gvozd.library.util.Db;
import com.gvozd.library.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/my/books")
public class MyBooksServlet extends HttpServlet {

    private static final int MAX_OPEN_LOANS = 3;

    private final LoanService loanService =
            new LoanService(new JdbcLoanDao(Db.getDataSource()), MAX_OPEN_LOANS);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Flash.moveToRequest(req);

        SessionUser user = (SessionUser) req.getSession().getAttribute(LoginServlet.SESSION_USER);
        req.setAttribute("loans", loanService.getUserLoans(user.getId()));

        req.getRequestDispatcher("/WEB-INF/views/my-books.jsp").forward(req, resp);
    }
}