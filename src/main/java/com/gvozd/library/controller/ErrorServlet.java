package com.gvozd.library.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/error")
public class ErrorServlet extends HttpServlet {

    private static final System.Logger LOG = System.getLogger(ErrorServlet.class.getName());

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer status = (Integer) req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Throwable cause = (Throwable) req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (cause != null) {
            Object uri = req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            LOG.log(System.Logger.Level.ERROR, "Необработанная ошибка при запросе " + uri, cause);
        }

        resp.setStatus(status);
        req.setAttribute("errorStatus", status);
        req.setAttribute("errorTitle", titleFor(status));
        req.setAttribute("errorText", textFor(status));
        req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
    }

    static String titleFor(int status) {
        return switch (status) {
            case 403 -> "Доступ запрещён";
            case 404 -> "Страница не найдена";
            case 405 -> "Действие недоступно";
            default -> "Что-то пошло не так";
        };
    }

    static String textFor(int status) {
        return switch (status) {
            case 403 -> "У вас нет прав для просмотра этой страницы.";
            case 404 -> "Такой страницы нет. Возможно, адрес введён с ошибкой или страница удалена.";
            case 405 -> "Эту страницу нельзя открыть таким способом.";
            default -> "Произошла внутренняя ошибка. Попробуйте повторить действие позже.";
        };
    }
}