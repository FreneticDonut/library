package com.gvozd.library.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class PasswordHasher {

    private static final int COST = 12;

    private PasswordHasher() {}

    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray());
    }

    public static boolean verify(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified;
    }
}