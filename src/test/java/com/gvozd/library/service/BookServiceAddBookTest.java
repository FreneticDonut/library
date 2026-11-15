package com.gvozd.library.service;

import com.gvozd.library.dao.BookDao;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceAddBookTest {

    @Mock
    private BookDao bookDao;

    private BookService service;

    @BeforeEach
    void setUp() {
        service = new BookService(bookDao, 10);
    }

    @Test
    void savesBookWithNormalizedData() {
        when(bookDao.existsByIsbn("9780134685991")).thenReturn(false);
        when(bookDao.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        service.addBook("  Effective Java ", "978-0-13-468599-1", "2018", "3");

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookDao).save(captor.capture());
        Book saved = captor.getValue();

        assertEquals("Effective Java", saved.getTitle());
        assertEquals("9780134685991", saved.getIsbn());
        assertEquals(2018, saved.getPublishYear());
        assertEquals(3, saved.getTotalCopies());
        assertEquals(3, saved.getAvailableCopies());
    }

    @Test
    void blankIsbnAndYearAreStoredAsNull() {
        when(bookDao.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        service.addBook("Книга", "   ", "", "1");

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookDao).save(captor.capture());

        assertNull(captor.getValue().getIsbn());
        assertNull(captor.getValue().getPublishYear());
        verify(bookDao, never()).existsByIsbn(any());
    }

    @Test
    void isbn10WithXIsAccepted() {
        when(bookDao.existsByIsbn("080442957X")).thenReturn(false);
        when(bookDao.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        service.addBook("Книга", "0-8044-2957-x", "", "1");

        verify(bookDao).save(any(Book.class));
    }

    @Test
    void emptyTitleIsRejected() {
        assertThrows(ValidationException.class, () -> service.addBook("   ", "", "", "1"));

        verify(bookDao, never()).save(any());
    }

    @Test
    void tooLongTitleIsRejected() {
        String longTitle = "a".repeat(301);

        assertThrows(ValidationException.class, () -> service.addBook(longTitle, "", "", "1"));

        verify(bookDao, never()).save(any());
    }

    @Test
    void invalidIsbnIsRejected() {
        assertThrows(ValidationException.class, () -> service.addBook("Книга", "123", "", "1"));

        verify(bookDao, never()).save(any());
    }

    @Test
    void existingIsbnIsRejected() {
        when(bookDao.existsByIsbn("9780134685991")).thenReturn(true);

        assertThrows(ValidationException.class,
                () -> service.addBook("Книга", "9780134685991", "", "1"));

        verify(bookDao, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "999", "3000"})
    void invalidYearIsRejected(String year) {
        assertThrows(ValidationException.class, () -> service.addBook("Книга", "", year, "1"));

        verify(bookDao, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc", "0", "-1", "1001"})
    void invalidCopiesAreRejected(String copies) {
        assertThrows(ValidationException.class, () -> service.addBook("Книга", "", "", copies));

        verify(bookDao, never()).save(any());
    }
}