package com.gvozd.library.service;

import com.gvozd.library.dao.AuthorDao;
import com.gvozd.library.dao.BookDao;
import com.gvozd.library.model.Author;
import com.gvozd.library.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceCatalogAuthorsTest {

    @Mock
    private BookDao bookDao;

    @Mock
    private AuthorDao authorDao;

    private BookService service;

    @BeforeEach
    void setUp() {
        service = new BookService(bookDao, authorDao, 10);
    }

    private static Book book(long id) {
        return new Book(id, "Книга " + id, null, 2020, 1, 1);
    }

    @Test
    void authorsOfAllPageBooksAreLoadedWithOneCall() {
        List<Book> books = List.of(book(1L), book(2L), book(3L));
        when(bookDao.count("")).thenReturn(3L);
        when(bookDao.findPage("", 10, 0)).thenReturn(books);
        when(authorDao.findByBookIds(List.of(1L, 2L, 3L))).thenReturn(Map.of());

        service.getCatalogPage("", 1);

        verify(authorDao).findByBookIds(List.of(1L, 2L, 3L));
        verifyNoMoreInteractions(authorDao);
    }

    @Test
    void pageContainsAuthorsReturnedByDao() {
        Map<Long, List<Author>> authors = Map.of(
                1L, List.of(new Author(10L, "Лев Толстой")),
                2L, List.of(new Author(11L, "Антон Чехов"), new Author(12L, "Максим Горький")));
        when(bookDao.count("")).thenReturn(2L);
        when(bookDao.findPage("", 10, 0)).thenReturn(List.of(book(1L), book(2L)));
        when(authorDao.findByBookIds(List.of(1L, 2L))).thenReturn(authors);

        CatalogPage result = service.getCatalogPage("", 1);

        assertEquals(authors, result.authors());
    }

    @Test
    void bookWithoutAuthorsHasNoEntry() {
        when(bookDao.count("")).thenReturn(2L);
        when(bookDao.findPage("", 10, 0)).thenReturn(List.of(book(1L), book(2L)));
        when(authorDao.findByBookIds(List.of(1L, 2L)))
                .thenReturn(Map.of(1L, List.of(new Author(10L, "Лев Толстой"))));

        CatalogPage result = service.getCatalogPage("", 1);

        assertTrue(result.authors().containsKey(1L));
        assertFalse(result.authors().containsKey(2L));
    }

    @Test
    void emptyPageDoesNotTouchAuthorDao() {
        when(bookDao.count("xyz")).thenReturn(0L);
        when(bookDao.findPage("xyz", 10, 0)).thenReturn(List.of());

        CatalogPage result = service.getCatalogPage("xyz", 1);

        assertTrue(result.authors().isEmpty());
        verifyNoInteractions(authorDao);
    }
}