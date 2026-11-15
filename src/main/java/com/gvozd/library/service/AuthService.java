package com.gvozd.library.service;

import com.gvozd.library.dao.UserDao;
import com.gvozd.library.exception.AuthException;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.model.Role;
import com.gvozd.library.model.User;
import com.gvozd.library.util.PasswordHasher;

import java.util.regex.Pattern;

public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_EMAIL_LENGTH = 255;
    private static final int MAX_NAME_LENGTH = 100;

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User register(String email, String password, String name) {
        String normalizedEmail = normalizeEmail(email);
        String trimmedName = (name == null) ? "" : name.trim();

        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()
                || normalizedEmail.length() > MAX_EMAIL_LENGTH) {
            throw new ValidationException("Некорректный email");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException(
                    "Пароль должен содержать не менее " + MIN_PASSWORD_LENGTH + " символов");
        }
        if (trimmedName.isEmpty() || trimmedName.length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Имя должно содержать от 1 до " + MAX_NAME_LENGTH + " символов");
        }
        if (userDao.existsByEmail(normalizedEmail)) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        User user = new User(null, normalizedEmail, PasswordHasher.hash(password),
                trimmedName, Role.READER);
        return userDao.save(user);
    }

    public User login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);

        User user = userDao.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AuthException("Неверный email или пароль"));

        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new AuthException("Неверный email или пароль");
        }
        return user;
    }

    private static String normalizeEmail(String email) {
        return (email == null) ? "" : email.trim().toLowerCase();
    }
}