package com.gvozd.library.service;

import com.gvozd.library.dao.AuthorDao;
import com.gvozd.library.dao.BookDao;
import com.gvozd.library.exception.NotFoundException;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceUpdateBookTest {

    @Mock
    private BookDao bookDao;

    @Mock
    private AuthorDao authorDao;

    private BookService service;

    // всего 5 экземпляров, свободно 2, значит на руках 3
    private Book existing;

    @BeforeEach
    void setUp() {
        service = new BookService(bookDao, authorDao, 10);
        existing = new Book(1L, "Old", "9780134685991", 2018, 5, 2);
    }

    @Test
    void updatesFieldsAndRecalculatesAvailable() {
        when(bookDao.findById(1L)).thenReturn(Optional.of(existing));
        when(bookDao.findByIsbn("9785170000001")).thenReturn(Optional.empty());

        service.updateBook(1L, "  New  ", "978-5-17-000000-1", "2020", "4", null);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookDao).update(captor.capture(), anyList());
        Book saved = captor.getValue();

        assertEquals("New", saved.getTitle());
        assertEquals("9785170000001", saved.getIsbn());
        assertEquals(2020, saved.getPublishYear());
        assertEquals(4, saved.getTotalCopies());
        assertEquals(1, saved.getAvailableCopies());
    }

    @Test
    void totalCannotBeBelowBorrowedCopies() {
        when(bookDao.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(ValidationException.class,
                () -> service.updateBook(1L, "Old", "", "", "2", null));

        verify(bookDao, never()).update(any(), anyList());
    }

    @Test
    void totalMayEqualBorrowedCopies() {
        when(bookDao.findById(1L)).thenReturn(Optional.of(existing));

        service.updateBook(1L, "Old", "", "", "3", null);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookDao).update(captor.capture(), anyList());

        assertEquals(3, captor.getValue().getTotalCopies());
        assertEquals(0, captor.getValue().getAvailableCopies());
    }

    @Test
    void ownIsbnIsNotTreatedAsDuplicate() {
        when(bookDao.findById(1L)).thenReturn(Optional.of(existing));
        when(bookDao.findByIsbn("9780134685991")).thenReturn(Optional.of(existing));

        service.updateBook(1L, "Old", "9780134685991", "2018", "5", null);

        verify(bookDao).update(any(Book.class), anyList());
    }

    @Test
    void isbnOfAnotherBookIsRejected() {
        Book other = new Book(2L, "Other", "9785170000001", 2000, 1, 1);
        when(bookDao.findById(1L)).thenReturn(Optional.of(existing));
        when(bookDao.findByIsbn("9785170000001")).thenReturn(Optional.of(other));

        assertThrows(ValidationException.class,
                () -> service.updateBook(1L, "Old", "9785170000001", "", "5", null));

        verify(bookDao, never()).update(any(), anyList());
    }

    @Test
    void invalidTitleIsRejected() {
        when(bookDao.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(ValidationException.class,
                () -> service.updateBook(1L, "   ", "", "", "5", null));

        verify(bookDao, never()).update(any(), anyList());
    }

    @Test
    void updatingUnknownBookThrowsNotFound() {
        when(bookDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.updateBook(99L, "Title", "", "", "1", null));

        verify(bookDao, never()).update(any(), anyList());
    }

    @Test
    void gettingUnknownBookThrowsNotFound() {
        when(bookDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getBook(99L));
    }
}