package com.gvozd.library.dao;

import com.gvozd.library.model.Author;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AuthorDao {

    Author findOrCreate(String name);

    List<Author> findByBookId(long bookId);

    Map<Long, List<Author>> findByBookIds(Collection<Long> bookIds);
}