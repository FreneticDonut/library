package com.gvozd.library.controller;

import com.gvozd.library.dao.impl.JdbcBookDao;
import com.gvozd.library.exception.NotFoundException;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.model.Book;
import com.gvozd.library.service.BookService;
import com.gvozd.library.util.Db;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/books/edit")
public class AdminBookEditServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    private final BookService bookService =
            new BookService(new JdbcBookDao(Db.getDataSource()), PAGE_SIZE);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long id = parseId(req.getParameter("id"));
        try {
            Book book = bookService.getBook(id);
            fillForm(req, id,
                    book.getTitle(),
                    book.getIsbn() == null ? "" : book.getIsbn(),
                    book.getPublishYear() == null ? "" : String.valueOf(book.getPublishYear()),
                    String.valueOf(book.getTotalCopies()));
            req.getRequestDispatcher("/WEB-INF/views/admin-book-form.jsp").forward(req, resp);
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long id = parseId(req.getParameter("id"));
        String title = req.getParameter("title");
        String isbn = req.getParameter("isbn");
        String publishYear = req.getParameter("publishYear");
        String totalCopies = req.getParameter("totalCopies");

        try {
            bookService.updateBook(id, title, isbn, publishYear, totalCopies);
            resp.sendRedirect(req.getContextPath() + "/admin/books/edit?id=" + id + "&saved=1");
        } catch (ValidationException e) {
            req.setAttribute("error", e.getMessage());
            fillForm(req, id, title, isbn, publishYear, totalCopies);
            req.getRequestDispatcher("/WEB-INF/views/admin-book-form.jsp").forward(req, resp);
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static void fillForm(HttpServletRequest req, long id, String title, String isbn,
                                 String publishYear, String totalCopies) {
        req.setAttribute("bookId", id);
        req.setAttribute("title", title);
        req.setAttribute("isbn", isbn);
        req.setAttribute("publishYear", publishYear);
        req.setAttribute("totalCopies", totalCopies);
    }

    private static long parseId(String value) {
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