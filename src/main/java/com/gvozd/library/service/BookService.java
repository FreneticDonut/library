package com.gvozd.library.service;

import com.gvozd.library.dao.BookDao;
import com.gvozd.library.model.Book;

import java.util.List;

public class BookService {

    private final BookDao bookDao;
    private final int pageSize;

    public BookService(BookDao bookDao, int pageSize) {
        this.bookDao = bookDao;
        this.pageSize = pageSize;
    }

    public CatalogPage getCatalogPage(String query, int requestedPage) {
        String q = (query == null) ? "" : query.trim();

        long total = bookDao.count(q);
        int totalPages = (int) Math.max(1, Math.ceil((double) total / pageSize));

        int page = Math.min(Math.max(requestedPage, 1), totalPages);
        int offset = (page - 1) * pageSize;

        List<Book> books = bookDao.findPage(q, pageSize, offset);
        return new CatalogPage(books, q, page, totalPages);
    }
}