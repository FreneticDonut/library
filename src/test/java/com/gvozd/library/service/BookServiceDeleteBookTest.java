package com.gvozd.library.service;

import com.gvozd.library.dao.BookDao;
import com.gvozd.library.exception.NotFoundException;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceDeleteBookTest {

    @Mock
    private BookDao bookDao;

    private BookService service;

    @BeforeEach
    void setUp() {
        service = new BookService(bookDao, 10);
    }

    @Test
    void deletesBookWithoutLoans() {
        when(bookDao.findById(1L)).thenReturn(Optional.of(new Book(1L, "Книга", null, null, 1, 1)));
        when(bookDao.hasLoans(1L)).thenReturn(false);

        service.deleteBook(1L);

        verify(bookDao).delete(1L);
    }

    @Test
    void refusesToDeleteBookWithLoans() {
        when(bookDao.findById(1L)).thenReturn(Optional.of(new Book(1L, "Книга", null, null, 1, 0)));
        when(bookDao.hasLoans(1L)).thenReturn(true);

        assertThrows(ValidationException.class, () -> service.deleteBook(1L));

        verify(bookDao, never()).delete(1L);
    }

    @Test
    void unknownBookThrowsNotFound() {
        when(bookDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteBook(99L));

        verify(bookDao, never()).delete(99L);
    }
}