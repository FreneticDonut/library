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

    private final BookDao bookDao = new JdbcBookDao(Db.getDataSource());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Book> books = bookDao.findPage("", 10, 0);

        req.setAttribute("books", books);
        req.getRequestDispatcher("/WEB-INF/views/catalog.jsp").forward(req, resp);
    }
}