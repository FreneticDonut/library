package com.gvozd.library.service;

import com.gvozd.library.exception.ValidationException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AuthorNames {

    public static final int MAX_AUTHORS = 10;
    public static final int MAX_NAME_LENGTH = 200;

    private AuthorNames() {}

    public static List<String> parse(String input) {
        if (input == null || input.isBlank()) {
            return List.of();
        }

        Map<String, String> unique = new LinkedHashMap<>();
        for (String part : input.split("[,;]")) {
            String name = part.trim().replaceAll("\\s+", " ");
            if (name.isEmpty()) {
                continue;
            }
            if (name.length() > MAX_NAME_LENGTH) {
                throw new ValidationException(
                        "Имя автора не должно быть длиннее " + MAX_NAME_LENGTH + " символов");
            }
            unique.putIfAbsent(name.toLowerCase(Locale.ROOT), name);
        }

        if (unique.size() > MAX_AUTHORS) {
            throw new ValidationException("У книги не может быть больше " + MAX_AUTHORS + " авторов");
        }
        return List.copyOf(unique.values());
    }
}