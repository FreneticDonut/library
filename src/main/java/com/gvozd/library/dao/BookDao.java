package com.gvozd.library.dao;

import com.gvozd.library.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookDao {

    List<Book> findPage(String titleQuery, int limit, int offset);

    long count(String titleQuery);

    Optional<Book> findById(long id);

    Book save(Book book);
}