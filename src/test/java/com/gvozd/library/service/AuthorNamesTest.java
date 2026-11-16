package com.gvozd.library.service;

import com.gvozd.library.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorNamesTest {

    @Test
    void nullAndBlankGiveEmptyList() {
        assertTrue(AuthorNames.parse(null).isEmpty());
        assertTrue(AuthorNames.parse("").isEmpty());
        assertTrue(AuthorNames.parse("   ").isEmpty());
    }

    @Test
    void splitsByCommaAndSemicolon() {
        List<String> result = AuthorNames.parse("Лев Толстой, Фёдор Достоевский; Антон Чехов");

        assertEquals(List.of("Лев Толстой", "Фёдор Достоевский", "Антон Чехов"), result);
    }

    @Test
    void trimsAndCollapsesSpaces() {
        assertEquals(List.of("Лев Толстой"), AuthorNames.parse("  Лев    Толстой  ,  "));
    }

    @Test
    void ignoresEmptyParts() {
        assertEquals(List.of("Иван"), AuthorNames.parse(",,Иван,;"));
    }

    @Test
    void duplicatesIgnoringCaseAreRemovedKeepingFirst() {
        assertEquals(List.of("Толстой"), AuthorNames.parse("Толстой, ТОЛСТОЙ, толстой"));
    }

    @Test
    void maxNumberOfAuthorsIsAllowed() {
        String input = IntStream.rangeClosed(1, AuthorNames.MAX_AUTHORS)
                .mapToObj(i -> "Автор " + i)
                .collect(Collectors.joining(", "));

        assertEquals(AuthorNames.MAX_AUTHORS, AuthorNames.parse(input).size());
    }

    @Test
    void tooManyAuthorsAreRejected() {
        String input = IntStream.rangeClosed(1, AuthorNames.MAX_AUTHORS + 1)
                .mapToObj(i -> "Автор " + i)
                .collect(Collectors.joining(", "));

        assertThrows(ValidationException.class, () -> AuthorNames.parse(input));
    }

    @Test
    void nameOfMaxLengthIsAllowed() {
        String name = "a".repeat(AuthorNames.MAX_NAME_LENGTH);

        assertEquals(List.of(name), AuthorNames.parse(name));
    }

    @Test
    void tooLongNameIsRejected() {
        String name = "a".repeat(AuthorNames.MAX_NAME_LENGTH + 1);

        assertThrows(ValidationException.class, () -> AuthorNames.parse(name));
    }
}