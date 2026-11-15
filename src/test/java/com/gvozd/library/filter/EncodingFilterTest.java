package com.gvozd.library.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class EncodingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Test
    void setsUtf8BeforePassingRequestOn() throws Exception {
        new EncodingFilter().doFilter(request, response, chain);

        InOrder order = inOrder(request, response, chain);
        order.verify(request).setCharacterEncoding("UTF-8");
        order.verify(response).setCharacterEncoding("UTF-8");
        order.verify(chain).doFilter(request, response);
    }
}