package com.gvozd.library.service;

import com.gvozd.library.dao.AuthorDao;
import com.gvozd.library.dao.BookDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private static final int PAGE_SIZE = 3;

    @Mock
    private BookDao bookDao;

    @Mock
    private AuthorDao authorDao;

    private BookService service;

    @BeforeEach
    void setUp() {
        service = new BookService(bookDao, authorDao, PAGE_SIZE);
    }

    @Test
    void totalPagesIsRoundedUp() {
        when(bookDao.count("")).thenReturn(7L);

        CatalogPage result = service.getCatalogPage("", 1);

        assertEquals(3, result.totalPages());
    }

    @Test
    void totalPagesIsExactWhenBooksDivideEvenly() {
        when(bookDao.count("")).thenReturn(6L);

        CatalogPage result = service.getCatalogPage("", 1);

        assertEquals(2, result.totalPages());
    }

    @Test
    void emptyResultStillHasOnePage() {
        when(bookDao.count("xyz")).thenReturn(0L);

        CatalogPage result = service.getCatalogPage("xyz", 1);

        assertEquals(1, result.totalPages());
        assertEquals(1, result.page());
    }

    @Test
    void pageBeyondLastIsClampedToLast() {
        when(bookDao.count("")).thenReturn(7L);

        CatalogPage result = service.getCatalogPage("", 999);

        assertEquals(3, result.page());
        verify(bookDao).findPage("", PAGE_SIZE, 6);
    }

    @Test
    void pageBelowOneIsClampedToFirst() {
        when(bookDao.count("")).thenReturn(7L);

        CatalogPage result = service.getCatalogPage("", -5);

        assertEquals(1, result.page());
        verify(bookDao).findPage("", PAGE_SIZE, 0);
    }

    @Test
    void offsetDependsOnPage() {
        when(bookDao.count("")).thenReturn(7L);

        service.getCatalogPage("", 2);

        verify(bookDao).findPage("", PAGE_SIZE, 3);
    }

    @Test
    void nullQueryIsTreatedAsEmpty() {
        when(bookDao.count("")).thenReturn(0L);

        CatalogPage result = service.getCatalogPage(null, 1);

        assertEquals("", result.query());
    }

    @Test
    void queryIsTrimmed() {
        when(bookDao.count("clean")).thenReturn(1L);

        CatalogPage result = service.getCatalogPage("  clean  ", 1);

        assertEquals("clean", result.query());
        verify(bookDao).findPage("clean", PAGE_SIZE, 0);
    }
}