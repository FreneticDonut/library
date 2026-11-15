package com.gvozd.library.controller;

import com.gvozd.library.dao.impl.JdbcBookDao;
import com.gvozd.library.exception.NotFoundException;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.service.BookService;
import com.gvozd.library.util.Db;
import com.gvozd.library.util.Flash;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/books/delete")
public class AdminBookDeleteServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    private final BookService bookService =
            new BookService(new JdbcBookDao(Db.getDataSource()), PAGE_SIZE);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            bookService.deleteBook(parseId(req.getParameter("id")));
            Flash.success(req, "Книга удалена");
        } catch (ValidationException e) {
            Flash.error(req, e.getMessage());
        } catch (NotFoundException e) {
            Flash.error(req, "Книга не найдена");
        }
        resp.sendRedirect(req.getContextPath() + "/catalog");
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