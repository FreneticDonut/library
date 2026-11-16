package com.gvozd.library.service;

import com.gvozd.library.model.Author;
import com.gvozd.library.model.Book;

import java.util.List;
import java.util.Map;

public record CatalogPage(List<Book> books, String query, int page, int totalPages,
                          Map<Long, List<Author>> authors) {
}