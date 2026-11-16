package com.gvozd.library.controller;

import com.gvozd.library.dao.impl.JdbcUserDao;
import com.gvozd.library.exception.AuthException;
import com.gvozd.library.model.SessionUser;
import com.gvozd.library.model.User;
import com.gvozd.library.service.AuthService;
import com.gvozd.library.util.Db;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(LoginServlet.class);
    public static final String SESSION_USER = "currentUser";

    private final AuthService authService =
            new AuthService(new JdbcUserDao(Db.getDataSource()));

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            User user = authService.login(email, password);

            HttpSession old = req.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            HttpSession session = req.getSession(true);
            session.setAttribute(SESSION_USER, SessionUser.from(user));

            resp.sendRedirect(req.getContextPath() + "/catalog");
        } catch (AuthException e) {
            LOG.warn("Failed login attempt from {}", req.getRemoteAddr());
            req.setAttribute("error", e.getMessage());
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }
}