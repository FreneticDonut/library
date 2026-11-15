package com.gvozd.library.filter;

import com.gvozd.library.controller.LoginServlet;
import com.gvozd.library.model.Role;
import com.gvozd.library.model.SessionUser;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter("/admin/*")
public class RoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        Object attribute = (session == null) ? null : session.getAttribute(LoginServlet.SESSION_USER);

        if (!(attribute instanceof SessionUser user)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (user.getRole() != Role.LIBRARIAN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }
}