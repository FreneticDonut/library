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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/my/orders")
public class OrderServlet extends HttpServlet {

    private static final int MAX_OPEN_LOANS = 3;

    private final LoanService loanService =
            new LoanService(new JdbcLoanDao(Db.getDataSource()), MAX_OPEN_LOANS);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser user = (SessionUser) req.getSession().getAttribute(LoginServlet.SESSION_USER);

        try {
            loanService.orderBook(user.getId(), parseLong(req.getParameter("bookId")));
            Flash.success(req, "Книга заказана. Заберите её у библиотекаря");
        } catch (ValidationException e) {
            Flash.error(req, e.getMessage());
        } catch (NotFoundException e) {
            Flash.error(req, e.getMessage());
        }

        resp.sendRedirect(catalogUrl(req));
    }

    private static String catalogUrl(HttpServletRequest req) {
        String q = req.getParameter("q");
        String encodedQuery = URLEncoder.encode(q == null ? "" : q, StandardCharsets.UTF_8);
        long page = Math.max(1, parseLong(req.getParameter("page")));
        return req.getContextPath() + "/catalog?q=" + encodedQuery + "&page=" + page;
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