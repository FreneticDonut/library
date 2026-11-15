package com.gvozd.library.controller;

import com.gvozd.library.dao.impl.JdbcBookDao;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.service.BookService;
import com.gvozd.library.util.Db;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/books/new")
public class AdminBookCreateServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    private final BookService bookService =
            new BookService(new JdbcBookDao(Db.getDataSource()), PAGE_SIZE);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/admin-book-form.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String title = req.getParameter("title");
        String isbn = req.getParameter("isbn");
        String publishYear = req.getParameter("publishYear");
        String totalCopies = req.getParameter("totalCopies");

        try {
            bookService.addBook(title, isbn, publishYear, totalCopies);
            resp.sendRedirect(req.getContextPath() + "/admin/books/new?added=1");
        } catch (ValidationException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("title", title);
            req.setAttribute("isbn", isbn);
            req.setAttribute("publishYear", publishYear);
            req.setAttribute("totalCopies", totalCopies);
            req.getRequestDispatcher("/WEB-INF/views/admin-book-form.jsp").forward(req, resp);
        }
    }
}