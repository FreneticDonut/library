package com.gvozd.library.filter;

import com.gvozd.library.controller.LoginServlet;
import com.gvozd.library.model.Role;
import com.gvozd.library.model.SessionUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private HttpSession session;

    private final AuthFilter filter = new AuthFilter();

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/login");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void sessionWithoutUserIsRedirectedToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(LoginServlet.SESSION_USER)).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/login");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void loggedInUserPassesThrough() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(LoginServlet.SESSION_USER))
                .thenReturn(new SessionUser(1L, "Иван", Role.READER));

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect("/login");
    }
}