package com.gvozd.library.service;

import com.gvozd.library.dao.UserDao;
import com.gvozd.library.exception.AuthException;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.model.Role;
import com.gvozd.library.model.User;
import com.gvozd.library.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String PASSWORD = "secret123";
    private static final String HASH = PasswordHasher.hash(PASSWORD);

    @Mock
    private UserDao userDao;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userDao);
    }

    // ---------- регистрация ----------

    @Test
    void registerSavesReaderWithHashedPassword() {
        when(userDao.existsByEmail("ivan@mail.ru")).thenReturn(false);
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.register("  Ivan@Mail.ru ", PASSWORD, "  Иван ");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("ivan@mail.ru", saved.getEmail());
        assertEquals("Иван", saved.getName());
        assertEquals(Role.READER, saved.getRole());
        assertNotEquals(PASSWORD, saved.getPasswordHash());
        assertTrue(PasswordHasher.verify(PASSWORD, saved.getPasswordHash()));
    }

    @Test
    void registerRejectsInvalidEmail() {
        assertThrows(ValidationException.class,
                () -> service.register("not-an-email", PASSWORD, "Иван"));

        verify(userDao, never()).save(any());
    }

    @Test
    void registerRejectsShortPassword() {
        assertThrows(ValidationException.class,
                () -> service.register("ivan@mail.ru", "1234567", "Иван"));

        verify(userDao, never()).save(any());
    }

    @Test
    void registerRejectsNullPassword() {
        assertThrows(ValidationException.class,
                () -> service.register("ivan@mail.ru", null, "Иван"));

        verify(userDao, never()).save(any());
    }

    @Test
    void registerRejectsEmptyName() {
        assertThrows(ValidationException.class,
                () -> service.register("ivan@mail.ru", PASSWORD, "   "));

        verify(userDao, never()).save(any());
    }

    @Test
    void registerRejectsExistingEmail() {
        when(userDao.existsByEmail("ivan@mail.ru")).thenReturn(true);

        assertThrows(ValidationException.class,
                () -> service.register("ivan@mail.ru", PASSWORD, "Иван"));

        verify(userDao, never()).save(any());
    }

    // ---------- вход ----------

    @Test
    void loginSucceedsWithCorrectCredentials() {
        User stored = new User(1L, "ivan@mail.ru", HASH, "Иван", Role.READER);
        when(userDao.findByEmail("ivan@mail.ru")).thenReturn(Optional.of(stored));

        User result = service.login("ivan@mail.ru", PASSWORD);

        assertEquals(1L, result.getId());
    }

    @Test
    void loginIgnoresEmailCaseAndSpaces() {
        User stored = new User(1L, "ivan@mail.ru", HASH, "Иван", Role.READER);
        when(userDao.findByEmail("ivan@mail.ru")).thenReturn(Optional.of(stored));

        User result = service.login("  IVAN@Mail.RU ", PASSWORD);

        assertEquals("ivan@mail.ru", result.getEmail());
    }

    @Test
    void loginFailsWithWrongPassword() {
        User stored = new User(1L, "ivan@mail.ru", HASH, "Иван", Role.READER);
        when(userDao.findByEmail("ivan@mail.ru")).thenReturn(Optional.of(stored));

        assertThrows(AuthException.class, () -> service.login("ivan@mail.ru", "wrong-pass"));
    }

    @Test
    void loginFailsForUnknownEmail() {
        when(userDao.findByEmail("nobody@mail.ru")).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> service.login("nobody@mail.ru", PASSWORD));
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveSameMessage() {
        User stored = new User(1L, "ivan@mail.ru", HASH, "Иван", Role.READER);
        when(userDao.findByEmail("ivan@mail.ru")).thenReturn(Optional.of(stored));
        when(userDao.findByEmail("nobody@mail.ru")).thenReturn(Optional.empty());

        AuthException wrongPassword = assertThrows(AuthException.class,
                () -> service.login("ivan@mail.ru", "wrong-pass"));
        AuthException unknownEmail = assertThrows(AuthException.class,
                () -> service.login("nobody@mail.ru", PASSWORD));

        assertEquals(wrongPassword.getMessage(), unknownEmail.getMessage());
    }
}