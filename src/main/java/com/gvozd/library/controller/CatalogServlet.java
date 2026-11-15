package com.gvozd.library.controller;

import com.gvozd.library.dao.impl.JdbcBookDao;
import com.gvozd.library.service.BookService;
import com.gvozd.library.service.CatalogPage;
import com.gvozd.library.util.Db;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/catalog")
public class CatalogServlet extends HttpServlet {

    private static final int PAGE_SIZE = 3;

    private final BookService bookService =
            new BookService(new JdbcBookDao(Db.getDataSource()), PAGE_SIZE);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        moveFlashToRequest(req, AdminBookDeleteServlet.FLASH_MESSAGE);
        moveFlashToRequest(req, AdminBookDeleteServlet.FLASH_ERROR);

        CatalogPage result = bookService.getCatalogPage(
                req.getParameter("q"),
                parsePage(req.getParameter("page")));

        req.setAttribute("books", result.books());
        req.setAttribute("q", result.query());
        req.setAttribute("page", result.page());
        req.setAttribute("totalPages", result.totalPages());
        req.getRequestDispatcher("/WEB-INF/views/catalog.jsp").forward(req, resp);
    }

    private static void moveFlashToRequest(HttpServletRequest req, String name) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        Object value = session.getAttribute(name);
        if (value != null) {
            req.setAttribute(name, value);
            session.removeAttribute(name);
        }
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