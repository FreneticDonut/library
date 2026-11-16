package com.gvozd.library.service;

import com.gvozd.library.dao.AuthorDao;
import com.gvozd.library.dao.BookDao;
import com.gvozd.library.model.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceAuthorsLineTest {

    @Mock
    private BookDao bookDao;

    @Mock
    private AuthorDao authorDao;

    private BookService service;

    @BeforeEach
    void setUp() {
        service = new BookService(bookDao, authorDao, 10);
    }

    @Test
    void authorsAreJoinedWithCommaInDaoOrder() {
        when(authorDao.findByBookId(5L)).thenReturn(List.of(
                new Author(1L, "Лев Толстой"),
                new Author(2L, "Фёдор Достоевский")));

        assertEquals("Лев Толстой, Фёдор Достоевский", service.getAuthorsLine(5L));
    }

    @Test
    void singleAuthorHasNoSeparator() {
        when(authorDao.findByBookId(5L)).thenReturn(List.of(new Author(1L, "Антон Чехов")));

        assertEquals("Антон Чехов", service.getAuthorsLine(5L));
    }

    @Test
    void noAuthorsGiveEmptyString() {
        when(authorDao.findByBookId(5L)).thenReturn(List.of());

        assertEquals("", service.getAuthorsLine(5L));
    }
}