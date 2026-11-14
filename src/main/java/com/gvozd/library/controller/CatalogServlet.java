package com.gvozd.library.controller;

import com.gvozd.library.dao.BookDao;
import com.gvozd.library.dao.impl.JdbcBookDao;
import com.gvozd.library.model.Book;
import com.gvozd.library.util.Db;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/catalog")
public class CatalogServlet extends HttpServlet {

    private static final int PAGE_SIZE = 3;

    private final BookDao bookDao = new JdbcBookDao(Db.getDataSource());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String query = req.getParameter("q");
        if (query == null) {
            query = "";
        }
        query = query.trim();

        long total = bookDao.count(query);
        int totalPages = (int) Math.max(1, Math.ceil((double) total / PAGE_SIZE));

        int page = parsePage(req.getParameter("page"));
        page = Math.min(Math.max(page, 1), totalPages);
        int offset = (page - 1) * PAGE_SIZE;

        List<Book> books = bookDao.findPage(query, PAGE_SIZE, offset);

        req.setAttribute("books", books);
        req.setAttribute("q", query);
        req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages);
        req.getRequestDispatcher("/WEB-INF/views/catalog.jsp").forward(req, resp);
    }

    private static int parsePage(String value) {
        if (value == null) {
            return 1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}