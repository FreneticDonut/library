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
class RoleFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private HttpSession session;

    private final RoleFilter filter = new RoleFilter();

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/login");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void readerGetsForbidden() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(LoginServlet.SESSION_USER))
                .thenReturn(new SessionUser(1L, "Иван", Role.READER));

        filter.doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void librarianPassesThrough() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(LoginServlet.SESSION_USER))
                .thenReturn(new SessionUser(2L, "Мария", Role.LIBRARIAN));

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}