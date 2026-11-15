package com.gvozd.library.controller;

import com.gvozd.library.dao.impl.JdbcUserDao;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.service.AuthService;
import com.gvozd.library.util.Db;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService =
            new AuthService(new JdbcUserDao(Db.getDataSource()));

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String email = req.getParameter("email");
        String name = req.getParameter("name");
        String password = req.getParameter("password");

        try {
            authService.register(email, password, name);
            resp.sendRedirect(req.getContextPath() + "/register?registered=1");
        } catch (ValidationException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("email", email);
            req.setAttribute("name", name);
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        }
    }
}