package com.gvozd.library.controller;

import com.gvozd.library.dao.BookDao;
import com.gvozd.library.dao.impl.JdbcBookDao;
import com.gvozd.library.model.Book;
import com.gvozd.library.util.Db;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/ping")
public class BooksPingServlet extends HttpServlet {

    private final BookDao bookDao = new JdbcBookDao(Db.getDataSource());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        List<Book> books = bookDao.findPage("", 10, 0);
        for (Book book : books) {
            out.println(book);
        }
    }
}