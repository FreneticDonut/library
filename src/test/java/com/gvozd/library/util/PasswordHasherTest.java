package com.gvozd.library.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hashIsNotEqualToPassword() {
        String hash = PasswordHasher.hash("secret123");

        assertNotEquals("secret123", hash);
        assertEquals(60, hash.length());
    }

    @Test
    void samePasswordGivesDifferentHashes() {
        String first = PasswordHasher.hash("secret123");
        String second = PasswordHasher.hash("secret123");

        assertNotEquals(first, second);
    }

    @Test
    void correctPasswordIsVerified() {
        String hash = PasswordHasher.hash("secret123");

        assertTrue(PasswordHasher.verify("secret123", hash));
    }

    @Test
    void wrongPasswordIsRejected() {
        String hash = PasswordHasher.hash("secret123");

        assertFalse(PasswordHasher.verify("Secret123", hash));
    }

    @Test
    void nullArgumentsAreRejected() {
        String hash = PasswordHasher.hash("secret123");

        assertFalse(PasswordHasher.verify(null, hash));
        assertFalse(PasswordHasher.verify("secret123", null));
    }

    @Test
    void emptyPasswordCannotBeHashed() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null));
    }
}