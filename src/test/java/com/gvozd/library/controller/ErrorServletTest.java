package com.gvozd.library.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorServletTest {

    private static final String ERROR_PAGE = "/WEB-INF/views/error.jsp";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    private final ErrorServlet servlet = new ErrorServlet();

    @Test
    void keepsStatusAndForwardsToErrorPage() throws Exception {
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(404);
        when(request.getRequestDispatcher(ERROR_PAGE)).thenReturn(dispatcher);

        servlet.service(request, response);

        verify(response).setStatus(404);
        verify(request).setAttribute("errorStatus", 404);
        verify(request).setAttribute("errorTitle", "Страница не найдена");
        verify(dispatcher).forward(request, response);
    }

    @Test
    void directAccessWithoutErrorIsNotFound() throws Exception {
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        verify(response, never()).setStatus(404);
    }

    @Test
    void knownStatusesHaveOwnTitles() {
        assertNotEquals(ErrorServlet.titleFor(403), ErrorServlet.titleFor(404));
        assertNotEquals(ErrorServlet.titleFor(404), ErrorServlet.titleFor(405));
        assertNotEquals(ErrorServlet.titleFor(403), ErrorServlet.titleFor(500));
    }

    @Test
    void otherStatusesUseGenericMessage() {
        assertEquals(ErrorServlet.titleFor(500), ErrorServlet.titleFor(503));
        assertEquals(ErrorServlet.textFor(500), ErrorServlet.textFor(418));
    }
}