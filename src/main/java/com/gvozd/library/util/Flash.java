package com.gvozd.library.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class Flash {

    public static final String MESSAGE = "flashMessage";
    public static final String ERROR = "flashError";

    private Flash() {}

    public static void success(HttpServletRequest req, String text) {
        req.getSession().setAttribute(MESSAGE, text);
    }

    public static void error(HttpServletRequest req, String text) {
        req.getSession().setAttribute(ERROR, text);
    }

    public static void moveToRequest(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        for (String name : new String[]{MESSAGE, ERROR}) {
            Object value = session.getAttribute(name);
            if (value != null) {
                req.setAttribute(name, value);
                session.removeAttribute(name);
            }
        }
    }
}