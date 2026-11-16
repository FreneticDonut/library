package com.gvozd.library.service;

import com.gvozd.library.dao.BookDao;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceAuthorsTest {

    @Mock
    private BookDao bookDao;

    @Captor
    private ArgumentCaptor<List<String>> namesCaptor;

    private BookService service;

    @BeforeEach
    void setUp() {
        service = new BookService(bookDao, 10);
    }

    private static String elevenAuthors() {
        return IntStream.rangeClosed(1, AuthorNames.MAX_AUTHORS + 1)
                .mapToObj(i -> "Автор " + i)
                .collect(Collectors.joining(", "));
    }

    @Test
    void addBookPassesParsedAuthorsToDao() {
        when(bookDao.save(any(Book.class), anyList())).thenAnswer(inv -> inv.getArgument(0));

        service.addBook("Книга", "", "", "1", " Лев  Толстой ,Фёдор Достоевский;; ");

        verify(bookDao).save(any(Book.class), namesCaptor.capture());
        assertEquals(List.of("Лев Толстой", "Фёдор Достоевский"), namesCaptor.getValue());
    }

    @Test
    void addBookWithoutAuthorsPassesEmptyList() {
        when(bookDao.save(any(Book.class), anyList())).thenAnswer(inv -> inv.getArgument(0));

        service.addBook("Книга", "", "", "1", null);

        verify(bookDao).save(any(Book.class), namesCaptor.capture());
        assertTrue(namesCaptor.getValue().isEmpty());
    }

    @Test
    void updateBookPassesParsedAuthorsToDao() {
        Book existing = new Book(1L, "Old", null, 2018, 5, 5);
        when(bookDao.findById(1L)).thenReturn(Optional.of(existing));

        service.updateBook(1L, "New", "", "2020", "5", "Автор Один; Автор Два");

        verify(bookDao).update(any(Book.class), namesCaptor.capture());
        assertEquals(List.of("Автор Один", "Автор Два"), namesCaptor.getValue());
    }

    @Test
    void invalidAuthorsStopAddBeforeAnyDatabaseAccess() {
        assertThrows(ValidationException.class,
                () -> service.addBook("Книга", "9780134685991", "", "1", elevenAuthors()));

        verifyNoInteractions(bookDao);
    }

    @Test
    void invalidAuthorsInUpdateAreRejected() {
        Book existing = new Book(1L, "Old", null, 2018, 5, 5);
        when(bookDao.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(ValidationException.class,
                () -> service.updateBook(1L, "New", "", "", "5", elevenAuthors()));

        verify(bookDao, never()).update(any(), anyList());
    }
}